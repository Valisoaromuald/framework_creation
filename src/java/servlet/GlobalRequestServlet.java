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
    public void actionToDo(MappingMethodClass mcc, HttpServletRequest req, HttpServletResponse res) throws Exception {
        try {
            Class<?> c = Class.forName(mcc.getClassName());
            Method m = ClasseUtilitaire.getMethodByNom(c, mcc.getMethodName());

            if (m == null) {
                throw new Exception("Méthode introuvable: " + mcc.getMethodName());
            }

            Object[] objects = null;

            Object instance = c.getDeclaredConstructor().newInstance();
            Object obj = m.invoke(instance, objects);

            if (obj == null) {
                obj = "";
            }

            Class<?> typeRetour = m.getReturnType();


            if (typeRetour.equals(String.class)) {
                res.setContentType("text/plain; charset=UTF-8");
                res.getWriter().println(obj);
                return;
            }

            
            if (ModelView.class.isAssignableFrom(typeRetour)) {
                ModelView mv = (ModelView) obj;

                if (mv.getObjects() != null) {
                    for (Map.Entry<String, Object> entry : mv.getObjects().entrySet()) {
                        req.setAttribute(entry.getKey(), entry.getValue());
                    }
                }

                RequestDispatcher dispatcher = req.getRequestDispatcher("/" + mv.getView());
                dispatcher.forward(req, res);
                return;
            }

            
            throw new Exception("Type retour non supporté : " + typeRetour.getName());

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur dans actionToDo", e);
        }
    }

}