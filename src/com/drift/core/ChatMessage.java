package com.drift.core;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class ChatMessage implements Serializable, JSONAware {	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int messageId;
	private int senderId;
	private String senderName;
	private int receiverId;
	private String receiverName;
	private Timestamp ts;
	private String content;	
	
	public ChatMessage(int messageId, int senderId, String senderName, int receiverId, String receiverName,
			Timestamp ts, String content) {
		super();
		this.messageId = messageId;
		this.senderId = senderId;
		this.senderName = senderName;
		this.receiverId = receiverId;
		this.receiverName = receiverName;
		this.ts = ts;
		this.content = content;
	}
	
	ChatMessage() {		
	}

	public int getMessageId() {
		return messageId;
	}

	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}

	public int getSenderId() {
		return senderId;
	}

	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}
	
	public String getSenderName() {
		return senderName;
	}
	
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public int getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(int receiverId) {
		this.receiverId = receiverId;
	}
	
	public String getReceiverName() {
		return receiverName;
	}
	
	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}

	public Timestamp getTs() {
		return ts;
	}

	public void setTs(Timestamp ts) {
		this.ts = ts;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toJSONString() {
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//JSONObject obj = new JSONObject();
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("senderId", senderId);
		map.put("senderName", senderName);
		map.put("receiverId", receiverId);
		map.put("receiverName", receiverName);
		map.put("content", content);
		map.put("ts", sdf.format(ts));
		
		JSONObject obj = new JSONObject(map);
		
		//obj.put("senderId", senderId);
		//obj.put("receiverId", receiverId);
		//obj.put("content", content);
		//obj.put("ts", sdf.format(ts));
		return obj.toJSONString();
	}


	


}
