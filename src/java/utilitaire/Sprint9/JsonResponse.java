package utilitaire.Sprint9;

import java.util.List;
import java.util.List;
import java.util.Map;

public class JsonResponse<T> {

    private String status;
    private int code;
    private Object result;   // IMPORTANT : Object, pas T
    private Integer count;

    public JsonResponse(String status, int code, T data) {
        this.status = status;
        this.code = code;

        if (data == null) {
            // Force {} en JSON
            this.result = Map.of();
        } 
        else if (data instanceof List<?> list) {
            this.result = list;
            if (!list.isEmpty()) {
                this.count = list.size();
            }
        } 
        else {
            this.result = data;
        }
    }

    public String getStatus() { return status; }
    public int getCode() { return code; }
    public Object getResult() { return result; }
    public Integer getCount() { return count; }
}
