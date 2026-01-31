package utilitaire.Sprint11Bis;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import annotation.Authorized;
import annotation.Role;
import utilitaire.ClasseUtilitaire;
import utilitaire.Sprint8;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.ServletContext;

public class Sprint11Bis {

    public static Properties loadProperties(ServletContext context) {
        try (InputStream is = context.getResourceAsStream("/WEB-INF/authentification.properties")) {

            if (is == null) {
                throw new IllegalStateException("authentification.properties introuvable dans WEB-INF");
            }

            Properties props = new Properties();
            props.load(is);
            return props;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean MethodCanBeInvoked(Method method, HttpServletRequest request,ServletContext context) throws Exception {
        Authorized authAnnotation = ClasseUtilitaire.getSpecificAnnotation(method, Authorized.class);
        Role roleAnnotation = ClasseUtilitaire.getSpecificAnnotation(method, Role.class);
        Properties props = loadProperties(context);
        UserSession userSession = null;
        if (authAnnotation != null) {
            userSession = getUserSessionForMethod(request, props.getProperty("session.user"));
            if (userSession != null) {
                return userSession.isAuthentified();
            }
            return false;
        } else if (roleAnnotation != null) {
            userSession = getUserSessionForMethod(request, props.getProperty("session.user"));
            String[] roles = roleAnnotation.listeRoles();
                for (String role : roles) {
                    if (userSession.hasRole(role)) {
                        return true;
                    }
            }
            return false;
        }
        return true;
    }
    public static UserSession getUserSessionForMethod(HttpServletRequest request,String property) throws Exception{
        UserSession userSession = (UserSession) request.getSession().getAttribute(property);   
        if(userSession == null){
            throw new Exception("aucune session utilisateur");
        }
        return userSession;
    }
}
