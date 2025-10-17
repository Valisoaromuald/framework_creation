package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        URL resourceUrl = getServletContext().getResource(path);

        if (resourceUrl != null) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(path);
            dispatcher.forward(request, response);
        } else {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<h2>Debug Request</h2>");
            out.println("<ul>");
            out.println("<li><strong>Méthode :</strong> " + request.getMethod() + "</li>");
            out.println("<li><strong>Chemin demandé :</strong> " + request.getRequestURI() + "</li>");
            out.println("<li><strong>ServletPath :</strong> " + request.getServletPath() + "</li>");
            out.println("<li><strong>ContextPath :</strong> " + request.getContextPath() + "</li>");
            out.println("<li><strong>Query String :</strong> " + request.getQueryString() + "</li>");
            out.println("</ul>");
        }
    }
}