<%@page import="com.drift.util.JSONUtil"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.drift.core.DBConnector"%>
<%@page import="com.drift.servlet.MyServletUtil"%>
<%@ page contentType="application/json; charset=utf8" %>

<%
	String uidStr = request.getParameter("uid");
	String nickname = request.getParameter("nickname");
	String birthday = request.getParameter("birthday");		//example: '1990-01-01'
	String school = request.getParameter("school");
	String department = request.getParameter("department");
	String major = request.getParameter("major");
	String enrollYear = request.getParameter("enrollYear");

	int uid = 0;
	try {
		uid = Integer.parseInt(uidStr);
	} catch (Exception e) {
		e.printStackTrace();
	}

	final int SUCCESS = 40000;
	final int ERR_UNKOWN = 40001;
	final int ERR_BAD_ARGS = 40002;

	int status = ERR_UNKOWN;
	Map<String, Object> map = new HashMap<String, Object>();
	
	int rtval = DBConnector.DB_STATUS_ERR_GENERIC;
	rtval = DBConnector.editProfile(uid, nickname, birthday, school, department, enrollYear, major);
	
	switch (rtval) {
	case DBConnector.DB_STATUS_OK:
		status = SUCCESS;
		map.put("result", "Succeed");
		break;
	case DBConnector.DB_STATUS_ERR_BAD_ARGS:
		status = ERR_BAD_ARGS;
		map.put("result", "Bad Arguments");
		break;
	default:
		map.put("result", "Unknown Error");
	}
	map.put("code", status);
	//System.out.println(status);

	out.print(JSONUtil.toJSONString(map));
	out.flush();
%>