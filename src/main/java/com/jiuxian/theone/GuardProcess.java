package com.jiuxian.theone;

/**
 * 只存活一个的进程
 * 
 * @author <a href="mailto:wangyuxuan@jiuxian.com">Yuxuan Wang</a>
 *
 */
public abstract class GuardProcess implements Process {

	private Process process;

	public GuardProcess(Process process) {
		super();
		this.process = process;
	}

	@Override
	public void run() {

		fetchLock();
		process.run();

	}

	protected abstract void fetchLock();

}
