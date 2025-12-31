package utilitaire.Sprint9;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.annotation.JsonInclude;


public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // Configurer l'ObjectMapper pour ignorer les champs null
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String toJson(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }
}
