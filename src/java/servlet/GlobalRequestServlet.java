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
        String contextPath = request.getContextPath();  
        String uri = request.getRequestURI();          
        String path = uri.substring(contextPath.length()); 

        System.out.println("🔍 Requête: " + uri);
        System.out.println("➡️ Chemin relatif: " + path);
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
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().println("URL demandée : " + request.getRequestURI());
        }
    }
}