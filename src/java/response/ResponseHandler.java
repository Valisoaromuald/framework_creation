package response;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import annotation.Json;
import context.ControllerContext;
import response.strategy.JsonResponseStrategy;
import response.strategy.ModelViewResponseStrategy;
import response.strategy.ResponseStrategy;
import response.strategy.StringResponseStrategy;
import utilitaire.ModelView;
import utilitaire.Sprint9.JsonResponse;
import utilitaire.Sprint9.JsonUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ResponseHandler {

    private final List<ResponseStrategy> strategies;

    public ResponseHandler() {

        strategies = List.of(
                new StringResponseStrategy(),
                new ModelViewResponseStrategy(),
                new JsonResponseStrategy());

    }

    public void handle(
            ControllerContext context,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        for (ResponseStrategy strategy : strategies) {

            if (strategy.supports(context)) {

                strategy.handle(context, request, response);

                return;
            }

        }

        throw new Exception(
                "Aucune stratégie trouvée pour "
                        + context.getMethod().getReturnType().getName());

    }

    public void writeJson(HttpServletResponse response, Object obj)
            throws IOException {

        response.setContentType("application/json");
        response.getWriter().write(JsonUtil.toJson(obj));

    }

    public void handleException(
            ControllerContext context,
            Throwable exception,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Method method = context.getMethod();
       
        // Cas JSON
        if (method.isAnnotationPresent(Json.class)) {

            JsonResponse<Object> json = new JsonResponse<>(
                    "error",
                    response.getStatus(),
                    exception.getMessage());

            writeJson(response, json);
            return;
        }

        // Cas ModelView
        if (method.getReturnType().equals(ModelView.class)) {

            request.setAttribute("exception", exception);
            request.setAttribute("message", exception.getMessage());

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");

            dispatcher.forward(request, response);
            return;
        }

        // Cas String 
        if (method.getReturnType().equals(String.class)) {
            response.setContentType("text/plain");
            response.getWriter().println(exception.getMessage());
            return;
        }

        response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                exception.getMessage());
    }

}