package text;
/**
 *
 * @author k.dynowski
 *
 * Sources:
 * https://en.wikipedia.org/wiki/ANSI_escape_code
 * http://ascii-table.com/ansi-escape-sequences.php
 * http://invisible-island.net/xterm/ctlseqs/ctlseqs.html
 */

public class Ansi {
	public static class Code {
		/** Null */				public final static char NUL = (char)0x0;
		/** Start of Header */	public final static char SOH = (char)0x1;
		/** Start of Text */	public final static char STX = (char)0x2;
		/** End of Text */		public final static char ETX = (char)0x3;
		/** End of Transmit */	public final static char EOT = (char)0x4;
		/** Enquire */			public final static char ENQ = (char)0x5;
		/** Acknowledge */		public final static char ACK = (char)0x6;
		/** Bell */				public final static char BEL = (char)0x7;
		/** Backspace */		public final static char BS = (char)0x8;
		/** Horizontal Tab */	public final static char HT = (char)0x9;
		/** Line Feed */		public final static char LF = (char)0xa;
		/** Vertical Tab */		public final static char VT = (char)0xb;
		/** Form Feed */		public final static char FF = (char)0xc;
		/** Carriage Return */	public final static char CR = (char)0xd;
		/** Shift Out */		public final static char SO = (char)0xe;
		/** Shift In */			public final static char SI = (char)0xf;
		/** Data Link Escape */	public final static char DLE = (char)0x10;
		/** Device Control 1 */	public final static char DC1 = (char)0x11;
		/** Device Control 2 */	public final static char DC2 = (char)0x12;
		/** Device Control 3 */	public final static char DC3 = (char)0x13;
		/** Device Control 4 */	public final static char DC4 = (char)0x14;
		/** Negative Ack */		public final static char NAK = (char)0x15;
		/** Synchronous idle */	public final static char SYN = (char)0x16;
		/** End Trans. Block */	public final static char ETB = (char)0x17;
		/** Cancel (last op)*/	public final static char CAN = (char)0x18;
		/** End of Medium */	public final static char EM = (char)0x19;
		/** Substitute */		public final static char SUB = (char)0x1a;
		/** Escape */ 			public final static char ESC = (char)0x1b;
		/** File Separator */	public final static char FS = (char)0x1c;
		/** Group Separator */	public final static char GS = (char)0x1d;
		/** Record Separator */	public final static char RS = (char)0x1e;
		/** Unit Separator */	public final static char US = (char)0x1f;
	}

	private final static String[] CODENAME = {
		"NUL", "STX", "SOT", "ETX", "EOT", "ENQ", "ACK", "BEL",  "BS", "HT",  "LF",  "VT", "FF", "CR", "SO", "SI",
		"DLE", "DC1", "DC2", "DC3", "DC4", "NAK", "SYN", "ETB", "CAN", "EM", "SUB", "ESC", "FS", "GS", "RS", "US"
	};
	public final static String codeName(int code) {
		if (code >= 0 && code < CODENAME.length) return String.format("<%s>", CODENAME[code]);
		if (Character.isAlphabetic(code)) return String.format("%c", code);
		return String.format("<%02X>", code);
	}

	// Character Sequence Information
	public final static String CSI_C0 = "\u001b[";
	public final static String CSI_C1 = "\u009b"; //only on 8bit terminal
	public final static String CSI = CSI_C0;

	public final static String CURSOR_UP = CSI + "A"; // CSI n A move cursor up n times
	public final static String CURSOR_DOWN = CSI + "B"; // CSI n A move cursor down n times
	public final static String CURSOR_FORW = CSI + "C"; // CSI n A move cursor forward n times
	public final static String CURSOR_BACK = CSI + "D"; // CSI n D move cursor backward n times
	public final static String CURSOR_NEXTLN = CSI + "E"; // CSI n E move cursor next line n times
	public final static String CURSOR_PREVLN = CSI + "F"; // CSI n F move cursor prev line n times
	public final static String CURSOR_ROW = CSI + "G"; // CSI n G move cursor to row n (n,1)
	public final static String CURSOR_POS = CSI + "H"; // CSI n;m H move cursor to position (n,m)

	public final static String ERASE_BELOW = CSI + "J"; // clear from cursor to end of screen
	public final static String ERASE_ABOVE = CSI + "1J"; // clear from cursor to beginning of screen
	public final static String ERASE_ALL = CSI + "2J"; // clear entire screen

	public final static String ERASE_LN = CSI + "K";
	public final static String SCROLL_UP = CSI + "S";
	public final static String SCROLL_DN = CSI + "T";

	public final static String SCR_MODE19 = CSI + "=19h";// 320x200 256colors
	public final static String SCR_RESET19 = CSI + "=19l";

	public final static String SGR_RESET = CSI + "m";
	public final static String SGR_BOLD = CSI + "1m"; //increased intensity
	public final static String SGR_FAINT = CSI + "2m";//decreased intensity
	public final static String SGR_ITALIC = CSI + "3m";
	public final static String SGR_BOLDITALIC = CSI + "1;3m";
	public final static String SGR_NORMAL = CSI + "22m";// Normal color and intensity

	public final static String SGR_BLACK = CSI + "0;30m";
	public final static String SGR_RED = CSI + "0;31m";
	public final static String SGR_GREEN = CSI + "0;32m";
	public final static String SGR_YELLOW = CSI + "0;33m";
	public final static String SGR_BLUE = CSI + "0;34m";
	public final static String SGR_MAGENTA = CSI + "0;35m";
	public final static String SGR_CYAN = CSI + "0;36m";
	public final static String SGR_GRAYLIGHT = CSI + "0;37m";
	public final static String SGR_GRAY = CSI + "1;30m";
	public final static String SGR_REDLIGHT = CSI + "1;31m";
	public final static String SGR_GREENLIGHT = CSI + "1;32m";
	public final static String SGR_LIGHTYELLOW = CSI + "1;33m";
	public final static String SGR_LIGHTBLUE = CSI + "1;34m";
	public final static String SGR_LIGHTMAGENTA = CSI + "1;35m";
	public final static String SGR_LIGHTCYAN = CSI + "1;36m";
	public final static String SGR_WHITE = CSI + "1;37m";

	//not commonly supported
	public final static String gen_RGB(int r, int g, int b) {
		return String.format("%s38;2;%d;%d;%dm", CSI, r, g, b);
	}
}
