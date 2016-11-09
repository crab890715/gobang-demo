package com.yunzhijia.gobang.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yunzhijia.gobang.ai.Chess;

public class AiUtils {
	private static Map<String,Chess> cached = new ConcurrentHashMap<String,Chess>();
	public static Chess get(String key){
		return cached.get(key);
	}
	public static void put(String key,Chess chess){
		cached.put(key,chess);
	}
}
