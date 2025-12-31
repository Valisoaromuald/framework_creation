package utilitaire;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import annotation.InputParam;
import jakarta.servlet.http.HttpServletRequest;
import utilitaire.Sprint8Bis.Sprint8Bis;

public class Sprint8 {

    public static boolean hasMap(Method m) {
        System.out.println("afficahge de la methode: " + m.getName());
        boolean response = false;
        Parameter[] parameters = m.getParameters();
        for (Parameter p : parameters) {
            Type genericType = p.getParameterizedType();
            if (genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                if (pt.getActualTypeArguments().length > 1) {
                    Type keyType = pt.getActualTypeArguments()[0];
                    Type valueType = pt.getActualTypeArguments()[1];
                    if (keyType == String.class && valueType == Object.class) {
                        System.out.println("manaraka norme elah");
                        response = true;
                        break;
                    }
                }
            }
        }

        return response;
    }

    public static List<Class<?>> getClassesWithFields(List<String> allClasses) throws Exception {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        Field[] fields = null;
        for (String clazz : allClasses) {
            Class<?> cl = ClasseUtilitaire.createClass(clazz);
            fields = cl.getDeclaredFields();
            if (fields.length != 0 && fields != null) {
                classes.add(cl);
            }
        }
        return classes;

    }

    public static List<String> getFieldsNames(Class<?> clazz) {
        List<String> fieldsNames = new ArrayList<String>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            fieldsNames.add(f.getName());
        }
        return fieldsNames;
    }

    public static Class<?> RelevantClassWithHttpParameters(List<String> paramsNames, List<String> allClasses)
            throws Exception {
        List<Class<?>> classes = getClassesWithFields(allClasses);
        boolean mitovy = false;
        List<String> fieldsNames = null;
        for (Class<?> clazz : classes) {
            fieldsNames = getFieldsNames(clazz);
            mitovy = new HashSet<>(fieldsNames).equals(new HashSet<>(paramsNames));
            if (mitovy) {
                return clazz;
            }
        }
        throw new Exception("aucune classe n'a  d'attributs qui correspondent avec ces noms de parametres");
    }

    public static Map<String, Object> buildMap(HttpServletRequest req, MappingMethodClass mc, List<String> allClasses)
            throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        Enumeration<String> listeParametres = req.getParameterNames();
        List<String> paramLists = Collections.list(listeParametres);

        Class<?> clazz = Class.forName(mc.getClassName());
        Class<?> referenceClassForMap = RelevantClassWithHttpParameters(paramLists, allClasses);
        Field[] fields = clazz.getDeclaredFields();
        Object tempo = null;
        String[] paramValues = null;
        if (clazz != null) {
            for (int i = 0; i < paramLists.size(); i++) {
                paramValues = req.getParameterValues(paramLists.get(i));
                if (paramValues.length == 1) {
                    result.put(paramLists.get(i), paramValues[0]);
                } else {
                    result.put(paramLists.get(i), Arrays.asList(paramValues));
                }
            }
        }
        return result;
    }
    public static String getStringForResearchInRequestParameters(Parameter p){
        String andramanaVoalohany= p.getName();
        Type type = p.getType();
        if(type instanceof Class<?> typeClass){
            System.out.println("afficheo ihany aloha :"+p.getName()+"tableau ve:"+typeClass.isArray());
            if(!Sprint8Bis.isJavaClass(typeClass)){
                if(!typeClass.isArray())
                    andramanaVoalohany = andramanaVoalohany.substring(0, 1).toLowerCase() + ".";
            }
        }
        return andramanaVoalohany;
    }
    public static List<String> getAppropriateRequestParamName(Parameter p, List<String> reqParamsNames) {
        String andramanaVoalohany= getStringForResearchInRequestParameters(p);
        List<String> results = new ArrayList<String>();
        for (String s : reqParamsNames) {
            int indexOfFirstOccurency = s.indexOf(andramanaVoalohany);
            System.out.println("index of first occurency: " + indexOfFirstOccurency);
            if (indexOfFirstOccurency == 0) {
                results.add(s);
            } else {
                boolean hasAnnotation = p.isAnnotationPresent(InputParam.class);
                System.out.println("annotation:" + hasAnnotation);
                if (hasAnnotation) {
                    InputParam inpParam = p.getAnnotation(InputParam.class);
                    indexOfFirstOccurency = s.indexOf(inpParam.paramName());
                    System.out.println("index of first occurency with annotation: " + indexOfFirstOccurency);

                    if (indexOfFirstOccurency == 0) {
                        results.add(s);
                    }
                } 
            }
        }
        System.out.println("any aloha ny valiny e:"+results);
        return results;
    }
}
