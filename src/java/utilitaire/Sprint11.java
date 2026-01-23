package utilitaire;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotation.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class Sprint11 {
    
    public static Map<String, Object> buildMapForSession(HttpServletRequest request) {
        Map<String, Object> maps = new HashMap<>();
        HttpSession session = request.getSession();
        List<String> httpParameters = ClasseUtilitaire.getHttpParameters(request);
        for(String httpParam: httpParameters){
            session.setAttribute(httpParam,request.getParameter(httpParam));
        }
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                Object attributeValue = session.getAttribute(attributeName);
                maps.put(attributeName, attributeValue);
            }
        return maps;
    }

    public static Map<String, Object> extractSessionMap(
            Object[] objects,
            Parameter[] parameters) {

        if (objects == null || parameters == null) {
            throw new IllegalArgumentException("Les tableaux ne doivent pas être null");
        }

        if (objects.length != parameters.length) {
            throw new IllegalArgumentException(
                    "La taille du tableau parameters doit être égale à celle du tableau objects");
        }

        for (int i = 0; i < parameters.length; i++) {
            if (isSessionMap(parameters[i])) {

                if (!(objects[i] instanceof Map)) {
                    throw new IllegalStateException(
                            "Le paramètre marqué SessionMap n'est pas une Map");
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> sessionMap = (Map<String, Object>) objects[i];
                return sessionMap;
            }
        }

        throw new IllegalStateException("Aucun paramètre de type SessionMap trouvé");
    }

    public static void remettreMapDansSession(HttpServletRequest req,Map<String,Object> sessionMap){
        HttpSession session = req.getSession();
        for(Map.Entry<String,Object> entry: sessionMap.entrySet()){
            System.out.println("velom mahereza");
            session.setAttribute(entry.getKey(),entry.getValue());
        }
    }
    public static boolean isSessionMap(Parameter p) {
        Session sessionAnnotation = ClasseUtilitaire.getSpecificAnnotation(p, Session.class);
        Type type = p.getParameterizedType();
        System.out.println("annotation session: "+sessionAnnotation);
        if (sessionAnnotation != null && type instanceof ParameterizedType pt) {
            Object[] args = pt.getActualTypeArguments();
            if (args.length == 2 && args[0] == String.class && args[1] == Object.class) {
                return true;
            }
        }
        return false;
    }
    public static Parameter getSessionMap(Parameter [] params) {
        for(Parameter p : params){
            Session sessionAnnotation = ClasseUtilitaire.getSpecificAnnotation(p, Session.class);
            Type type = p.getParameterizedType();
            System.out.println("annotation session: "+sessionAnnotation);
            if (sessionAnnotation != null && type instanceof ParameterizedType pt) {
                Object[] args = pt.getActualTypeArguments();
                if (args.length == 2 && args[0] == String.class && args[1] == Object.class) {
                    return p;
                }
            }
        }
        return null;
    }
}