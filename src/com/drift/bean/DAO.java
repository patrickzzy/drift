package com.drift.bean;

//import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
//import java.util.Properties;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.drift.service.impl.Result;
import com.drift.servlet.MyServletUtil;
import com.drift.util.MD5Util;
import com.drift.util.SendEmail;

/**
 * @deprecated
 */
public class DAO {
	
	// Error status code
	public static final int DB_STATUS_OK = 0;
	public static final int DB_STATUS_ERR_GENERIC = -1;
	public static final int DB_STATUS_ERR_BAD_ARGS = -2;
	public static final int DB_STATUS_ERR_SQL = -3;
	public static final int DB_STATUS_ERR_USER_NOT_EXIST = -4;
	public static final int DB_STATUS_ERR_EMAIL_NOT_EXIST = -5;
	public static final int DB_STATUS_ERR_USER_EXISTS = -6;
	public static final int DB_STATUS_ERR_EMAIL_EXISTS = -7;
	public static final int DB_STATUS_ERR_PASSWORD = -8;
	public static final int DB_STATUS_ERR_USER_NOT_ACTIVATED = -9;
	public static final int DB_STATUS_ERR_USER_ID = -10;
	public static final int DB_STATUS_ERR_BOTTLE_ID = -11;
	public static final int DB_STATUS_ERR_NO_BOTTLE = -12;
	public static final int DB_STATUS_ERR_EMAIL_REJECTED = -13;
	
	public static final int USER_TYPE_THIS_SITE = 0;
	public static final int USER_TYPE_SINA = 1;
	public static final int USER_TYPE_RENREN = 2;
	public static final int USER_TYPE_QQ = 3;
	public static final int USER_TYPE_BAIDU = 4;	
	public static final int USER_TYPE_MAX = 5;	
	
	static DataSource ds = null;
	
/*	static private String DB_PROPS_PATH = "db.properties";
	static private String dbUrl;
	static private String dbUser;
	static private String dbPwd;*/
	
	static {
		/*try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new ExceptionInInitializerError("Initialize mysql jdbc driver error!");
		}

		//System.out.println(new File(".").getAbsolutePath());
		//System.out.println(new File("/").getAbsolutePath());
		//System.out.println(System.getProperty("user.dir"));
		//System.out.println(getClass().getResource("/"));

		Properties props = new Properties();
		//String path = getClass().getResource("/") + DB_PROPS_PATH;
		InputStream in = DAO.class.getClassLoader().getResourceAsStream(DB_PROPS_PATH);
		//System.out.println("path is: " + path);
		//File file = new File(path);
		try {
			//props.load(new FileInputStream(file));
			props.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		dbUrl = props.getProperty("dbUrl");
		dbUser = props.getProperty("dbUser");
		dbPwd = props.getProperty("dbPwd");
		//System.out.println("dbUrl: " + dbUrl + "; dbUser: " + dbUser + "; dbPwd: " + dbPwd);
		*/
		Context ctx;
		try {
			ctx = new InitialContext();
		} catch (NamingException e) {
			e.printStackTrace();
			throw new ExceptionInInitializerError("No Context");		
		}			
		
		try {
			ds = (DataSource) ctx.lookup("java:comp/env/jdbc/DateDB");
		} catch (NamingException e) {
			e.printStackTrace();
			throw new ExceptionInInitializerError("Data source look up failure!");	
		}
		
	}

	static public Connection getConnection()throws Exception{
		//return java.sql.DriverManager.getConnection(dbUrl,dbUser,dbPwd);
		return ds.getConnection();
	}

