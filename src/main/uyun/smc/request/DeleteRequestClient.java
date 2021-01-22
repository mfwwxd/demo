package uyun.smc.request;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;
import uyun.smc.common.request.RequestEntity;

/**
 * DELETE请求
 */
public class DeleteRequestClient extends AbstractRequestClient implements RequestClient {

    public DeleteRequestClient(RequestEntity entity) {
        super(entity);
    }

    @Override
    protected Request createDirectRequest(RequestEntity requestEntity) {
        final String requestUtl = appendRequestParam2URL(requestEntity);
        Headers headers = covertMapToHeaders(requestEntity.getHeaders());
        RequestBody requestBody = generateRequestBody(requestEntity);
        Request.Builder builder = new Request.Builder().url(requestUtl).headers(headers).delete(requestBody);
        return builder.build();
    }
}