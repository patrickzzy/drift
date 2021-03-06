package com.drift.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.drift.bean.User;

public final class MyServletUtil {
	private static final String pattToJsp = "/WEB-INF/pages";
	
	public static final String loginJspPage = pattToJsp + "/login.jsp";
	public static final String registerJspPage = pattToJsp + "/register.jsp";
	public static final String registerOkJspPage = pattToJsp + "/register_ok.jsp";
	public static final String mainJspPage = pattToJsp + "/main.jsp";
	public static final String activateJspPage = pattToJsp + "/activate_tip.jsp";
	public static final String getBottleJspPage = pattToJsp + "/get_bottle.jsp";
	public static final String doPostBottleJspPage = pattToJsp + "/do_post.jsp";
	public static final String postBottleJspPage = pattToJsp + "/post_bottle.jsp";
	public static final String messagesJspPage = pattToJsp + "/messages.jsp";
	public static final String logoutJspPage = pattToJsp + "/logout.jsp";
	public static final String editProfileJspPage = pattToJsp + "/edit_profile.jsp";
	public static final String editPhotoJspPage = pattToJsp + "/edit_photo.jsp";
	public static final String showUserJspPage = pattToJsp + "/user.jsp";
	public static final String errorJspPage = pattToJsp + "/errorpage.jsp";
	public static final String chatJspPage = pattToJsp + "/chat.jsp";
	public static final String testEmailJspPage = pattToJsp + "/test_email.jsp";
	public static final String thirdPartyRegisterJspPage = pattToJsp + "/third_party_register.jsp";
	
	private static final String MY_SERVLET_UTIL_PROPS_PATH = "MyServletUtil.properties";
	public static String entryURL = null;
	//public final String entryURL = "http://localhost:8080/drift";

	static {
		//System.out.println(new File(".").getAbsolutePath());
		//System.out.println(new File("/").getAbsolutePath());
		//System.out.println(System.getProperty("user.dir"));
		//System.out.println(getClass().getResource("/"));
		Properties props = new Properties();
		//String path = getClass().getResource("/") + DB_PROPS_PATH;
		InputStream in = MyServletUtil.class.getClassLoader().getResourceAsStream(MY_SERVLET_UTIL_PROPS_PATH);
		//System.out.println("path is: " + path);
		//File file = new File(path);
		try {
			//props.load(new FileInputStream(file));
			props.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		entryURL = props.getProperty("entryUrl");
		//System.out.println("entry URL: " + entryURL);
	}


	
	// SESSION Variable Constants
	public static final String SESS_UID = "userId";			// Deprecated
	public static final String SESS_USERNAME = "username";	// Deprecated
	public static final String SESS_USER = "user";
	public static final String SESS_FOREIGN_UID = "fuid";
	public static final String SESS_FOREIGN_ACCESS_TOKEN = "accessToken";
	
	
	// REQUEST Variable Constants
	public static final String REQ_EDIT_PROFILE_USER_STRING = "user";
	
	/*public static DAO getDateDB(ServletContext context) throws Exception {
		DAO dateDB = (DAO)context.getAttribute("dateDB");
		if(dateDB == null) {
			dateDB = new DAO();
			context.setAttribute("dateDB", dateDB);
		}
		return dateDB;
	}*/
	
	@SuppressWarnings("deprecation")
	public static String timestampToDate(long ts) {
		Date date = new Date(ts);
		int year = date.getYear() + 1900;
		String yearString = year + "";
		int month = date.getMonth() + 1;
		String monthString = (month < 10) ? ("0" + month) : ("" + month); 
		
		return yearString + monthString;
	}	
	
	public static void setCharacterEncoding(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8"); /* seems this is only useful for POST method, not GET */
		response.setCharacterEncoding("UTF-8");
	}
	
	// Check to see if logged in, if true then return User
	public static User checkLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//String user = (String) request.getSession().getAttribute("user");
		
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute(MyServletUtil.SESS_USER);
		
		if(user == null || user.getUid() <= 0) {
			PrintWriter out = response.getWriter();
			out.println("<html><head>");
			out.println("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />");
			out.println("</head><body>");
			out.println("<font color='red' size='4'>");
			out.println("<center>");
			out.println("<b>错误信息：</b>");
			out.println("您访问的页面失效，或您还未登录。");
			out.println("请<a href='login'>重新登录</a>");
			out.println("</center>");
			out.println("</font>");
			out.println("</body></html>");
			request.setAttribute("loginCheck", "fail");
			return null;
		} else {
			return user;
		}
	}

	
	// REST API ApiLogin
	public static final int API_CODE_LOGIN_SUCCEED = 100;
	public static final int API_CODE_LOGIN_ERR_UNKOWN = 101;
	public static final int API_CODE_LOGIN_BAD_ARGS = 102;
	public static final int API_CODE_LOGIN_USER_NOT_EXIST = 103;
	public static final int API_CODE_LOGIN_ERR_PASSWORD = 104;
	public static final int API_CODE_LOGIN_EMAIL_NOT_EXIST = 105;
	public static final int API_CODE_LOGIN_USER_NOT_ACTIVATED = 106;
	
