package response;


import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;

import annotation.Json;
import context.ControllerContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;
import utilitaire.ModelView;
import utilitaire.Sprint9.JsonResponse;
import utilitaire.Sprint9.JsonUtil;

public class ResponseHandler {

    public void handle(
            ControllerContext context,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Class<?> typeRetour = context.getMethod().getReturnType();
        Object obj = context.getResult();
        Method m = context.getMethod();
        if (typeRetour.equals(String.class)) {
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();
            out.println(obj);
        } else if (typeRetour.equals(ModelView.class)) {
            response.setContentType("text/html");
            ModelView mv = (ModelView) obj;
            if (mv.getObjects() != null) {
                for (Map.Entry<String, Object> entry : mv.getObjects().entrySet()) {
                    request.setAttribute(entry.getKey(), entry.getValue());
                }
            }
            RequestDispatcher dispatcher = request.getRequestDispatcher("/" + mv.getView());
            dispatcher.forward(request, response);
            return;
        } else {
            if (m != null) {
                Json jsonAnnotation = m.getAnnotation(Json.class);
                if (jsonAnnotation != null) {
                    try {
                        JsonResponse<Object> jsonResponse = new JsonResponse<>("success", response.getStatus(), obj);
                        writeJson(response, jsonResponse);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writeJson(response, new JsonResponse<>("error", response.getStatus(), null));
            }

        }

    }

    public void writeJson(HttpServletResponse resp, Object obj) throws Exception {
        resp.setContentType("application/json");
        try {
            resp.getWriter().write(JsonUtil.toJson(obj));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}