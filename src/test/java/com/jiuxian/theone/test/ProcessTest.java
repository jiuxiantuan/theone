package com.jiuxian.theone.test;

import org.junit.Test;

import com.jiuxian.theone.UniqueProcess;
import com.jiuxian.theone.Process;
import com.jiuxian.theone.zk.ZookeeperUniqueProcess;

public class ProcessTest {

	@Test
	public void test() {
		Process process = new Process() {

			@Override
			public void run() {
				System.out.println(this);
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void close() throws Exception {
				System.out.println("Closing resources.");
			}
		};
		UniqueProcess guard = new ZookeeperUniqueProcess(process, "192.168.5.99,192.168.5.104");
		guard.run();
		try {
			guard.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
