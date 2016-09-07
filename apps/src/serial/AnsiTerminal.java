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

package serial;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import sys.Log;
import sys.Sound;
import sys.XThread;
import text.Ansi;
import text.Text;
import ui.MainPanel;

/**
 * Simple Terminal
 * <p>
 * input: swing events system -> sent to remote OutputStream<br>
 * output: JTextComponent <- responses from remote InputStream
 *
 * http://invisible-island.net/xterm/ctlseqs/ctlseqs.html
 *
 * @author k.dynowski
 *
 */
public class AnsiTerminal extends JPanel implements FocusListener,KeyListener {
	private static final long serialVersionUID = 0;

	final static Border focusedBorder = BorderFactory.createLineBorder(Color.GRAY, 3);
	final static Border unfocusedBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);

	final static Color[] colorTable = {
		Color.GRAY, Color.RED, Color.GREEN.darker(), Color.YELLOW, new Color(0x8080FF), Color.MAGENTA.darker(), Color.CYAN, Color.WHITE
	};

	private static final int MAX_IN_BUFFER=1024*1024;
	private static final int MAX_COL=120;
	private static final int MAX_ROW=60;

	private JTextComponent editor = new JTextPane();
	private JLabel title = new JLabel();
	private AttributeSet attrib = SimpleAttributeSet.EMPTY;
	private StringBuilder inputBuffer = new StringBuilder();
	private StringBuilder outputBuffer = new StringBuilder();
	private Point cpos = new Point(0, 0);
	private int linesInBuf=0;
	private boolean paused = false;
	private boolean escSeq = false;

	//to get focus component must satisfy: 1.visible, 2.enabled, 3. focusable
	public AnsiTerminal(String t, boolean editable) {
		super(new BorderLayout());
		setName(t);

		setBorder(unfocusedBorder);

		title.setText(t);

		editor.setFont(Font.decode(Font.MONOSPACED));
		editor.setEditable(editable);
		editor.setFocusable(true); // this allow selection of text
		editor.setBackground(Color.DARK_GRAY);
		editor.setForeground(Color.LIGHT_GRAY);
		editor.setCaretColor(Color.WHITE);
		editor.addFocusListener(this);
		editor.addKeyListener(this);

		//don't traversal
		Set<KeyStroke> emptyset = new HashSet<KeyStroke>();
		editor.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,emptyset);
		editor.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,emptyset);

		disableActions("caret-down", "caret-up", "caret-backward", "caret-forward");

		JPanel p = new JPanel(null);
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		p.add(title);
		p.add(Box.createHorizontalGlue());
		JButton b;
		b=new JButton("~");
		b.setFocusable(false);
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {sendLoop("~", 10000, 150);}
		});
		p.add(b);
		b=new JButton("C");
		b.setFocusable(false);
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {eraseAll();}
		});
		p.add(b);
		b=new JButton("P");
		b.setFocusable(false);
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {paused=!paused;}
		});
		p.add(b);

		add(p, BorderLayout.NORTH);
		add(MainPanel.createScrolledPanel(editor), BorderLayout.CENTER);

		editor.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			@Override
			public void mousePressed(MouseEvent e) {
			}
			@Override
			public void mouseExited(MouseEvent e) {
			}
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (ev.getButton() != MouseEvent.BUTTON2) return ;

				String s=editor.getSelectedText();
				if (s!=null) {
					cursorEnd();
					inputBuffer.append(s);
					return ;
				}

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				Transferable t = clipboard.getContents(clipboard);
				if (t==null) return ;
				if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					try {
						s = (String)(t.getTransferData(DataFlavor.stringFlavor));
						cursorEnd();
						inputBuffer.append(s);
					} catch (Exception e) {}
				}
			}
		});
	}

	private void disableActions(String ...names) {
		ActionMap m = editor.getActionMap();
		for (String n : names) {
	        Action a = m.get(n);
	        if (a != null) a.setEnabled(false);
		}
	}

	private void sendLoop(final String t, final long tm, final long step) {
		new Thread() {
			@Override
			public void run() {
				Log.debug("send %s", t);
				long stop = System.currentTimeMillis()+tm;
				while (stop > System.currentTimeMillis()) {
					inputBuffer.append(t);
					XThread.sleep(step);
				}
			}
		}.start();
	}

	@Override
	public void focusGained(FocusEvent e) {
		setBorder(focusedBorder);
		editor.getCaret().setVisible(true);
	}
	@Override
	public void focusLost(FocusEvent e) {
		setBorder(unfocusedBorder);
		editor.getCaret().setVisible(false);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		char c = e.getKeyChar();
		if (c == Ansi.Code.LF) {
			cursorEnd();
		}
		else if (c == Ansi.Code.HT) {
			Log.debug("HT key");
			inputBuffer.setLength(0);
		}

		Log.debug("input %s", Ansi.toString(c));
		inputBuffer.append(c);
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getModifiers() != 0) return ;

		int code = e.getKeyCode();

		if (code == KeyEvent.VK_UP) { //up-arrow
			Log.debug("Key UP");
			inputBuffer.append(Ansi.CSI+"A");
		}
		else if (code == KeyEvent.VK_DOWN) { //down-arrow
			Log.debug("Key DOWN");
			inputBuffer.append(Ansi.CSI+"B");
		}
		else if (code == KeyEvent.VK_RIGHT) { //right-arrow
			Log.debug("Key RIGHT");
			inputBuffer.append(Ansi.CSI+"C");
		}
		else if (code == KeyEvent.VK_LEFT) { //left-arrow
			Log.debug("Key LEFT");
			inputBuffer.append(Ansi.CSI+"D");
		}
		else if (code == KeyEvent.VK_HOME) {
			Log.debug("Key HOME");
			inputBuffer.append(Ansi.CSI+"H");
		}
		else if (code == KeyEvent.VK_END) {
			Log.debug("Key END");
			inputBuffer.append(Ansi.CSI+"F");
		}
		else if (code == KeyEvent.VK_DELETE) {
			Log.debug("Key DEL");
			inputBuffer.append(Ansi.CSI+"3~");
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {}

	private void flushOutput() {
		if (outputBuffer.length() == 0) return ;
		int p0 = editor.getCaretPosition();
		try {
			Document doc = editor.getDocument();
			if (p0 < doc.getLength()) {
				int l = Math.min(doc.getLength() - p0, outputBuffer.length());
				Log.debug("Replace @ %d %s with %s", p0, Text.vis(doc.getText(p0, l)), Text.vis(outputBuffer));
				doc.remove(p0, l);
			}
			else {
				Log.debug("Append: %s", Text.vis(outputBuffer));
			}
			doc.insertString(p0, outputBuffer.toString(), attrib);
			editor.setCaretPosition(p0+outputBuffer.length());
			outputBuffer.setLength(0);
			if (doc.getLength() > MAX_IN_BUFFER) {
				doc.remove(0,3*MAX_IN_BUFFER/4);
			}
		} catch (BadLocationException e) {Log.error(e.toString());}
	}

	public void clearInput() {
		inputBuffer.setLength(0);
		escSeq = false;
	}
	public int getInputBuffer(byte[] b) {
		if (inputBuffer.length() == 0) return 0;

		synchronized (inputBuffer) {
			//Log.debug("Input: %s", inputBuffer.toString());
			byte[] bi = inputBuffer.toString().getBytes();
			int l = Math.min(b.length, bi.length);
			for (int i = 0; i < l; ++i) b[i] = bi[i];
			inputBuffer.delete(0, l);
			return l;
		}
	}

	private void handleEscSeq(String seq) {
		if (seq.charAt(0) != Ansi.Code.ESC) {
			throw new RuntimeException("No Escape mark");
		}
		boolean done=true;
		if (seq.startsWith(Ansi.CSI)) {
			if (seq.equals(Ansi.ERASE_ALL)) eraseAll();
			else if (seq.equals(Ansi.ERASE_BELOW)) eraseLineBelow();
			else if (seq.equals(Ansi.ERASE_ABOVE)) eraseLineAbove();
			else if (seq.equals(Ansi.ERASE_INLINE)) eraseLineRight();
			else if (seq.equals(Ansi.CURSOR_POS)) cursorHome();
			else if (seq.equals(Ansi.SGR_RESET)) {
				attrib = SimpleAttributeSet.EMPTY;
			}
			else if (seq.endsWith("@")) {
				int n = Integer.parseInt(seq.substring(Ansi.CSI.length(), seq.length()-1));
				insertBlank(n);
			}
			else if (seq.endsWith("C")) {
				int n = Integer.parseInt(seq.substring(Ansi.CSI.length(), seq.length()-1));
				cursorMove(n);
			}
			else if (seq.endsWith("D")) {
				int n = Integer.parseInt(seq.substring(Ansi.CSI.length(), seq.length()-1));
				cursorMove(-n);
			}
			else if (seq.endsWith("H")) { //set cursor position row;col
				int x=-1,y=-1;
				String code = seq.substring(Ansi.CSI.length(), seq.length()-1);
				for (int j,i=0; i < code.length(); i=j+1) {
					j=code.indexOf(';', i);
					if (j<0) j=code.length();
					try {
						x=Integer.parseInt(code.substring(i, j));
						if (y < 0) {y=x; x=-1;}
					} catch (Exception e) {
						Log.error("can't parse seq: '%s'", Text.vis(seq));
					}
				}
				cursorLocate(x, y);
			}
			else if (seq.endsWith("K")) {
				int n = Integer.parseInt(seq.substring(Ansi.CSI.length(), seq.length()-1));
				if (n == 0) eraseLineRight();
				else if (n == 1) eraseLineLeft();
				else if (n == 2) eraseAll();
				else done=false;
			}
			else if (seq.endsWith("P")) {
				String code = seq.substring(Ansi.CSI.length(), seq.length()-1);
				int x=0;
				try {
					x=Integer.parseInt(code);
					eraseLeft(x);
				}
				catch (Exception e) {}
			}
			else if (seq.endsWith("h")) {
				if (seq.equals(Ansi.CSI + "?1049")) {
					eraseAll();
				}
				else done=false;
			}
			else if (seq.endsWith("m")) {
				StyleContext sc = StyleContext.getDefaultStyleContext();
				String code = seq.substring(Ansi.CSI.length(), seq.length()-1);
				int regIntens=0;
				for (int j,i=0; i < code.length(); i=j+1) {
					j=code.indexOf(';', i);
					if (j<0) j=code.length();
					if (i==j) continue;
					int n = 0;
					try {
						n=Integer.parseInt(code.substring(i, j));
					} catch (Exception e) {
						Log.error("can't parse seq: '%s'", Text.vis(seq));
					}

					if (n==0) { //normal
						attrib = SimpleAttributeSet.EMPTY;
						regIntens=0;
					}
					else if (n==1) { //bold = intense
						regIntens=1;
					}
					else if (n==2) {  //italic
					}
					else if (n==7) {  //inverse
					}
					else if (n>=30 && n<38) { //text color
						Color c = colorTable[n-30];
						if (regIntens == 0) c=c.darker();
						attrib = sc.addAttribute(attrib, StyleConstants.Foreground, c);
					}
					else if (n>=40 && n<48) { //background color
						Color c = colorTable[n-40];
						if (regIntens == 0) c=c.darker();
						attrib = sc.addAttribute(attrib, StyleConstants.Background, c);
					}
					else done=false;
				}
			}
			/*else if (seq.endsWith("n")) {
				if (seq.equals(Ansi.CSI + "5n")) {
					inputBuffer.append(Ansi.CSI + "0n");
				}
				else if (seq.equals(Ansi.CSI + "6n")) {
					inputBuffer.append(Ansi.CSI + String.format("%d;%dR", cpos.x, cpos.y));
				}
			}*/
			else done=false;
		}
		else done=false;

		if (!done) Log.warn("%s: seq %s is not handled", getName(), Text.vis(seq));
		else Log.notice("%s: seq %s [OK]", getName(), Text.vis(seq));
	}

	public void append(char c) {
		if (paused && !escSeq) {
			return ;
		}
		if (escSeq) {
			outputBuffer.append(c);
			if (outputBuffer.length() < 3) return ;
			String  s0 = outputBuffer.substring(0, 2);
			if (s0.equals(Ansi.CSI)) {
				if (Character.isLetter(c) || c=='@') {escSeq=false;c=0;}
				else if ((c=='['||c==']'||c=='~') && outputBuffer.length() > 2) {
					escSeq=false;
				}
			}
			else if (s0.equals(Ansi.OSC)) {
				if (c == Ansi.Code.BEL) {escSeq=false;c=0;}
			}

			if (!escSeq) {
				String seq = outputBuffer.toString();
				outputBuffer.setLength(0);
				handleEscSeq(seq);
				if (c!=0) outputBuffer.append(c);
			}
			else if (c < 0x20) {
				escSeq=false;
				outputBuffer.setLength(0);
			}
		}
		else if (c < 0x20) {
			//if (c != Ansi.Code.LF && c != Ansi.Code.CR && c != Ansi.Code.ESC && c != Ansi.Code.ENQ)
			//	Log.debug("ansi %s", Ansi.toString(c));

			if (c == Ansi.Code.CR) {
				flushOutput();
				cursorLineBegin();
				attrib = SimpleAttributeSet.EMPTY;
			}
			else if (c == Ansi.Code.BEL) {
				beep();
			}
			else if (c == Ansi.Code.HT) {
				outputBuffer.append(c);
			}
			else if (c == Ansi.Code.VT || c == Ansi.Code.LF) {
				cursorEnd(); //go end of line
				outputBuffer.append('\n');
				flushOutput();
				++linesInBuf;
			}
			else if (c == Ansi.Code.BS) {
				flushOutput();
				cursorMove(-1);
			}
			else if (c == Ansi.Code.ESC) {
				flushOutput();
				outputBuffer.append(c);
				escSeq=true;
			}
			else {
				Log.debug("%s: Ignore %s", getName(), Ansi.toString(c));
			}
		}
		else {
			outputBuffer.append(c);
		}

	}
	public void write(byte[] b, int off, int len) {
		for (int i=0; i<len; ++i) {
			append((char)(b[off+i]&0xff));
		}
		if (!escSeq) flushOutput();
	}
	public void write(CharSequence s) {
		for (int i=0; i<s.length(); ++i) {
			append(s.charAt(i));
		}
		if (!escSeq) flushOutput();
	}

	public void setTitle(String t) {
		if (t==null || t.isEmpty()) title.setText(getName());
		else title.setText(String.format("[%s] %s",getName(),t));
	}
	public void beep() {
		try { Sound.dong(); } catch (Exception e) {}
	}
	public void cursorLocate(int x, int y) {
		Log.debug(1,"setSursor %s", cpos.toString());
		if (x<0) x=0;
		else if (x > MAX_COL) x=MAX_COL;
		if (y<=0) y=1;
		else if (y > MAX_ROW) x=MAX_ROW;
		cpos.x=x; cpos.y=y;
	}
	public void cursorHome() {
		Log.debug("cursor: home");
		editor.setCaretPosition(0);
		cursorLocate(0, 0);
	}
	public void cursorEnd() {
		Log.debug(1,"cursor: end");
		Document doc = editor.getDocument();
		int p0 = doc.getLength();
		editor.setCaretPosition(p0);
	}
	public void cursorMove(int n) {
		Log.debug(1,"cursor: moverel %d",n);
		int p0 = editor.getCaretPosition();
		if (p0+n > 0 && p0+n < editor.getDocument().getLength())
			editor.setCaretPosition(p0+n);
	}
	public void cursorLineBegin() {
		Log.debug(1,"cursor: line_begin");
		Document doc = editor.getDocument();
		int p, p0 = editor.getCaretPosition();
		for (p=p0; p0 > 0; --p0) {
			try {
				if (doc.getText(p0-1, 1).equals("\n")) break;
			} catch (BadLocationException e) {Log.error(e.toString());}
		}

		if (p != p0) {
			editor.setCaretPosition(p0);
		}
	}
	public void insertBlank(int n) {
		Log.debug(1,"buffer: blank %d",n);
		Document doc = editor.getDocument();
		int p0 = editor.getCaretPosition();
		try {
			doc.insertString(p0, Text.repeat(" ", n), attrib);
		} catch (BadLocationException e) {}
		editor.setCaretPosition(p0);
	}
	public void eraseAll() {
		Log.debug("buffer: eraseAll");
		Document doc = editor.getDocument();
		int p0=0, p = doc.getLength();
		if (p != p0) {
			try {
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {Log.error(e.toString());}
			editor.setCaretPosition(p0);
		}
		linesInBuf=0;
		cursorLocate(0, 0);
	}
	public void eraseLineAbove() {
		Log.debug("buffer: eraseLineAbove");
		Document doc = editor.getDocument();
		int p, p0 = editor.getCaretPosition();
		p = 0;
		if (p != p0) {
			try {
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {Log.error(e.toString());}
			editor.setCaretPosition(p0);
		}
	}
	public void eraseLineBelow() {
		Document doc = editor.getDocument();
		int p, p0 = editor.getCaretPosition();
		p=doc.getLength();
		if (p != p0) {
			try {
				Log.debug("buffer: eraseLineBelow '%s'",doc.getText(p0, p-p0));
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {Log.error(e.toString());}
			editor.setCaretPosition(p0);
		}
	}
	public void eraseLeft(int n) {
		Log.debug(1,"buffer: eraseLeft");
		Document doc = editor.getDocument();
		int p, p0 = editor.getCaretPosition();
		p=p0;
		if (p0 > n) p0-=n;
		else p0=0;

		if (p != p0) {
			try {
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {Log.error(e.toString());}
			editor.setCaretPosition(p0);
		}
	}
	public void eraseLineLeft() {
		Log.debug("buffer: eraseLineLeft");
		Document doc = editor.getDocument();
		int p, p0 = editor.getCaretPosition();
		for (p=p0; p0 > 0; --p0) {
			try {
				if (doc.getText(p0-1, 1).equals("\n")) break;
			} catch (BadLocationException e) {Log.error(e.toString());}
		}

		if (p != p0) {
			try {
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {Log.error(e.toString());}
			editor.setCaretPosition(p0);
		}
	}
	public void eraseLineRight() {
		Log.debug("buffer: eraseLineRight");
		Document doc = editor.getDocument();
		int p,p0 = editor.getCaretPosition();
		for (p=p0; p < doc.getLength(); ++p) {
			try {
				if (doc.getText(p, 1).equals("\n")) break;
			} catch (BadLocationException e) {Log.error(e.toString());}
		}

		if (p != p0) {
			try {
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {Log.error(e.toString());}
			editor.setCaretPosition(p0);
		}
	}
}
