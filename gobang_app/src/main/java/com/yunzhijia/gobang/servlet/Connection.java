package com.yunzhijia.gobang.servlet;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.alibaba.fastjson.JSONObject;
import com.yunzhijia.gobang.model.Message;
import com.yunzhijia.gobang.utils.ThreadPoolUtils;

/**
 * Servlet implementation class MessageServlet
 */
@ServerEndpoint(value = "/ws/{uid}")
public class Connection {
   
	 /**
     * 连接对象集合
     */
    private static final Map<String,Session> connections = new ConcurrentHashMap<String,Session>();
	/**
	 * 打开连接
	 * 
	 * @param session
	 * @param nickName
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam(value = "uid") String uid) {
		connections.put(uid, session);
	}

	/**
	 * 关闭连接
	 */
	@OnClose
	public void onClose() {

	}

	/**
	 * 接收信息
	 * 
	 * @param message
	 * @param nickName
	 */
	@OnMessage
	public void onMessage(String message, @PathParam(value = "uid") String uid) {
		System.err.println(message);
	}

	/**
	 * 错误信息响应
	 * 
	 * @param throwable
	 */
	@OnError
	public void onError(Throwable throwable) {
		System.out.println(throwable.getMessage());
	}
	public static void send(final Message message){
		ThreadPoolUtils.execute(new Runnable() {
			@Override
			public void run() {
				Session session = connections.get(message.getTarget());
				try {
					session.getBasicRemote().sendText(JSONObject.toJSONString(message));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
