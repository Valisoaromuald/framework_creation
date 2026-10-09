package context;

import java.lang.reflect.Method;

public class ControllerContext {

    private Class<?> controllerClass;

    private Object controllerInstance;

    private Method method;

    private Object[] arguments;

    private Object result;


    public ControllerContext() {
    }

    public ControllerContext(Class<?> controllerClass,
                             Object controllerInstance,
                             Method method) {

        this.controllerClass = controllerClass;
        this.controllerInstance = controllerInstance;
        this.method = method;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public Object getControllerInstance() {
        return controllerInstance;
    }

    public void setControllerInstance(Object controllerInstance) {
        this.controllerInstance = controllerInstance;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

        public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }


}