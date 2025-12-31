package utilitaire.Sprint8Bis;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import utilitaire.ClasseUtilitaire;

public class Sprint8Bis {

    public static boolean isJavaClass(Class<?> clazz) {

        if (clazz == null)
            return false;

        while (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }

        if (clazz.isPrimitive())
            return true;

        Package pkg = clazz.getPackage();
        if (pkg == null)
            return false;

        String packageName = pkg.getName();

        return packageName.startsWith("java.")
                || packageName.startsWith("javax.")
                || packageName.startsWith("jakarta.")
                || packageName.startsWith("jdk.");
    }

    public static List<String> precisionNomsAttributs(Class<?> clazz) {
        List<String> results = new ArrayList<String>();
        Field[] fields = clazz.getDeclaredFields();
        String initialeNomClasse = clazz.getName().substring(0, 1).toLowerCase();
        for (Field f : fields) {
            results.add(initialeNomClasse + "." + f.getName().toLowerCase());
        }
        return results;
    }

    public static Object configurerValeursAttributs(String partReqParamName, int compteur, List<String> httpParamsName,
            Class<?> clazz,
            HttpServletRequest req) throws Exception {

        List<String> reqParamsName = ClasseUtilitaire.getHttpParameters(req);
        Object instance = clazz.getDeclaredConstructor().newInstance();
        System.out.println("instance e:" + instance);
        System.out.println("compteur :" + compteur);
        Field[] fields = clazz.getDeclaredFields();
        if (compteur == 0) {
            partReqParamName = clazz.getName().substring(0, 1).toLowerCase();
        }
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            Class<?> fieldType = f.getType();
            partReqParamName = addFieldNameIn(partReqParamName, f.getName());
            System.out.println("affichage: " + partReqParamName);
            List<String> reqParamsNameNecessary = httpParamsName != null && httpParamsName.size() != 0
                    && appearsInEachStringOfLists(partReqParamName, httpParamsName) ? httpParamsName
                            : getCorrespondingReqParamName(partReqParamName, reqParamsName);
            Object obj = null;
            System.out.println("reqParamNecessary: " + reqParamsNameNecessary);
            if (reqParamsNameNecessary != null && reqParamsNameNecessary.size() != 0) {
                if (isJavaClass(fieldType)) {
                    System.out.println("fieldName: " + f.getName());
                    if (ObjectChecking.isListType(fieldType)) {
                        Type typeTenaIlaina = f.getGenericType();
                        System.out.println("type Ilaina BEE: " + typeTenaIlaina);
                        obj = ObjectChecking.createAndFillList(typeTenaIlaina, partReqParamName, 0, null,
                                reqParamsNameNecessary, req);
                    } else if (fieldType.isArray()) {
                        reqParamsNameNecessary = getSousChainesPourAllocation(f, reqParamsNameNecessary);
                        obj = allouerTableau(0, 0, reqParamsNameNecessary, fieldType);
                        fillArrayRecursive(partReqParamName, obj, new ArrayList<Integer>(), req);
                    } else {
                        String paramValue = req.getParameter(reqParamsNameNecessary.get(0));
                        obj = ClasseUtilitaire.parseStringToType(paramValue, fieldType);
                    }
                } else {
                    if (!ObjectChecking.isArrayType(fieldType) && ObjectChecking.isListType(fieldType)) {
                        obj = configurerValeursAttributs(partReqParamName, compteur + 1, null, fieldType, req);
                    } else {
                        if (ObjectChecking.isArrayType(fieldType)) {
                            obj = allouerTableau(0, 0, reqParamsNameNecessary, fieldType);
                            fillArrayRecursive(partReqParamName, obj, new ArrayList<Integer>(), req);
                        }

                    }
                }
                callSetter(instance, f.getName(), obj);
            }
            partReqParamName = removeFieldNameIn(partReqParamName, f.getName());
        }
        return instance;
    }

    public static boolean appearsInEachStringOfLists(String chaine, List<String> chaines) {
        for (String ch : chaines) {
            if (!ch.contains(chaine)) {
                return false;
            }
        }
        return true;
    }

    public static List<Integer> getNombresEntreCrochet(String str) {
        List<Integer> results = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\[(\\d+)\\]");
        Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            results.add(Integer.parseInt(matcher.group(1)));
        }

        return results;
    }

    public static int comparer(List<Integer> liste1, List<Integer> liste2) throws Exception {
        if (liste1.size() != liste2.size()) {
            throw new Exception("la comparaison entre deux listes qui ont des tailles differentes est imposible");
        }
        for (int i = 0; i < liste1.size(); i++) {
            int a = liste1.get(i);
            int b = liste2.get(i);
            if (a > b)
                return 1;
            if (a < b)
                return -1;
        }
        return 0;
    }

    public static int getTailleMax(int indice, List<String> chaines) throws Exception {
        List<Integer> nombresEntreCrochetPremierElement = getNombresEntreCrochet(chaines.get(0));
        int result = nombresEntreCrochetPremierElement.get(indice);
        int nombre = 0;
        for (int i = 0; i < chaines.size(); i++) {
            nombre = getNombresEntreCrochet(chaines.get(i)).get(indice);
            if (nombre > result) {
                result = nombre;
            }
        }
        return result + 1;
    }

    public static List<String> chainesAvecNombreCrochetAyantElementAtIndice(int el, int indice, List<String> chaines) {
        List<String> results = new ArrayList<String>();
        List<Integer> nombresEntreCrochetChaine = null;
        for (int i = 0; i < chaines.size(); i++) {
            nombresEntreCrochetChaine = getNombresEntreCrochet(chaines.get(i));
            if (nombresEntreCrochetChaine.get(indice) == el) {
                results.add(chaines.get(i));
            }
        }
        return results;
    }

    public static Object allouerTableau(int indiceActuel, int elementPrecedent, List<String> chaines, Class<?> clazz)
            throws Exception {
        List<Integer> nombreEntreCrochets = getNombresEntreCrochet(chaines.get(0));
        int tailleMax = getTailleMax(indiceActuel, chaines);
        if (indiceActuel > 0) {
            List<String> nouvelleChaines = chainesAvecNombreCrochetAyantElementAtIndice(elementPrecedent,
                    indiceActuel - 1, chaines);
            tailleMax = getTailleMax(indiceActuel, nouvelleChaines);
        }
        if (indiceActuel == nombreEntreCrochets.size() - 1) {
            return Array.newInstance(clazz.getComponentType(), tailleMax);
        }
        Object tableauExterne = Array.newInstance(clazz.getComponentType(), tailleMax);
        for (int i = 0; i < tailleMax; i++) {
            Object valeur;

            if (clazz.getComponentType().isArray()) {
                valeur = allouerTableau(
                        indiceActuel + 1,
                        i,
                        chaines,
                        clazz.getComponentType());
            } else {
                valeur = null;
            }

            Array.set(tableauExterne, i, valeur);
        }

        return tableauExterne;
    }

    public static List<String> getSousChainesPourAllocation(Field f, List<String> chaines) {
        List<String> results = new ArrayList<String>();
        ArrayList<String> resultats = new ArrayList<String>();
        String nomAttribut = f.getName();
        Class<?> clazz = f.getClass();
        int lastIndex = 0;
        for (String str : chaines) {
            int indexOfFirstOccurence = str.indexOf(f.getName());
            int indexForSearch = indexOfFirstOccurence + nomAttribut.length();
            if (isJavaClass(clazz)) {
                lastIndex = str.lastIndexOf("]", str.length() - 1);
                lastIndex += 1;
            } else {
                lastIndex = str.indexOf(".", indexForSearch);
            }
            results.add(str.substring(indexOfFirstOccurence, lastIndex));
        }
        return results;
    }

    public static void setValue(
            Object array,
            int index,
            String partReqParamName,
            List<String> listAngalanaValeurs, HttpServletRequest req) throws Exception {

        try {
            Class<?> componentType = getTypeComposant(array);
            // Création du tableau si nécessaire
            if (listAngalanaValeurs != null && listAngalanaValeurs.size() != 0) {
                if (array == null) {
                    array = Array.newInstance(componentType, index + 1);
                }
                if (isJavaClass(componentType)) {
                    String value = req.getParameter(listAngalanaValeurs.get(0));
                    Object converted = ClasseUtilitaire.parseStringToType(value, componentType);

                    if (componentType.isPrimitive()) {

                        if (componentType == int.class) {
                            Array.setInt(array, index, ((Number) converted).intValue());
                        } else if (componentType == long.class) {
                            Array.setLong(array, index, ((Number) converted).longValue());
                        } else if (componentType == double.class) {
                            Array.setDouble(array, index, ((Number) converted).doubleValue());
                        } else if (componentType == float.class) {
                            Array.setFloat(array, index, ((Number) converted).floatValue());
                        } else if (componentType == short.class) {
                            Array.setShort(array, index, ((Number) converted).shortValue());
                        } else if (componentType == byte.class) {
                            Array.setByte(array, index, ((Number) converted).byteValue());
                        } else if (componentType == boolean.class) {
                            Array.setBoolean(array, index, (Boolean) converted);
                        } else if (componentType == char.class) {
                            Array.setChar(array, index, (Character) converted);
                        } else {
                            throw new IllegalArgumentException(
                                    "Type primitif non supporté : " + componentType.getName());
                        }

                    } else {
                        Array.set(array, index, converted);
                    }

                } else {
                    // Type non Java → instanciation récursive
                    Object nestedObject = componentType.getDeclaredConstructor().newInstance();
                    nestedObject = configurerValeursAttributs(partReqParamName, 1, listAngalanaValeurs,
                            nestedObject.getClass(), req);
                    Array.set(array, index, nestedObject);
                }
            }
        } catch (Exception e) {
            throw new Exception(
                    "Erreur dans setValue ' : " + e.getMessage(),
                    e);
        }
    }

    public static void callSetter(
            Object target,
            String attributeName,
            Object value) throws Exception {

        String setterName = "set" +
                Character.toUpperCase(attributeName.charAt(0)) +
                attributeName.substring(1);

        Method[] methods = target.getClass().getMethods();

        for (Method m : methods) {
            if (!m.getName().equals(setterName)) {
                continue;
            }

            Class<?>[] params = m.getParameterTypes();
            if (params.length != 1) {
                continue;
            }

            Class<?> paramType = params[0];

            // Cas null
            if (value == null) {
                if (!paramType.isPrimitive()) {
                    m.invoke(target, value);
                    return;
                }
                continue;
            }

            // Compatibilité normale (List, interface, héritage)
            if (paramType.isAssignableFrom(value.getClass())) {
                m.invoke(target, value);
                return;
            }
        }

        throw new NoSuchMethodException(
                "Setter compatible introuvable : " + setterName);
    }

    public static List<String> getCorrespondingReqParamName(String partReqParamName,
            List<String> reqParamsName) {
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < reqParamsName.size(); i++) {
            if (reqParamsName.get(i).contains(partReqParamName)) {
                results.add(reqParamsName.get(i));
            }
        }
        return results;
    }

    public static void fillArrayRecursive(
            String partReqParamName,
            Object array, List<Integer> nombresEntreCrochet, HttpServletRequest req) throws Exception {
        List<String> reqParamsName = ClasseUtilitaire.getHttpParameters(req);
        Class<?> componentType = array.getClass().getComponentType();
        int length = Array.getLength(array);
        if (nombresEntreCrochet == null) {
            nombresEntreCrochet = new ArrayList<Integer>();
        }
        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);
            nombresEntreCrochet.add(i);

            if (element != null && element.getClass().isArray()) {
                fillArrayRecursive(partReqParamName, element, nombresEntreCrochet, req);
            } else {
                partReqParamName = buildPartReqParamName(partReqParamName, nombresEntreCrochet);
                List<String> listAngalanaValeurs = getCorrespondingReqParamName(partReqParamName, reqParamsName);
                nombresEntreCrochet.removeLast();
                setValue(array, i, partReqParamName, listAngalanaValeurs, req);
            }
        }
        nombresEntreCrochet = new ArrayList<Integer>();
    }

    public static String buildPartReqParamName(String partReqParamName, List<Integer> nombresEntreCrochet) {
        int indexOfLastOpenBracket = partReqParamName.indexOf("[");
        System.out.println("dernier indice crochet ouvrant:" + indexOfLastOpenBracket);
        if (indexOfLastOpenBracket != -1) {
            partReqParamName = partReqParamName.substring(0, indexOfLastOpenBracket);
        }
        System.out.println("apres traitement:" + partReqParamName);
        if (nombresEntreCrochet != null && nombresEntreCrochet.size() != 0) {
            for (Integer nb : nombresEntreCrochet) {
                partReqParamName += "[" + nb + "]";
            }
        }
        System.out.println("apres traitement final:" + partReqParamName);
        return partReqParamName;
    }

    public static Class<?> getTypeComposant(Object object) {
        Class<?> typeComposant = object.getClass();
        while (typeComposant.isArray()) {
            typeComposant = typeComposant.getComponentType();
        }
        return typeComposant;
    }

    public static String removeFieldNameIn(String chaine, String fieldName) {
        int fieldNameOccurencyIndex = chaine.indexOf(fieldName);
        if (fieldNameOccurencyIndex != -1) {
            int indexOfClosingBracketAfterFieldName = chaine.lastIndexOf("]",
                    fieldNameOccurencyIndex + fieldName.length());

            if (indexOfClosingBracketAfterFieldName != -1) {
                chaine = chaine.substring(0, indexOfClosingBracketAfterFieldName + 1);
            } else {
                chaine = chaine.substring(0, fieldNameOccurencyIndex);
            }
        }
        return chaine;
    }

    public static String addFieldNameIn(String chaine, String fieldName) {
        if (chaine.substring(chaine.length() - 1).equals(".")) {
            chaine += fieldName;
        } else {
            chaine += "." + fieldName;
        }
        return chaine;
    }

}
