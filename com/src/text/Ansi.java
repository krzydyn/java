package text;

public class Ansi {
	public static class Code {
		public final static char NUL = (char)0x0;
		public final static char STX = (char)0x1; // Start Transmission Block
		public final static char SOT = (char)0x2; // Start Of Text
		public final static char ETX = (char)0x3; // End Transmission Block
		public final static char EOT = (char)0x4; // End Of Text
		public final static char ENQ = (char)0x5; // Enquire ("I am ready")
		public final static char ACK = (char)0x6; // Acknowledge
		public final static char DEL = (char)0x7;
		public final static char BS = (char)0x8;
		public final static char HT = (char)0x9;
		public final static char LF = (char)0xa;
		public final static char CR = (char)0xd;

		public final static char NAK = (char)0x15; // Not Acknowledge
		public final static char ESC = (char)0x1b;
	}

	public final static String CSI_C0 = "\u001b[";
	public final static String CSI_C1 = "\u009b"; //only on 8bit terminal
	public final static String CSI = CSI_C0;

	public final static String SCR_MODE19 = CSI + "=19h";// 320x200 256colors
	public final static String SCR_RESET19 = CSI + "=19l";

	public final static String SGR_RESET = CSI + "0m";
	public final static String SGR_BOLD = CSI + "1m";
	public final static String SGR_ITALIC = CSI + "3m";
	public final static String SGR_BOLDITALIC = CSI + "2;3m";
	public final static String SGR_NORMAL = CSI + "22;29;39m";

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
