package utilitaire;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;

import jakarta.servlet.http.HttpServletRequest;

public class Sprint10 {
    public static Field FieldForUpload(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            if (f.getType().getSimpleName().equals("Path")) {
                return f;
            }
        }
        return null;
    }

    public static void injectUploadIfNeeded(
            Object instance,
            Path uploadFolder,
            Class<?> controllerClass) throws Exception {

        Field uploadField = Sprint10.FieldForUpload(controllerClass);
        if (uploadField == null)
            return;

        String fieldName = uploadField.getName();
        String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

        Method setter = controllerClass.getMethod(setterName, uploadField.getType());
        setter.invoke(instance, uploadFolder);
    }

}
