package uyun.smc.request;

import okhttp3.Request;
import uyun.smc.common.request.RequestEntity;

/**
 * GET请求
 */
public class GetRequestClient extends AbstractRequestClient implements RequestClient {

    public GetRequestClient(RequestEntity entity) {
        super(entity);
    }

    @Override
    protected Request createDirectRequest(RequestEntity requestEntity) {

        String requestUtl = appendRequestParam2URL(requestEntity);

        // 构建请求的build实例
        Request.Builder builder = new Request.Builder().get().url(requestUtl);
        // 构建Request实例
        Request request = builder.headers(covertMapToHeaders(requestEntity.getHeaders())).build();
        return request;
    }
}