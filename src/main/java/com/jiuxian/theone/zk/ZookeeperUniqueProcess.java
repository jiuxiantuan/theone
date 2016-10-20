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
import com.jiuxian.theone.UniqueProcess;
import com.jiuxian.theone.Process;
import com.jiuxian.theone.util.NetworkUtils;

/**
 * Process that guarantees uniqueness by zookeeper
 * 
 * @author <a href="mailto:wangyuxuan@jiuxian.com">Yuxuan Wang</a>
 *
 */
public class ZookeeperUniqueProcess extends UniqueProcess {

	private String zkroot;
	private long interval;

	private CuratorFramework client;

	private static final String DEFAULT_ROOT = "/theone/guard";
	private static final int HEART_BEAT = 10 * 1000;
	private static final int DEFAULT_INTERVAL = 10 * 1000;
	private static final String LOCK = "lock";

	private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperUniqueProcess.class);

	/**
	 * @param process
	 * @param zks
	 */
	public ZookeeperUniqueProcess(Process process, String zks) {
		this(process, zks, DEFAULT_ROOT, HEART_BEAT, DEFAULT_INTERVAL);
	}

	/**
	 * @param process
	 * @param zks
	 * @param zkroot
	 *            Zookeeper root for the lock
	 * @param heartbeat
	 *            zookeeper heartbeat interval
	 * @param interval
	 *            interval for lock competition
	 */
	public ZookeeperUniqueProcess(Process process, String zks, String zkroot, int heartbeat, int interval) {
		super(process);
		this.zkroot = zkroot;
		this.interval = interval;

		client = CuratorFrameworkFactory.newClient(zks, heartbeat, heartbeat, new ExponentialBackoffRetry(1000, 3));
		ConnectionListener connectionListener = new ConnectionListener();
		connectionListener.setResource(process);
		client.getCuratorListenable().addListener(connectionListener);
		client.start();
		try {
			if (client.checkExists().forPath(zkroot) == null) {
				LOGGER.info("Root {} not exists, create it.", zkroot);
				client.create().creatingParentsIfNeeded().forPath(zkroot);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw Throwables.propagate(e);
		}
	}

	@Override
	protected void fetchLock() {
		String path = ZKPaths.makePath(zkroot, LOCK);
		try {
			while (true) {
				Stat exists = client.checkExists().forPath(path);
				if (exists == null) {
					try {
						client.create().withMode(CreateMode.EPHEMERAL).forPath(path, NetworkUtils.getLocalIp().getBytes());
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
}
