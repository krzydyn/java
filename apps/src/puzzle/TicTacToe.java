package puzzle;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import puzzles.GameBoard;

public class TicTacToe {
	static class TBoard extends GameBoard.Char {
		public TBoard(int w, int h) {
			super(w, h);
		}
		@Override
		public void print() {
			for (int y=0; y < h; ++y) {
				for (int x=0; x < w; ++x) {
					char c=get(x,y);
					System.out.printf(" %c |", c!=' '?c:(char)('1'+y*w+x));
				}
				System.out.println("\n------------");
			}
		}
	}
	static class Player {
		char symbol;
		void setSymbol(char s) {symbol=s;}
		int nextMove(GameBoard.Char b) {return -1;}
	}
	static class HumanPlayer extends Player{
		Scanner sc;
		HumanPlayer(Scanner sc) {
			this.sc=sc;
		}
		@Override
		int nextMove(GameBoard.Char b) {
			List<Integer> l=getLegalMoves(b);
			if (l.size() == 0) return -1;
			while (true) {
				b.print();
				System.out.printf("Player %c move:",symbol);
				int i = sc.nextInt();
				if (i<1 || i>9) {
					System.out.printf("Select field number 1-%d\n",b.getN());
					continue;
				}
				--i;
				if (b.get(i) != ' ') {
					System.out.println("Field is not available");
					continue;
				}
				return i;
			}
		}
	}
	static class CompPlayer extends Player{
		@Override
		int nextMove(GameBoard.Char b) {
			List<Integer> l=getLegalMoves(b);
			if (l.size() == 0) return -1;
			if (l.size() == 9) return 0;

			// 1. check if can I win
			for (Integer i : l) {
				if (isWinnerMove(b, i, symbol))
					return i;
			}
			// 2. check if should I block opponent
			for (Integer i : l) {
				char oponent = symbol=='O'?'X':'O';
				if (isWinnerMove(b, i, oponent))
					return i;
			}
			// 3. use random
			if (b.get(4) == ' ') return 4;
			return l.get(0);
		}
	}
	TBoard board = new TBoard(3, 3);

	static List<Integer> getLegalMoves(GameBoard.Char b) {
		List<Integer> l = new ArrayList<>();
		for (int i=0; i < b.getN(); ++i) {
			if (b.get(i)==' ') l.add(i);
		}
		return l;
	}
	static boolean isWinnerMove(GameBoard.Char b, int i, char s) {
		if (b.get(i)!=' ') return false;
		char save=b.get(i);
		b.set(i, s);
		boolean r = isWinner(b, s);
		b.set(i, save);
		return r;
	}
	static boolean isWinner(GameBoard.Char b, char s) {
		boolean r;
		for (int y=0; y < b.h; ++y) {
			r=true;
			for (int x=0; x < b.w; ++x) {
				if (b.get(x, y)!=s) {r=false;break;}
			}
			if (r) return true;
		}
		for (int x=0; x < b.w; ++x) {
			r=true;
			for (int y=0; y < b.h; ++y) {
				if (b.get(x, y)!=s) {r=false;break;}
			}
			if (r) return true;
		}
		r=true;
		for (int x=0; x < b.w; ++x) {
			int y=x;
			if (b.get(x, y)!=s) {r=false;break;}
		}
		if (r) return true;
		r=true;
		for (int x=0; x < b.w; ++x) {
			int y=b.h-x-1;
			if (b.get(x, y)!=s) {r=false;break;}
		}
		return r;
	}

	TicTacToe() {}

	void play() {
		Scanner sc = new Scanner(System.in);
		for (int i=0; i < board.getN(); ++i)
			board.set(i, ' ');
		System.out.println("This is simple TicTacToe game (starts player O)");
		char humanSymbol=0;
		while (humanSymbol==0) {
			System.out.println("Choose your symbol (X or O):");
			String s=sc.next();
			if (s.length()!=1) {
				System.out.printf("Wrong symbol %s",s);
				continue;
			}
			char hs=s.toUpperCase().charAt(0);
			if (hs!='O' && hs!='X') {
				System.out.printf("Wrong symbol %s",s);
				continue;
			}
			humanSymbol=hs;
		}

		Player p1=new HumanPlayer(sc);
		Player p2=new CompPlayer();

		p1.setSymbol(humanSymbol);
		p2.setSymbol(humanSymbol=='O'?'X':'O');

		Player current = humanSymbol=='O' ? p1 : p2;
		while (true) {
			int i = current.nextMove(board);
			if (i<0) break;
			board.set(i, current.symbol);
			if (isWinner(board, current.symbol)) break;
			if (current == p1) current=p2;
			else current=p1;
		}
		current=null;
		if (isWinner(board, p1.symbol)) current=p1;
		else if (isWinner(board, p2.symbol)) current=p2;

		board.print();
		if (current==null) System.out.println("It is DRAW");
		else System.out.println("The winner is "+current.symbol);
	}

	public static void main(String[] args) {
		TicTacToe game = new TicTacToe();
		game.play();
	}
}
