package utilitaire;

import java.lang.reflect.Field;
import jakarta.servlet.http.HttpServletRequest;

public class Sprint10 {
    public static Field FieldForUpload(Class<?>clazz){
        Field[] fields = clazz.getDeclaredFields();
        for(Field f: fields){
            if(f.getType().getSimpleName().equals("Path")){
                return f;
            }
        }
        return null;
    }
}
