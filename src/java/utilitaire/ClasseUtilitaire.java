package utilitaire;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.Annotation;
import java.util.AbstractMap;
import java.util.ArrayList;
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
        return classe;
    }

    public static Map<String, List<MappingMethodClass>> generateUrlsWithMappedMethodClass(File file) throws Exception {
        Map<String, List<MappingMethodClass>> results = new HashMap<String, List<MappingMethodClass>>();
        List<String> classNames = findAllClassNames(file, "");
        List<MappingMethodClass> mappingMethods = new ArrayList<>();
        Object annotation = null;
        String lastKey = null;
        String methodName = null;
        List<String> keys = new ArrayList<>();
        for (String className : classNames) {
            Class<?> clazz = createClass(className);
            if (clazz != null) {
                if (clazz.isAnnotationPresent(Controleur.class)) {
                    Method[] methodes = ArrangerMethodClass(clazz);
                    for (Method m : methodes) {
                        annotation = null;
                        methodName = null;
                        if (m.isAnnotationPresent(UrlMapping.class)) {
                            annotation = (UrlMapping) m.getAnnotation(UrlMapping.class);
                            lastKey = ((UrlMapping) annotation).url();
                            methodName = "ALL";

                        } else if (m.isAnnotationPresent(GetHttp.class)) {
                            annotation = (GetHttp) m.getAnnotation(GetHttp.class);
                            lastKey = ((GetHttp) annotation).url();
                            methodName = "GET";

                        } else if (m.isAnnotationPresent(PostHttp.class)) {
                            annotation = (PostHttp) m.getAnnotation(PostHttp.class);
                            lastKey = ((PostHttp) annotation).url();
                            methodName = "POST";
                        }
                        keys = results.keySet().stream().toList();
                        if (!cleDansLaListe(lastKey, keys)) {
                            mappingMethods = new ArrayList<>();
                        }
                        if (className != null && methodName != null) {
                            results.put(lastKey, mappingMethods);
                            mappingMethods.add(new MappingMethodClass(clazz.getName(), m.getName(), methodName));
                        }
                    }
                }
            }
        }
        return results;
    }

    public static Method[] ArrangerMethodClass(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        Method[] arrangedMethods = new Method[methods.length];
        List<String> urls = new ArrayList<String>();
        int i = 0;
        String urlTempo = null;
        for (Method m : methods) {
            if (m.isAnnotationPresent(UrlMapping.class)) {
                urlTempo = m.getAnnotation(UrlMapping.class).url();
                if (!urls.contains(urlTempo)) {
                    urls.add(urlTempo);
                }
            } else if (m.isAnnotationPresent(GetHttp.class)) {
                urlTempo = m.getAnnotation(GetHttp.class).url();
                if (!urls.contains(urlTempo)) {
                    urls.add(urlTempo);
                }
            } else if (m.isAnnotationPresent(PostHttp.class)) {
                urlTempo = m.getAnnotation(PostHttp.class).url();
                if (!urls.contains(urlTempo)) {
                    urls.add(urlTempo);
                }
            }
        }
        for (String u : urls) {
            for (Method m : methods) {
                if (m.isAnnotationPresent(UrlMapping.class)) {
                    urlTempo = m.getAnnotation(UrlMapping.class).url();
                    if (u.equals(urlTempo)) {
                        arrangedMethods[i] = m;
                        i++;
                    }
                } else if (m.isAnnotationPresent(GetHttp.class)) {
                    urlTempo = m.getAnnotation(GetHttp.class).url();
                    if (u.equals(urlTempo)) {
                        arrangedMethods[i] = m;
                        i++;
                    }
                } else if (m.isAnnotationPresent(PostHttp.class)) {
                    urlTempo = m.getAnnotation(PostHttp.class).url();
                    if (u.equals(urlTempo)) {
                        arrangedMethods[i] = m;
                        i++;
                    }
                }
            }
        }
        return arrangedMethods;
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
        try {
            if ((url).isEmpty() || (httpMethod).isEmpty()) {
                throw new Exception("url ou httpMethod est vide");
            }
            for (Map.Entry<String, List<MappingMethodClass>> entry : urlsWithMappedMethodClass.entrySet()) {
                matcher = matchUrl(entry.getKey(), url);
                System.out.println("afficheo aloha: " + entry + "url : " + url + "matcher: " + matcher);

                if (matcher == null || matcher.size() == 0) {
                    if (entry.getKey().equals(url)) {
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

                } else {
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
            if (result == null) {
                throw new Exception("Aucune méthode trouvée pour l'url et la méthode HTTP spécifiées.");
            }
        } catch (Exception e) {
            throw e;
        }
        if (result != null) {
            System.out.println("tena tsy mankato ve" + result.getKey());
        }
        return result;
    }

    public static boolean cleDansLaListe(String key, List<String> liste) {
        for (String s : liste) {
            if (s.equals(key)) {
                return true;
            }
        }
        return false;
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
                boolean hasAnnotation = p.isAnnotationPresent(InputParam.class);
                if (hasAnnotation) {
                    InputParam inpParam = p.getAnnotation(InputParam.class);
                    if (inpParam.paramName().equals(name)) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    public static Object[] giveMethodParameters(Map.Entry<String, MappingMethodClass> map, HttpServletRequest req,
            String url) throws Exception {
        Class<?> c = Class.forName(map.getValue().getClassName());
        Method m = ClasseUtilitaire.getMethodByNom(c, map.getValue().getMethodName());
        int nombreParametres = m.getParameterCount();
        Object[] objects = (nombreParametres != 0)
                ? new Object[nombreParametres]
                : null;

        String routePattern = null;
        routePattern = map.getKey();
        Map<String, String> matchingUrl = null;
        int i = 0;
        String value = null;
        Enumeration<String> reqParams = req.getParameterNames();
        List<String> params = Collections.list(reqParams);
        if (params.size() != 0) {
            for (String paramName : params) {
                Parameter p = findMethodParamHavingName(m, paramName);
                if (p != null) {
                    value = req.getParameter(paramName).trim();
                    objects[i] = parseStringToType(value, p.getType());
                    i++;
                }
            }
        } else {
            matchingUrl = matchUrl(routePattern, url);
            for (Map.Entry<String, String> entry : matchingUrl.entrySet()) {

                Parameter p = findMethodParamHavingName(m, entry.getKey());
                if (p != null) {
                    Parameter[] parameters = m.getParameters();
                    value = entry.getValue().trim();
                    for (int j = 0; j < m.getParameterCount(); j++) {
                        if (parameters[j].getName().equals(p.getName())) {
                            objects[j] = parseStringToType(value, p.getType());
                        }
                    }
                }
            }
        }

        return objects;
    }

}
