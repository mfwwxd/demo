package uyun.smc.request;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;
import uyun.smc.common.request.RequestEntity;

/**
 * PUT请求
 */
public class PutRequestClient extends AbstractRequestClient implements RequestClient {

    public PutRequestClient(RequestEntity entity) {
        super(entity);
    }

    @Override
    protected Request createDirectRequest(RequestEntity requestEntity) {
        final String requestUtl = appendRequestParam2URL(requestEntity);

        Headers headers = covertMapToHeaders(requestEntity.getHeaders());
        RequestBody requestBody = generateRequestBody(requestEntity);
        Request.Builder builder = new Request.Builder().url(requestUtl).headers(headers).put(requestBody);
        return builder.build();
    }
}