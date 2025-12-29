package utilitaire.Sprint9;

import java.util.List;

public class JsonResponse<T> {

    private String status; // "success" ou "error"
    private int code;      // code HTTP
    private T result;      // objet unique ou liste
    private Integer count; // optionnel : seulement pour les listes

    public JsonResponse(String status, int code, T result) {
        this.status = status;
        this.code = code;

        if (result instanceof List) {
            this.result = result;
            this.count = ((List<?>) result).size();
        } else {
            this.result = result;
        }
    }

    // Getters et setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public T getResult() { return result; }
    public void setResult(T result) { this.result = result; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}

