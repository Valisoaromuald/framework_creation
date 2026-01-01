package utilitaire;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
// import java.text.Annotation;
import java.nio.file.Path;
import java.lang.annotation.Annotation;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import annotation.Controleur;
import annotation.GetHttp;
import annotation.InputParam;
import annotation.PostHttp;
import annotation.UrlMapping;

import jakarta.servlet.http.HttpServletRequest;
import servlet.GlobalRequestServlet;
import utilitaire.Sprint8Bis.ObjectChecking;
import utilitaire.Sprint8Bis.Sprint8Bis;

public class ClasseUtilitaire {
    public static List<String> findAllClassNames(File rootDir, String packageName) throws ClassNotFoundException {
        List<String> classes = new ArrayList<>();
        for (File file : rootDir.listFiles()) {
            if (file.isDirectory()) {
                if (!file.getName().trim().equals(".git")) {
                    String subPackage = packageName.isEmpty() ? file.getName() : packageName + "." + file.getName();
                    classes.addAll(findAllClassNames(file, subPackage));
                }
            } else if (file.getName().endsWith(".class")) {
                String pathClassFile = packageName.isEmpty()
                        ? file.getName().replace(".class", "")
                        : packageName + "." + file.getName().replace(".class", "");
                classes.add(pathClassFile);
            }
        }
        return classes;
    }

    public static Class<?> createClass(String name) throws Exception {
        String[] sousChemin = name.split("\\.");
        int cpt = sousChemin.length - 1;
        String realClassName = "";
        Class<?> classe = null;
        while (cpt >= 0) {
            if (cpt == sousChemin.length - 1) {
                realClassName = sousChemin[cpt];
            } else {
                realClassName = sousChemin[cpt] + "." + realClassName;
            }
            try {
                classe = Class.forName(realClassName);
                if (classe != null) {
                    break;
                }
            } catch (ClassNotFoundException e) {
            }
            cpt--;
        }
        System.out.println("misy olana sahady:" + classe);
        return classe;
    }

    public static Map<String, List<MappingMethodClass>> generateUrlsWithMappedMethodClass(File file) throws Exception {

        Map<String, List<MappingMethodClass>> results = new HashMap<>();

        List<String> classNames = findAllClassNames(file, "");

        for (String className : classNames) {

            Class<?> clazz = createClass(className);
            if (clazz == null)
                continue;

            // On ne considère que les contrôleurs
            if (!clazz.isAnnotationPresent(Controleur.class))
                continue;

            for (Method m : clazz.getDeclaredMethods()) {

                String url = null;
                String httpMethod = null;

                // ======================
                // Détection des annotations HTTP
                // ======================
                if (m.isAnnotationPresent(UrlMapping.class)) {
                    UrlMapping ann = m.getAnnotation(UrlMapping.class);
                    url = ann.url();
                    httpMethod = "ALL";

                } else if (m.isAnnotationPresent(GetHttp.class)) {
                    GetHttp ann = m.getAnnotation(GetHttp.class);
                    url = ann.url();
                    httpMethod = "GET";

                } else if (m.isAnnotationPresent(PostHttp.class)) {
                    PostHttp ann = m.getAnnotation(PostHttp.class);
                    url = ann.url();
                    httpMethod = "POST";
                }

                // Aucune annotation → on ignore la méthode
                if (url == null || httpMethod == null)
                    continue;

                // ======================
                // Récupération / création sûre de la liste
                // ======================
                List<MappingMethodClass> mappingMethods = results.computeIfAbsent(url, k -> new ArrayList<>());

                // ======================
                // Ajout de la méthode
                // ======================
                mappingMethods.add(
                        new MappingMethodClass(
                                clazz.getName(),
                                m.getName(),
                                httpMethod));
            }
        }

        return results;
    }

    public static Map.Entry<String, MappingMethodClass> getRelevantMethodAndClassNames(
            Map<String, List<MappingMethodClass>> urlsWithMappedMethodClass, File file, String url, String httpMethod)
            throws Exception {
        Map.Entry<String, MappingMethodClass> result = null;

        try {
            result = getMethodClassNameByUrlAndMethod(urlsWithMappedMethodClass, url, httpMethod);

        } catch (Exception e) {
            System.out.println(
                    "erreur lors de la recupération des noms de classe contenant la mehtodes associés à l'url: "
                            + e.getMessage());
        }
        return result;
    }

