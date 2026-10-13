package response;
import java.util.List;

import context.ControllerContext;
import response.strategy.JsonResponseStrategy;
import response.strategy.ModelViewResponseStrategy;
import response.strategy.ResponseStrategy;
import response.strategy.StringResponseStrategy;
import utilitaire.Sprint9.JsonUtil;
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
    public  static void writeJson(HttpServletResponse resp, Object obj) throws Exception {
        resp.setContentType("application/json");
        try {
            resp.getWriter().write(JsonUtil.toJson(obj));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}