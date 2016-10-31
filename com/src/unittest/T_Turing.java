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

package unittest;

import algebra.TuringMach;
import sys.Log;
import sys.UnitTest;

public class T_Turing extends UnitTest {
	static void duplicateSymbols() {
		TuringMach tm = new TuringMach();
		tm.loadMachine(
				//cs, i, o, m, ns
				 "q0, #, #, R, q0\n" //q0 = empty, keep going right
				+"q0, 1, #, R, q1\n" //q1 = '1' is found, keep going right
				+"q1, #, #, R, q2\n" //q2 = end found
				+"q1, 1, 1, R, q1\n"
				+"q2, #, 1, R, q3\n" //first '1'
				+"q2, 1, 1, R, q2\n" //keep going to next empty
				+"q3, #, 1, L, q4\n" //second '1'
				+"q3, 1, 1, R, T\n"

				+"q4, #, #, L, q5\n" //go back
				+"q4, 1, 1, L, q4\n"

				+"q5, #, #, R, T\n"
				+"q5, 1, 1, L, q6\n"

				+"q6, #, #, R, q0\n"
				+"q6, 1, 1, L, q6\n"
				);

		Log.debug("r: %s", tm.run("q0", "#,1,1,1"));
		check(tm.run("q0", "#,1,1,1"), "1,1,1,1,1,1");
	}
}
