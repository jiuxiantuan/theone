package com.jiuxian.theone;

public interface Guard {

	/**
	 * Try to auth with the gate
	 * 
	 * @param gateName
	 * @param interval
	 */
	void auth(String gateName, int interval);

}
