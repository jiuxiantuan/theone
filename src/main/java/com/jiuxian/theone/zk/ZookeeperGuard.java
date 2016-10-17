package com.jiuxian.theone.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.jiuxian.theone.Guard;

public class ZookeeperGuard implements Guard, AutoCloseable {

	private static final String DEFAULT_ROOT = "/theone/guard";

	private static final int HEART_BEAT = 10 * 1000;

	private String root;

	private CuratorFramework client;

	private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperGuard.class);

	public ZookeeperGuard(String zks) {
		this(zks, DEFAULT_ROOT, HEART_BEAT);
	}

	public ZookeeperGuard(String zks, int heartbeat) {
		this(zks, DEFAULT_ROOT, heartbeat);
	}

	public ZookeeperGuard(String zks, String root, int heartbeat) {
		super();
		this.root = root;

		client = CuratorFrameworkFactory.newClient(zks, heartbeat, heartbeat, new ExponentialBackoffRetry(1000, 3));
		client.start();
		try {
			if (client.checkExists().forPath(root) == null) {
				LOGGER.info("Root {} not exists, create it.", root);
				client.create().creatingParentsIfNeeded().forPath(root);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void auth(String gateName, int interval) {
		String path = ZKPaths.makePath(root, gateName);
		try {
			while (true) {
				Stat exists = client.checkExists().forPath(path);
				if (exists == null) {
					try {
						client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
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
		if (client != null) {
			client.close();
		}
	}

}
