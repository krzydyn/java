/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package gthreads;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GreenThreadsPool {

	List<GreenThread> threads=new LinkedList<GreenThread>();
	private final Object lock=new Object();

	void addThread(GreenThread t) {
		threads.add(t);
	}
	void tick() throws InterruptedException {
		if (threads.size()<2) return ;
		synchronized (lock) {
			lock.notify();
			lock.wait();
		}
	}

	static final Object vLock=new Object();
	static int value;
	public static void main(String[] args) {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		Runnable incr=new Runnable() {
			@Override
			public void run() {
				//synchronized (vLock) {
					++value;
				//}
			}
		};
		for (int i=0; i< 10000; ++i)
			executor.execute(incr);
		executor.shutdown();
		try {
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(value);
	}
}
