package response.strategy;

import java.io.PrintWriter;
import java.util.Map;

import context.ControllerContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;
import utilitaire.ModelView;

public class ModelViewResponseStrategy implements ResponseStrategy {

    @Override
    public boolean supports(ControllerContext context) {

        return context
                .getMethod()
                .getReturnType()
                .equals(ModelView.class);
    }

    @Override
    public void handle(
            ControllerContext context,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        response.setContentType("text/html");
            ModelView mv = (ModelView) context.getResult();
            if (mv.getObjects() != null) {
                for (Map.Entry<String, Object> entry : mv.getObjects().entrySet()) {
                    request.setAttribute(entry.getKey(), entry.getValue());
                }
            }
            RequestDispatcher dispatcher = request.getRequestDispatcher("/" + mv.getView());
            dispatcher.forward(request, response);

    }

}