package invoker;

import java.lang.reflect.Method;

import context.ControllerContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import utilitaire.Sprint11Bis.Sprint11Bis;

public class MethodInvoker {

    public void invoke(
            ControllerContext context,
            HttpServletRequest request,
            ServletContext servletContext) throws Exception {

        Method method = context.getMethod();

        if (!Sprint11Bis.MethodCanBeInvoked(method, request, servletContext)) {
            return ;
        }
        context.setResult(
        method.invoke(
                context.getControllerInstance(),
                context.getArguments()));
    }

}