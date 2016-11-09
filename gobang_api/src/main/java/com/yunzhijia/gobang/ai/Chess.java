package com.yunzhijia.gobang.ai;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * 五子棋处理引擎
 */
public final class Chess implements Serializable {
	private static final long serialVersionUID = 73329L;
	private static final int IMPOSSIBLE = 7;

	/**
	 * 记录所有的胜利棋形(棋盘确定后，胜利棋形固定不变)
	 */
	private transient Pattern[] PATTERN_POINTS;
	/**
	 * 记录每个点对应到的胜利棋形。(棋盘确定后，关联关系固定不变) [n=1]X坐标 [n=2]Y坐标，该点关联的胜利棋形会有多个，因此是个数组
	 */
	private transient int[][][] POINTS_PATTERN;
	/**
	 * 平局检测：确定双方均无法完成胜利棋形时，即为平局。 但如果从开始起就每步检测平局是没有必要的浪费，因此规定落子达到数量后，再开始检测平局。
	 */
	transient int drawPossiable;
	/**
	 * AI外部配置读入。
	 */
	transient Properties properties;
	/**
	 * 是否打印出每步坐标
	 */
	transient boolean printStep;

	private transient boolean autoMode = false;

	/**
	 * 棋盘宽
	 */
	int width;
	/**
	 * 棋盘高
	 */
	int height;

	private int[][] chessBoard; // 记录盘面。
	private int[][] patternProgress;// 记录两个玩家各自的棋形完成度，[n=1]两个玩家
									// [n=2]胜利棋形编号。用于盘面分值评估。
	Player next = Player.BLACK; // 轮到
	Player winner = null; // 赢家
	History his = new History();

	/**
	 * 根据棋盘大小构造棋局
	 * 
	 * @param p
	 * @param width
	 *            宽度
	 * @param height
	 *            高度
	 */
	public Chess(int width, int height) {
		resetSize(width, height);
		this.properties = new Properties();
		loadConfig();
	}

	/**
	 * 下一个轮到谁
	 * 
	 * @return
	 */
	public Player getNext() {
		if (isReviewMode()) {
			return his.reviewIndex % 2 == 0 ? Player.WHITE : Player.BLACK;
		} else {
			return next;
		}
	}

