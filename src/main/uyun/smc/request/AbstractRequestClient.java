package uyun.smc.request;

import okhttp3.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.smc.common.RouteKey;
import uyun.smc.common.SdkConstant;
import uyun.smc.common.enums.AccessModeEnum;
import uyun.smc.common.enums.ContentTypeEnum;
import uyun.smc.common.enums.HttpMethod;
import uyun.smc.common.request.RequestEntity;
import uyun.smc.common.response.ResponseResult;
import uyun.smc.config.SdkConfig;
import uyun.smc.sign.Signature;
import uyun.smc.sign.UrlUtil;
import uyun.smc.sign.Util;
import uyun.smc.utils.CacheManager;
import uyun.smc.utils.SdkUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * RequestClient请求基础封装
 * 实现 {@link RequestClient}
 * <p>execute 方法适配SMC的请求特性, 会添加公共头部等<p/>
 * <p>commonRequest 方法用于普通的Http请求<p/>
 */
public abstract class AbstractRequestClient implements RequestClient {

    public static final Logger logger = LoggerFactory.getLogger(AbstractRequestClient.class);

    private RequestEntity requestEntity;

    protected AbstractRequestClient(RequestEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Request config can not be null");
        }
        if (StringUtils.isEmpty(entity.getAccessKey()) || StringUtils.isEmpty(entity.getSecretKey())) {
            throw new IllegalArgumentException("illegal config, please check the ak/sk value");
        }

        if (entity.getHttpMethod() == null) {
            throw new IllegalArgumentException("http Method can not be null");
        }

        if (StringUtils.isEmpty(entity.getUrl())) {
            throw new IllegalArgumentException("http url can not be null");
        }

