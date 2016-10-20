package com.jiuxian.theone.test;

import com.jiuxian.theone.Process;

public class SimpleProcess implements Process {
	
	@Override
	public void run() {
		System.out.println("This is a process example.");
	}

	@Override
	public void close() throws Exception {
		System.out.println("Close resources");
	}

}
