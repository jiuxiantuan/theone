package com.jiuxian.theone.zk;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.jiuxian.theone.Guard;
import com.jiuxian.theone.util.LocalUtils;

public class ZookeeperGuard implements Guard, AutoCloseable {

	private static final String DEFAULT_ROOT = "/theone/guard";

	private static final String COMPETERS = "competers";

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
			validate(gateName);
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

	private void validate(String gateName) throws Exception {
		String path = ZKPaths.makePath(root, gateName + "-" + COMPETERS);
		if (client.checkExists().forPath(path) == null) {
			LOGGER.info("Competers path {} not exists, create it.", path);
			try {
				client.create().creatingParentsIfNeeded().forPath(path);
			} catch (NodeExistsException e) {
				// Just ignore
			}
		}

		String hostName = LocalUtils.getLocalIp();
		String hostPath = ZKPaths.makePath(path, hostName);
		if (client.checkExists().forPath(hostPath) != null) {
			throw new RuntimeException("There is a instance of host " + hostName + " already.");
		} else {
			try {
				client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(hostPath);
			} catch (NodeExistsException e) {
				throw new RuntimeException("There is a instance of host " + hostName + " already.");
			}
		}

		Preconditions.checkArgument(!COMPETERS.equals(gateName), "competers is reserve name, choose another one.");
	}

	@Override
	public void close() throws Exception {
		if (client != null) {
			client.close();
		}
	}

	@Override
	public List<String> competers(String gateName) {
		List<String> hosts = null;
		String path = ZKPaths.makePath(root, gateName + "-" + COMPETERS);
		try {
			List<String> children = client.getChildren().forPath(path);
			if (children != null && children.size() > 0) {
				String prefixToRemove = path + "/";
				hosts = children.stream().map(e -> StringUtils.removeStart(e, prefixToRemove)).collect(Collectors.toList());
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		return hosts;
	}

}
