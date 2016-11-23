package puzzle;

import puzzles.Pentomino;

public class PentoSolver {
	public static void main(String[] args) {
		//Pentomino.printAllFigs();
		Pentomino p = new Pentomino(10, 6);
		p.solve();
	}
}
