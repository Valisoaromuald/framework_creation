package utilitaire.Sprint8Bis;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import utilitaire.ClasseUtilitaire;
import utilitaire.Sprint8;
import jakarta.servlet.http.HttpServletRequest;

public class ObjectChecking {
    public static boolean isArrayType(Type type) {
        return (type instanceof Class<?> && ((Class<?>) type).isArray())
                || (type instanceof GenericArrayType);
    }

    public static boolean isListType(Type type) {
        if (type instanceof ParameterizedType) {
            Type raw = ((ParameterizedType) type).getRawType();
            return raw instanceof Class<?> &&
                    List.class.isAssignableFrom((Class<?>) raw);
        }
        return type instanceof Class<?> &&
                List.class.isAssignableFrom((Class<?>) type);
    }

    public static Object createAndFillList(
            Type type,
            String partReqParam,
            int indiceActuel,
            List<Integer> nombresEntreCrochet,
            List<String> chaines,
            HttpServletRequest req) throws Exception {

        if (nombresEntreCrochet == null) {
            nombresEntreCrochet = new ArrayList<Integer>();
        }

        /*
         * ======================
         * CAS 1 : List<T>
         * ======================
         */
        if (type instanceof ParameterizedType pt) {
            Type raw = pt.getRawType();

            if (raw instanceof Class<?> rawClass &&
                    List.class.isAssignableFrom(rawClass)) {

                List<Object> list = new ArrayList<>();
                Type elementType = pt.getActualTypeArguments()[0];
                int length = Sprint8Bis.getTailleMax(indiceActuel, chaines);
                System.out.println("gangstabab:"+elementType);
                for (int i = 0; i < length; i++) {
                    // PUSH
                    nombresEntreCrochet.add(i);

                    Object value = createAndFillList(
                            elementType,
                            partReqParam,
                            indiceActuel + 1,
                            nombresEntreCrochet,
                            chaines,
                            req);
                    list.add(value);

                    // POP
                    nombresEntreCrochet.remove(nombresEntreCrochet.size() - 1);
                }
                return list;
            }
        }

        /*
         * ======================
         * CAS 2 : Tableau T[]
         * ======================
         */
        if (type instanceof Class<?> clazz && clazz.isArray()) {
            int depth = nombresEntreCrochet.size();
            int elementPrecedent = Math.max(0, depth - 1);

            Object array = Sprint8Bis.allouerTableau(
                    depth,
                    elementPrecedent,
                    chaines,
                    clazz);
            Sprint8Bis.fillArrayRecursive(partReqParam, array, nombresEntreCrochet, req);
            return array;
        }

        /*
         * ======================
         * CAS 3 : Classe
         * ======================
         */
        if (type instanceof Class<?> clazz) {

            String realReqParamName = Sprint8Bis.buildPartReqParamName(partReqParam, nombresEntreCrochet);
            System.out.println("tsy aiko fa :"+realReqParamName);
            /*
             * ===== Types Java simples =====
             */
            if (Sprint8Bis.isJavaClass(clazz)) {
                
                String value = req.getParameter(realReqParamName);
                return ClasseUtilitaire.parseStringToType(value, clazz);
            }

            /*
             * ===== Classe métier =====
             */
            return Sprint8Bis.configurerValeursAttributs(
                    realReqParamName,
                    indiceActuel+1,
                    chaines,
                    clazz,
                    req);
        }

        throw new IllegalArgumentException("Type non supporté : " + type.getTypeName());
    }

}
