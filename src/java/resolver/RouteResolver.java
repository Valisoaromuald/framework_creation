package resolver;

import java.io.File;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import annotation.Controleur;
import annotation.GetHttp;
import annotation.PostHttp;
import annotation.UrlMapping;
import context.FrameworkContext;
import jakarta.servlet.http.HttpServletRequest;
import utilitaire.ClasseUtilitaire;
import utilitaire.MappingMethodClass;

public class RouteResolver {

    public static Map<String, List<MappingMethodClass>> generateUrlsWithMappedMethodClass(File file) throws Exception {

        Map<String, List<MappingMethodClass>> results = new HashMap<>();

        List<String> classNames = ClasseUtilitaire.findAllClassNames(file, "");

        for (String className : classNames) {

            Class<?> clazz = ClasseUtilitaire.createClass(className);
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
                                m,
                                httpMethod));
            }
        }

        return results;
    }

    public static Map.Entry<String, MappingMethodClass> getRelevantMethodAndClassNames(
            Map<String, List<MappingMethodClass>> urlsWithMappedMethodClass, String url, String httpMethod)
            throws Exception {
        Map.Entry<String, MappingMethodClass> result = null;

        try {
            System.out.println("http methode e: " + httpMethod);
            System.out.println("resultat: " + urlsWithMappedMethodClass);
            result = getMethodClassNameByUrlAndMethod(urlsWithMappedMethodClass, url, httpMethod);

        } catch (Exception e) {
            System.out.println(
                    "erreur lors de la recupération des noms de classe contenant la mehtodes associés à l'url: "
                            + e.getMessage());
        }
        return result;
    }

    public static Map.Entry<String, MappingMethodClass> getMethodClassNameByUrlAndMethod(
            Map<String, List<MappingMethodClass>> urlMappings,
            String url,
            String httpMethod) throws Exception {

        validateInputs(url, httpMethod);

        for (Map.Entry<String, List<MappingMethodClass>> entry : urlMappings.entrySet()) {

            if (!urlMatches(entry.getKey(), url)) {
                continue;
            }

            MappingMethodClass method = findMmcByHttpMethod(entry.getValue(), httpMethod);
            if (method != null) {
                return new AbstractMap.SimpleEntry<>(entry.getKey(), method);
            }
        }

        throw new Exception("Aucune méthode trouvée pour l'url et la méthode HTTP spécifiées.");
    }

    private static void validateInputs(String url, String httpMethod) throws Exception {
        if (url == null || url.isEmpty()) {
            throw new Exception("url est vide");
        }
        if (httpMethod == null || httpMethod.isEmpty()) {
            throw new Exception("httpMethod est vide");
        }
    }

    private static boolean urlMatches(String routePattern, String url) {
        if (routePattern.equals(url)) {
            return true;
        }
        Map<String, String> matcher = matchUrl(routePattern, url);
        return matcher != null && !matcher.isEmpty();
    }

    private static MappingMethodClass findMmcByHttpMethod(
            List<MappingMethodClass> methods,
            String httpMethod) {

        for (MappingMethodClass mmc : methods) {
            if (mmc.getHttpMethod().equals(httpMethod) || mmc.getHttpMethod().equals("ALL")) {
                return mmc;
            }
        }
        return null;
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
    public static  String getPath(HttpServletRequest request){
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri.substring(contextPath.length());
        return path;
    }
    public Map.Entry<String, MappingMethodClass> resolve(
            HttpServletRequest request,
            FrameworkContext frameworkContext)
            throws Exception {
        String path = getPath(request);
        String httpMethod = request.getMethod();
        Map<String, List<MappingMethodClass>> urlsWithMappedMethodAndClass = frameworkContext
                .getMappings();
        Map.Entry<String, MappingMethodClass> urlInfo = getRelevantMethodAndClassNames(urlsWithMappedMethodAndClass, path, httpMethod);
        return urlInfo;
    }

}
