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

@WebServlet(name = "init", urlPatterns = { "/init" })
public class InitServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3449956867928997711L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String sid = request.getSession().getId();
		System.err.println(sid);
		int w = Integer.valueOf(request.getParameter("w"));
		int h = Integer.valueOf(request.getParameter("h"));
		int x = Integer.valueOf(request.getParameter("x"));
		int y = Integer.valueOf(request.getParameter("y"));
		Chess chess = new Chess(w, h);
		chess.initGame(true, false);
		chess.doMove(new Point(x, y));
		Point com = chess.computerMove();
		AiUtils.put(sid, chess);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().append(JSONObject.toJSONString(com));
	}

}
