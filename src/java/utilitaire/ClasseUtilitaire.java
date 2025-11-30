package utilitaire;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import annotation.Controleur;
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

    public static Map<String, MappingMethodClass> generateUrlsWithMappedMethodClass(File file) throws Exception {
        Map<String, MappingMethodClass> results = new HashMap<String, MappingMethodClass>();
        List<String> classNames = findAllClassNames(file, "");
        for (String className : classNames) {
            Class<?> clazz = createClass(className);
            if (clazz != null) {
                if (clazz.isAnnotationPresent(Controleur.class)) {
                    Method[] methodes = clazz.getDeclaredMethods();
                    for (Method m : methodes) {
                        if (m.isAnnotationPresent(UrlMapping.class)) {
                            results.put(m.getAnnotation(UrlMapping.class).url(),
                                    new MappingMethodClass(clazz.getName(), m.getName()));
                        }
                    }
                }
            }
        }
        return results;
    }

    public static Map.Entry<String, MappingMethodClass> getRelevantMethodAndClassNames(
            Map<String, MappingMethodClass> urlsWithMappedMethodClass, File file, String url) throws Exception {
        Map.Entry<String, MappingMethodClass> result = null;
        Matcher matcher = null;
        try {
            for (Map.Entry<String, MappingMethodClass> entry : urlsWithMappedMethodClass.entrySet()) {
                matcher = urlMatcher(entry.getKey(), url);
                if (matcher == null) {
                    if (entry.getKey().equals(url)) {
                        result = entry;
                        break;
                    }
                } else {
                    result = entry;
                }
            }
        } catch (Exception e) {
            System.out.println(
                    "erreur lors de la recupération des noms de classe contenant la mehtodes associés à l'url: "
                            + e.getMessage());
        }
        return result;
    }

    public static Matcher urlMatcher(String path, String url) {
        String regex = "^" + path.replaceAll("\\{[^/]+?}", "([^/]+)") + "$";
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(url);
            if (matcher.matches()) {
                return matcher;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static int getNombreParametres(Method m) {
        return m.getParameterCount();
    }

    public static Method getMethodByNom(Class<?> classe, String name)throws Exception {
        Method[] methods = classe.getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        throw new Exception("Méthode introuvable pour: "+name);
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
            }
        }
        return null;
    }

    public static Object[] giveMethodParameters(Method m, HttpServletRequest req) throws Exception {

        int nombreParametres = m.getParameterCount();
        Object[] objects = (nombreParametres != 0)
                ? new Object[nombreParametres]
                : null;

        Enumeration<String> reqParams = req.getParameterNames();
        List<String> params = Collections.list(reqParams);

        int i = 0;

        for (String paramName : params) {
            Parameter p = findMethodParamHavingName(m, paramName);
            if (p != null) {
                String value = req.getParameter(paramName);
                objects[i] = parseStringToType(value, p.getType());
                i++;
            }
        }

        return objects;
    }
}
