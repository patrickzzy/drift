package com.drift.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.drift.bean.ChatMessage;
import com.drift.bean.User;
import com.drift.service.MessageService;
import com.drift.service.UserService;
import com.drift.service.impl.Result;
import com.drift.service.impl.ServiceFactory;
import com.drift.util.JSONUtil;

/**
 * Servlet implementation class ApiMyBottles
 */
@WebServlet(ApiUtil.API_ROOT + "/my_bottles")
public class ApiMyBottles extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private UserService uService = ServiceFactory.createUserService();
	private MessageService mService = ServiceFactory.createMessageService();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ApiMyBottles() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ApiUtil.doCommonTasks(request, response);
		int status = ApiUtil.API_ERR_OTHER;
		Map<String, Object> map = new HashMap<String, Object>();
		
		String uidStr = request.getParameter("uid");
		if(uidStr == null) {
			status = ApiUtil.API_ERR_BAD_ARGS;
		} else {
			int uid = 0;

			try {
				uid = Integer.parseInt(uidStr);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Result resultObj = uService.getUserById(uid);
			int result = resultObj.getCode();
			User user = (User) resultObj.getResultObject();
			if(result == Result.SUCCESS && user != null) {
				//TODO: don't use fixed number
				resultObj = mService.getMyBottles(user, 0, 30);
			}
			status = ApiUtil.mapCode(resultObj.getCode());
			if(status == ApiUtil.API_ACTION_OK) {
				@SuppressWarnings("unchecked")
				List<ChatMessage> messages = (List<ChatMessage>) resultObj.getResultObject();
				if(messages.isEmpty()) {
					status = ApiUtil.API_ERR_NO_BOTTLE;
				} else {
					map.put("bottles", messages);
				}
			}
		}
		String msg = ApiUtil.API_CODE_STRINGS.get(status);
		map.put("code", status);
		map.put("result", msg);
		
		PrintWriter out = response.getWriter();
		out.print(JSONUtil.toJSONString(map));
		out.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
