package com.drift.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.drift.bean.User;
import com.drift.bean.User.SiteType;
import com.drift.service.UserService;
import com.drift.service.impl.Result;
import com.drift.service.impl.ServiceFactory;
import com.drift.util.PhotoUtil;

/**
 * Servlet implementation class RegisterServlet
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private UserService service = ServiceFactory.createUserService();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		MyServletUtil.setCharacterEncoding(request, response);
		String action = request.getParameter("action");
		String page = MyServletUtil.registerJspPage;
		if(action != null) {
			if(action.equals("register")) {
				page = doRegister(request);
			} else if (action.equals("activate")) {
				page = doActivate(request);
			} else if (action.equals("foreign_register")) {
				page = doForeignRegister(request);
			}
		}
		getServletContext().getRequestDispatcher(page).forward(request, response);
	}

	private String doForeignRegister(HttpServletRequest request) {
		String page = MyServletUtil.loginJspPage;
		
		// Get all parameters
		String birthday = request.getParameter("birthday");		//example: '1990-01-01'
		String school = request.getParameter("school");
		String department = request.getParameter("department");
		String major = request.getParameter("major");
		String enrollYearStr = request.getParameter("enrollYear");
		String email = request.getParameter("email");
		
		String f_uid = request.getParameter("f_uid");
		String username = request.getParameter("username");
		String nickname = request.getParameter("nickname");
		String imgUrl = request.getParameter("imgUrl");
		String sexStr = request.getParameter("sex");
		int sex = -1;
		
		try {
			sex = Integer.parseInt(sexStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int enrollYear = 0;		
		try {
			enrollYear = Integer.parseInt(enrollYearStr);
		} catch (Exception e) {
			// This is not an issue
		}
		User user = null;

		Result result = new Result();
		try {
			user = new User(username, nickname, sex, school, department,
					major, email, birthday, enrollYear);
		} catch (Exception e) {
			result.setCode(Result.ERR_BAD_ARGS);
		}
		if(result.getCode() != Result.ERR_BAD_ARGS) {
			result = service.registerForeign(user, SiteType.SINA, f_uid);
		}
				
		if(result.getCode() == Result.SUCCESS) {
			user = (User) result.getResultObject();
			HttpSession session = request.getSession();
			session.setAttribute(MyServletUtil.SESS_USER, user);
			
			String path = getServletContext().getRealPath("/photo"); //上传文件目录
			PhotoUtil.downloadForeignPhoto(service, imgUrl, path, user);
			
			page = MyServletUtil.mainJspPage;
		} else if(result.getCode() == Result.ERR_EMAIL_REJECTED) {
			request.setAttribute("msg", "仅支持*.edu.cn邮箱");
		} else {
			request.setAttribute("msg", "第三方注册错误");
		}
		
		return page;
	}

	private String doActivate(HttpServletRequest request) {
		String page = MyServletUtil.activateJspPage;
		
		// Get all parameters
		String email = request.getParameter("email");
		String activateCode = request.getParameter("activateCode");
		
		if(email == null || email.isEmpty() 
				|| activateCode == null || activateCode.isEmpty()) {
			request.setAttribute("msg", "激活错误！");
			return page;
		}
		
		int result = service.doActivation(email, activateCode);		
		//System.out.println(result);
		switch (result) {
		case Result.SUCCESS:
			request.setAttribute("msg","<font color='green'>激活成功</font><br/><br/>"
					+ "<a href='" + MyServletUtil.entryURL + "'>点此登录</a>");
			break;

		default:
			request.setAttribute("msg","<font color='red'>激活失败！</font>");
			break;
		}
		
		return page;
	}

	/*
	 * Return value: the page to forward to
	 */
	private String doRegister(HttpServletRequest request) {
		String page = MyServletUtil.registerJspPage;
		
		// Get all parameters
		String nickname = request.getParameter("nickname");
		String password = request.getParameter("password");
		String sexStr = request.getParameter("sex");
		String birthday = request.getParameter("birthday");		//example: '1990-01-01'
		String username = request.getParameter("username");
		String school = request.getParameter("school");
		String department = request.getParameter("department");
		String major = request.getParameter("major");
		String email = request.getParameter("email");
		String enrollYearStr = request.getParameter("enrollYear");
		
		if(username == null || username.isEmpty() ||
		   password == null || password.isEmpty() || 
		   sexStr == null || sexStr.isEmpty() ||
		   email == null || email.isEmpty()) {
			request.setAttribute("msg","用户名、密码、性别、邮箱不能为空！");
			return page;
		}
		
		int sex = -1;
		try {
			sex = Integer.parseInt(sexStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int enrollYear = 0;		
		try {
			enrollYear = Integer.parseInt(enrollYearStr);
		} catch (Exception e) {
			// This is not an issue
		}
		User user = null;
		int result = Result.ERR_GENERIC;
		try {
			user = new User(username, nickname, sex, school, department,
					major, email, birthday, enrollYear);
		} catch (Exception e) {
			result = Result.ERR_BAD_ARGS;
		}
		if(result != Result.ERR_BAD_ARGS) {
			result = service.register(user, password);
		}
		
		switch (result) {
		case Result.SUCCESS:
			page = MyServletUtil.registerOkJspPage;
			request.setAttribute("username",username);
			request.setAttribute("email", email);
			break;
		case Result.ERR_USER_EXISTS:
			request.setAttribute("msg","用户名已存在！");
			break;			
		case Result.ERR_EMAIL_EXISTS:
			request.setAttribute("msg","邮箱已被注册！");
			break;
		case Result.ERR_SQL:
			System.err.println("数据库错误");
		default:
			request.setAttribute("msg","未知错误！");
			break;
		}
			
		return page;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
