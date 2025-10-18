package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletContext;

public class GlobalRequestServlet extends HttpServlet {    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException{
        service(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException{
        service(request,response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        service(request,response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        service(request,response);
    }
    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        service(request,response);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        ServletContext context = getServletContext(); 
        String contextPath = request.getContextPath();  // ex: /Sprint1
        String uri = request.getRequestURI();           // ex: /Sprint1/aaaaa
        String path = uri.substring(contextPath.length()); // -> /aaaaa

        System.out.println("🔍 Requête: " + uri);
        System.out.println("➡️ Chemin relatif: " + path);
        if (path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }
       URL res = context.getResource(path);
        if (res != null) { 
            System.out.println("eto foana: "+(context.getResource(path)));
              // Essayer de forward vers le servlet "default" pour les fichiers statiques
              RequestDispatcher defaultDispatcher = context.getNamedDispatcher("default");
            if (defaultDispatcher != null) {
                defaultDispatcher.forward(request, response);
            }
        } else {
            // Si le servlet default n’existe pas, juste afficher l’URL
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().println("URL demandée : " + request.getRequestURI());
        }
    }
}