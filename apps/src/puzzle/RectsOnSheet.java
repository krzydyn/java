package puzzle;

import puzzles.RectPack;

public class RectsOnSheet {
	public static void main(String[] args) {
		RectPack.Dim sheet = new RectPack.Dim(15,15);
		RectPack.Dim rect = new RectPack.Dim(4,3);

		int ssq = sheet.w*sheet.h;
		int rsq = rect.w*rect.h;
		RectPack rp = new RectPack(sheet);

		int n = rp.solve(rect);
		System.out.printf("Sheet: %d mm^2, %d rects = %d mm^2, lost: %d mm^2\n", ssq, n, n*rsq, ssq-n*rsq);

		n = rp.solve(rect.rotated());
		System.out.printf("Sheet: %d mm^2, %d rects = %d mm^2, lost: %d mm^2\n", ssq, n, n*rsq, ssq-n*rsq);
}
}
