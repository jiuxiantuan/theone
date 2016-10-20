package com.jiuxian.theone;

/**
 * Unique process in cluster
 * 
 * @author <a href="mailto:wangyuxuan@jiuxian.com">Yuxuan Wang</a>
 *
 */
public abstract class UniqueProcess implements Process {

	private Process process;

	public UniqueProcess(Process process) {
		super();
		this.process = process;
	}

	@Override
	public void run() {

		fetchLock();
		process.run();

	}

	protected abstract void fetchLock();

	@Override
	public void close() throws Exception {
		if (process != null) {
			process.close();
		}
	}

}
