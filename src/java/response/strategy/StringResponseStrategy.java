package response.strategy;

import java.io.PrintWriter;

import context.ControllerContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class StringResponseStrategy implements ResponseStrategy {

    @Override
    public boolean supports(ControllerContext context) {

        return context
                .getMethod()
                .getReturnType()
                .equals(String.class);
    }

    @Override
    public void handle(
            ControllerContext context,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        response.setContentType("text/plain");

        PrintWriter out = response.getWriter();

        out.println(context.getResult());

    }

}