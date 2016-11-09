package com.yunzhijia.gobang.servlet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Game {
	/**
	 * 连接对象集合
	 */
	private static final Map<String, String> games = new ConcurrentHashMap<String, String>();

	public static boolean put(String source, String target) {
		if (games.containsKey(target) && games.get(target) != null&&!games.get(target).equals(source)) {
			return false;
		}
		games.put(source, target);
		games.put(target, source);
		return true;
	}
}
