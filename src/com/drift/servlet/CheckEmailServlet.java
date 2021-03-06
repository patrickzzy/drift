package com.drift.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.drift.service.impl.Result;
import com.drift.service.impl.ServiceFactory;

/**
 * Servlet implementation class CheckEmailServlet
 */
@WebServlet("/check_email")
public class CheckEmailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CheckEmailServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		MyServletUtil.setCharacterEncoding(request, response);
		String email = request.getParameter("email");
		//System.out.println(email);
		PrintWriter out = response.getWriter();
		
		if(email == null || email.isEmpty()) {
			out.print("false");
			return;
		}		
		
		int result = ServiceFactory.createUserService().checkEmail(email);
		
		if(result == Result.SUCCESS) {
			out.print("ok");
		} else if(result == Result.ERR_EMAIL_EXISTS){
			out.print("exist");
		} else if(result == Result.ERR_EMAIL_REJECTED) {
			out.print("reject");
		} else {
			out.print("error");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