	public static final String[] API_CODE_LOGIN_STRINGS = {
		"Succeed",
		"Unknown Error",
		"Bad Arguments",
		"User name has not been registered",
		"Wrong Password",
		"Email has not been registered",
		"User has not yet activated"		
	};
	
	/*public static int getLoginStatusCode(int db_code) {		
		int code;
		
		switch (db_code) {
		case DAO.DB_STATUS_OK:
			code = API_CODE_LOGIN_SUCCEED;
			break;
		case DAO.DB_STATUS_ERR_BAD_ARGS:
			code = API_CODE_LOGIN_BAD_ARGS;
			break;
		case DAO.DB_STATUS_ERR_PASSWORD:
			code = API_CODE_LOGIN_ERR_PASSWORD;
			break;
		case DAO.DB_STATUS_ERR_USER_NOT_EXIST:
			code = API_CODE_LOGIN_USER_NOT_EXIST;
			break;
		case DAO.DB_STATUS_ERR_EMAIL_NOT_EXIST:
			code = API_CODE_LOGIN_EMAIL_NOT_EXIST;
			break;
		case DAO.DB_STATUS_ERR_USER_NOT_ACTIVATED:
			code = API_CODE_LOGIN_USER_NOT_ACTIVATED;
			break;
		default:
			code = API_CODE_LOGIN_ERR_UNKOWN;
			break;
		}
		
		return code;
	}*/
	
	public static String getLoginMessage(int status) {
		return API_CODE_LOGIN_STRINGS[status - MyServletUtil.API_CODE_LOGIN_SUCCEED];
	}
	
	// REST API Register
	public static final int API_CODE_REGISTER_SUCCEED = 110;
	public static final int API_CODE_REGISTER_ERR_UNKOWN = 111;
	public static final int API_CODE_REGISTER_BAD_ARGS = 112;
	public static final int API_CODE_REGISTER_USER_EXIST = 113;
	public static final int API_CODE_REGISTER_EMAIL_EXIST = 114;
	
	public static final String[] API_CODE_REGISTER_STRINGS = {
		"Succeed",
		"Unknown Error",
		"Bad Arguments",
		"User name has already been registered",
		"Email has already been registered",
	};
	
	/*public static int getRegisterStatusCode(int db_code) {		
		int code;
		
		switch (db_code) {
		case DAO.DB_STATUS_OK:
			code = API_CODE_REGISTER_SUCCEED;
			break;
		case DAO.DB_STATUS_ERR_BAD_ARGS:
			code = API_CODE_REGISTER_BAD_ARGS;
			break;
		case DAO.DB_STATUS_ERR_USER_EXISTS:
			code = API_CODE_REGISTER_USER_EXIST;
			break;
		case DAO.DB_STATUS_ERR_EMAIL_EXISTS:
			code = API_CODE_REGISTER_EMAIL_EXIST;
			break;
		default:
			code = API_CODE_REGISTER_ERR_UNKOWN;
			break;
		}
		
		return code;
	}*/
	
	public static String getRegisterMessage(int status) {
		return API_CODE_REGISTER_STRINGS[status - MyServletUtil.API_CODE_REGISTER_SUCCEED];
	}
	
	

	
}
