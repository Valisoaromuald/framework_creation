package utilitaire;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotation.Controleur;
import annotation.UrlMapping;

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
            if(clazz != null){

            }
            if (clazz.isAnnotationPresent(Controleur.class)) {
                Method[] methodes = clazz.getDeclaredMethods();
                for (Method m : methodes) {
                    if (m.isAnnotationPresent(UrlMapping.class)) {
                        results.put(m.getAnnotation(UrlMapping.class).url(),
                                new MappingMethodClass(clazz.getName(), m.getName()));
                        break;
                    }
                }
            }
        }
        return results;
    }
    public static Map.Entry<String,MappingMethodClass> getRelevantMethodAndClassNames(Map<String, MappingMethodClass>urlsWithMappedMethodClass,File file,String url) throws Exception{
        Map.Entry<String,MappingMethodClass> result = null;
        try {
            for(Map.Entry<String,MappingMethodClass> entry: urlsWithMappedMethodClass.entrySet()){
                if(entry.getKey().equals(url)){
                    result = entry;
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("erreur lors de la recupération des noms de classe contenant la mehtodes associés à l'url: "+e.getMessage());
        }
        if(result == null){
            throw new Exception("aucun resultat trouvé");
        }
        return result;
    }
}
