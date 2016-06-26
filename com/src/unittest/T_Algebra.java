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

import puzzles.Sudoku;
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
		check(n == 6, "should be 6 permutations");
	}

	static void blackjack() {
		String[] test = {"", "AA", "XA", "9AA", "8AA", "8AAA"};
		int [] res    = {0, 12, 21, 21, 20, 21};
		for (int i=0; i < test.length; ++i) {
			int r=Maths.blackjackPoints(test[i]);
			check(r==res[i], String.format("%s=%d, should be %d", test[i], r, res[i]));
		}
	}
	
	static void sudoku() {
		String[] examples = {
			"..8....1.|27.84..6.|..6...4.8|...4.6.7.|.8..2..5.|.4.1.3...|7.4...6..|.6..35.42|.2....9..",
			".........|8...2...5|.....624.|.38..71..|2.4...3.9|..74..52.|.725.....|6...8...1|.........",
			"6.214..7.8.......4.4..8..1....85....1..2.4......96.....8..2..6.7.......92.673..4.",
			// hard
			".6.9....35....4.2.....8.4..8......5...3...7...9......1..1.5.....7.3....99....2.4.",
			"1....7.9..3..2...8..96..5....53..9...1..8...26....4...3......1..4......7..7...3..",
			"3217.4...64..9...7.............459....51874....496.............2...7..19...6.9582",
		};
		Sudoku s=new Sudoku(3);
		for (String ex : examples) {
			s.parse(ex);
			s.print();
			for (int i=0; s.solve(); ++i) {
				System.out.printf("Solution %d:\n",i+1);
				s.print();
				if (i==2) break;
			}
		}
	}
	/*static void sudokuGen() {
		Sudoku s=new Sudoku(3);
		List<Integer> box = new ArrayList<Integer>();
		int i;
		for (i=0; i < 9; ++i) box.add(i+1);
		while (Permutate.nextPermutation(box)) {
			s.clear();
			for (i=0; i < 9; ++i) {
				s.set(i%9, i/9, i+1);
			}
			int sol;
			for (sol=0; s.solve(); ++sol) {
				//System.out.printf("%d: ",sol+1);
				//s.printshort();
			}
			System.out.printf("Solutions %d ",sol+1);
		}
	}*/
}
