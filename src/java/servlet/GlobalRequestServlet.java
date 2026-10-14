package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.Part;
import resolver.ExceptionResolver;
import resolver.RouteResolver;
import response.ResponseHandler;
import session.SessionHandler;
import context.ControllerContext;
import context.FrameworkContext;

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
    private FrameworkContext frameworkContext;
    private ControllerFactory controllerFactory;
    private ParameterBinder parameterBinder;
    private MethodInvoker methodInvoker;
    private ServletContext context;
    private ResponseHandler responseHandler;
    private SessionHandler sessionHandler;
    private ExceptionResolver exceptionResolver;
    private RouteResolver routeResvoResolver;

    @Override
    public void init() throws ServletException {

        try {

            parameterBinder = new ParameterBinder();
            controllerFactory = new ControllerFactory();
            methodInvoker = new MethodInvoker();
            responseHandler = new ResponseHandler();
            sessionHandler = new SessionHandler();
            exceptionResolver = new ExceptionResolver(responseHandler);
            routeResvoResolver = new RouteResolver();
            this.context = getServletContext();

            frameworkContext = new FrameworkContext();

            String rootPath = context.getRealPath("/");

            File root = new File(rootPath);

            Path uploadFolder = Paths.get(rootPath, "uploads");

            List<String> classNames = ClasseUtilitaire.findAllClassNames(root, "");

            Map<String, List<MappingMethodClass>> mappings = RouteResolver.generateUrlsWithMappedMethodClass(root);

            frameworkContext.setRootDirectory(root);
            frameworkContext.setUploadFolder(uploadFolder);
            frameworkContext.setClassNames(classNames);
            frameworkContext.setMappings(mappings);

            context.setAttribute("frameworkContext", frameworkContext);

        } catch (Exception e) {

            throw new ServletException("Erreur d'initialisation du framework", e);

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

    private boolean isStaticResource(HttpServletRequest request)
            throws MalformedURLException {

        ServletContext context = getServletContext();

        String path = RouteResolver.getPath(request);

        if (path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        URL resource = context.getResource(path);

        return resource != null;
    }

    private void forwardToDefaultServlet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        RequestDispatcher dispatcher = getServletContext().getNamedDispatcher("default");

        if (dispatcher != null) {
            dispatcher.forward(request, response);
        }
    }

    @Override
    protected void service(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {

            if (isStaticResource(request)) {
                forwardToDefaultServlet(request, response);
                return;
            }

            Map.Entry<String, MappingMethodClass> route = routeResolver.resolve(request, frameworkContext);

            if (route == null) {

                String path = RouteResolver.getPath(request);

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Aucune route trouvée : " + path);

                return;
            }

            actionToDo(
                    route,
                    RouteResolver.getPath(request),
                    request,
                    response);

        } catch (Exception e) {

            try {

                exceptionResolver.resolve(
                        e,
                        null,
                        request,
                        response);

            } catch (Exception ex) {

                throw new ServletException(ex);

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

    public void actionToDo(
            Map.Entry<String, MappingMethodClass> map,
            String url,
            HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        ControllerContext controllerContext = null;
        try {

            controllerContext = controllerFactory.create(map.getValue());
            parameterBinder.bind(
                    controllerContext,
                    frameworkContext,
                    map,
                    req,
                    url);

            methodInvoker.invoke(
                    controllerContext,
                    req,
                    context);

            sessionHandler.handle(controllerContext, req);
            responseHandler.handle(
                    controllerContext,
                    req,
                    res);

        } catch (Exception e) {
            System.out.print("exception");
            exceptionResolver.resolve(e, controllerContext, req, res);
        }

    }

}