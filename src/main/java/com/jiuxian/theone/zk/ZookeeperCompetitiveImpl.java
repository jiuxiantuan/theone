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
import com.jiuxian.theone.Competitive;
import com.jiuxian.theone.util.NetworkUtils;

/**
 * Competitive implementation that guarantees uniqueness by zookeeper<br>
 * Processes with be grouped by value of group, and there will be only one
 * process alive in each group
 * 
 * @author <a href="mailto:wangyuxuan@jiuxian.com">Yuxuan Wang</a>
 *
 */
public class ZookeeperCompetitiveImpl implements Competitive {

	/**
	 * zookeeper root for the lock
	 */
	private String group;
	/**
	 * interval for lock competition
	 */
	private long interval;

	private CuratorFramework client;
	private ConnectionListener connectionListener;

	private static final String ZK_ROOT = "/theone";
	private static final int HEART_BEAT = 10 * 1000;
	private static final int DEFAULT_INTERVAL = 10 * 1000;
	private static final String LOCK = "lock";
	private static final String DEFAULT_GROUP = "default";

	private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperCompetitiveImpl.class);

	/**
	 * @param zks
	 *            zookeeper address
	 */
	public ZookeeperCompetitiveImpl(String zks) {
		this(zks, DEFAULT_GROUP);
	}

	/**
	 * @param zks
	 *            zookeeper address
	 * @param group
	 *            group for the lock
	 */
	public ZookeeperCompetitiveImpl(String zks, String group) {
		this(zks, group, HEART_BEAT, DEFAULT_INTERVAL);
	}

	/**
	 * @param zks
	 *            zookeeper address
	 * @param group
	 *            group for the lock
	 * @param heartbeat
	 *            zookeeper heartbeat interval
	 * @param interval
	 *            interval for lock competition
	 */
	public ZookeeperCompetitiveImpl(String zks, String group, int heartbeat, int interval) {
		super();
		this.group = group;
		this.interval = interval;

		client = CuratorFrameworkFactory.newClient(zks, heartbeat, heartbeat, new ExponentialBackoffRetry(1000, 3));
		connectionListener = new ConnectionListener();
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
	public void close() {
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

	@Override
	public void registerCorrelativeResource(AutoCloseable resource) {
		connectionListener.setResource(resource);
	}
}
