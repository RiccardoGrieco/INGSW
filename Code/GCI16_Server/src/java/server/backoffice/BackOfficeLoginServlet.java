package server.backoffice;

import dao.interfaces.OperatorDAO;
import entities.Operator;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Handles request of login.
 * Answers with response code 200 followed by a session cookie in case of
 * succesful login.
 * Instead return 461 in case of inexistent user, or 463 in case of missing parameters.
 * Finally it could return 500 if there is an internal server error.
 * 
 * @author GCI16_25
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/Login"})
public class BackOfficeLoginServlet extends HttpServlet {
    private volatile OperatorDAO operatorDAO;
    
    @Override
    public void init(){
        setOperatorDAO(new dao.concrete.oraclesql.OperatorDAOOracleSQL());
    }
    
    public void setOperatorDAO(OperatorDAO operatorDAO){
        this.operatorDAO = operatorDAO;
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String user = request.getParameter("user");
        String pass = request.getParameter("pass");
        if(user==null || user.length() ==0 || pass==null || pass.length() == 0){
            response.sendError(463, "Missing parameter");
            return;
        }
        
        Operator op = new Operator(user, pass, Operator.TYPE_BACKOFFICE);
        Boolean log = operatorDAO.exists(op);
        if(log==null){
            response.sendError(500, "Internal server error");
            return;
        }
        if(log == true){
            response.setStatus(200);
            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(43200); //12 hours
        }else{
            response.sendError(461, "Wrong id or password");
            
        }    
    }  
}
