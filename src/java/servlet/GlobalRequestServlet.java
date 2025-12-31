package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;

import annotation.Json;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utilitaire.ClasseUtilitaire;
import utilitaire.MappingMethodClass;
import utilitaire.ModelView;
import utilitaire.Sprint8;
import utilitaire.Sprint9.JsonResponse;
import utilitaire.Sprint9.JsonUtil;
import jakarta.servlet.ServletContext;

public class GlobalRequestServlet extends HttpServlet {
    private File root;

    @Override
    public void init() throws ServletException {
        try {
            ServletContext context = getServletContext();
            String rootPath = context.getRealPath("/");
            root = new File(rootPath);
            Map<String, List<MappingMethodClass>> mappingMethodClass = ClasseUtilitaire
                    .generateUrlsWithMappedMethodClass(root);
            context.setAttribute("hashmap", mappingMethodClass);
            context.setAttribute("rootPath", root);
            System.out.println("classe avec des attributs: ");
            List<Class<?>> classes = Sprint8.getClassesWithFields(ClasseUtilitaire.findAllClassNames(root, ""));
            for (Class<?> clazz : classes) {
                System.out.println(clazz.getName());
            }
        } catch (Exception e) {
            System.out.println("Erreur d'initialisation : " + e.getMessage());
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
        String httpMethod = request.getMethod();
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
                Map<String, List<MappingMethodClass>> urlsWithMappedMethodAndClass = (Map<String, List<MappingMethodClass>>) context
                        .getAttribute("hashmap");


                Map.Entry<String, MappingMethodClass> urlInfo = ClasseUtilitaire
                        .getRelevantMethodAndClassNames(urlsWithMappedMethodAndClass, root, path, httpMethod);
                if (urlInfo == null) {
                    PrintWriter out = response.getWriter();
                    out.println("<h1>404 - Page / Not found</h1>");
                    out.println("Url demandée: " + path);
                    return;
                }

                actionToDo(urlInfo, path, request, response);

            } catch (Exception e) {

                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("text/plain");
                PrintWriter out = response.getWriter();
                out.println("UNE ERREUR EST SURVENUE");
                out.println("Message : " + e.getMessage());
                out.println();
                e.printStackTrace(out);
            }
        }
    }

    public void actionToDo(Map.Entry<String, MappingMethodClass> map, String url, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        try {
            ServletContext context = getServletContext();
            File rootDir = (File) context.getAttribute("rootPath");
            List<String> classesNames = ClasseUtilitaire.findAllClassNames(rootDir, "");
            Object[] objects = ClasseUtilitaire.giveMethodParameters(map, req, url, classesNames);
            Class<?> c = Class.forName(map.getValue().getClassName());
            Method m = ClasseUtilitaire.getMethodByNom(c, map.getValue().getMethodName());
            Object instance = c.getDeclaredConstructor().newInstance();
            Object obj = m.invoke(instance, objects);
            Class<?> typeRetour = m.getReturnType();

            if (typeRetour.equals(String.class)) {
                res.setContentType("text/plain");
                PrintWriter out = res.getWriter();
                out.println(obj);
            } else if (typeRetour.equals(ModelView.class)) {
                res.setContentType("text/html");
                RequestDispatcher dispat = null;
                ModelView mv = (ModelView) obj;

                if (mv.getObjects() != null) {
                    for (Map.Entry<String, Object> entry : mv.getObjects().entrySet()) {
                        req.setAttribute(entry.getKey(), entry.getValue());
                    }
                }

                RequestDispatcher dispatcher = req.getRequestDispatcher("/" + mv.getView());
                dispatcher.forward(req, res);
                return;
            } else {
                if (m != null) {                    
                    Json jsonAnnotation = m.getAnnotation(Json.class);
                    if (jsonAnnotation != null) {
                        try {
                            JsonResponse<Object> jsonResponse = new JsonResponse<>("success", res.getStatus(), obj);
                            writeJson(res, jsonResponse);

                        } catch (Exception e) {
                            e.printStackTrace();
                            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            writeJson(res, new JsonResponse<>("error", res.getStatus(), null));
                        }
                    }
                }

            }
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause(); // <-- vraie exception du contrôleur
            cause.printStackTrace();
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            JsonResponse<Object> errorResponse = new JsonResponse<>("error", res.getStatus(), cause.getMessage());

            writeJson(res, errorResponse);
        } catch (

        Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur dans actionToDo:" + e.getMessage());
        }
    }

    private void writeJson(HttpServletResponse resp, Object obj) throws IOException {
        resp.setContentType("application/json");
        try {
            resp.getWriter().write(JsonUtil.toJson(obj));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}