	/**
	 * 重新设置棋盘大小
	 * 
	 * @param width
	 * @param height
	 */
	public boolean resetSize(int width, int height) {
		if (this.width == width && this.height == height)
			return false;

		this.width = width;
		this.height = height;

		// 完成型和棋子坐标的对应表
		PATTERN_POINTS = new Pattern[getPatternCount()];
		POINTS_PATTERN = new int[width][height][];// 记录每个点所关联到的棋形编号
		this.drawPossiable = (width - 3) * (height - 3);

		IntList[][] temp = new IntList[width][height];
		int icount = 0;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < (width - 4); j++) {
				Pattern pattern = new Pattern(icount);
				PATTERN_POINTS[icount] = pattern;
				for (int k = 0; k < 5; k++) {
					if (temp[j + k][i] == null) {
						temp[j + k][i] = new IntList();
					}
					temp[j + k][i].add(icount);

					Point point = new Point(j + k, i);
					pattern.points[k] = point;
				}
				icount++;
			}
		}

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < (height - 4); j++) {
				Pattern pattern = new Pattern(icount);
				PATTERN_POINTS[icount] = pattern;
				for (int k = 0; k < 5; k++) {
					if (temp[i][j + k] == null) {
						temp[i][j + k] = new IntList();
					}
					temp[i][j + k].add(icount);

					Point point = new Point(i, j + k);
					pattern.points[k] = point;
				}
				icount++;
			}
		}
		for (int i = 0; i < (height - 4); i++) {
			for (int j = 0; j < (width - 4); j++) {
				Pattern pattern = new Pattern(icount);
				PATTERN_POINTS[icount] = pattern;
				for (int k = 0; k < 5; k++) {
					if (temp[j + k][i + k] == null) {
						temp[j + k][i + k] = new IntList();
					}
					temp[j + k][i + k].add(icount);
					Point point = new Point(j + k, i + k);
					pattern.points[k] = point;
				}
				icount++;
			}
		}
		for (int i = 0; i < (height - 4); i++) {
			for (int j = (width - 1); j >= 4; j--) {
				Pattern pattern = new Pattern(icount);
				PATTERN_POINTS[icount] = pattern;
				for (int k = 0; k < 5; k++) {
					if (temp[j - k][i + k] == null) {
						temp[j - k][i + k] = new IntList();
					}
					temp[j - k][i + k].add(icount);
					Point point = new Point(j - k, i + k);
					pattern.points[k] = point;
				}
				icount++;
			}
		}
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				this.POINTS_PATTERN[i][j] = temp[i][j].toArrayUnsafe();
			}
		}
		this.chessBoard = new int[width][height]; // 落子表
		this.patternProgress = new int[2][PATTERN_POINTS.length];// 棋型达成度表;
		return true;
	}

	private void loadConfig() {
		this.properties.clear();
		try {
			this.properties.load(this.getClass().getResourceAsStream("/ai.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		printStep = "true".equalsIgnoreCase(this.properties.getProperty("print"));
	}

	/**
	 * 在指定位置落子
	 * 
	 * @param point
	 */
	public synchronized void doMove(Point point) {
		if (isReviewMode()) {
			reviewNext();
			return;
		}
		Player player = this.next;
		if (player == null)
			return;

		int x = point.x;
		int y = point.y;
		if (x >= width || y >= height) {
			System.out.println("无效落子，该点" + point + "超出棋盘范围！");
			return; // 无效
		}
		if (chessBoard[x][y] != 0) {
			System.out.println("无效落子，该点" + point + "已有" + Player.values()[chessBoard[x][y] - 1]);
			return; // 无效
		}
		if (printStep) {
			System.out.println(player + "\t(" + x + "," + y + ")");
		}
		his.add(point, player);

		Player opp = player.getOpp();
		this.next = opp;
		this.chessBoard[x][y] = player.color();// 落子
		// 落子后的盘面判定
		for (int i : POINTS_PATTERN[x][y]) {
			if (patternProgress[player.ordinal()][i] != IMPOSSIBLE) {
				this.patternProgress[player.ordinal()][i]++; // 该状态下棋型达成度+1
			}
			patternProgress[opp.ordinal()][i] = IMPOSSIBLE; // 对手的该种棋型变得不可能
			if (5 == this.patternProgress[player.ordinal()][i]) {// 完成！
				this.winner = player;
				this.next = null;
				break;
			}
		}
		if (winner == null && his.count() > drawPossiable) {
			checkDraw();// 平局检测
		}
		if (next == null) {// 记录结束
			onOver();
		}
	}

	/**
	 * 退一步状态
	 */
	public Player rollback() {
		if (isReviewMode()) {
			reviewPrev();
			return null;
		}
		Player rollPlayer = his.getLastPlayer();
		if (rollPlayer == null)
			return null;
		this.winner = null;
		this.next = rollPlayer;

		Point point = his.pop();

		int x = point.x;
		int y = point.y;
		// if (chessBoard[x][y] == 0) {
		// throw new IllegalArgumentException("Point " + point +
		// " has no chess." );
		// }
		chessBoard[x][y] = 0;

		Player opp = rollPlayer.getOpp();

		for (int i : POINTS_PATTERN[x][y]) { // 局势判断，检查所有棋型的完成度
			if (patternProgress[rollPlayer.ordinal()][i] != IMPOSSIBLE) {
				patternProgress[rollPlayer.ordinal()][i]--; // 棋形完成度减一
			}
			// //由于是我方少下一子，因此原先无法完成的棋形还是无法完成.因此不用计算我方IMPOSSIBLE棋形的完成度

			// 处理对方棋形的完成度，由于我方少下一子对对方可能棋形的完成度无影响，只会影响对方IMPOSSIBLE的棋形完成度
			if (patternProgress[opp.ordinal()][i] == IMPOSSIBLE) {//
				Pattern pattern = PATTERN_POINTS[i];// 重算完成度
				int progress = 0;
				for (Point p : pattern.points) {
					int pcolor = chessBoard[p.x][p.y];
					if (pcolor == opp.color()) {
						progress++;
					} else if (pcolor == rollPlayer.color()) {
						progress = IMPOSSIBLE;
						break;
					}
				}
				patternProgress[opp.ordinal()][i] = progress;
			}
		}
		return rollPlayer;
	}

	private void checkDraw() {
		int blackPossible = 0;
		int whitePossible = 0;
		int patterns = this.PATTERN_POINTS.length;
		for (int i = 0; i < patterns; i++) { // 局势判断，检查关联棋型的完成度
			if (patternProgress[0][i] != IMPOSSIBLE) {
				blackPossible++;
			}
			if (patternProgress[1][i] != IMPOSSIBLE) {
				whitePossible++;
			}
		}
		if (blackPossible == 0 && whitePossible == 0) {
			next = null; // 平局
		}
	}

	/**
	 * 自动下一步
	 */
	public synchronized Point computerMove() {
		if (isReviewMode()) {
			reviewNext();
		}
		Point myLoc;
		if (his.count() == 1) {
			Point last = his.getLast();
			int x = last.x;
			int y = last.y;
			x += (x + 1 - width / 2) > 0 ? -1 : 1;// 第一步时占更为靠近中心的一点
			y += (y + 1 - height / 2) > 0 ? -1 : 1;
			myLoc = new Point(x, y);
		} else if (his.count() == 0) {
			myLoc = new Point(width / 2, height / 2);
		} else {
			AI ai = next.getAi();
			if (ai == null) {
				next.setAi(createAI(next));
				next.human = true; // 走一步赋予ai不能认为改变角色
				ai = next.getAi();
			}
			myLoc = ai.compute();
		}
		doMove(myLoc);
		return myLoc;
	}

	public void initGame(boolean isBlackHuman, boolean isWhiteHuman) {
		if (isBlackHuman) {
			Player.BLACK.setAi(null);
		} else {
			Player.BLACK.setAi(createAI(Player.BLACK));
		}
		if (isWhiteHuman) {
			Player.WHITE.setAi(null);
		} else {
			Player.WHITE.setAi(createAI(Player.WHITE));
		}
		this.winner = null;
		his.clear();
		this.next = Player.BLACK;
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++) {
				this.chessBoard[i][j] = 0;
			}

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < PATTERN_POINTS.length; j++)
				this.patternProgress[i][j] = 0;
		}
		if (!next.isHuman()) {
			computerMove();
		}
	}

	AI createAI(Player p) {
		String ai = properties.getProperty(p.name().toLowerCase());
		System.err.println(ai);
		try {
			if (ai != null && ai.length() > 0) {
				Class<?> clz = Class.forName("com.yunzhijia.gobang.ai." + ai.trim());
				Constructor<?> c = clz.getConstructor(Chess.class);
				return (AI) c.newInstance(this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new AI.Default(this);
	}

	/**
	 * 得到指定点产生关联的胜利棋形
	 * 
	 * @param x
	 *            坐标x
	 * @param y
	 *            坐标y
	 * @return 关联的所有棋形编号
	 */
	public int[] getPointToPattern(int x, int y) {
		return POINTS_PATTERN[x][y];
	}

	/**
	 * 得到玩家在这个棋形上的完成度
	 * 
	 * @param player
	 *            玩家
	 * @param patternId
	 *            棋形
	 * @return 棋形完成度
	 */
	public int getPatternScore(Player player, int patternId) {
		return patternProgress[player.ordinal()][patternId];
	}

	public Thread startAuto(final int wait) {
		Thread t = new Thread() {
			@Override
			public void run() {
				autoMode = true;
				while (next != null && !next.isHuman()) {
					try {
						if (wait > 0)
							Thread.sleep(wait);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					computerMove();
				}
				autoMode = false;
			}

		};
		t.start();
		return t;
	}

	public void onOver() {
		int white = his.count() / 2;
		int black = his.count() - white;
		String message = "白子：" + white + " , 黑子" + black + " 总手数" + his.count() + "  "
				+ (winner == null ? "平局" : winner + "胜利");
		if (printStep)
			System.out.println(message);
	}

	private int getPatternCount() {
		int w_ = width - 4;
		int h_ = height - 4;
		if (w_ < 0)
			w_ = 0;
		if (h_ < 0)
			h_ = 0;
		return width * h_ + height * w_ + h_ * w_ * 2;
	}

	/**
	 * 直接后台运行1000局棋，计算胜率。用于评估AI
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int white = 0;
		int black = 0;
		int draw = 0;
		int size = 16;
		Chess c = new Chess(size, size);
		c.printStep = true;
		int minStep = 999;
		int totalCount = 0;
		int maxStep = 0;
		c.initGame(true, true);
		c.doMove(new Point(7,7));
		c.computerMove();
		c.computerMove();
		c.computerMove();
		c.computerMove();
		c.computerMove();
		c.computerMove();
//		long start = System.currentTimeMillis();
//		int loop = 1000;
//		for (int i = 0; i < loop; i++) {
//			c.initGame(false, false);
//			while (c.next != null) {
//				c.computerMove();
//			}
//			int count = c.his.count();
//			if (c.winner == Player.WHITE) {
//				white++;
//			} else if (c.winner == Player.BLACK) {
//				black++;
//			} else {
//				draw++;
//			}
//			totalCount += count;
//			if (count < minStep)
//				minStep = count;
//			if (count > maxStep)
//				maxStep = count;
//		}
//		long cost = System.currentTimeMillis() - start;
//		String m = loop + "局完成。" + Player.BLACK + "胜:" + black + " " + Player.WHITE + "胜:" + white + " 平局:" + draw
//				+ " 总耗时:" + (cost) + "ms，每局平均" + ((float) cost / loop) + "ms。";
//
//		System.out.println(m);
//		System.out.println("最小手数" + minStep + "  最大手数" + maxStep + "  平均手数" + (totalCount / loop));
	}

	public Pattern[] getPatterns() {
		return PATTERN_POINTS;
	}

	public int[][] getTable() {
		System.out.println("*******************************************");
		for(int i=0;i<chessBoard.length;i++){
			StringBuffer sb = new StringBuffer();
			for(int j=0;j<chessBoard[i].length;j++){
				sb.append(chessBoard[i][j]);
			}
			System.err.println(sb.toString());
		}
		System.out.println("*******************************************");
		return chessBoard;
	}

	/**
	 * 托管
	 */
	public void changeToAi() {
		if (next == null)
			return;
		if (next.getAi() == null) {
			next.setAi(createAI(next));
		}
		if (next.getOpp().isHuman()) {
			computerMove();
		} else {
			startAuto(200);
		}
	}

	public void close() {
		this.chessBoard = null;
		this.next = null;
		this.his = null;
		this.properties = null;

	}

	public boolean isAutoRunning() {
		return autoMode;
	}

	/**
	 * 复盘模式
	 * 
	 * @return
	 */
	public boolean isReviewMode() {
		return his.reviewIndex >= 0;
	}

	public void load(File file) {
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(file));
			Chess chess = (Chess) in.readObject();
			this.resetSize(chess.width, chess.height);
			this.chessBoard = chess.chessBoard;
			this.patternProgress = chess.patternProgress;
			this.winner = chess.winner;
			this.next = chess.next;
			this.his = chess.his;
			this.his.reviewIndex = -1;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public void save(File file) {
		try {
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
			os.writeObject(this);
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void review(int i) {
		his.review(i, this);
	}

	public void reviewNext() {
		if (isReviewMode()) {
			his.reviewNext(this);
		}
	}

	public void reviewPrev() {
		if (isReviewMode()) {
			his.reviewPrev(this);
		}
	}

	public void exitReview(boolean flag) {
		if (isReviewMode()) {
			his.exitReview(this, flag);
		}
	}

	public void reCalcProgress() {
		Player winner = null;
		for (Pattern pt : PATTERN_POINTS) {
			int blackPrg = 0;
			int whitePrg = 0;
			for (Point p : pt.points) {
				switch (chessBoard[p.x][p.y]) {
				case 0:
					break;
				case 1:
					blackPrg++;
					whitePrg = 7;
					break;
				case 2:
					whitePrg++;
					blackPrg = 7;
					break;
				}
			}
			if (whitePrg > IMPOSSIBLE)
				whitePrg = IMPOSSIBLE;
			if (blackPrg > IMPOSSIBLE)
				blackPrg = IMPOSSIBLE;
			this.patternProgress[0][pt.getId()] = blackPrg;
			this.patternProgress[1][pt.getId()] = whitePrg;
			if (blackPrg == 5)
				winner = Player.BLACK;
			if (whitePrg == 5)
				winner = Player.WHITE;
		}
		this.winner = winner;
	}

}

class IntList {
	private int[] list;
	private int size = 0;

	public IntList() {
		this.list = new int[16];
	}

	public int[] toArrayUnsafe() {
		if (size == list.length) {
			return list;
		} else {
			int[] array = new int[size];
			System.arraycopy(list, 0, array, 0, size);
			return array;
		}
	}

	public void add(int e) {
		ensureCapacity(size + 1); // Increments modCount!!
		list[size++] = e;
	}

	private void ensureCapacity(int i) {
		if (list.length < i) {
			int newLen = list.length * 2;
			while (newLen < i) {
				newLen *= 2;
			}
			this.list = Arrays.copyOf(list, newLen);
		}
	}
}

/**
 * 胜利棋形，包含五个点
 * 
 * @author jiyi
 *
 */
class Pattern {
	Pattern(int id) {
		this.id = id;
	}

	/**
	 * 由五子坐标组成
	 */
	Point[] points = new Point[5];

	private int id;

	public Point[] getPoints() {
		return points;
	}

	public int getId() {
		return id;
	}
}

enum Player {
	BLACK, WHITE;

	private AI ai;
	boolean human;

	int color() {
		return ordinal() + 1;
	}

	public AI getAi() {
		return ai;
	}

	public void setAi(AI ai) {
		this.ai = ai;
		human = ai == null;
	}

	public boolean isHuman() {
		return human;
	}

	Player getOpp() {
		return this == BLACK ? WHITE : BLACK;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this == BLACK ? "黑" : "白");
		if (ai == null) {
			sb.append("(人脑)");
		} else {
			sb.append('(').append(ai.getClass().getName().substring(3)).append(")");
		}
		return sb.toString();
	}
}

@SuppressWarnings("serial")
class History implements Serializable {
	private List<Point> steps = new ArrayList<Point>(128);
	private Player last = null;
	transient int reviewIndex = -1;

	public int count() {
		if (reviewIndex >= 0)
			return reviewIndex + 1;
		return steps.size();
	}

	public void reviewNext(Chess chess) {
		if (reviewIndex == steps.size() - 1) {
			exitReview(chess, false); // 已经演算到最后一步，退出复盘状态
		} else {
			int[][] board = chess.getTable();
			Point p = steps.get(++reviewIndex);
			board[p.x][p.y] = (reviewIndex % 2) + 1;
		}
	}

	// Flag为true时，从当前盘面状态开始对弈。否则恢复为复盘之前的棋局.
	public void exitReview(Chess chess, boolean flag) {
		if (reviewIndex == steps.size() - 1) {// 已经遍历到最后一步了，直接退出即可
			reviewIndex = -1;
			return;
		}
		//
		if (!flag) {
			while (reviewIndex != -1) {
				reviewNext(chess);
			}
			return;
		}
		// 中途退出，作为残局进行游戏
		for (int n = steps.size() - 1; n > reviewIndex; n--) {
			steps.remove(n);
		}
		this.last = getLastPlayer();// 上一玩家
		chess.next = last.getOpp(); // 設置當前玩家
		chess.reCalcProgress();// 重新計算勝利可能
		reviewIndex = -1; // 退出复盤模式
		if (chess.next != null && !chess.next.isHuman()) {// 如果輪到電腦下，自動下一步
			chess.computerMove();
		}
	}

	public void reviewPrev(Chess chess) {
		if (reviewIndex == 0) {
			return;
		} else {
			int[][] board = chess.getTable();
			Point p = steps.get(reviewIndex--);
			board[p.x][p.y] = 0;
		}
	}

	public void review(int step, Chess chess) {
		if (step >= steps.size() || step < 0) {
			return;
		}
		if (reviewIndex == -1)
			clearChess(chess);
		while (reviewIndex < step) {
			reviewNext(chess);
		}
		while (reviewIndex > step) {
			reviewPrev(chess);
		}
	}

	private void clearChess(Chess chess) {
		int[][] board = chess.getTable();
		for (int i = 0; i < chess.width; i++) {
			for (int j = 0; j < chess.height; j++) {
				board[i][j] = 0;
			}
		}
	}

	public void add(Point point, Player p) {
		this.last = p;
		steps.add(point);
	}

	public void clear() {
		this.last = null;
		this.reviewIndex = -1;
		steps.clear();
	}

	public Point getLast() {
		if (reviewIndex >= 0) {
			return steps.get(reviewIndex);
		}
		if (steps.isEmpty())
			return null;
		return steps.get(steps.size() - 1);
	}

	public Point pop() {
		if (steps.isEmpty()) {
			return null;
		}
		Point p = steps.remove(steps.size() - 1);
		this.last = steps.isEmpty() ? null : last.getOpp();
		return p;
	}

	/**
	 * 得到上一手的玩家
	 * 
	 * @return
	 */
	public Player getLastPlayer() {
		if (reviewIndex >= 0) {
			return reviewIndex % 2 == 0 ? Player.BLACK : Player.WHITE;
		}
		return last;
	}

	public Point getPrevLast() {
		int index = reviewIndex == -1 ? steps.size() - 1 : reviewIndex;
		if (index > 0) {
			return steps.get(index - 1);
		} else {
			return null;
		}
	}

	public Point get(int i) {
		return steps.get(i);
	}
}