	static public void closeConnection(Connection con){
		try{
			if(con!=null) con.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	static public void closePrepStmt(PreparedStatement prepStmt){
		try{
			if(prepStmt!=null) prepStmt.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	static public void closeResultSet(ResultSet rs){
		try{
			if(rs!=null) rs.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * Check if email exists already
	 */
	static public int checkEmail(String email){
		Connection con=null;
		PreparedStatement prepStmt=null;
		ResultSet rs=null;
		int result = DB_STATUS_ERR_GENERIC;
		
		if(email== null || email.isEmpty()) {
			return DB_STATUS_ERR_BAD_ARGS;
		}
		
		if(!allowEmail(email)) {
			return DB_STATUS_ERR_EMAIL_REJECTED;
		}

		try {
			con=getConnection();
			String selectStatement = "select ID from users where EMAIL=?";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, email);
			rs = prepStmt.executeQuery();

			if (rs.next()) {
				//email exists in DB already
				result = DB_STATUS_ERR_EMAIL_EXISTS;
			} else {
				result = DB_STATUS_OK;
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}finally{
			closeResultSet(rs);
			closePrepStmt(prepStmt);
			closeConnection(con);
		}
		
		return result;		
	}
  
	/*
	 * Check if username exists already
	 */
	static public int checkUsername(String username){
		Connection con=null;
		PreparedStatement prepStmt=null;
		ResultSet rs=null;
		int result = DB_STATUS_ERR_GENERIC;

		if(username == null || username.isEmpty()) {
			return DB_STATUS_ERR_BAD_ARGS;
		}
		
		try {
			con=getConnection();
			String selectStatement = "select ID from users where NAME=? and TYPE=0";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, username);
			rs = prepStmt.executeQuery();

			if (rs.next()) {
				//user name exists in DB already
				result = DB_STATUS_ERR_USER_EXISTS;
			} else {
				result = DB_STATUS_OK;
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}finally{
			closeResultSet(rs);
			closePrepStmt(prepStmt);
			closeConnection(con);
		}
		
		return result;		
	}
  
	/* Register a User into the DB
	 * Args: username, password, sex, school, major, email
	 *
	 * Return value:
	 *	0:		succeed
	 *	< 0:	error
	 */
	static public int register(String username,
			String nickname,
			String password,
			int sex,
			String birthday,	/* caller's responsibility to make sure it's format is '1990-01-01' */
			String school,
			String department,
			String major,
			String enrollYear,
			String email) {
		Connection con = null;
		PreparedStatement selectPrepStmt1 = null;
		ResultSet selectRs1 = null;
		PreparedStatement selectPrepStmt2 = null;
		ResultSet selectRs2 = null;
		PreparedStatement insertPrepStmt = null;
		PreparedStatement selectPrepStmt3 = null;
		ResultSet selectRs3 = null;
		
		int result = DB_STATUS_ERR_GENERIC;
		
		if(username == null || username.isEmpty() ||
			password == null || password.isEmpty() ||
			email == null || email.isEmpty() ||
			(sex != 0 && sex != 1)) {
			return DB_STATUS_ERR_BAD_ARGS;
		}
		
		if(enrollYear == null || enrollYear.isEmpty())
			enrollYear = 0 + "";

		try {
			con = getConnection();
			String selectStatement = "select ID from users where NAME=? and TYPE=0";
			selectPrepStmt1 = con.prepareStatement(selectStatement);
			selectPrepStmt1.setString(1, username);
			selectRs1 = selectPrepStmt1.executeQuery();

			if (selectRs1.next()) {
				//user name exists in DB already
				return DB_STATUS_ERR_USER_EXISTS;
			} 

			selectStatement = "select ID from users where EMAIL=?";
			selectPrepStmt2 = con.prepareStatement(selectStatement);
			selectPrepStmt2.setString(1, email);
			selectRs2 = selectPrepStmt2.executeQuery();

			if(selectRs2.next()) {
				//email exists in DB already
				return DB_STATUS_ERR_EMAIL_EXISTS;
			}

			//java.util.Date date = new java.util.Date();
			//java.sql.Timestamp timestamp = new java.sql.Timestamp(date.getTime());
			//System.out.println("date is " + date.toString());
			//System.out.println("timestamp is " + date.getTime());
			String insertStatement = "insert into users (NAME, NICKNAME, PASSWORD, SEX, BIRTHDAY, " + 
					"SCHOOL, DEPARTMENT, MAJOR, ENROLLYEAR, EMAIL, REGISTER_TIME) values " + 
					"(?,?,?,?,?,?,?,?,?,?,NOW())";
			//System.out.println("Register: INSERT SQL String: " + insertStatement);
			insertPrepStmt = con.prepareStatement(insertStatement);
			insertPrepStmt.setString(1, username);
			insertPrepStmt.setString(2, nickname);
			insertPrepStmt.setString(3, password);
			insertPrepStmt.setInt(4, sex);
			insertPrepStmt.setString(5, birthday);
			insertPrepStmt.setString(6, school);
			insertPrepStmt.setString(7, department);
			insertPrepStmt.setString(8, major);
			insertPrepStmt.setString(9, enrollYear);
			insertPrepStmt.setString(10, email);			

			if(insertPrepStmt.executeUpdate() != 0) {
				//sendRegisterEmail(email);
				selectStatement = "select ID, REGISTER_TIME from users where NAME=?";
				selectPrepStmt3 = con.prepareStatement(selectStatement);
				selectPrepStmt3.setString(1, username);
				selectRs3 = selectPrepStmt3.executeQuery();

				if(selectRs3.next()) {
					int uid = selectRs3.getInt(1);
					java.sql.Timestamp timestamp2 = selectRs3.getTimestamp(2);
					long ts = timestamp2.getTime();
					sendActivationEmail(username, sex, email, uid, ts);
					
					//System.out.println("DB timestamp is " + timestamp2.getTime());
					result = DB_STATUS_OK;
				} else {
					result = DB_STATUS_ERR_SQL;
				}
			} else {
				result = DB_STATUS_ERR_SQL;
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}finally{
			closeResultSet(selectRs3);
			closeResultSet(selectRs2);
			closeResultSet(selectRs1);
			closePrepStmt(selectPrepStmt3);
			closePrepStmt(selectPrepStmt2);
			closePrepStmt(selectPrepStmt1);
			closePrepStmt(insertPrepStmt);
			closeConnection(con);
		}

		return result;
	}

	private static void sendActivationEmail(String username, int sex,
			String email, int uid, long ts) {
		String activationCode = getActivationCode(username, sex, email, uid, ts);

		String subject = username + "，欢迎注册寻寻觅觅";
		StringBuffer content = new StringBuffer();
		content.append("尊敬的用户" + username + ":<br/><br/>");
		content.append("您好！欢迎注册寻寻觅觅！<br/><br/>");
		content.append("请点击以下链接激活账号，链接仅使用一次，请尽快激活！<br/>");    		  
		String link = MyServletUtil.entryURL + "/register?action=activate&email="
				+ email + "&activateCode=" + activationCode;
		content.append("<a href=\"" + link + "\">");
		content.append(link);
		content.append("</a>");
		content.append("<br/><br/>寻寻觅觅工作组");
		SendEmail.send(email, subject, content.toString());
	}

	/* Register a Foreign site user into the DB
	 * Args: username, password, sex, school, major, email
	 *
	 * Return value: and user
	 *	0:		succeed
	 *	< 0:	error
	 */
	static public Result registerForeign(int type,
			String f_uid,
			String username,
			String nickname,
			int sex,
			String birthday,	/* caller's responsibility to make sure it's format is '1990-01-01' */
			String school,
			String department,
			String major,
			String enrollYear,
			//String imgUrl,
			String email) {
		Connection con = null;
		PreparedStatement selectPrepStmt1 = null;
		ResultSet selectRs1 = null;
		PreparedStatement selectPrepStmt2 = null;
		ResultSet selectRs2 = null;
		PreparedStatement insertPrepStmt = null;
		PreparedStatement selectPrepStmt3 = null;
		ResultSet selectRs3 = null;
		Result result = new Result();
		
		if(type <= USER_TYPE_THIS_SITE || type >= USER_TYPE_MAX || 
			f_uid == null || f_uid.isEmpty() ||
			username == null || username.isEmpty() ||
			email == null || email.isEmpty() ||
			(sex != 0 && sex != 1)) {
			result.setCode(DB_STATUS_ERR_BAD_ARGS);
			return result;
		}
		
		if(!allowEmail(email)) {
			result.setCode(DB_STATUS_ERR_EMAIL_REJECTED);
			return result;
		}

		if(enrollYear == null || enrollYear.isEmpty())
			enrollYear = 0 + "";
		
		try {
			con = getConnection();
			String selectStatement = "select ID from users where TYPE=? and F_UID=?";
			selectPrepStmt1 = con.prepareStatement(selectStatement);
			selectPrepStmt1.setInt(1, type);
			selectPrepStmt1.setString(2, f_uid);
			selectRs1 = selectPrepStmt1.executeQuery();

			if (selectRs1.next()) {
				//user exists in DB already
				result.setCode(DB_STATUS_ERR_USER_EXISTS);
			} else {
				selectStatement = "select ID from users where EMAIL=?";
				selectPrepStmt2 = con.prepareStatement(selectStatement);
				selectPrepStmt2.setString(1, email);
				selectRs2 = selectPrepStmt2.executeQuery();

				if(selectRs2.next()) {
					//email exists in DB already
					result.setCode(DB_STATUS_ERR_EMAIL_EXISTS);
				} else {
					String insertStatement = "insert into users (NAME, NICKNAME, SEX, BIRTHDAY, " + 
							"SCHOOL, DEPARTMENT, MAJOR, ENROLLYEAR, EMAIL, REGISTER_TIME, " + 
							"TYPE, F_UID) values " + 
							"(?,?,?,?,?,?,?,?,?,NOW(),?,?)";
					//System.out.println("Register: INSERT SQL String: " + insertStatement);
					insertPrepStmt = con.prepareStatement(insertStatement);
					insertPrepStmt.setString(1, username);
					insertPrepStmt.setString(2, nickname);
					insertPrepStmt.setInt(3, sex);
					insertPrepStmt.setString(4, birthday);
					insertPrepStmt.setString(5, school);
					insertPrepStmt.setString(6, department);
					insertPrepStmt.setString(7, major);
					insertPrepStmt.setString(8, enrollYear);
					insertPrepStmt.setString(9, email);			
					insertPrepStmt.setInt(10, type);			
					insertPrepStmt.setString(11, f_uid);

					if(insertPrepStmt.executeUpdate() != 0) {
						selectStatement = "select ID, REGISTER_TIME from users where TYPE=? and F_UID=?";
						selectPrepStmt3 = con.prepareStatement(selectStatement);
						selectPrepStmt3.setInt(1, type);
						selectPrepStmt3.setString(2, f_uid);
						selectRs3 = selectPrepStmt3.executeQuery();

						if(selectRs3.next()) {
							int uid = selectRs3.getInt(1);
							java.sql.Timestamp timestamp = selectRs3.getTimestamp(2);
							long ts = timestamp.getTime();
					        sendActivationEmail(username, sex, email, uid, ts);
					        
							User user = getUser(uid);
							result.setResultObject(user);
							result.setCode(DB_STATUS_OK);
						} else {
							result.setCode(DB_STATUS_ERR_SQL);
						}
					} else {
						result.setCode(DB_STATUS_ERR_SQL);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}finally{
			closeResultSet(selectRs3);
			closeResultSet(selectRs2);
			closeResultSet(selectRs1);
			closePrepStmt(selectPrepStmt3);
			closePrepStmt(selectPrepStmt2);
			closePrepStmt(selectPrepStmt1);
			closePrepStmt(insertPrepStmt);
			closeConnection(con);
		}

		return result;
	}

	/**
	 * Allow only *@*.edu.cn for now
	 * @param email
	 * @return
	 */
	private static boolean allowEmail(String email) {		
		String patternStr = "@\\w+\\.edu\\.cn";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(email);
		return matcher.find();
	}

	static private String getActivationCode(String username, int sex, String email, int uid, long ts) {
		StringBuffer sb = new StringBuffer();
		sb.append(uid);
		sb.append(email);
		sb.append(username);
		sb.append(Gender.makeGender(sex).toString());
		sb.append(ts);
		return MD5Util.encode2hex(sb.toString());
	}
	
	static public int checkActivate(String email, String codeInput) {
		int rtval = DB_STATUS_ERR_GENERIC;
		Connection con = null;
		PreparedStatement prepStmt = null;
		PreparedStatement updatePrepStmt = null;
		ResultSet rs = null;
		
		try {
			con = getConnection();
			String selectStatement = "select ID, NAME, SEX, REGISTER_TIME from users where EMAIL=?";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, email);
			rs = prepStmt.executeQuery();

			if (!rs.next()) {
				//user name does not exist in DB
				rtval = DB_STATUS_ERR_USER_NOT_EXIST;
			} else {
				int uid = rs.getInt(1);
				String username = rs.getString(2);
				int sex = rs.getInt(3);
				java.sql.Timestamp timestamp = rs.getTimestamp(4);
				
				String activateCode = getActivationCode(username, sex, email, uid, timestamp.getTime());
				//System.out.println(activateCode);

				if(codeInput.equals(activateCode)) {
					// Update this ACTIVATED code in Database
					String updateStatement = "UPDATE `users` SET `ACTIVATED`=1 WHERE `ID`=?"; 
					//System.out.println("edit: UPDATE SQL String: " + updateStatement);
					updatePrepStmt = con.prepareStatement(updateStatement);
					updatePrepStmt.setInt(1, uid);
					if(updatePrepStmt.executeUpdate() != 0) {
						rtval = DB_STATUS_OK;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}finally{
			closeResultSet(rs);
			closePrepStmt(prepStmt);
			closePrepStmt(updatePrepStmt);
			closeConnection(con);
		}		
		
		return rtval;
	}
  
	static public Result login(String username, String password) {
		Connection con=null;
		PreparedStatement prepStmt=null;
		ResultSet rs=null;
		Result result = new Result();
		
		if(username == null || username.isEmpty() ||
				password == null || password.isEmpty()) {
			result.setCode(DB_STATUS_ERR_BAD_ARGS);
			return result;
		}

		String email = null;
		if(username != null && (username.indexOf('@') != -1)) {
			email = username;
		}

		try {
			con=getConnection();
			if(email == null) {  // login using username
				String selectStatement = "select ID, PASSWORD, ACTIVATED from users where NAME=? and TYPE=0";
				prepStmt = con.prepareStatement(selectStatement);
				prepStmt.setString(1, username);
				rs = prepStmt.executeQuery();

				if (!rs.next()) {
					//user name does not exist in DB
					result.setCode(DB_STATUS_ERR_USER_NOT_EXIST);
				} else {
					int uid = rs.getInt(1);
					String dbPassword = rs.getString(2);
					int activated = rs.getInt(3);

					if(dbPassword.equals(password)) {
						if(activated == 0) {
							result.setCode(DB_STATUS_ERR_USER_NOT_ACTIVATED);
						} else {
							result.setResultObject(getUser(uid));
							result.setCode(DB_STATUS_OK);
						}
					} else {
						result.setCode(DB_STATUS_ERR_PASSWORD);
					}
				}
			} else { // login using email
				String selectStatement = "select ID, PASSWORD, ACTIVATED from users where EMAIL=?";
				prepStmt = con.prepareStatement(selectStatement);
				prepStmt.setString(1, email);
				rs = prepStmt.executeQuery();

				if (!rs.next()) {
					//email does not exist in DB
					result.setCode(DB_STATUS_ERR_EMAIL_NOT_EXIST);
				} else {
					int uid = rs.getInt(1);
					String dbPassword = rs.getString(2);
					int activated = rs.getInt(3);

					if(dbPassword.equals(password)) {
						if(activated == 0) {
							result.setCode(DB_STATUS_ERR_USER_NOT_ACTIVATED);
						} else {
							result.setResultObject(getUser(uid));
							result.setCode(DB_STATUS_OK);
						}
					} else {
						result.setCode(DB_STATUS_ERR_PASSWORD);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}finally{
			closeResultSet(rs);
			closePrepStmt(prepStmt);
			closeConnection(con);
		}
		return result;
	}

	/* 
	 * Return value:
	 *	>0:	success, user id
	 *     0: other error
	 * 	-1:	user does not exist
	 */
	/*public int get_user_id(String username) throws Exception {
		Connection con=null;
		PreparedStatement prepStmt=null;
		ResultSet rs=null;
		int rtval= 0;

		try {
			con=getConnection();
			String selectStatement = "select ID from users where NAME=?";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, username);
			rs = prepStmt.executeQuery();

			if (!rs.next()) {
				//username does not exist in DB
				rtval = -1;
				//throw new Exception("Username does not exist");
			} else {
				rtval = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}finally{
			closeResultSet(rs);
			closePrepStmt(prepStmt);
			closeConnection(con);
		}
		return rtval;
	}*/


	/* 
	 * Return value:
	 *	-1: username does not exist
	 *	0:	sql error
	 *	1:	succeed
	 */
	static public int editProfile(int uid,
			String nickname,
			String birthday,	/* caller's repsonsibility to make sure it's format is '1990-01-01' */
			String school,
			String department,
			String enrollYear,
			String major) {
		Connection con=null;
		PreparedStatement prepStmt=null;
		ResultSet rs=null;
		int rtval = DB_STATUS_ERR_GENERIC;
		
		if(uid <= 0) {
			return DB_STATUS_ERR_BAD_ARGS;
		}
		
		if(checkUser(uid) == false) {
			return DB_STATUS_ERR_USER_ID;
		}

		if(birthday == null || birthday.isEmpty()) {
			birthday = "1990-01-01";
		}
		
		try {
			con=getConnection();
			String updateStatement = "UPDATE `users` SET `BIRTHDAY`=?,`SCHOOL`=?,`DEPARTMENT`=?," +
					"`MAJOR`=?,`NICKNAME`=?,`ENROLLYEAR`=? WHERE `ID`=?";
			//System.out.println("edit: UPDATE SQL String: " + updateStatement);
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setString(1, birthday);
			prepStmt.setString(2, school);
			prepStmt.setString(3, department);
			prepStmt.setString(4, major);
			prepStmt.setString(5, nickname);
			prepStmt.setString(6, enrollYear);
			prepStmt.setInt(7, uid);
			if(prepStmt.executeUpdate() != 0) {
				rtval = DB_STATUS_OK;
			} else {
				rtval = DB_STATUS_ERR_SQL;
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}finally{
			closeResultSet(rs);
			closePrepStmt(prepStmt);
			closeConnection(con);
		}
		return rtval;
	}

	/* Post a bottle into the DB
	 * Args: username, content
	 *
	 * Return value:
	 *	-1: username does not exist
	 *	0:	sql error
	 *	1:	succeed
	 */
	static public int postBottle(int uid, String content) {
		Connection con=null;
		PreparedStatement prepStmt=null;
		ResultSet rs=null;
		int rtval = DB_STATUS_ERR_GENERIC;
		
		if(uid <= 0 || content == null || content.isEmpty()) {
			return DB_STATUS_ERR_BAD_ARGS;
		}
		
		if(checkUser(uid) == false) {
			return DB_STATUS_ERR_USER_ID;
		}

		try {
			con=getConnection();
			String insertStatement = "insert into bottles (SENDER, CONTENT, SENDTIME) values (?,?, NOW())";
			prepStmt = con.prepareStatement(insertStatement);
			prepStmt.setInt(1, uid);
			prepStmt.setString(2, content);
			if(prepStmt.executeUpdate() != 0) {
				rtval = DB_STATUS_OK;
			} else {
				rtval = DB_STATUS_ERR_SQL;
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}finally{
			closeResultSet(rs);
			closePrepStmt(prepStmt);
			closeConnection(con);
		}
		return rtval;
	}
	
	/* Check user info from DB
	 *
	 * Return value:
	 *	null: error
	 */
	static public User getUser(int userId) {
//		Connection con=null;
//		PreparedStatement prepStmt=null;
//		ResultSet rs=null;
		User user = null;
//
//		if(userId <= 0) {
//			return null;
//		}
//
//		try {
//			con=getConnection();
//			String selectStatement = "select NAME, NICKNAME, SEX, SCHOOL, DEPARTMENT, MAJOR, EMAIL" 
//					+ ", BIRTHDAY, ENROLLYEAR, REGISTER_TIME from users where ID=?";
//			prepStmt = con.prepareStatement(selectStatement);
//			prepStmt.setInt(1, userId);
//			rs = prepStmt.executeQuery();
//
//			if (rs.next()) {
//				String name = rs.getString(1);
//				String nickname = rs.getString(2);
//				int sex = rs.getInt(3);
//				String school = rs.getString(4);
//				String department = rs.getString(5);
//				String major = rs.getString(6);
//				String email = rs.getString(7);
//				String birthday = rs.getString(8);
//				String enrollYear = rs.getString(9);
//				java.sql.Timestamp ts = rs.getTimestamp(10);
//
//				user = new User(userId, name, nickname, Gender.makeGender(sex), school, department, major, email, birthday, enrollYear, ts);
//			} else {
//				//username does not exist in DB
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}finally{
//			closeResultSet(rs);
//			closePrepStmt(prepStmt);
//			closeConnection(con);
//		}
		return user;
	}
 
	/*
	 * Get a foreign site user
	 */
	static public Result getForeignUser(int type, String f_uid) {
		Connection con=null;
		PreparedStatement prepStmt=null;
		ResultSet rs=null;
		User user = null;
		Result result = new Result();
		
		if(type < 0 || f_uid == null || f_uid.isEmpty()) {
			result.setCode(DB_STATUS_ERR_BAD_ARGS);
			return result;
		}

		try {
			con=getConnection();
			String selectStatement = "select ID, ACTIVATED, REGISTER_TIME from users where TYPE=? and F_UID=?";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, type);
			prepStmt.setString(2, f_uid);
			rs = prepStmt.executeQuery();

			if (!rs.next()) {
				//user name does not exist in DB
				result.setCode(DB_STATUS_ERR_USER_NOT_EXIST);
			} else {
				int uid = rs.getInt(1);
				int activated = rs.getInt(2);
				if(activated==0) {
					Timestamp ts = rs.getTimestamp(3);
					long registerTs = ts.getTime() / 1000;
					long now = System.currentTimeMillis() / 1000;
					/* Give the user some time to try out before activation is required 
					 */
					if((now - registerTs) > 24 * 3600) {
						System.err.println("now: " + now +", register: " + registerTs);
						System.err.println("uid " + uid + " not activated!");
						result.setCode(DB_STATUS_ERR_USER_NOT_ACTIVATED);
						return result;
					}
				}
				user = getUser(uid);
				if(user == null) {
					result.setCode(DB_STATUS_ERR_SQL);
				} else {
					result.setResultObject(user);
					result.setCode(DB_STATUS_OK);
				}
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}finally{
			closeResultSet(rs);
			closePrepStmt(prepStmt);
			closeConnection(con);
		}
		return result;
	}
	
	/*
	 * Update the access token into the database
	 */
//	static public int updateForeignAccessToken(int uid, String token) {
//		Connection con = null;
//		PreparedStatement prepStmt1 = null;
//		PreparedStatement prepStmt2 = null;
//		ResultSet rs1 = null;
//		ResultSet rs2 = null;
//		
//		int result = DB_STATUS_ERR_GENERIC;
//		
//		if(uid <= 0 || token == null || token.isEmpty()) {
//			result = DB_STATUS_ERR_BAD_ARGS;
//			return result;
//		}
//		
//		try {
//			con=getConnection();
//			/* TODO: may need to reconsider this scheme, and to save some session variable instead of
//			 * querying all these information every time
//			 */
//			String selectStatement = "SELECT TYPE, F_UID FROM users WHERE ID=?";
//			prepStmt1 = con.prepareStatement(selectStatement);
//			prepStmt1.setInt(1, uid);
//			rs1 = prepStmt1.executeQuery();
//			
//			if(rs1.next()) {
//				int type = rs1.getInt(1);
//				String fUid = rs1.getString(2);
//				String updateStatement = "REPLACE INTO `f_users` SET `TYPE`=?, `ID`=?, `AccessToken`=?";
//				prepStmt2 = con.prepareStatement(updateStatement);
//				prepStmt2.setInt(1, type);
//				prepStmt2.setString(2, fUid);
//				prepStmt2.setString(3, token);
//				if(prepStmt2.executeUpdate() != 0) {
//					result = DB_STATUS_OK;
//				} else {
//					result = DB_STATUS_ERR_SQL;
//				}
//			} else {
//				result = DB_STATUS_ERR_SQL;
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}finally{
//			closeResultSet(rs2);
//			closeResultSet(rs1);
//			closePrepStmt(prepStmt2);
//			closePrepStmt(prepStmt1);
//			closeConnection(con);
//		}
//		
//		return result;
//	}

	/*
	 * check to see if the uid User exists
	 */
	static public boolean checkUser(int uid) {
		Connection con=null;
		PreparedStatement prepStmt=null;
		ResultSet rs=null;
		boolean rtval = false;
		
		if(uid <= 0) {
			return rtval;
		}

		try {
			con=getConnection();
			String selectStatement = "select ID from users where ID=?";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, uid);
			rs = prepStmt.executeQuery();

			if (rs.next()) {
				rtval = true;
			} else {
				//user does not exist in DB
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			closeResultSet(rs);
			closePrepStmt(prepStmt);
			closeConnection(con);
		}
		return rtval;
	}
	
	/* set user photo filename info from DB
	 *
	 * Return value:
	 *	
	 */
	static public int setPhoto(int uid, String filename) {
		Connection con=null;
		PreparedStatement prepStmt=null;
		ResultSet rs=null;
		int rtval = DB_STATUS_ERR_GENERIC;

		if(uid <= 0 || filename == null || filename.isEmpty()) {
			return DB_STATUS_ERR_BAD_ARGS;
		}
		
		try {
			con=getConnection();
			// Update photo filename in Database
			String updateStatement = "UPDATE `users` SET `PHOTO`=? WHERE `ID`=?"; 
			System.out.println("set photo: UPDATE SQL String: " + updateStatement);
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setString(1, filename);
			prepStmt.setInt(2, uid);
			if(prepStmt.executeUpdate() != 0) {
				rtval = DB_STATUS_OK;
			} else {
				rtval = DB_STATUS_ERR_SQL;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			closeResultSet(rs);
			closePrepStmt(prepStmt);
			closeConnection(con);
		}
		return rtval;
	}
	
	/* get user photo url info from DB
	 *
	 * Return value:
	 *	
	 */
	static public String getPhotoUrl(int uid) {
//		Connection con=null;
//		PreparedStatement prepStmt=null;
//		ResultSet rs=null;
		String url = null;
		
//		if(uid <= 0) {
//			return null;
//		}
//
//		try {
//			con=getConnection();
//			// Update photo filename in Database
//			String selectStatement = "select SEX, REGISTER_TIME, PHOTO from users where ID=?";
//			prepStmt = con.prepareStatement(selectStatement);
//			prepStmt.setInt(1, uid);
//			rs = prepStmt.executeQuery();
//			
//			if(rs.next()) {
//				int sex = rs.getInt(1);
//				Timestamp ts = rs.getTimestamp(2);
//				String photoFilename = rs.getString(3);
//				
//				if(photoFilename == null || photoFilename.isEmpty()) {
//					url = "img/" + Gender.makeGender(sex).toString() + ".jpg";
//				} else {
//					url = "photo/" + MyServletUtil.timestampToDate(ts) + "/" + uid
//							+ "/" + photoFilename;
//				}
//			} 
//		} catch (Exception e) {
//			e.printStackTrace();
//		}finally{
//			closeResultSet(rs);
//			closePrepStmt(prepStmt);
//			closeConnection(con);
//		}
		return url;
	}
	
	/* Get a bottle from the DB
	 * 	
	 * TODO: This function should be synchronized
	 */
	static public Result getBottle(int uid) {
//		Connection con = null;
//		PreparedStatement prepStmt = null;
//		ResultSet rs = null;
//		PreparedStatement prepStmt2 = null;
//		ResultSet rs2 = null;
//		PreparedStatement updatePrepStmt = null;
//		int sex = 0;
		Result result = new Result();
//		Bottle bottle = null;
//		
//		if(uid <= 0) {
//			result.setCode(DB_STATUS_ERR_BAD_ARGS);
//			return result;
//		}
//
//		try {
//			con=getConnection();
//			String selectStatement = "select SEX from users where ID=?";
//			prepStmt = con.prepareStatement(selectStatement);
//			prepStmt.setInt(1, uid);
//			rs = prepStmt.executeQuery();
//
//			if (rs.next()) {
//				//id = rs.getInt(1);
//				sex = rs.getInt(1);
//				//sex = sex.equals("male") ? "female" : "male";
//				sex = sex ^ 1;
//				selectStatement = "select users.name, bottles.id, bottles.sender, bottles.content from bottles, users "
//						+ "where bottles.sender=users.id and users.sex=? and bottles.receiver=0 order by rand() limit 1";
//				prepStmt2 = con.prepareStatement(selectStatement);
//				prepStmt2.setInt(1, sex);
//				rs2 = prepStmt2.executeQuery();
//				if(rs2.next()) {
//					String senderName = rs2.getString(1);
//					int bottleId = rs2.getInt(2);
//					int senderId= rs2.getInt(3);
//					String content = rs2.getString(4);
//					String updateStatement = "UPDATE `bottles` SET `receiver`=? WHERE `ID`=?"; 
//					updatePrepStmt = con.prepareStatement(updateStatement);
//					updatePrepStmt.setInt(1, uid);
//					updatePrepStmt.setInt(2, bottleId);
//					if(updatePrepStmt.executeUpdate() != 0) {
//						bottle = new Bottle(bottleId, senderId, senderName, content);
//						result.setResultObject(bottle);
//						result.setCode(DB_STATUS_OK);
//					}
//				} else {
//					// No bottle left
//					result.setCode(DB_STATUS_ERR_NO_BOTTLE);
//				}
//			} else {
//				//username does not exist in DB
//				result.setCode(DB_STATUS_ERR_USER_ID);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}finally{
//			closeResultSet(rs2);
//			closeResultSet(rs);
//			closePrepStmt(prepStmt2);
//			closePrepStmt(prepStmt);
//			closePrepStmt(updatePrepStmt);
//			closeConnection(con);
//		}
		return result;
	}

	// return value: sender ID
	static public int replyBottle(int uid, int bid)
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		PreparedStatement insertPrepStmt = null;
		
		int rtval = DB_STATUS_ERR_GENERIC;
		
		if(uid <= 0 || bid <= 0) {
			return DB_STATUS_ERR_BAD_ARGS;
		}

		try {
			con = getConnection();
			String selectStatement = "select sender, sendtime, content from bottles where ID=?";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, bid);
			rs = prepStmt.executeQuery();

			if (rs.next()) {
				int senderId = rs.getInt(1);
				java.sql.Timestamp ts = rs.getTimestamp(2);
				String content = rs.getString(3);
			
				String insertStatement = "insert into messages (senderID, receiverID, sendTime, readFlag, content)" 
						+ " values (?,?,?,?,?)";
				//System.out.println(insertStatement);
				insertPrepStmt = con.prepareStatement(insertStatement);
				insertPrepStmt.setInt(1, senderId);
				insertPrepStmt.setInt(2, uid);
				insertPrepStmt.setTimestamp(3, ts);
				insertPrepStmt.setInt(4, 1);
				insertPrepStmt.setString(5, content);
				System.out.println(senderId + " " + uid + " " + ts + " " + content);
				if(insertPrepStmt.executeUpdate() != 0) {
					rtval = senderId;
				} else {
					rtval = DB_STATUS_ERR_SQL;
				}
			} else {
				//bottle does not exist in DB
				rtval = DB_STATUS_ERR_BOTTLE_ID;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			closeResultSet(rs);
			closePrepStmt(prepStmt);
			closePrepStmt(insertPrepStmt);
			closeConnection(con);
		}
		return rtval;
	}
	
	static public int setBottleUnread(int bid)
	{
		Connection con=null;
		PreparedStatement prepStmt=null;
		ResultSet rs=null;
		int rtval = DB_STATUS_ERR_GENERIC;
		
		if(bid <= 0) {
			return DB_STATUS_ERR_BAD_ARGS;
		}

		try {
			con = getConnection();
			String updateStatement = "UPDATE `bottles` SET `receiver`=0 WHERE `ID`=?"; 
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, bid);
			if(prepStmt.executeUpdate() != 0) {
				rtval = DB_STATUS_OK;
			} else {
				rtval = DB_STATUS_ERR_BOTTLE_ID;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			closeResultSet(rs);
			closePrepStmt(prepStmt);
			closeConnection(con);
		}
		return rtval;
	}
	
	/*
	 * getMyBottels
	 * 
	 * Get a list of friends that the specific user had conversation to,
	 * Also get the latest message with that friend
	 * 
	 */
	static public Result getMyBottles(int uid, int start, int length)
	{
//		Connection con = null;
//		PreparedStatement prepStmt = null;
//		ResultSet rs = null;
//		PreparedStatement updatePrepStmt = null;
//		//Set<User> friends = new TreeSet<User>();
//		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		Result result = new Result();
//		
//		if(uid <= 0) {
//			result.setCode(DB_STATUS_ERR_BAD_ARGS);
//			return result;
//		}
//		
//		if(checkUser(uid) == false) {
//			result.setCode(DB_STATUS_ERR_USER_ID);
//			return result;
//		}
//
//		try {
//			con = getConnection();
//			/*String selectStatement = "select ID, senderID, sendTime, content from messages where ID in " + 
//					"(select max(ID) from messages where receiverID=" + uid + " group by senderID)"
//					+ " order by sendTime DESC limit " + start + "," + length;*/
//			/*
//			 * select ID, senderID, receiverID, sendTime, content from messages where ID in (select max(ID) from (select * from (select ID, senderID as friend, sendTime from messages where receiverID=15) as a UNION (select ID, receiverID as friend, sendTime from messages where senderID=15)) as b group by friend) order by sendTime DESC limit 0, 30;
//			 */
//			String selectStatement = "select ID, senderID, receiverID, sendTime, content from messages " + 
//				"where ID in (select max(ID) from (select * from (select ID, senderID as friend, sendTime " + 
//				"from messages where receiverID=?) as a UNION (select ID, receiverID as friend, sendTime " + 
//				"from messages where senderID=?)) as b group by friend) order by sendTime DESC limit ?, ?";
//			prepStmt = con.prepareStatement(selectStatement);
//			prepStmt.setInt(1, uid);
//			prepStmt.setInt(2, uid);
//			prepStmt.setInt(3, start);
//			prepStmt.setInt(4, length);
//			rs = prepStmt.executeQuery();
//
//			while (rs.next()) {
//				int messageId = rs.getInt(1);
//				int senderId = rs.getInt(2);
//				int receiverId = rs.getInt(3);
//				Timestamp ts = rs.getTimestamp(4);
//				String content = rs.getString(5);
//				
//				User senderUser = getUser(senderId);
//				User receiverUser = getUser(receiverId);
//				
//				ChatMessage message = new ChatMessage(messageId, senderId, senderUser.getUsername(),
//						receiverId, receiverUser.getUsername(), ts, content);
//				messages.add(message);
//			}
//			/*
//			 * Do not set the readFlag until the message is read from chat.jsp or get_unread
//			String updateStatement = "UPDATE `messages` SET `readFlag`=1 WHERE `receiverID`=?"; 
//			updatePrepStmt = con.prepareStatement(updateStatement);
//			updatePrepStmt.setInt(1, uid);
//			if(updatePrepStmt.executeUpdate() != 0) {
//				// anything to do?
//			}
//			*/
//			result.setResultObject(messages);
//			result.setCode(DB_STATUS_OK);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}finally{
//			closeResultSet(rs);
//			closePrepStmt(prepStmt);
//			closePrepStmt(updatePrepStmt);
//			closeConnection(con);
//		}
		return result;
	}
	
	/*
	 * getConversation
	 * 
	 * Get the messages with the specific friend, and set readFlag to 1
	 */
	static public Result getConversation(int uid, int friendId)
	{
//		Connection con = null;
//		PreparedStatement prepStmt = null;
//		PreparedStatement updatePrepStmt = null;
//		ResultSet rs = null;
//		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		Result result = new Result();
//		
//		if(uid <= 0 || friendId <= 0) {
//			result.setCode(DB_STATUS_ERR_BAD_ARGS);
//			return result;
//		}
//
//		try {
//			con=getConnection();
//			String selectStatement = "select ID, senderID, receiverID, sendTime, content, readFlag " + 
//					" from messages where (receiverID=? and senderID=?) or (receiverID=? and " + 
//					"senderID=?) order by sendTime ASC, ID ASC";// limit 0, 30";
//			prepStmt = con.prepareStatement(selectStatement);
//			prepStmt.setInt(1, uid);
//			prepStmt.setInt(2, friendId);
//			prepStmt.setInt(3, friendId);
//			prepStmt.setInt(4, uid);
//			rs = prepStmt.executeQuery();
//
//			while (rs.next()) {
//				int messageId = rs.getInt(1);
//				int senderId = rs.getInt(2);
//				int receiverId = rs.getInt(3);
//				int readFlag = rs.getInt(6);
//				Timestamp ts = rs.getTimestamp(4);
//				String content = rs.getString(5);
//				
//				User senderUser = getUser(senderId);
//				User receiverUser = getUser(receiverId);
//				
//				ChatMessage message = new ChatMessage(messageId, senderId, senderUser.getUsername(),
//						receiverId, receiverUser.getUsername(), ts, content);
//				messages.add(message);
//				// set readFlag to 1
//				if(readFlag == 0) {
//					String updateStatement = "UPDATE `messages` SET `readFlag`=1 WHERE `receiverID`=?"; 
//					updatePrepStmt = con.prepareStatement(updateStatement);
//					updatePrepStmt.setInt(1, uid);
//					if(updatePrepStmt.executeUpdate() != 0) {
//						// anything to do?
//					}
//				}
//			}
//			result.setResultObject(messages);
//			result.setCode(DB_STATUS_OK);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}finally{
//			closeResultSet(rs);
//			closePrepStmt(prepStmt);
//			closeConnection(con);
//		}
		return result;
	}
	
	/*
	 * getNewMessagesFromFriend
	 * 
	 * Get new unread messages from the specific friend, and set readFlag to 1
	 */
	static public Result getNewMessagesFromFriend(int uid, int friendId)
	{
//		Connection con=null;
//		PreparedStatement prepStmt=null;
//		ResultSet rs=null;
//		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		Result result = new Result();
//		
//		if(uid <= 0 || friendId <= 0) {
//			result.setCode(DB_STATUS_ERR_BAD_ARGS);
//			return result;
//		}
//
//		try {
//			con=getConnection();
//			String selectStatement = "select ID, senderID, receiverID, sendTime, content, readFlag " + 
//					" from messages where receiverID=? and senderID=? and readFlag=0" + 
//					" order by sendTime ASC, ID ASC";
//			prepStmt = con.prepareStatement(selectStatement);
//			prepStmt.setInt(1, uid);
//			prepStmt.setInt(2, friendId);
//			rs = prepStmt.executeQuery();
//
//			while (rs.next()) {
//				int messageId = rs.getInt(1);
//				int senderId = rs.getInt(2);
//				int receiverId = rs.getInt(3);
//				Timestamp ts = rs.getTimestamp(4);
//				String content = rs.getString(5);
//				
//				User senderUser = getUser(senderId);
//				User receiverUser = getUser(receiverId);
//				
//				String updateStatement = "UPDATE `messages` SET `readFlag`=1 WHERE `ID`=?"; 
//				prepStmt = con.prepareStatement(updateStatement);
//				prepStmt.setInt(1, messageId);
//				if(prepStmt.executeUpdate() != 0) {
//					ChatMessage message = new ChatMessage(messageId, senderId, senderUser.getUsername(),
//							receiverId, receiverUser.getUsername(), ts, content);
//					messages.add(message);
//				}				
//			}
//			result.setResultObject(messages);
//			result.setCode(DB_STATUS_OK);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}finally{
//			closeResultSet(rs);
//			closePrepStmt(prepStmt);
//			closeConnection(con);
//		}
		return result;
	} 
	
	static public int getNewMessageCount(int uid)
	{
		Connection con=null;
		PreparedStatement prepStmt=null;
		ResultSet rs=null;
		int result = 0;
		
		if(uid <= 0) {
			return result;
		}

		try {
			con=getConnection();
			String selectStatement = "select COUNT(ID) from messages where receiverID=? and readFlag=0";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, uid);
			rs = prepStmt.executeQuery();

			while (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			closeResultSet(rs);
			closePrepStmt(prepStmt);
			closeConnection(con);
		}
		return result;
	}
	
	/*
	 *  getNotification
	 *  
	 *  get unread messages, containing the a list of following information:
	 *  	friendId: the user id from which the unread message belongs to
	 *  	ts: the newest timestamp
	 *  	content: the last message body
	 *  	unreadCount: number of unread messages from him/her
	 *  	
	 */
	static public Result getNotification(int uid)
	{
		Connection con = null;
		PreparedStatement prepStmt1 = null;
		PreparedStatement prepStmt2 = null;
		PreparedStatement prepStmt3 = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		Result result = new Result();
		List<NotificationMessage> notifications = new ArrayList<>();
		
		if(uid <= 0) {
			result.setCode(DB_STATUS_ERR_BAD_ARGS);
			return result;
		}

		try {
			con=getConnection();
			String selectStatement1 = "SELECT DISTINCT senderID FROM messages WHERE receiverID=? AND readFlag=0";
			prepStmt1 = con.prepareStatement(selectStatement1);
			prepStmt1.setInt(1, uid);
			rs1 = prepStmt1.executeQuery();

			String selectStatement2 = "SELECT COUNT(*) FROM messages WHERE senderID=? AND receiverID=? AND readFlag=0";
			prepStmt2 = con.prepareStatement(selectStatement2);
			
			String selectStatement3 = "SELECT sendTime, content FROM messages WHERE " +
					"senderID=? AND receiverID=? AND readFlag=0 ORDER BY sendTime DESC LIMIT 0,1";
			prepStmt3 = con.prepareStatement(selectStatement3);
			
			while (rs1.next()) {
				int senderID = rs1.getInt(1);
				NotificationMessage message = new NotificationMessage();
				message.setSenderId(senderID);
				User sender = getUser(senderID);
				message.setSenderName(sender.getUsername());
				
				prepStmt2.setInt(1, senderID);
				prepStmt2.setInt(2, uid);
				rs2 = prepStmt2.executeQuery();
				int unreadCount = 0;
				if(rs2.next()) {
					unreadCount = rs2.getInt(1);
					message.setUnreadCount(unreadCount);
				}
				
				prepStmt3.setInt(1, senderID);
				prepStmt3.setInt(2, uid);
				rs3 = prepStmt3.executeQuery();
				if(rs3.next()) {
					Timestamp ts = rs3.getTimestamp(1);
					String content = rs3.getString(2);
					message.setTs(ts);
					message.setContent(content);					
				}
				notifications.add(message);
			}
			result.setResultObject(notifications);
			result.setCode(DB_STATUS_OK);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			closeResultSet(rs3);
			closeResultSet(rs2);
			closeResultSet(rs1);
			closePrepStmt(prepStmt3);
			closePrepStmt(prepStmt2);
			closePrepStmt(prepStmt1);
			closeConnection(con);
		}
		return result;
	}
	
	/*
	 * sendMessage
	 * 
	 * Send a message to friend
	 */
	static public int sendMessage(int uid, int friendId, String content)
	{
		Connection con=null;
		PreparedStatement prepStmt=null;
		ResultSet rs=null;
		//List<ChatMessage> messages = null;
		int result = DB_STATUS_ERR_GENERIC;
		
		if(uid <= 0 || friendId <=0 || 
				content == null || content.isEmpty()) {
			return DB_STATUS_ERR_BAD_ARGS;
		}
				
		try {
			con=getConnection();
			String insertStatement = "insert into messages (senderID, receiverID, sendTime, readFlag, content)" 
					+ " values (?,?,NOW(),0,?)";
			//System.out.println(insertStatement);
			prepStmt = con.prepareStatement(insertStatement);
			prepStmt.setInt(1, uid);
			prepStmt.setInt(2, friendId);
			prepStmt.setString(3, content);
			if(prepStmt.executeUpdate() != 0) {
				//messages.add(new ChatMessage(0, uid, friendId, new Timestamp(System.currentTimeMillis()), content));
				result = DB_STATUS_OK;
			} else {
				result = DB_STATUS_ERR_SQL;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			closeResultSet(rs);
			closePrepStmt(prepStmt);
			closeConnection(con);
		}
		return result;
	}
	
/*	public static void main(String[] args) {
		System.out.println("");
	}*/

}


