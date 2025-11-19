package utilitaire;

import java.util.HashMap;
import java.util.Map;

public class ModelView {
    public ModelView(String view) {
        this.view = view;
    }

    public ModelView() {
    }

    private String view;
    private Map<String,Object> objects = new HashMap<String,Object>();

    

    public Map<String, Object> getObjects() {
        return objects;
    }

    public void setObjects(Map<String, Object> objects) {
        this.objects = objects;
    }
    public void addObject(String name , Object obj){
        this.objects.put(name,obj);
    }
    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }
}
