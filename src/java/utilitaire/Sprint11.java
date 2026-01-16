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
    public static Map<String,Object> buildMapForSession(HttpServletRequest request) {
        Map<String,Object> maps = new HashMap<>();
        HttpSession session = request.getSession(false);
        if(session!= null){
            Enumeration<String> attributeNames = session.getAttributeNames();
            while(attributeNames.hasMoreElements()){
                String attributeName= attributeNames.nextElement();
                Object attributeValue = session.getAttribute(attributeName);
                maps.put(attributeName,attributeValue);
            }
        }
        return maps;
    }
    public static void remettreDansSession(HttpServletRequest req,Method Map<String,Object> mapSession){
        HttpSession session = req.getSession();
        for(Map.Entry<String,Object> entry: mapSession.entrySet()){
            session.setAttribute(entry.getKey(),entry.getValue());
        }
    }
}