package com.jiuxian.theone.test;

import org.junit.Test;

import com.jiuxian.theone.UniqueProcess;
import com.jiuxian.theone.Process;
import com.jiuxian.theone.zk.ZookeeperUniqueProcess;

public class ProcessTest {

	@Test
	public void test() {
		String zks = "192.168.5.99,192.168.5.104";
		Process process = new SimpleProcess();
		UniqueProcess guard = new ZookeeperUniqueProcess(process, zks);
		guard.run();
		try {
			guard.close();
		} catch (Exception e) {
		}
	}

}
