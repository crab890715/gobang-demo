package com.yunzhijia.gobang.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.yunzhijia.gobang.model.Message;

/**
 * Servlet implementation class MainServlet
 */
@WebServlet(name = "send", urlPatterns = { "/send" })
public class SendServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3449956867928997711L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String message =inputStream2String(request.getInputStream());
		System.out.println(inputStream2String(request.getInputStream()));
		Connection.send(JSONObject.parseObject(message, Message.class));
	}

	private String inputStream2String(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = -1;
		while ((i = is.read()) != -1) {
			baos.write(i);
		}
		return baos.toString();
	}
}
