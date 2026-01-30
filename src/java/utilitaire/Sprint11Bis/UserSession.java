package utilitaire.Sprint11Bis;

import java.util.List;

public interface UserSession {
    public String[] getRoles();
    public void setRoles(String[] roles);
    public boolean hasRole(String role);
    public boolean isAuthentified();
}
