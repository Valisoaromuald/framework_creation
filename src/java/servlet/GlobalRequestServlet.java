package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.Part;
import response.ResponseHandler;
import context.ControllerContext;

import javax.naming.Context;

import annotation.Json;
import binder.ParameterBinder;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utilitaire.ClasseUtilitaire;
import utilitaire.MappingMethodClass;
import utilitaire.ModelView;
import utilitaire.Sprint11;
import utilitaire.Sprint8;
import utilitaire.Sprint11Bis.Sprint11Bis;
import utilitaire.Sprint9.JsonResponse;
import utilitaire.Sprint9.JsonUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.MultipartConfig;
import factory.ControllerFactory;
import invoker.MethodInvoker;

@MultipartConfig
public class GlobalRequestServlet extends HttpServlet {
    private File root;
    private ControllerFactory controllerFactory;
    private ParameterBinder parameterBinder;
    private MethodInvoker methodInvoker;
    ServletContext context;
    private ResponseHandler responseHandler;

    @Override
    public void init() throws ServletException {
        try {
            parameterBinder = new ParameterBinder();
            controllerFactory = new ControllerFactory();
            methodInvoker = new MethodInvoker();
            responseHandler = new ResponseHandler();
            this.context = getServletContext();
            String rootPath = context.getRealPath("/");
            root = new File(rootPath);
            String uploadFolderName = rootPath + "uploads";
            Path uploadFolder = Paths.get(uploadFolderName);
            Map<String, List<MappingMethodClass>> mappingMethodClass = ClasseUtilitaire
                    .generateUrlsWithMappedMethodClass(root);
            this.context.setAttribute("hashmap", mappingMethodClass);
            this.context.setAttribute("rootPath", root);
            this.context.setAttribute("uploadFolder", uploadFolder);

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
        System.out.println("eto kely anie: " + path);
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

    public static boolean hasAttachedFiles(HttpServletRequest req) throws Exception {
        return req.getParts() != null && req.getParts().size() != 0;
    }

    public static boolean isMultiPart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    public static Map<String, byte[]> buildMapForFile(HttpServletRequest req) throws Exception {
        Map<String, byte[]> result = new HashMap<String, byte[]>();
        for (Part part : req.getParts()) {

            String fileName = part.getSubmittedFileName();
            if (fileName == null || fileName.isBlank()) {
                continue;
            }
            byte[] data = part.getInputStream().readAllBytes();
            result.put(fileName, data);
        }
        return result;
    }

    public void actionToDo(Map.Entry<String, MappingMethodClass> map, String url, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        try {

            File rootDir = (File) this.context.getAttribute("rootPath");
            Path uploadFolder = (Path) this.context.getAttribute("uploadFolder");
            List<String> classesNames = ClasseUtilitaire.findAllClassNames(rootDir, "");
            ControllerContext controllerContext = controllerFactory.create(map.getValue());
            Method m = controllerContext.getMethod();
            Object[] objects = null;
            objects = parameterBinder.bind(
                controllerContext,
                uploadFolder,
                map,
                req,
                url,
                classesNames);
                methodInvoker.invoke(controllerContext, req, this.context);
                Object obj = controllerContext.getResult();
            Class<?> typeRetour = m.getReturnType();
            // System.out.println("map session: " +
            // Sprint11.getSessionMap(m.getParameters()));
            if (Sprint11.getSessionMap(m.getParameters()) != null) {
                System.out.println("mankato lesy zandry an" + Sprint11.extractSessionMap(objects, m.getParameters()));
                Sprint11.remettreMapDansSession(req, Sprint11.extractSessionMap(objects, m.getParameters()));
            }
            responseHandler.handle(
                    controllerContext,
                    req,
                    res);
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause(); // <-- vraie exception du contrôleur
            cause.printStackTrace();
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            JsonResponse<Object> errorResponse = new JsonResponse<>("error", res.getStatus(), cause.getMessage());

            responseHandler.writeJson(res, errorResponse);
        } catch (

        Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur dans actionToDo:" + e.getMessage());
        }
    }



}
