package utilitaire;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import annotation.Controleur;
import annotation.UrlMapping;

public class MappingMethodClass {
    private String className;
    private String methodName;
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

    public String getMethodName() {
        return this.methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setHttpMethod(String httpMethod) {
        HttpMethod = httpMethod;
    }

    public MappingMethodClass(String className, String methodName, String httpMethod) {
        this.className = className;
        this.methodName = methodName;
        this.HttpMethod = httpMethod;
    }

}