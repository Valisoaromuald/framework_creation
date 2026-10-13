package utilitaire;

import java.lang.reflect.Method;

public class MappingMethodClass {
    private String className;
    private Method method;
    private String HttpMethod;

    public String getHttpMethod() {
        return HttpMethod;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Method getMethod() {
        return this.method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setHttpMethod(String httpMethod) {
        HttpMethod = httpMethod;
    }

    public MappingMethodClass(String className, Method method, String httpMethod) {
        this.className = className;
        this.method = method;
        this.HttpMethod = httpMethod;
    }

}