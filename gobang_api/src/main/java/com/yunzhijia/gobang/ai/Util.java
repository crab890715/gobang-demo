package com.yunzhijia.gobang.ai;
import java.awt.Point;
import java.io.File;
import java.util.List;
import java.util.Random;

import javax.swing.JFileChooser;


public class Util {
	private static Random r=new Random();
	
	public static Point random(List<Point> points) {
		if (points.size() == 1) {
			return points.get(0);
		} else {
			return points.get(r.nextInt(points.size()));
		}
	}
	
	private static final char[] CA_SIM={'┼','●','○','★','◎'};
	private static final char[] NUM_SIM="0.⒈⒉⒊⒋⒌⒍⒎⒏⒐⒑⒒⒓⒔⒕⒖⒗⒘⒙⒚⒛".toCharArray();
	
	public static void print(int[][] obj,Point p1,Point p2) {
		if(obj.length==0)return;
		int y=obj.length;
		int x=obj[0].length;
		if(p1!=null){
			obj[p1.x][p1.y]+=2;
		}
		if(p2!=null){
			obj[p2.x][p2.y]+=2;
		}
		StringBuilder sb=new StringBuilder();
		System.out.print("    ");
		for(int i=0;i<=y;i++){
			if(i>21){
				System.out.print(i-1);
			}else{
				System.out.print(NUM_SIM[i]);
			}
		}
		System.out.println();
		for(int i=0;i<x;i++){
			sb.append(i<10?"0"+i:i).append("  ");
			for(int j=0;j<y;j++){
				sb.append(CA_SIM[obj[j][i]]);
			}
			System.out.println(sb);
			sb.setLength(0);
		}
		if(p1!=null){
			obj[p1.x][p1.y]-=2;
		}
		if(p2!=null){
			obj[p2.x][p2.y]-=2;
		}
	}
	
	public static File fileOpenDialog(String msg, int selectionMode,String defaultValue) {
		JFileChooser chooser = new JFileChooser();
		// 设置文件选择模式,只要文件
		if(defaultValue!=null && defaultValue.length()>0)
			chooser.setSelectedFile(new File(defaultValue));
		chooser.setFileSelectionMode(selectionMode);
		chooser.showOpenDialog(null);
		File file = chooser.getSelectedFile();
		return file;
	}

	public static File fileSaveDialog(String msg, int selectionMode,String defaultValue) {
		JFileChooser chooser = new JFileChooser();
		// 设置文件选择模式,只要目录
		if(defaultValue!=null && defaultValue.length()>0)
			chooser.setSelectedFile(new File(defaultValue));
		chooser.setFileSelectionMode(selectionMode);
		chooser.showSaveDialog(null);
		File file = chooser.getSelectedFile();
		return file;
	}
}