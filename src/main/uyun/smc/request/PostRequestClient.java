package uyun.smc.request;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;
import uyun.smc.common.request.RequestEntity;

/**
 * POST请求
 */
public class PostRequestClient extends AbstractRequestClient implements RequestClient {

    public PostRequestClient(RequestEntity entity) {
        super(entity);
    }

    @Override
    protected Request createDirectRequest(RequestEntity requestEntity) {
        final String requestUtl = appendRequestParam2URL(requestEntity);

        Headers headers = covertMapToHeaders(requestEntity.getHeaders());
        RequestBody requestBody = generateRequestBody(requestEntity);
        Request.Builder builder = new Request.Builder().url(requestUtl).headers(headers).post(requestBody);
        return builder.build();
    }
}