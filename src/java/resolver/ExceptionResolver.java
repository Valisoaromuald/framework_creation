package resolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import context.ControllerContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import response.ResponseHandler;
import utilitaire.Sprint9.JsonResponse;

public class ExceptionResolver {

    private final ResponseHandler responseHandler;

    public ExceptionResolver(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    public void resolve(
            Exception exception,
            ControllerContext controllerContext,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Exception realException = exception;

        if (exception instanceof InvocationTargetException ite
                && ite.getCause() instanceof Exception cause) {

            realException = cause;
        }

        // Le contrôleur est connu
        if (controllerContext != null) {

            responseHandler.handleException(
                    controllerContext,
                    realException,
                    request,
                    response);

            return;
        }

        // Aucun contrôleur
        response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                realException.getMessage());
    }
}