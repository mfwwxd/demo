package uyun.smc.request;

import uyun.smc.common.enums.HttpMethod;
import uyun.smc.common.request.RequestEntity;
import uyun.smc.common.response.ResponseResult;

/**
 * @author wangyl Create at 2020-04-30 16:19
 */
public class RequestSender {

    public static RequestClient getClientInstance(RequestEntity requestEntity) {
        if (requestEntity == null) {
            throw new IllegalArgumentException("request entity can not be null");
        }
        if (requestEntity.getHttpMethod() == null) {
            throw new IllegalArgumentException("http Method can not be null");
        }

        HttpMethod httpMethod = requestEntity.getHttpMethod();

        switch (httpMethod) {
            case GET:
                return new GetRequestClient(requestEntity);
            case PUT:
                return new PutRequestClient(requestEntity);
            case POST:
                return new PostRequestClient(requestEntity);
            case DELETE:
                return new DeleteRequestClient(requestEntity);
        }
        return null;
    }

    public static ResponseResult execute(RequestEntity requestEntity) {
        RequestClient client = getClientInstance(requestEntity);
        if(client == null) {
            throw new IllegalArgumentException("http method not supported");
        }
        return client.execute();
    }
}