    public static Map.Entry<String, MappingMethodClass> getMethodClassNameByUrlAndMethod(
            Map<String, List<MappingMethodClass>> urlsWithMappedMethodClass, String url, String httpMethod)
            throws Exception {
        Map.Entry<String, MappingMethodClass> result = null;
        boolean checked = false;
        Map<String, String> matcher = null;
        System.out.println("methode http: " + httpMethod);
        try {
            if ((url).isEmpty() || (httpMethod).isEmpty()) {
                throw new Exception("url ou httpMethod est vide");
            }
            for (Map.Entry<String, List<MappingMethodClass>> entry : urlsWithMappedMethodClass.entrySet()) {
                matcher = matchUrl(entry.getKey(), url);
                System.out.println("efa mety ve eto e vjalmfjlk: ");
                if (matcher == null || matcher.size() == 0) {
                    if (entry.getKey().equals(url)) {
                        for (MappingMethodClass mmc : entry.getValue()) {
                            if (mmc.getHttpMethod().equals(httpMethod)) {
                                checked = true;
                                result = new AbstractMap.SimpleEntry<>(entry.getKey(), mmc);
                                break;
                            }
                        }
                        if (!checked) {
                            for (MappingMethodClass mmc : entry.getValue()) {
                                if (mmc.getHttpMethod().equals("ALL")) {
                                    result = new AbstractMap.SimpleEntry<>(entry.getKey(), mmc);
                                }
                            }
                        }
                    }

                } else {
                    System.out.println("babason");
                    for (MappingMethodClass mmc : entry.getValue()) {
                        if (mmc.getHttpMethod().equals(httpMethod)) {
                            checked = true;
                            result = new AbstractMap.SimpleEntry<>(entry.getKey(), mmc);
                        }
                    }
                    if (!checked) {
                        for (MappingMethodClass mmc : entry.getValue()) {
                            if (mmc.getHttpMethod().equals("ALL")) {
                                result = new AbstractMap.SimpleEntry<>(entry.getKey(), mmc);
                            }
                        }
                    }
                }
            }
            System.out.println("result: " + result.getValue().getMethodName());
            if (result == null) {
                throw new Exception("Aucune méthode trouvée pour l'url et la méthode HTTP spécifiées.");
            }
        } catch (Exception e) {
            throw e;
        }
        return result;
    }

    public static Map<String, String> matchUrl(String routePattern, String actualUrl) {
        List<String> varNames = new ArrayList<>();
        Matcher m = Pattern.compile("\\{([^/}]+?)(\\.\\.\\.)?\\}").matcher(routePattern);
        StringBuffer regexBuffer = new StringBuffer();
        int lastPos = 0;

        while (m.find()) {
            regexBuffer.append(Pattern.quote(routePattern.substring(lastPos, m.start())));

            String varName = m.group(1);
            boolean isCatchAll = m.group(2) != null;
            varNames.add(varName);

            regexBuffer.append(isCatchAll ? "(.+)" : "([^/]+)");

            lastPos = m.end();
        }

        regexBuffer.append(Pattern.quote(routePattern.substring(lastPos)));

        String regex = "^" + regexBuffer.toString() + "$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(actualUrl);

        if (!matcher.matches()) {
            return null; // pas de correspondance
        }

        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < varNames.size(); i++) {
            result.put(varNames.get(i), matcher.group(i + 1));
        }

