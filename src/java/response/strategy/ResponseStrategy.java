package response.strategy;

import context.ControllerContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import response.ResponseHandler;

public interface ResponseStrategy {
    ResponseHandler responseHandler = new ResponseHandler();
    boolean supports(ControllerContext context);

    void handle(
            ControllerContext context,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception;

}