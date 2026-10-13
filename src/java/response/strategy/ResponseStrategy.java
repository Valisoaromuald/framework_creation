package response.strategy;

import context.ControllerContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ResponseStrategy {

    boolean supports(ControllerContext context);

    void handle(
            ControllerContext context,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception;

}