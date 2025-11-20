package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
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
import utilitaire.ModelView;
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
                actionToDo(mmc, request, response);
                } catch (Exception e) {
                e.printStackTrace();
                response.getWriter().println("<h1>404-Non trouvé</h1>");
                response.getWriter().println("URL demandée : " + request.getRequestURI());
            }
        }
    }
    public void actionToDo(MappingMethodClass mcc,HttpServletRequest req, HttpServletResponse res)throws Exception{
        try {
            Class<?> c = Class.forName(mcc.getClassName());
            Method m = ClasseUtilitaire.getMethodByNom(c, mcc.getMethodName());
            int nombreParametreMethodes = ClasseUtilitaire.getNombreParametres(m);
            Object[] objects = null;
            if(nombreParametreMethodes != 0){
                objects = new Object[nombreParametreMethodes];
                for(int i = 0 ; i < nombreParametreMethodes;i++){
                    objects[i] = null;
                }
            }
            Object instance = c.getDeclaredConstructor().newInstance();
            Object obj = m.invoke(instance,objects);
            Class<?> typeRetour = m.getReturnType();
            if(typeRetour.equals(String.class)){
                res.setContentType("text/plain");
                PrintWriter out = res.getWriter();
                out.println(obj);
            }
            else if(typeRetour.equals(ModelView.class)){
                res.setContentType("text/html");
                RequestDispatcher dispat = null;
                ModelView mv = (ModelView) obj;
                if(mv.getObjects().size() != 0){
                    for(Map.Entry<String,Object> object: mv.getObjects().entrySet()){
                        req.setAttribute(object.getKey(),object.getValue());
                    }
                }
                dispat = req.getRequestDispatcher("/"+mv.getView());
                dispat.forward(req,res);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

}