package uyun.smc.common;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author wangyl Create at 2020-04-14 14:32
 */
public final class RouteKey implements Serializable {

    private String httpMethod;

    private String requestURI;

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteKey routeKey = (RouteKey) o;
        return Objects.equals(httpMethod, routeKey.httpMethod) &&
                Objects.equals(requestURI, routeKey.requestURI);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpMethod, requestURI);
    }

    private RouteKey (Builder builder) {
        this.httpMethod = builder.httpMethod;
        this.requestURI = builder.requestURI;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String httpMethod;

        private String requestURI;

        public Builder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder requestURI(String requestURI) {
            this.requestURI = requestURI;
            return this;
        }

        public RouteKey build() {
            return new RouteKey(this);
        }
    }
}
