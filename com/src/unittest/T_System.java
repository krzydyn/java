/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package unittest;

import java.io.File;
import java.io.PrintStream;
import snd.Sound;
import sys.Log;
import sys.UnitTest;
import text.Ansi;

public class T_System extends UnitTest {

	public static void sound() throws Exception {
		Sound.Note notes[] = {
				Sound.Note.C4,Sound.Note.C4h,Sound.Note.D4,Sound.Note.D4h,Sound.Note.E4,
				Sound.Note.F4,Sound.Note.F4h,Sound.Note.G4,Sound.Note.G4h,};
		long t,tm=700;

		for (int i=0; i < notes.length; ++i) {
			t=System.currentTimeMillis();
			Sound.play(notes[i], tm);
			t=System.currentTimeMillis()-t;
			check(String.format("time=%d < %dms",t,tm),t>tm);
		}
	}

	public static void listPackages() throws Exception {
		System.out.println(UnitTest.getClasses("unittest"));
	}

	public static void listDirs() throws Exception {
		System.out.println(sys.Env.getDirs(new File("/home"), 2));
	}


	public static void ansiSequences() throws Exception {
		PrintStream p = System.out;

		for (int i=0; i < 16; ++i)
			p.printf("Color %d: %s%d;%dmsample text%s\n", i, Ansi.CSI, i/8, 30+i%8, Ansi.SGR_RESET);
	}

	public static void loggerColors() {
		Log.error("Error");
		Log.warn("Warning");
		Log.debug("Debug");
		Log.trace("Trace");
		Log.info("Info");
		Log.notice("Notice");
	}
}