        return result;
    }

    public static int getNombreParametres(Method m) {
        return m.getParameterCount();
    }

    public static Method getMethodByNom(Class<?> classe, String name) throws Exception {
        Method[] methods = classe.getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        throw new Exception("Méthode introuvable pour: " + name);
    }

    /**
     * Retourne une valeur initiale par défaut pour un type donné
     * 
     * @param type Classe du paramètre
     * @return Objet initialisé
     */
    public static Object getDefaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null; // tous les objets non primitifs -> null
        }
        if (type.equals(int.class)) {
            return 0;
        }
        if (type.equals(boolean.class)) {
            return false;
        }
        if (type.equals(double.class)) {
            return 0.0;
        }
        if (type.equals(float.class)) {
            return 0.0f;
        }
        if (type.equals(long.class)) {
            return 0L;
        }
        if (type.equals(short.class)) {
            return (short) 0;
        }
        if (type.equals(byte.class)) {
            return (byte) 0;
        }
        if (type.equals(char.class)) {
            return '\0';
        }
        // Par défaut, retourne null (sécurité)
        return null;
    }

    public static Object parseStringToType(String value, Class<?> targetType) {
        if (value == null)
            return null;

        if (targetType == String.class) {
            return value;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.valueOf(value);
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.valueOf(value);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.valueOf(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.valueOf(value);
        } else if (targetType == Float.class || targetType == float.class) {
            return Float.valueOf(value);
        } else if (targetType == Short.class || targetType == short.class) {
            return Short.valueOf(value);
        } else if (targetType == Byte.class || targetType == byte.class) {
            return Byte.valueOf(value);
        } else if (targetType == Character.class || targetType == char.class) {
            if (value.length() != 1)
                throw new IllegalArgumentException("Cannot convert to char");
            return value.charAt(0);
        } else if (targetType == java.time.LocalDate.class) {
            return java.time.LocalDate.parse(value); // ISO_LOCAL_DATE par défaut
        } else if (targetType == java.time.LocalDateTime.class) {
            return java.time.LocalDateTime.parse(value);
        }
        throw new IllegalArgumentException("Type non supporté : " + targetType.getName());
    }

    public static Parameter findMethodParamHavingName(Method m, String name) {
        Parameter[] params = m.getParameters();
        for (Parameter p : params) {
            if (p.getName().equals(name)) {
                return p;
            } else {
                InputParam inputParamAnnotation = getSpecificAnnotation(p, InputParam.class);
                if (inputParamAnnotation != null) {
                    if (inputParamAnnotation.paramName().equals(name)) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    public static <T extends Annotation> T getSpecificAnnotation(
            Parameter p, Class<T> annotationClass) {
        return p.getAnnotation(annotationClass);
    }

    public static List<String> getHttpParameters(HttpServletRequest req) {
        Enumeration<String> reqParams = req.getParameterNames();
        List<String> params = Collections.list(reqParams);
        return params;
    }

    public static Object[] giveMethodParameters(
            Object instance,
            Path uploadFolder,
            Map.Entry<String, MappingMethodClass> map,
            HttpServletRequest req,
            String url,
            List<String> classes) throws Exception {

        Class<?> controllerClass = Class.forName(map.getValue().getClassName());
        Method method = ClasseUtilitaire.getMethodByNom(
                controllerClass,
                map.getValue().getMethodName());

        Parameter[] parameters = method.getParameters();
        Object[] resolvedParams = parameters.length == 0 ? null : new Object[parameters.length];

        List<String> requestParams = getHttpParameters(req);
        boolean hasAttachedFiles = GlobalRequestServlet.isMultiPart(req)
                && GlobalRequestServlet.hasAttachedFiles(req);

        Object bodyMap = null;
        if (!requestParams.isEmpty() || hasAttachedFiles) {
            bodyMap = Sprint8.buildMap(req, map.getValue(), classes);
        }

        for (int i = 0; i < parameters.length; i++) {
            resolvedParams[i] = resolveSingleParameter(
                    parameters[i],
                    req,
                    requestParams,
                    bodyMap);
        }

        Sprint10.injectUploadIfNeeded(instance, uploadFolder, controllerClass);
        resolvePathVariables(method, resolvedParams, map.getKey(), url);

        return resolvedParams;
    }

    private static Object resolveSingleParameter(
        Parameter parameter,
        HttpServletRequest req,
        List<String> requestParams,
        Object bodyMap) throws Exception {

    List<String> matchingParams =
            Sprint8.getAppropriateRequestParamName(parameter, requestParams);

    if (!matchingParams.isEmpty()) {
        return resolveFromRequestParam(parameter, req, requestParams);
    }

    return bodyMap;
}


    public static Object resolveFromRequestParam(
            Parameter parameter,
            HttpServletRequest req,
            List<String> requestParams) throws Exception {

        Type type = parameter.getParameterizedType();
        Class<?> clazz = parameter.getType();

        String paramName = getSpecificAnnotation(parameter, InputParam.class) != null
                ? getSpecificAnnotation(parameter, InputParam.class).paramName()
                : parameter.getName();

        List<String> relatedParams = Sprint8Bis.getCorrespondingReqParamName(paramName, requestParams);

        if (Sprint8Bis.isJavaClass(clazz)) {
            return ObjectChecking.resolveJavaType(type, clazz, paramName, relatedParams, req);
        }

        return ObjectChecking.resolveComplexType(type, clazz, paramName, relatedParams, req);
    }

    public static void resolvePathVariables(
            Method method,
            Object[] resolvedParams,
            String routePattern,
            String url) throws Exception {

        Map<String, String> pathVariables = matchUrl(routePattern, url);
        if (pathVariables == null)
            return;

        Parameter[] parameters = method.getParameters();

        for (Map.Entry<String, String> entry : pathVariables.entrySet()) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].getName().equals(entry.getKey())) {
                    resolvedParams[i] = parseStringToType(entry.getValue().trim(), parameters[i].getType());
                }
            }
        }
    }

}