        this.requestEntity = entity;
    }

    protected RequestEntity getRequestEntity() {
        return this.requestEntity;
    }

    /**
     * 发起请求
     *
     * @return
     */
    public ResponseResult execute() {
        return doRequest(requestEntity);
    }

    /**
     * 普通的直连地址http请求
     *
     * @return
     */
    public Response commonRequest() throws IOException {
        return doDirectRequest(requestEntity);
    }

    /**
     * 发起请求调用的具体逻辑
     *
     * @param requestEntity
     * @return
     */
    private ResponseResult doRequest(RequestEntity requestEntity) {

        // Cachekey中不包含QueryString
        String cacheUrl = UrlUtil.trimQueryString(requestEntity.getUrl());
        RouteKey routeKey = RouteKey.builder().httpMethod(this.requestEntity.getHttpMethod().name()).requestURI(cacheUrl).build();
        String routeRemoteUrl = CacheManager.getNormalRouteCache(routeKey);

        // 1. 一级缓存是否存在
        if (StringUtils.isNotEmpty(routeRemoteUrl)) {
            // 存在映射直接请求
            try {
                RequestEntity rc = RequestEntity.copy(requestEntity);
                // 替换远程URL地址
                rc.setUrl(UrlUtil.changeRemoteUrl(requestEntity.getUrl(), routeRemoteUrl));
                Response response = doDirectRequest(rc);
                // 非正常的情况清除缓存
                if (!response.isSuccessful()) {
                    CacheManager.delElementCache(routeKey);
                }

                return ResponseResult.resolveResponse(response);
            } catch (IOException e) {
                // 连接异常的情况
                CacheManager.delElementCache(routeKey);
                throw new RuntimeException("can not access the service according the first level cache", e);
            }
        } else { // 缓存不存在, 通过SMC 寻址或者路由
            try {
                Response response = doSmcRequest(requestEntity);
                if (response.isSuccessful()) {
                    Headers headers = response.headers();
                    if (headers != null && headers.size() > 0) {
                        String accessMode = headers.get(SdkConstant.ACCESS_MODE);
                        if (AccessModeEnum.ADDRESSING.getValue().equals(accessMode)) {
                            String remoteUrl = headers.get(SdkConstant.REMOTE_URL);
                            // 寻址模式，再次调用转发的地址、并将该地址和转发地址映射后放入缓存中
                            if (logger.isDebugEnabled()) {
                                logger.debug("##current access is in addressing mode, request to real remote url again ...");
                            }
                            CacheManager.addElementCache(routeKey, remoteUrl);
                            try {
                                RequestEntity rc = RequestEntity.copy(requestEntity);
                                rc.setUrl(UrlUtil.changeRemoteUrl(requestEntity.getUrl(), remoteUrl));
                                response = doDirectRequest(rc);
                            } catch (IOException e) {
                                throw new RuntimeException("can not request the service with the second level cache", e);
                            }
                        }
                    }
                }
                return ResponseResult.resolveResponse(response);
            } catch (IOException e) {
                // SMC网关维护中
                logger.warn("can not access smc gateway, try to find second level cache");
                routeRemoteUrl = CacheManager.getElementCache(routeKey);
                if (StringUtils.isNotEmpty(routeRemoteUrl)) {
                    try {
                        RequestEntity rc = RequestEntity.copy(requestEntity);
                        rc.setUrl(UrlUtil.changeRemoteUrl(requestEntity.getUrl(), routeRemoteUrl));
                        Response response = doDirectRequest(rc);
                        return ResponseResult.resolveResponse(response);
                    } catch (IOException ex) {
                        throw new RuntimeException("can not request the service with the second level cache", e);
                    }
                } else {
                    throw new RuntimeException("can request the service, can not access smc gateway and can not find cache in local");
                }
            }
        }
    }

    /**
     * 创建请求对象(模板方法)
     *
     * @param requestEntity
     * @return
     */
    protected abstract Request createDirectRequest(RequestEntity requestEntity);

    /**
     * 创建向网关发送的请求对象
     *
     * @param requestEntity
     * @return
     */
    private Request createSmcRequest(RequestEntity requestEntity) {
        // 构建Request实例
        Request request = createDirectRequest(requestEntity);
        Request.Builder builder = request.newBuilder();

        // 组装SMC需要的接口信息
        String timestamp = SdkUtils.getTimestampStr();
        String nonce = SdkUtils.getUUID();
        builder.addHeader(SdkConstant.APPKEY, this.getRequestEntity().getAccessKey());
        builder.addHeader(SdkConstant.NONCE, nonce);
        builder.addHeader(SdkConstant.TIMESTAMP, timestamp);
        String regionCode = this.getRequestEntity().getRegionCode();
        if (regionCode != null) {
            builder.addHeader(SdkConstant.REGION, regionCode);
        }
        String sign = generateSign(this.getRequestEntity().getHttpMethod(), requestEntity.getUrl(), timestamp, nonce, requestEntity.getRequestParams(), requestEntity.getRawBody());

        builder.addHeader(SdkConstant.SIGNATURE, sign);

        return builder.build();
    }

    /**
     * 向服务提供方直接发起访问
     *
     * @param requestEntity
     * @return
     * @throws IOException
     */
    private Response doDirectRequest(RequestEntity requestEntity) throws IOException {
        // 构建client实例(direct)
        OkHttpClient okHttpClient = SdkUtils.buildSmartOkHttpClient(
                requestEntity.getUrl(), SdkConfig.DIRECT_CONNECT_TIME_OUT, SdkConfig.DIRECT_WRITE_TIME_OUT, SdkConfig.DIRECT_READ_TIME_OUT);

        // 构建Request实例
        Request request = createDirectRequest(requestEntity);
        // 准备异步请求
        Call call = okHttpClient.newCall(request);
        // 执行异步请求
        Response response = call.execute();
        return response;
    }

    /**
     * 向SMC网关发起服务调用
     *
     * @param requestEntity
     * @return
     * @throws IOException
     */
    private Response doSmcRequest(RequestEntity requestEntity) throws IOException {
        // 构建client实例(smc)
        OkHttpClient okHttpClient = SdkUtils.buildSmartOkHttpClient(requestEntity.getUrl(), SdkConfig.SMC_CONNECT_TIME_OUT, SdkConfig.SMC_WRITE_TIME_OUT, SdkConfig.SMC_READ_TIME_OUT);

        Request request = this.createSmcRequest(requestEntity);
        // 准备异步请求
        Call call = okHttpClient.newCall(request);
        // 执行异步请求
        Response response = call.execute();
        return response;
    }

    /**
     * 构建Header
     *
     * @param heads
     * @return
     */
    protected Headers covertMapToHeaders(Map<String, String> heads) {
        Headers.Builder builder = new Headers.Builder();
        if (heads != null && !heads.isEmpty()) {
            for (Map.Entry<String, String> entry : heads.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    /**
     * 公用签名方法
     *
     * @param httpMethod
     * @param url
     * @param timestamp
     * @param nonce
     * @param requestParam
     * @param bodyStr
     * @return
     */
    private String generateSign(HttpMethod httpMethod, String url, String timestamp, String nonce,
                                final Map<String, String> requestParam, String bodyStr) {

        Util.HTTP method = Util.HTTP.validate(httpMethod.name());
        Signature.Builder builder = Signature.newBuilder().method(method.method()).url(url);

        builder.header(SdkConstant.NONCE, nonce)
                .header(SdkConstant.TIMESTAMP, timestamp)
                .header(SdkConstant.APPKEY, this.getRequestEntity().getAccessKey())
                .header(SdkConstant.REGION, this.getRequestEntity().getRegionCode())
                .secret(this.getRequestEntity().getSecretKey());

        // URL中的参数, 可能已经encode了, decode后参与签名
        Map<String, String> queryParam = UrlUtil.getQueryParam(url);
        if (queryParam != null && queryParam.size() > 0) {
            for (Map.Entry<String, String> entry : queryParam.entrySet()) {
                builder.parameter(entry.getKey(), UrlUtil.percentDecode(entry.getValue()));
            }
        }

        // GET请求传入的 requestParam, 可能已经encode了, decode后参与签名
        if (httpMethod.equals(HttpMethod.GET)) {
            if (requestParam != null && requestParam.size() > 0) {
                for (Map.Entry<String, String> entry : requestParam.entrySet()) {
                    builder.parameter(entry.getKey(), UrlUtil.percentDecode(entry.getValue()));
                }
            }
        } else {
            String contentType = UrlUtil.getContentType(requestEntity.getHeaders());
            if (StringUtils.isNotEmpty(contentType)) {
                if (contentType.startsWith(ContentTypeEnum.FORM.getValue())) {
                    // get以外的requestParam 需在encode后参与签名
                    if (requestParam != null && requestParam.size() > 0) {
                        for (Map.Entry<String, String> entry : requestParam.entrySet()) {
                            builder.parameter(entry.getKey(), UrlUtil.percentEncode(entry.getValue()));
                        }
                    }
                } else { // FORM体提交以外的情况, 参数是拼接到URL的, decode后参与签名
                    if (requestParam != null && requestParam.size() > 0) {
                        for (Map.Entry<String, String> entry : requestParam.entrySet()) {
                            builder.parameter(entry.getKey(), UrlUtil.percentDecode(entry.getValue()));
                        }
                    }
                }
                if (StringUtils.isNotEmpty(bodyStr)) {
                    builder.postString(bodyStr);
                }
            }
        }

        Signature sign = builder.build();
        return sign.sign();
    }

    /*
     * 组装requestBody
     */
    protected RequestBody generateRequestBody(RequestEntity requestEntity) {

        if (requestEntity.getHttpMethod().equals(HttpMethod.GET)) {
            throw new IllegalStateException("get method can not create request body");
        }

        if (StringUtils.isNotEmpty(requestEntity.getRawBody()) || !CollectionUtils.sizeIsEmpty(requestEntity.getFileParams())) {
            if (!UrlUtil.hasContentType(requestEntity.getHeaders())) {
                throw new IllegalArgumentException("content-type must be set when has body");
            }
        }

        String contentType = UrlUtil.getContentType(requestEntity.getHeaders());
        if (StringUtils.isEmpty(contentType)) {
            if (requestEntity.getHttpMethod().equals(HttpMethod.POST)
                    || requestEntity.getHttpMethod().equals(HttpMethod.PUT)) {
                return RequestBody.create(null, new byte[0]);
            }
            return null;
        }

        if (contentType.startsWith(ContentTypeEnum.FORM.getValue())) {
            FormBody.Builder formBuilder = new FormBody.Builder();
            if (requestEntity.getRequestParams() != null && requestEntity.getRequestParams().size() > 0) {
                for (Map.Entry<String, String> entry : requestEntity.getRequestParams().entrySet()) {
                    if (StringUtils.isNotEmpty(entry.getValue())) {
                        formBuilder.add(entry.getKey(), entry.getValue());
                    }
                }
            }
            return formBuilder.build();
        } else if (ContentTypeEnum.MULTIPART.getValue().equals(contentType)) {
            if (requestEntity.getFileParams() == null || requestEntity.getFileParams().isEmpty()) {
                throw new IllegalArgumentException("when content-type=" + ContentTypeEnum.MULTIPART.getValue() + " 'requestFileParam' can not be null");
            }
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            if (requestEntity.getRequestParams() != null) {
                for (String key : requestEntity.getRequestParams().keySet()) {
                    builder.addFormDataPart(key, requestEntity.getRequestParams().get(key));
                }
            }

            for (String key : requestEntity.getFileParams().keySet()) {
                File file = requestEntity.getFileParams().get(key);
                builder.addFormDataPart(key, file.getName(), RequestBody.create(MediaType.parse(ContentTypeEnum.STREAM.getValue()), file));
            }
            return builder.build();
        } else {
            return RequestBody.create(MediaType.parse(contentType), requestEntity.getRawBody());
        }
    }

    /**
     * GET以外的请求类型, 当有的RawBody但是RequestParam也有值时, 拼接请求参数到URL后面
     *
     * @param requestEntity
     * @return 重新拼接构建的URL
     */
    protected String appendRequestParam2URL(RequestEntity requestEntity) {
        // GET时URL拼接
        if ((requestEntity.getHttpMethod().equals(HttpMethod.GET)
                && requestEntity.getRequestParams() != null
                && !requestEntity.getRequestParams().isEmpty())
                || (StringUtils.isNotEmpty(requestEntity.getRawBody())
                && requestEntity.getRequestParams() != null
                && !requestEntity.getRequestParams().isEmpty())) {
            // merge原来的URL参数
            Map<String, String> urlParam = UrlUtil.getQueryParam(requestEntity.getUrl());
            Map<String, String> merge = new HashMap<>();
            merge.putAll(urlParam);
            merge.putAll(requestEntity.getRequestParams());

            String getParamString = UrlUtil.getRequestParamSplitJoin(merge);
            return UrlUtil.trimQueryString(requestEntity.getUrl()) + "?" + getParamString;
        }

        return requestEntity.getUrl();
    }
}