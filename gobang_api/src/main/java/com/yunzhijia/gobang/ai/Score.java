package com.yunzhijia.gobang.ai;

public enum Score {
	ONE(10), TWO(100), THREE(1000), FOUR(100000), FIVE(1000000), BLOCKED_ONE(1), BLOCKED_TWO(10), BLOCKED_THREE(
			100), BLOCKED_FOUR(10000);
	private int value;

	Score(int val) {
		this.value = val;
	}

	public int value() {
		return this.value;
	}
}
