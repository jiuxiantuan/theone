package com.jiuxian.theone.test;

import org.junit.Test;

import com.jiuxian.theone.Competitive;
import com.jiuxian.theone.CompetitiveProcess;
import com.jiuxian.theone.Process;
import com.jiuxian.theone.zk.ZookeeperCompetitiveImpl;

public class ProcessTest {

	@Test
	public void test() {
		String zks = "192.168.5.99,192.168.5.104";
		Process process = new SimpleProcess();
		Competitive competitive = new ZookeeperCompetitiveImpl(zks, "group1");
		try (CompetitiveProcess guard = new CompetitiveProcess(process, competitive)) {
			guard.run();
		} catch (Exception e) {
		}
	}

}
