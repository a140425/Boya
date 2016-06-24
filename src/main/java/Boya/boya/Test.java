package Boya.boya;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Created by allis on 5/17/16. 
 * 0. Given the following code, and you have following requirements, how do you enable your code to finish the requirement?
 * 1. I want my concurrent tasks (callable) to run just 100 times, thus you need to make callable to run repeatedly until total count = 100 reaches
 * 2. I want to show the end result once task finishes job, thus you need to design a mechanism to receive result of each iteration from each task
 * 3. I want a data structure to show the result with task name, how much time it performs (sleep time), and the finished time and use log to dump result to console
 * 4. You are free to use any logging framework, but not use System.out.println()
 */
public class Test {
	private final static int TASK_RUN_ITERATION = 100;
	private final static int THREAD_POOL_SIZE = 3;

	private static Logger LOG = Logger.getLogger(Test.class);

	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	
	private static Test t = new Test();

	public static void main(String[] args) {
		try {
			List<Future<Result>> results = test();
			for (Future<Result> f : results) {
				Result r = f.get();
				LOG.debug(r.toString());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	private static List<Future<Result>> test() throws InterruptedException,
			ExecutionException {
		ExecutorService executor = Executors.newWorkStealingPool(THREAD_POOL_SIZE);

		Callable<Result> c0 = callable("task0", 1);
		Callable<Result> c1 = callable("task1", 1);
		Callable<Result> c2 = callable("task2", 1);
		
		List<Callable<Result>> lCallableList = new ArrayList<Callable<Result>>();
		lCallableList.add(c0);
		lCallableList.add(c1);
		lCallableList.add(c2);

		List<Callable<Result>> callables = new ArrayList<Callable<Result>>();

		for (int i = 0; i < TASK_RUN_ITERATION; i++) {
			callables.add(lCallableList.get(i%3));
		}

		List<Future<Result>> results = executor.invokeAll(callables);
		executor.shutdown();
		return results;

	}

	private static void logResult(String taskName, long executeSeconds,
			String startAt) {
		LOG.debug(taskName + " execute " + executeSeconds
				+ " milliseconds, Start at " + startAt);
	}

	private static Callable<Result> callable(String taskName, long sleepSeconds) {
		return () -> {
			long startTime = System.currentTimeMillis();
			String startAt = sdf.format(Calendar.getInstance().getTime());

			// execute something

			TimeUnit.SECONDS.sleep(sleepSeconds);

			long endTime = System.currentTimeMillis();

			long executeSeconds = (endTime - startTime);

			logResult(taskName, executeSeconds, startAt);
			
			Result r = t.new Result(taskName, executeSeconds, startAt);

			return r;
		};
	}

	private class Result {
		private String strTaskName;
		private long lExecuteSeconds;
		private String strStartAt;
		
		public Result(String strTaskName, long lExecuteSeconds,
				String strStartAt) {
			super();
			this.strStartAt = strStartAt;
			this.lExecuteSeconds = lExecuteSeconds;
			this.strTaskName = strTaskName;
		}
		
		public String toString() {
			return strTaskName + " execute " + lExecuteSeconds
					+ " milliseconds, Start at " + strStartAt;
		}
	}
}
