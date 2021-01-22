package uyun.smc.common.response;

import okhttp3.Headers;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.smc.common.SdkConstant;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 返回结果
 */
public class ResponseResult {

    public static final Logger logger = LoggerFactory.getLogger(ResponseResult.class);

    /**
     * 响应编码
     */
    private Integer responseCode;

    /**
     * 是否是正常返回
     */
    private boolean isSuccessful;
    /**
     * 响应描述
     */
    private String message;
    /**
     * 响应头
     */
    private Map<String, List<String>> headers;
    /**
     * 常规结果，返回String类型
     */
    private String data;
    /**
     * 文件结果，返回为byte数组类型
     */
    private byte[] fileData;
    /**
     * 是否寻址模式
     */
    private boolean addressingMode;

    public static ResponseResult success(Integer responseCode, String data) {
        ResponseResult responseResult = new ResponseResult(responseCode);
        responseResult.data = data;
        return responseResult;
    }

    public static ResponseResult fail(Integer responseCode, String message) {
        ResponseResult responseResult = new ResponseResult(responseCode);
        responseResult.message = message;
        return new ResponseResult(responseCode);
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    private ResponseResult(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public boolean isAddressingMode() {
        return addressingMode;
    }

    public void setAddressingMode(boolean addressingMode) {
        this.addressingMode = addressingMode;
    }

    public static ResponseResult resolveResponse(Response response) throws IOException {
        ResponseResult responseResult = new ResponseResult(response.code());
        responseResult.setSuccessful(response.isSuccessful());
        String accessMode = response.headers().get(SdkConstant.ACCESS_MODE);
        responseResult.setAddressingMode("1".equals(accessMode));
        if(response.body() != null) {
            Headers headers = response.headers();
            String contentDisposition = headers.get(SdkConstant.CONTENT_DISPOSITION);
            if(contentDisposition != null && contentDisposition.contains(SdkConstant.FILE_HEADER_KEYWORD)){
                responseResult.setFileData(response.body().bytes());
                String filename = contentDisposition.replace("attachment;filename=", "");
                responseResult.setData(filename);
            }else{
                responseResult.setData(response.body().string());
            }
        }
        if(!response.isSuccessful()) {
            responseResult.setMessage(response.message());
        }
        responseResult.setHeaders(response.headers().toMultimap());

        return responseResult;
    }
}