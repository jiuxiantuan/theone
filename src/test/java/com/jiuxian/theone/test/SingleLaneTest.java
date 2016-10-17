package com.jiuxian.theone.test;

import org.junit.Test;

import com.jiuxian.theone.Guard;
import com.jiuxian.theone.Process;
import com.jiuxian.theone.SimpleSingleLane;
import com.jiuxian.theone.zk.ZookeeperGuard;

public class SingleLaneTest {

	@Test
	public void test() {
		String name = "gold";
		Guard guard = new ZookeeperGuard("192.168.5.99,192.168.5.104", 3 * 1000);
		Process process1 = new Process() {

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
		};
		new SimpleSingleLane(name, guard, process1).compete();
	}

}
