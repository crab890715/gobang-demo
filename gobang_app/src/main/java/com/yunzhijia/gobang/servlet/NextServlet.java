package com.yunzhijia.gobang.servlet;

import java.awt.Point;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.yunzhijia.gobang.ai.Chess;
import com.yunzhijia.gobang.utils.AiUtils;

@WebServlet(name = "next", urlPatterns = { "/next" })
public class NextServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3449956867928997711L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String sid = request.getSession().getId();
		System.err.println(sid);
		int x = Integer.valueOf(request.getParameter("x"));
		int y = Integer.valueOf(request.getParameter("y"));
		Chess chess = AiUtils.get(sid);
		System.err.println(String.format("x:%s,y:%s", x,y));
		chess.doMove(new Point(x, y));
		response.setContentType("application/json;charset=UTF-8");
		if(chess.getNext()!=null){
			Point com = chess.computerMove();
			response.getWriter().append(JSONObject.toJSONString(com));
		}else{
			response.getWriter().append(JSONObject.toJSONString(new Point(-1,-1)));
		}
		
	}

}
