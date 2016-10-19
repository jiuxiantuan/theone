package com.jiuxian.theone;

/**
 * 进程
 * 
 * @author <a href="mailto:wangyuxuan@jiuxian.com">Yuxuan Wang</a>
 *
 */
public interface Process extends AutoCloseable {

	void run();

}
