package uyun.smc.common.request;

import uyun.smc.common.enums.HttpMethod;

import java.io.File;
import java.util.Map;
import java.util.Objects;

/**
 * @author wangyl Create at 2020-04-30 14:48
 */
public class RequestEntity {
    private String accessKey;
    private String secretKey;
    private String regionCode;
    private HttpMethod httpMethod;
    private String url;
    private Map<String, String> headers;
    private Map<String, String> requestParams;
    private String rawBody;
    private Map<String, File> fileParams;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(Map<String, String> requestParams) {
        this.requestParams = requestParams;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRawBody() {
        return rawBody;
    }

    public void setRawBody(String rawBody) {
        this.rawBody = rawBody;
    }

    public Map<String, File> getFileParams() {
        return fileParams;
    }

    public void setFileParams(Map<String, File> fileParams) {
        this.fileParams = fileParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequestEntity)) return false;
        RequestEntity that = (RequestEntity) o;
        return Objects.equals(accessKey, that.accessKey) &&
                Objects.equals(secretKey, that.secretKey) &&
                Objects.equals(regionCode, that.regionCode) &&
                httpMethod == that.httpMethod &&
                Objects.equals(url, that.url) &&
                Objects.equals(headers, that.headers) &&
                Objects.equals(requestParams, that.requestParams) &&
                Objects.equals(rawBody, that.rawBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessKey, secretKey, regionCode, httpMethod, url, headers, requestParams, rawBody, fileParams);
    }

    private RequestEntity(String accessKey, String secretKey, String regionCode,
                          HttpMethod httpMethod, String url, Map<String, String> headers, Map<String, String> requestParams, String rawBody, Map<String, File> fileParams) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.regionCode = regionCode;
        this.httpMethod = httpMethod;
        this.url = url;
        this.headers = headers;
        this.requestParams = requestParams;
        this.rawBody = rawBody;
        this.fileParams = fileParams;
    }

    public static RequestEntity copy(final RequestEntity r) {
        RequestEntity content = new RequestEntity(
                r.accessKey,
                r.secretKey,
                r.regionCode,
                r.httpMethod,
                r.url,
                r.headers,
                r.requestParams,
                r.rawBody,
                r.fileParams
        );
        return content;
    }

    private RequestEntity(final RequestEntity.Builder builder) {
        this.accessKey = builder.accessKey;
        this.secretKey = builder.secretKey;
        this.regionCode = builder.regionCode;
        this.httpMethod = builder.httpMethod;
        this.url = builder.url;
        this.headers = builder.headers;
        this.requestParams = builder.requestParams;
        this.rawBody = builder.rawBody;
        this.fileParams = builder.fileParams;
    }


    public static RequestEntity.Builder builder() {
        return new RequestEntity.Builder();
    }

    public static class Builder {

        private String accessKey;
        private String secretKey;
        private String regionCode;
        private HttpMethod httpMethod;
        private String url;
        private Map<String, String> headers;
        private Map<String, String> requestParams;
        private String rawBody;
        private Map<String, File> fileParams;

        public RequestEntity.Builder accessKey(String accessKey) {
            this.accessKey = accessKey;
            return this;
        }

        public RequestEntity.Builder secretKey(String secretKey) {
            this.secretKey = secretKey;
            return this;
        }

        public RequestEntity.Builder regionCode(String regionCode) {
            this.regionCode = regionCode;
            return this;
        }

        public RequestEntity.Builder httpMethod(HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public RequestEntity.Builder url(String url) {
            this.url = url;
            return this;
        }

        public RequestEntity.Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public RequestEntity.Builder requestParams(Map<String, String> requestParams) {
            this.requestParams = requestParams;
            return this;
        }

        public RequestEntity.Builder rawBody(String rawBody) {
            this.rawBody = rawBody;
            return this;
        }

        public RequestEntity.Builder fileParams(Map<String, File> fileParams) {
            this.fileParams = fileParams;
            return this;
        }

        public RequestEntity build() {
            return new RequestEntity(this);
        }
    }
}
