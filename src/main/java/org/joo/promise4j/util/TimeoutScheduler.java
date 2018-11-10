package org.joo.promise4j.util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class TimeoutScheduler {

	static final class DaemonThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			t.setName("DeferredTimeoutScheduler");
			return t;
		}
	}

	static final ScheduledThreadPoolExecutor delayer;
	static {
		(delayer = new ScheduledThreadPoolExecutor(1, new DaemonThreadFactory())).setRemoveOnCancelPolicy(true);
	}

	public static ScheduledFuture<?> delay(Runnable command, long delay, TimeUnit unit) {
		return delayer.schedule(command, delay, unit);
	}
}
