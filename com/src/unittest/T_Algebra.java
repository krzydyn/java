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

import java.util.ArrayList;
import java.util.List;

import puzzles.Expression;
import puzzles.RectPack;
import algebra.Maths;
import algebra.Permutate;
import sys.UnitTest;

public class T_Algebra extends UnitTest {
	static void permutation() {
		List<Integer> l = new ArrayList<Integer>();
		l.add(2);
		l.add(4);
		l.add(7);
		int n=0;
		do {
			++n;
			//Log.debug("%d: %s", n, l.toString());
		} while (Permutate.nextPermutation(l));
		check("should be 6 permutations", n == 6);

		l.clear(); n=0;
		for (int i=0; i<6; ++i) l.add(i);
		do {
			++n;
			//Log.debug("%d: %s", n, l.toString());
		} while (Permutate.nextPermutation(l));
		check("should be 720 permutations", n == 720);
	}

	static void blackjack() {
		String[] test = {"", "AA", "XA", "9AA", "8AA", "8AAA"};
		int [] res    = {0,   12,   21,    21,    20,     21};
		for (int i=0; i < test.length; ++i) {
			int r=Maths.blackjackPoints(test[i]);
			check(String.format("%s=%d, should be %d", test[i], r, res[i]), r==res[i]);
		}
	}

	static void expression() {
		Expression e = new Expression("9/3");
		long r=e.evaluate();
		check(String.format("9/3 = %d != 3",r), r==3);
		e = new Expression("1+(1+2)*2");
		r=e.evaluate();
		check(String.format("1+(1+2)*2 = %d != 7",r), r==7);
		e = new Expression("1=2");
		r=e.evaluate();
		check(String.format("1=2 = %d != 0",r), r==0);
	}

	static void rectinrect() {
		RectPack.Rect r1 = new RectPack.Rect(3, 7, new RectPack.Dim(3,4));
		RectPack.Rect r2 = new RectPack.Rect(0, 8, new RectPack.Dim(4,3));
		check("r1 . r2", r1.intersects(r2) == true);
		check("r2 . r1", r2.intersects(r1) == true);
	}
}
