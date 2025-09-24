package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class GlobalRequestServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        // url et chemin
        out.println("<li><strong>Méthode :</strong> " + request.getMethod() + "</li>");
        out.println("<li><strong>Chemin demandé :</strong> " + request.getRequestURI() + "</li>");
        out.println("<li><strong>Chemin Servlet :</strong> " + request.getServletPath() + "</li>");
        out.println("<li><strong>Contexte :</strong> " + request.getContextPath() + "</li>");
        out.println("<li><strong>Query String :</strong> " + request.getQueryString() + "</li>");
    }
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
}
    