package uyun.smc.request;

import okhttp3.Response;
import uyun.smc.common.response.ResponseResult;

import java.io.IOException;

/**
 * @author wangyl Create at 2020-04-30 16:23
 */
public interface RequestClient {
    ResponseResult execute();

    Response commonRequest() throws IOException;
}
