package com.jiuxian.theone.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 监听进程到zookeeper之间连接的网络状态，当网络失联时自杀
 * 
 * @author <a href="mailto:wangyuxuan@jiuxian.com">Yuxuan Wang</a>
 *
 */
public class ConnectionListener implements CuratorListener {

	private AutoCloseable resource;

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionListener.class);

	@Override
	public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
		LOGGER.info(event.toString());
		final WatchedEvent watchedEvent = event.getWatchedEvent();
		if (watchedEvent != null && watchedEvent.getState() == KeeperState.Disconnected) {
			try {
				resource.close();
			} finally {
				System.exit(0);
			}
		}
	}

	public void setResource(AutoCloseable resource) {
		this.resource = resource;
	}

}
