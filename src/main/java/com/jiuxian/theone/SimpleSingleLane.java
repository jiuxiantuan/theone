package com.jiuxian.theone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class SimpleSingleLane implements SingleLane {

	private String name;

	private Process process;

	private Guard guard;

	/**
	 * Interval for competition, in mill seconds
	 */
	private int interval = DEFAULT_INTERVAL;

	private static final int DEFAULT_INTERVAL = 3000;

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSingleLane.class);

	public SimpleSingleLane(String name, Guard guard, Process process) {
		super();
		this.name = Preconditions.checkNotNull(name);
		this.process = Preconditions.checkNotNull(process);
		this.guard = Preconditions.checkNotNull(guard);
	}

	@Override
	public void compete() {
		guard.auth(name, interval);
		LOGGER.info("Process {} competation success.", process);
		process.run();
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

}
