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

package sys;

final public class XThread extends Thread {
	private boolean interrupted = false;
	public XThread(Runnable r) {
		super(r);
	}
	@Override
	public boolean isInterrupted() {
		if (!interrupted && super.isInterrupted()) interrupted = true;
		return interrupted;
	}
	@Override
	public void interrupt() { interrupted=true; super.interrupt(); }

	static public void sleep(long millis) {
		try {Thread.sleep(millis);}
		catch (InterruptedException e) { //InterruptedException clears interrupted flag of Thread
			Thread t = Thread.currentThread();
			t.interrupt(); // set the flag again
			if (t instanceof XThread) ((XThread)t).interrupted = true;
		}
	}
}
