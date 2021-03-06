package com.drift.bean;

//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;


public class User implements Comparable<User>, JSONAware {

	public enum SiteType {LOCAL, SINA, RENREN, QQ, BAIDU, LAST /* Keep this the last one */ }	

	private int uid = 0;
	private String username = null;
	private String nickname = null;
	private Gender sex = null;
	private String birthday = null;
	private String school = null;
	private int enrollYear;
	private String department = null;
	private String major = null;
	private String email = null;
	private long register_ts = 0;	/* TODO: may cause security issue */
	private String photoUrl = null;
	private SiteType siteType;
	private String foreignUid;
	
	
	@Override
	public String toString() {
		return uid + ":" + username;
	}
	
	@Override
	public String toJSONString() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("username", username);
		map.put("nickname", nickname);
		map.put("sex", sex);
		map.put("school", school);
		map.put("department", department);
		map.put("major", major);
		map.put("email", email);
		map.put("birthday", birthday);
		map.put("enrollYear", enrollYear);
		
		JSONObject obj = new JSONObject(map);
		return obj.toJSONString();
	}	

	public User() {
	}
	
	/**
	 * Used to create a user, and save to DB
	 * @throws Exception, when arguments are illegal
	 * 
	 * @param birthday: caller's responsibility to make sure it's format is '1990-01-01'
	 */
	public User(String username, String nickname, int sex, String school, 
			String department, String major, String email, String birthday,
			int enrollYear) throws Exception {
		if(username == null || username.isEmpty() ||
				email == null || email.isEmpty() ||
				(sex != 0 && sex != 1)) {
			throw new Exception("Bad Arguments");
		}		
		this.username = username;
		this.nickname = nickname;
		this.sex = Gender.makeGender(sex);
		this.school = school;
		this.department = department;
		this.major = major;
		this.email = email;
		this.birthday = birthday;
		this.enrollYear = enrollYear;
	}
	
	/**
	 * Used to query for a user from DB
	 */
	public User(int uid, String username, String nickname, int sex, String school, 
			String department, String major, String email, String birthday,
			int enrollYear,	long ts, String photoUrl)
	{
		this.uid = uid;
		this.username = username;
		this.nickname = nickname;
		this.sex = Gender.makeGender(sex);
		this.school = school;
		this.department = department;
		this.major = major;
		this.email = email;
		this.birthday = birthday;
		this.enrollYear = enrollYear;
		this.register_ts = ts;
		this.photoUrl = photoUrl;
	}
	
	public int compareTo(User another) {
		return username.compareTo(another.getUsername());
	}
	
	public boolean equals(Object another) {
		if(this == another)
			return true;
		if(another instanceof User) {
			User anotherUser = (User)another;
			return this.username.equals(anotherUser.getUsername());
		} else {
			return false;
		}
	}
	
	public SiteType getSiteType() {
		return siteType;
	}

	public String getForeignUid() {
		return foreignUid;
	}

	public long getRegisterTs() {
		return register_ts;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public void setSchool(String school) {
		this.school = school;
	}
	
	public void setDepartment(String department) {
		this.department = department;
	}
	
	public void setMajor(String major) {
		this.major = major;
	}
	
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	
	public void setEnrollYear(int enrollYear) {
		this.enrollYear = enrollYear;
	}
	
	public int getUid() {
		return uid;
	}
	
	public String getUsername() {
		return username;
	}

	public String getNickname() {
		return nickname==null ? "" : nickname;
	}
	
	public Gender getSex() {
		return sex;
	}
	
	public String getBirthday() {
		return birthday;
	}
	
	public String getSchool() {
		return school==null ? "" : school;
	}
	
	public String getDepartment() {
		return department==null ? "" : department;
	}
	
	public String getMajor() {
		return major==null ? "" : major;
	}
	
	public String getEmail() {
		return email;
	}

	public int getEnrollYear() {
		return enrollYear;
	}
}

