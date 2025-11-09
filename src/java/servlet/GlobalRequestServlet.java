package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utilitaire.ClasseUtilitaire;
import utilitaire.MappingMethodClass;
import jakarta.servlet.ServletContext;

public class GlobalRequestServlet extends HttpServlet {
    private File root;

    @Override
    public void init() throws ServletException {
        try {
            String rootPath = System.getProperty("user.dir");
            root = new File(rootPath);
            ServletContext context = getServletContext();
           Map<String, MappingMethodClass> mappingMethodClass = ClasseUtilitaire.generateUrlsWithMappedMethodClass(root);
    
            context.setAttribute("hashmap",mappingMethodClass);
        } catch (Exception e) {
            System.out.println("Erreur d'initialisation : " + e.getMessage());;
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        service(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        service(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        service(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        service(request, response);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        service(request, response);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext context = getServletContext();
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri.substring(contextPath.length());

        if (path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }
        URL res = context.getResource(path);
        if (res != null) {
            RequestDispatcher defaultDispatcher = context.getNamedDispatcher("default");
            if (defaultDispatcher != null) {
                defaultDispatcher.forward(request, response);
            }
        } else {
            response.setContentType("text/html;charset=UTF-8");
            try {
                Map<String,MappingMethodClass> urlsWithMappedMethodAndClass = (Map<String,MappingMethodClass>) context.getAttribute("hashmap");
                Map.Entry<String,MappingMethodClass>urlInfo = ClasseUtilitaire.getRelevantMethodAndClassNames(urlsWithMappedMethodAndClass,root, path);
                String urlDemande = request.getRequestURI(); 
                PrintWriter out = response.getWriter();
                String url =urlInfo.getKey();
                MappingMethodClass mmc = urlInfo.getValue();
                String className = mmc.getClassName();
                String methodName = mmc.getMethodName(); 
                out.println("<p><h4> url demande:</h4>"+url+"</p>");
                out.print("</br>");
                out.println("<p><h4>nom de classe:</h4>"+className+"</p>");
                out.print("</br>");
                out.println("<p><h4>methode associee:</h4> "+methodName+"</p>");
                out.print("</br>");
            } catch (Exception e) {
                response.getWriter().println("<h1>404-Non trouvé</h1>");
                response.getWriter().println("URL demandée : " + request.getRequestURI());
            }
        }
    }

}