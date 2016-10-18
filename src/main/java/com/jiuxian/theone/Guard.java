package com.jiuxian.theone;

import java.util.List;

public interface Guard {

	/**
	 * Try to auth with the gate
	 * 
	 * @param gateName
	 * @param interval
	 */
	void auth(String gateName, int interval);

	/**
	 * Get competers of the gate
	 * 
	 * @param gateName
	 * @return
	 */
	List<String> competers(String gateName);

}
