package session;

import context.ControllerContext;
import utilitaire.Sprint11;
import jakarta.servlet.http.HttpServletRequest;

public class SessionHandler {
    public void handle(
            ControllerContext context,
            HttpServletRequest request)
            throws Exception {
        if (Sprint11.getSessionMap(
                context.getMethod().getParameters()) != null) {

            Sprint11.remettreMapDansSession(
                    request,
                    Sprint11.extractSessionMap(
                            context.getArguments(),
                            context.getMethod().getParameters()));

        }
    }
}
