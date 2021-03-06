package com.drift.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.drift.util.SendEmail;

/**
 * Servlet implementation class TestEmailServlet 
 */
@WebServlet("/test_email")
public class TestEmailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TestEmailServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		MyServletUtil.setCharacterEncoding(request, response);
		String action = request.getParameter("action");
		if(action!=null && action.equals("send")) {
			String toEmail = request.getParameter("email");
			String subject = request.getParameter("subject");
			String content = request.getParameter("content");
			try {
				SendEmail.send(toEmail, subject, content);
			} catch (Exception e) {
				e.printStackTrace();
			}
			request.setAttribute("msg", "发送成功");
		}
		getServletContext().getRequestDispatcher(MyServletUtil.testEmailJspPage).forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
