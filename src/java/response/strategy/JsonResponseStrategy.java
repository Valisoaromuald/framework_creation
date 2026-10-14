package response.strategy;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import annotation.Json;
import context.ControllerContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import response.ResponseHandler;
import utilitaire.Sprint9.JsonResponse;
import utilitaire.Sprint9.JsonUtil;

public class JsonResponseStrategy implements ResponseStrategy {

    @Override
    public boolean supports(ControllerContext context) {
        Method method = context.getMethod();
        Json jsonAnnotation = method.getAnnotation(Json.class);
        return context.getMethod() != null && jsonAnnotation != null;
    }

    @Override
    public void handle(
            ControllerContext context,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Method method = context.getMethod();
        Json jsonAnnotation = method.getAnnotation(Json.class);
        try {
            JsonResponse<Object> jsonResponse = new JsonResponse<>("success", response.getStatus(), context.getResult());
            responseHandler.writeJson(response, jsonResponse);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

}