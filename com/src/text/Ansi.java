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
		/** Space */			public final static char SP = (char)0x20;
	}

	private final static String[] CODENAME = {
		"NUL", "STX", "SOT", "ETX", "EOT", "ENQ", "ACK", "BEL",  "BS", "HT",  "LF",  "VT", "FF", "CR", "SO", "SI",
		"DLE", "DC1", "DC2", "DC3", "DC4", "NAK", "SYN", "ETB", "CAN", "EM", "SUB", "ESC", "FS", "GS", "RS", "US",
		"SP"
	};
	public final static String toString(int code) {
		if (code < 0) return String.format("<&%02X>", code);
		if (code < CODENAME.length) return String.format("<%s>", CODENAME[code]);
		if (code < 0x7f) return String.format("%c", code);
		return String.format("<&%02X>", code);
	}

	// Character Sequence Indicator
	public final static String CSI_C0 = Code.ESC+"[";
	public final static String CSI_C1 = "\u009b"; //only on 8bit terminal
	public final static String CSI = CSI_C0;

	// Single Shift Select of G2
	public final static String SS2_0 ="\u008e";
	public final static String SS2_1 = Code.ESC+"N";
	public final static String SS2 = SS2_0;

	// Single Shift Select of G3
	public final static String SS3_0 = Code.ESC+"O";
	public final static String SS3_1 ="\u008f";
	public final static String SS3 = SS3_0;

	// Operating System Command
	public final static String OSC_C0 = Code.ESC+"]";
	public final static String OSC_C1 = "\u009d"; //only on 8bit terminal
	public final static String OSC = CSI_C0;

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


	public final static String TRM_ICON_TITLE = OSC + "0";	// OSC 0;IconName BEL
	public final static String TRM_ICON = OSC + "1";		// OSC 1;IconName BEL
	public final static String TRM_TITLE = OSC + "2";		// OSC 2;Title BEL
	public final static String TRM_PROP = OSC + "3";		// OSC 2;Prop=Value BEL
	public final static String TRM_COLORDEF = OSC + "4";	// OSC 4;idx;r;g;b ? - Define RGB color

	//not commonly supported
	public final static String seqSetRGB(int r, int g, int b) {
		return String.format("%s38;2;%d;%d;%dm", CSI, r, g, b);
	}
	public final static String seqSetColor(int i) {
		return String.format("%s48;5;%dm", CSI, i);
	}
	public final static String seqDefRGB(int i, int r, int g, int b) {
		return String.format("%s4;%d;%x;%x;%x\\", OSC, i, r, g, b);
	}
}
