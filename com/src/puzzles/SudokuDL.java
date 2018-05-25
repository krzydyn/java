package puzzles;
import java.util.*;

public class SudokuDL {

}

//ecole polytechnique 2011, c.durr + s.oudot
//english comments 2012

/* The class ExactCover implements the backtracking algorithm called dancing links.
We are given a universe of elements 0,1,...,universe-1.
We have sets over these elements.
The ExactCover problem consists in selecing a collection of these sets that partition the universe.
The algorithm is exponential in the worst case.

It is illustrated on the example of solving a 16*16 Sudoku.

http://www.spoj.pl/problems/SUDOKU/
*/



class Cell {
 Cell L,R,U,D;  // doubling chaining, vertical D,U, horizontal L,R
 int S,C;	  // two informations, for regular cells they represent row and column indices

 // creates acell and inserts in a vertical and a horizontal list.
 Cell(Cell left, Cell below, int _S, int _C) {
	S=_S;
	C=_C;
	if (left==null) {L=R=this;}
	else {
		L   = left.L;
		R   = left;
		L.R = this;
		R.L = this;
	}
	if (below==null) {U=D=this;}
	else {
		U   = below.U;
		D   = below;
		U.D = this;
		D.U = this;
	}
 }

 // -------------------------------------- cell manipulations

 void hideVert() {
	U.D = D;
	D.U = U;
 }

 void unhideVert() {
	D.U = this;
	U.D = this;
 }

 void hideHorz() {
	R.L = L;
	L.R = R;
 }

 void unhideHorz() {
	L.R = this;
	R.L = this;
 }

};

/* The constructor of this class takes as argument the size of the universe.
Then for every set, its elements are added with the method add.
The method solve, solves the problem (as you would have guessed
from the name), and if it returns true, the solution is on the
stack solution.
*/
class ExactCover {

 // -------------------------------------- attributes

 Stack<Integer> solution;

 Cell col[]; // maps column index to column header
 Cell h;	// main header

 // -------------------------------------- constructor
 ExactCover(int universe) {
	h   = new Cell(null, null, 0,0);
	col = new Cell[universe];

	for (int c=0; c<universe; c++)
		col[c] = new Cell(h, null, 0, c);

	solution = new Stack<Integer>();
 }

 int  last_row  = -1;
 Cell last_cell = null;	// first cell in row

 /* The calls to add have to be done grouped by r.
 */
 void add(int r, int c) {
	if (r!=last_row)
		last_cell = null;
	last_cell = new Cell(last_cell, col[c], r, c);
	col[c].S++;	 // update counter in column header
	last_row = r;	// remember for next call
 }



 // -------------------------------------- row/column manipulation
 void cover(int ic) {				// ic is the index of a column
	Cell c=col[ic];
	c.hideHorz();
	for (Cell i=c.D; i!=c; i=i.D)
		for (Cell j=i.R; j!=i; j=j.R) { //   loop over the row i
		j.hideVert();
		col[j.C].S--;			//   maintain counter
		}
 }

 void uncover(int ic) {
	Cell c=col[ic];
	for (Cell i=c.U; i!=c; i=i.U)
		for (Cell j=i.L; j!=i; j=j.L) { //   loop over row i
		col[j.C].S++;			//   maintain counter
		j.unhideVert();
		}
	c.unhideHorz();
 }

 // -------------------------------------- solve the problem

 boolean solve() {
	if (h.R==h) // empty matrix? We found a solution!
		return true;

	// select column c of smallest size
	int  s = Integer.MAX_VALUE;
	Cell c=null;
	for (Cell j=h.R; j!=h; j=j.R)
		if (j.S<s) {
			c = j;
			s = j.S;
		}

	cover(c.C);
	// try every row
	for (Cell r=c.D; r!=c; r=r.D) {
		solution.push(r.S);		  // add r to the solution
		for (Cell j=r.R; j!=r; j=j.R)
			cover(j.C);
		if (solve())
			return true;
		for (Cell j=r.L; j!=r; j=j.L)  // undo everything
			uncover(j.C);
		solution.pop();
	}
	uncover(c.C);

	return false;
 }


 // is used to test your code on a small example
 public static void main2(String args[]) {
	String test[] = {"0010110", "1001001", "0110010", "1001000", "0100001", "0001101"};
	int r = test.length, c = test[0].length();
	ExactCover e = new ExactCover(c);
	for (int i=0; i<r; i++)
		for (int j=0; j<c; j++)
			if (test[i].charAt(j)=='1')
			  e.add(i,j);
	if (e.solve())
		for (int i: e.solution)
			System.out.println(i+": "+test[i]);
	else System.out.println("no solution");
 }
}

/* we reduce sudoku to exact cover
http://www.spoj.pl/problems/SUDOKU/
*/
class Main {

 /* an assignment is an integer a which codes a row, a column, a
  * block and a value. Assignments are the sets of the exact cover
  * instance.
  */
 static int row(int a) {return a/16/16;}
 static int col(int a) {return (a/16)%16;}
 static int blk(int a) {return (row(a)/4)*4 + col(a)/4;}
 static int val(int a) {return a%16;}

 /* a constraint is a couple, either row,column or row,value or
  * column,value or block,value. THe constraints form the universe
  * of the exactc cover instance
  */
 static int rc(int a)  {return		 row(a)*16+col(a);}
 static int rv(int a)  {return   16*16 + row(a)*16+val(a);}
 static int cv(int a)  {return 2*16*16 + col(a)*16+val(a);}
 static int bv(int a)  {return 3*16*16 + blk(a)*16+val(a);}

 public static void main(String args[]) {
	char[] M = new char[256];

	Scanner in = new Scanner(System.in);
	int testCases = in.nextInt();
	while (testCases-->0) {
		// --- reads the grid
		for (int i=0; i<16; i++) {
			String w = in.next();
			for (int j=0; j<16; j++) {
			  M[16*i+j] = w.charAt(j);
			}
		}

		// --- reduce to exact cover
		ExactCover e = new ExactCover(4*16*16);

		for (int a=0; a<16*16*16; a++) {
			e.add(a, rc(a));
			e.add(a, rv(a));
			e.add(a, cv(a));
			e.add(a, bv(a));
		}

		// encode initial assignments
		for (int p=0; p<16*16; p++)
			if (M[p] != '-') {
			  int a = 16*p + M[p]-'A';
			  e.cover(rc(a));
			  e.cover(rv(a));
			  e.cover(cv(a));
			  e.cover(bv(a));
		}

		if (!e.solve())
			System.err.println("** pas de solution");
		// --- extract the solution
		for (int a : e.solution)
		M[a/16] = (char)('A' + a%16);

		// --- show the solution
		for (int p=0; p<256; p++) {
			System.out.print(M[p]);
			if (p%16==15)
			  System.out.println();
		}
		if (testCases >0)
			System.out.println();
	}
 }
}
