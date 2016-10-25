/**
 * Copyright 2015-2020 jiuxian.com.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jiuxian.theone.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.jiuxian.theone.Process;
import com.jiuxian.theone.CompetitiveProcess;
import com.jiuxian.theone.util.NetworkUtils;

/**
 * Process that guarantees uniqueness by zookeeper<br>
 * Processes with be grouped by value of group, and there will be only one
 * process alive in each group
 * 
 * @author <a href="mailto:wangyuxuan@jiuxian.com">Yuxuan Wang</a>
 *
 */
public class ZookeeperCompetitiveProcess extends CompetitiveProcess {

	/**
	 * zookeeper root for the lock
	 */
	private String group;
	/**
	 * interval for lock competition
	 */
	private long interval;

	private CuratorFramework client;

	private static final String ZK_ROOT = "/theone";
	private static final int HEART_BEAT = 10 * 1000;
	private static final int DEFAULT_INTERVAL = 10 * 1000;
	private static final String LOCK = "lock";
	private static final String DEFAULT_GROUP = "default";

	private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperCompetitiveProcess.class);

	/**
	 * @param process
	 *            process to be unique
	 * @param zks
	 *            zookeeper address
	 */
	public ZookeeperCompetitiveProcess(Process process, String zks) {
		this(process, zks, DEFAULT_GROUP);
	}

	/**
	 * @param process
	 *            process to be unique
	 * @param zks
	 *            zookeeper address
	 * @param group
	 *            group for the lock
	 */
	public ZookeeperCompetitiveProcess(Process process, String zks, String group) {
		this(process, zks, group, HEART_BEAT, DEFAULT_INTERVAL);
	}

	/**
	 * @param process
	 *            process to be unique
	 * @param zks
	 *            zookeeper address
	 * @param group
	 *            group for the lock
	 * @param heartbeat
	 *            zookeeper heartbeat interval
	 * @param interval
	 *            interval for lock competition
	 */
	public ZookeeperCompetitiveProcess(Process process, String zks, String group, int heartbeat, int interval) {
		super(process);
		this.group = group;
		this.interval = interval;

		client = CuratorFrameworkFactory.newClient(zks, heartbeat, heartbeat, new ExponentialBackoffRetry(1000, 3));
		ConnectionListener connectionListener = new ConnectionListener();
		connectionListener.setResource(process);
		client.getCuratorListenable().addListener(connectionListener);
		client.start();

		final String lockPath = ZKPaths.makePath(ZK_ROOT, group);
		try {
			if (client.checkExists().forPath(lockPath) == null) {
				LOGGER.info("Lock path {} not exists, create it.", lockPath);
				client.create().creatingParentsIfNeeded().forPath(lockPath);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void fetchLock() {
		final String lockNode = ZKPaths.makePath(ZKPaths.makePath(ZK_ROOT, group), LOCK);
		try {
			while (true) {
				if (client.checkExists().forPath(lockNode) == null) {
					try {
						client.create().withMode(CreateMode.EPHEMERAL).forPath(lockNode, NetworkUtils.getLocalIp().getBytes());
						break;
					} catch (NodeExistsException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
				Thread.sleep(interval);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void close() throws Exception {
		super.close();
		if (client != null) {
			client.close();
		}
	}

	@Override
	public String currentLocker() {
		final String lockNode = ZKPaths.makePath(ZKPaths.makePath(ZK_ROOT, group), LOCK);
		try {
			if (client.checkExists().forPath(lockNode) != null) {
				byte[] data = client.getData().forPath(lockNode);
				if (data != null) {
					return new String(data);
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}
}
