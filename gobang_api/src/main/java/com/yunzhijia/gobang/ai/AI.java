package com.yunzhijia.gobang.ai;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;


public interface AI {
	Point compute();
	 final class Default implements AI {
			private Chess chess;
			private int[][] oppGrades;
			private int[][] myGrades;

			public Default(Chess chess) {
				this.chess = chess;
				oppGrades = new int[chess.width][chess.height]; // 对手预计得分
				myGrades = new int[chess.width][chess.height]; // AI预计得分
			}

			@Override
			public Point compute() {
				Player myId = chess.next;
				Player opp = myId.getOpp();
				int[][] chessTable=chess.getTable();
				for (int i = 0; i < chess.width; i++)
					for (int j = 0; j < chess.height; j++) {
						this.oppGrades[i][j] = 0;
						this.myGrades[i][j] = 0;
						if (chessTable[i][j]!= 0)
							continue;

						for (int k : chess.getPointToPattern(i, j))
							switch (chess.getPatternScore(opp, k)) {
							case 1: // 一连子
								this.oppGrades[i][j] += 5;
								break;
							case 2: // 两连子
								this.oppGrades[i][j] += 50;
								break;
							case 3: // 三连子
								this.oppGrades[i][j] += 180;
								break;
							case 4: // 四连子
								this.oppGrades[i][j] += 400;
								break;
							}

						for (int k : chess.getPointToPattern(i, j))
							switch (chess.getPatternScore(myId, k)) {
							case 1: // 一连子
								this.myGrades[i][j] += 5;
								break;
							case 2: // 两连子
								this.myGrades[i][j] += 52;
								break;
							case 3: // 三连子
								this.myGrades[i][j] += 100;
								break;
							case 4: // 四连子
								this.myGrades[i][j] += 400;
								break;
							}
					}
				int maxPlayerGrades = -1, maxComputerGrades = -1;
				List<Point> maxAttPoints = new ArrayList<Point>();
				List<Point> maxDefPoints = new ArrayList<Point>();
				// 寻找最大分数即最优策略
				for (int i = 0; i < chess.width; i++)
					for (int j = 0; j < chess.height; j++) {
						if (chessTable[i][j]== 0){
							if (myGrades[i][j] > maxComputerGrades) {
								maxAttPoints.clear();
								maxAttPoints.add(new Point(i, j));
								maxComputerGrades = myGrades[i][j];
							} else if (myGrades[i][j] == maxComputerGrades) {
								maxAttPoints.add(new Point(i, j));
							}

							if (oppGrades[i][j] > maxPlayerGrades) {
								maxPlayerGrades = oppGrades[i][j];
								maxDefPoints.clear();
								maxDefPoints.add(new Point(i, j));
							} else if (oppGrades[i][j] == maxPlayerGrades) {
								maxDefPoints.add(new Point(i, j));
							}
						}

					}
				if (maxComputerGrades > maxPlayerGrades) {
					return Util.random(maxAttPoints);
				} else {
					return Util.random(maxDefPoints);
				}
			}
		}
	class Active implements AI {
		private Chess chess;
		private int[][] oppGrades;
		private int[][] myGrades;

		public Active(Chess chess) {
			this.chess = chess;
			oppGrades = new int[chess.width][chess.height]; // 对手预计得分
			myGrades = new int[chess.width][chess.height]; // AI预计得分
		}

		@Override
		public Point compute() {
			Player myId = chess.next;
			Player opp = myId.getOpp();
			int[][] chessTable=chess.getTable();
			for (int i = 0; i < chess.width; i++)
				for (int j = 0; j < chess.height; j++) {
					this.oppGrades[i][j] = 0;
					this.myGrades[i][j] = 0;
					if (chessTable[i][j]!= 0)
						continue;

					for (int k : chess.getPointToPattern(i, j)) {
						switch (chess.getPatternScore(opp, k)) {
						case 1: // 一连子
							this.oppGrades[i][j] += 4;
							break;
						case 2: // 两连子
							this.oppGrades[i][j] += 25;
							break;
						case 3: // 三连子
							this.oppGrades[i][j] += 90;
							break;
						case 4: // 四连子
							this.oppGrades[i][j] += 400;
							break;
						}
					}

					for (int k : chess.getPointToPattern(i, j)){
						switch (chess.getPatternScore(myId, k)) {
						case 1: // 一连子
							this.myGrades[i][j] += 5;
							break;
						case 2: // 两连子
							this.myGrades[i][j] += 15;
							break;
						case 3: // 三连子
							this.myGrades[i][j] += 180;
							break;
						case 4: // 四连子
							this.myGrades[i][j] += 800;
							break;
						}
					}
				}

			// 分值相同时，使用随机算法
			int maxGrades = -1;
			List<Point> maxPoints = new ArrayList<Point>();
			for (int i = 0; i < chess.width; i++) {
				for (int j = 0; j < chess.height; j++) {
					if (chessTable[i][j]== 0){
						int grade = myGrades[i][j] + oppGrades[i][j];
						if (grade > maxGrades) {
							maxPoints.clear();
							maxPoints.add(new Point(i, j));
							maxGrades = grade;
						} else if (grade == maxGrades) {
							maxPoints.add(new Point(i, j));
						}
					}

				}
			}
			return Util.random(maxPoints);
		}
	}
}