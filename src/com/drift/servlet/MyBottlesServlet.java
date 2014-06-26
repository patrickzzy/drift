package com.drift.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.drift.core.ChatMessage;
import com.drift.core.DBConnector;
import com.drift.core.User;

/**
 * Servlet implementation class MyBottlesServlet
 */
@WebServlet("/my_bottle")
public class MyBottlesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MyBottlesServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		MyServletUtil.setCharacterEncoding(request, response);
		
		User user = MyServletUtil.checkLogin(request, response);
		if(user == null) {
			return;
		}		
		
		//Set<User> friends = null;
		List<ChatMessage> messages = null;
		try {
			DBConnector dateDB = MyServletUtil.getDateDB(getServletContext());
			messages = dateDB.getMessages(user.getUid(), 0, 30);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		if(messages != null && !messages.isEmpty()) {
			request.setAttribute("messages", messages);
			//System.out.println(friends);
			getServletContext().getRequestDispatcher("/messages.jsp").forward(request, response);
		} else {
			PrintWriter out = response.getWriter();
			out.println("<html><head>");
			out.println("<meta http-equiv='Content-Type' content='text/html;charset=utf-8' />");
			out.println("</head><body><center><p>暂无消息</p><p><a href='main.jsp'>返回</a></p></center></body>");
			return;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}