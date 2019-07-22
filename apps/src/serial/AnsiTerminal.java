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
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import sys.Env;
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
 * TODO
 *   https://github.com/nyholku/purejavacomm/
 *   https://github.com/JetBrains/pty4j
 *   https://github.com/jawi/JPty
 *
 * @author k.dynowski
 *
 */
public class AnsiTerminal extends JPanel implements FocusListener,KeyListener {
	private static final long serialVersionUID = 0;

	private static class Peer {
		private InputStream is;
		private OutputStream os;
		public Peer(InputStream is, OutputStream os) { this.is = is; this.os = os; }
		public int read(byte[] b, int off, int len) throws IOException {
			//if (is.available() == 0) return 0;
			return is.read(b, off, len);
		}
		public void write(byte[] b, int off, int len) throws IOException { os.write(b, off, len); }
	}

	final static Font font = new Font(Font.MONOSPACED, Font.PLAIN, 15);
	private static int charWidth;
	final static Border focusedBorder = BorderFactory.createLineBorder(Color.GRAY, 3);
	final static Border unfocusedBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);

	final static Color[] colorTable = {
		Color.DARK_GRAY, new Color(0xff4040), Color.GREEN.darker(), Color.YELLOW, new Color(0x8080FF), Color.MAGENTA.darker(), Color.CYAN, Color.WHITE
	};

	private static final int MAX_IN_BUFFER=1024*1024;
	private static final int MAX_COL=120;
	private static final int MAX_ROW=60;

	private Peer peer;
	private final JTextComponent editor = new JTextPane();
	private final JLabel title = new JLabel();
	private final SimpleAttributeSet attrib = new SimpleAttributeSet();
	private final StringBuilder inputBuffer = new StringBuilder();
	private final StringBuilder outputBuffer = new StringBuilder();
	private final Point cpos = new Point(0, 0);
	private int absCurPos=0;
	private boolean escSeq = false;
	private boolean sendingTilde = false;
	private boolean running;

	public AnsiTerminal(String t) {
		this(t, null, null);
	}
	//to get focus component must satisfy: 1.visible, 2.enabled, 3. focusable
	public AnsiTerminal(String t, InputStream is, OutputStream os) {
		super(new BorderLayout());
		setName(t);

		setBorder(unfocusedBorder);

		title.setText(t);
		FontMetrics mtr = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB).getGraphics().getFontMetrics(font);
		charWidth = mtr.charWidth('a'); mtr=null;
		editor.setFont(font);
		editor.setEditable(false);
		editor.setFocusable(true); // this allow selection of text
		editor.setBackground(Color.DARK_GRAY);
		editor.setForeground(Color.LIGHT_GRAY);
		editor.setCaretColor(Color.WHITE);
		editor.addFocusListener(this);
		editor.addKeyListener(this);
		Document doc = editor.getDocument();
		if (doc instanceof PlainDocument)
			doc.putProperty(PlainDocument.tabSizeAttribute, 8);
		else if (doc instanceof StyledDocument) {
			final int TAB_PIXELS=charWidth*8;
			StyleContext sc = StyleContext.getDefaultStyleContext();
			AttributeSet paraSet = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.TabSet, new TabSet(null) {
				private static final long serialVersionUID = 1L;
				@Override
				public TabStop getTabAfter(float location) {
					int p =((int)Math.floor(location/TAB_PIXELS + 1))*TAB_PIXELS;
					return new TabStop(p);
				}
			});
			((DefaultStyledDocument)doc).setParagraphAttributes(0,Integer.MAX_VALUE,paraSet, false);
		}

		//disable traversal key bindings
		//Set<KeyStroke> emptyset = new HashSet<KeyStroke>();
		//editor.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,emptyset);
		//editor.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,emptyset);
		editor.setFocusTraversalKeysEnabled(false);
		disableActions("caret-down", "caret-up", "caret-backward", "caret-forward");

		JPanel p = new JPanel(null);
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		p.add(title);
		p.add(Box.createHorizontalGlue());
		JButton b;
		b=new JButton("~");
		b.setToolTipText("Generate '~' sequence");
		b.setFocusable(false);
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (sendingTilde) sendingTilde=false;
				else sendLoop("~", 150);
			}
		});
		p.add(b);
		b=new JButton("C");
		b.setFocusable(false);
		b.setToolTipText("Clear Screen");
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {eraseAll();}
		});
		p.add(b);

		add(p, BorderLayout.NORTH);
		final JScrollPane sp = MainPanel.createScrolledPanel(editor);
		//FIXME try to stop scrolling when scrollbar moved (not working)
		sp.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			BoundedRangeModel brm = sp.getVerticalScrollBar().getModel();
			boolean wasAtBottom = true;
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (!brm.getValueIsAdjusting()) {
					if (wasAtBottom) brm.setValue(brm.getMaximum());
				}
				else wasAtBottom = ((brm.getValue() + brm.getExtent()) == brm.getMaximum());

			}
		});
		add(sp, BorderLayout.CENTER);

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
						inputBuffer.append(s);
					} catch (Exception e) {}
				}
			}
		});

		if (is != null && os != null) {
			peer = new Peer(is, os);
			running = true;
			new Thread("write "+t) {
				@Override
				public void run() {
					writeLoop();
				}
			}.start();
			new Thread("read "+t) {
				@Override
				public void run() {
					readLoop();
				}
			}.start();
		}
	}

	private void disableActions(String ...names) {
		ActionMap m = editor.getActionMap();
		for (String n : names) {
	        Action a = m.get(n);
	        if (a != null) a.setEnabled(false);
		}
	}

	private void sendLoop(final String t, final long step) {
		new Thread() {
			@Override
			public void run() {
				sendingTilde=true;
				try {
					Log.debug("send %s", t);
					while (sendingTilde) {
						inputBuffer.append(t);
						XThread.sleep(step);
					}
				}
				catch (Throwable e) {Log.error(e);}
				finally {
					sendingTilde=false;
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
		}
		else if (c == Ansi.Code.HT) {
			inputBuffer.setLength(0);
		}
		else if (c < 0x20) {
			Log.debug("Control: "+Character.getName(c));
		}
		inputBuffer.append(c);
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getModifiersEx() != 0) {
			Log.debug("Key code = %x", e.getKeyCode());
			return ;
		}

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
		else if (code == KeyEvent.VK_PAGE_UP) {
			Log.debug("Key PAGEUP");
			inputBuffer.append(Ansi.CSI+"5~");
		}
		else if (code == KeyEvent.VK_PAGE_DOWN) {
			Log.debug("Key PAGEDOWN");
			inputBuffer.append(Ansi.CSI+"6~");
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
		else {
			Log.debug("Key code = %x", code);
			return ;
		}
		e.consume();
	}
	@Override
	public void keyReleased(KeyEvent e) {e.consume();}

	public void flushOutput() {
		if (outputBuffer.length() == 0) return ;
		if (escSeq) {
			Log.error("flush inside getting escseq");
			return ;
		}
		int p0 = absCurPos;
		try {
			Document doc = editor.getDocument();
			if (p0 < doc.getLength()) {
				int l = Math.min(doc.getLength() - p0, outputBuffer.length());
				Log.debug(1,"Replace @ %d %s with %s", p0, Text.vis(doc.getText(p0, l)), Text.vis(outputBuffer));
				doc.remove(p0, l);
			}
			else {
				Log.debug(1,"Append[%d]: %s", outputBuffer.length(), Text.vis(outputBuffer));
			}
			doc.insertString(p0, outputBuffer.toString(), attrib);
			absCurPos = p0+outputBuffer.length();
			outputBuffer.setLength(0);
			if (doc.getLength() > MAX_IN_BUFFER) {
				doc.remove(0,3*MAX_IN_BUFFER/4);
				absCurPos = doc.getLength();
			}
			editor.setCaretPosition(absCurPos);
		} catch (BadLocationException e) {Log.error(e.toString());}
	}

	public void clearInput() {
		inputBuffer.setLength(0);
		escSeq = false;
	}
	public int read(byte[] b) {
		if (inputBuffer.length() == 0) return 0;

		synchronized (inputBuffer) {
			byte[] bi = inputBuffer.toString().getBytes();
			int l = Math.min(b.length, bi.length);
			Log.debug("Input: %s", Text.vis(inputBuffer.substring(0,l)));
			for (int i = 0; i < l; ++i) b[i] = bi[i];
			inputBuffer.delete(0, l);
			return l;
		}
	}

	private void clearAttributes() {
		attrib.removeAttributes(attrib);
		Log.debug("clearAttributes (left %d)", attrib.getAttributeCount());
	}

	private void handleEscSeq(String seq) {
		if (seq.charAt(0) != Ansi.Code.ESC) {
			throw new RuntimeException("No Escape mark");
		}
		Log.notice("%s: seq %s", getName(), Text.vis(seq));
		boolean done=true;
		if (seq.startsWith(Ansi.CSI)) {
			if (seq.equals(Ansi.ERASE_ALL)) eraseAll();
			else if (seq.equals(Ansi.ERASE_BELOW)) eraseLineBelow();
			else if (seq.equals(Ansi.ERASE_ABOVE)) eraseLineAbove();
			else if (seq.equals(Ansi.ERASE_INLINE)) eraseLineRight();
			else if (seq.equals(Ansi.CURSOR_POS)) cursorHome();
			else if (seq.equals(Ansi.SGR_RESET)) clearAttributes();
			else if (seq.equals(Ansi.SGR_ITALIC)) {
			}
			else if (seq.equals(Ansi.DSR_STATUS)) {
				inputBuffer.append(String.format("%s%dn", Ansi.CSI, 0));
			}
			else if (seq.equals(Ansi.DSR_CURSOR) || seq.equals(Ansi.DSR_CURSOR_DEC)) {
				inputBuffer.append(String.format("%s%d;%dR", Ansi.CSI, cpos.y, cpos.x));
				//Log.debug("resp: %s",Text.vis(inputBuffer.toString()));
			}
			else if (seq.endsWith("@")) {
				int n = Integer.parseInt(seq.substring(Ansi.CSI.length(), seq.length()-1));
				insertBlank(n);
			}
			else if (seq.endsWith("C")) {
				int n = 0;
				try {
					Integer.parseInt(seq.substring(Ansi.CSI.length(), seq.length()-1));
				} catch (Exception e) {}
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
					eraseChars(x);
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
				String code = seq.substring(Ansi.CSI.length(), seq.length()-1);
				int regIntens=0;
				Log.debug("m-code %s", code);
				//process numbers separate with ';'
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
						clearAttributes();
						regIntens=0;
					}
					else if (n==1) { //increased intensity
						++regIntens;
						//StyleConstants.setItalic(attrib, true);
					}
					else if (n==2) { //decreased intensity
						--regIntens;
					}
					else if (n==3) { //italic
						StyleConstants.setItalic(attrib, true);
					}
					else if (n==7) {  //inverse
					}
					else if (n==22) { //normal color/intensity
						attrib.removeAttributes(attrib);
						regIntens=0;
					}
					else if (n==22) { //not italic
						StyleConstants.setItalic(attrib, false);
					}
					else if (n==24) { //not underline
						StyleConstants.setUnderline(attrib, false);
					}
					else if (n==27) { //not inverse
					}
					else if (n>=30 && n<38) { //text color
						Color c = colorTable[n-30];
						if (regIntens < 0) c=c.darker();
						else if (regIntens > 0) c=c.brighter();
						Log.debug("set color: 0x%06X",c.getRGB()&0xffffff);
						attrib.addAttribute(StyleConstants.Foreground, c);
					}
					else if (n>=40 && n<48) { //background color
						Color c = colorTable[n-40];
						if (regIntens == -1) c=c.darker();
						if (regIntens == 1) c=c.brighter();
						attrib.addAttribute(StyleConstants.Background, c);
					}
					else done=false;
				}
			}
			else done=false;
		}
		else done=false;

		if (!done) Log.warn("%s: seq %s is not handled", getName(), Text.vis(seq));
	}

	public void append(char c) {
		if (escSeq) {
			if (c >= 0x20) {
				outputBuffer.append(c);
				if (outputBuffer.length() < 3) return ;

				String s0 = outputBuffer.substring(0, 2);
				if (s0.equals(Ansi.CSI)) {
					if (Character.isLetter(c) || c=='@') {escSeq=false;}
					else if ((c=='['||c==']'||c=='~')) {escSeq=false;}
				}
				else if (s0.equals(Ansi.OSC)) {
					if (c == Ansi.Code.BEL) {escSeq=false;}
				}
				c=0;
			}
			else {
				Log.error("recv %x when in escseq mode", (int)c);
				outputBuffer.setLength(0);
				escSeq=false;
			}

			if (!escSeq && outputBuffer.length() > 0) {
				String seq = outputBuffer.toString();
				outputBuffer.setLength(0);
				handleEscSeq(seq);
				if (c!=0) {
					outputBuffer.append(c);
					if (c==Ansi.Code.ESC) escSeq=true;
				}
			}
		}
		else if (c < 0x20) {
			//if (c != Ansi.Code.LF && c != Ansi.Code.CR && c != Ansi.Code.ESC && c != Ansi.Code.ENQ)
			//	Log.debug("ansi %s", Ansi.toString(c));

			if (c == Ansi.Code.CR) {
				flushOutput();
				cursorLineBegin();
			}
			else if (c == Ansi.Code.BEL) {
				flushOutput();
				beep();
			}
			else if (c == Ansi.Code.HT) {
				outputBuffer.append(c);
			}
			else if (c == Ansi.Code.VT || c == Ansi.Code.LF) {
				outputBuffer.append('\n');
				cursorEnd();
				flushOutput();
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
	}
	public void write(CharSequence s) {
		for (int i=0; i<s.length(); ++i) {
			append(s.charAt(i));
		}
	}
	public void send(CharSequence s) {
		try {
			peer.os.write(s.toString().getBytes());
		} catch (IOException e) {
			Log.error(e);
		}
	}

	public void setTitle(String t) {
		if (t==null || t.isEmpty()) title.setText(getName());
		else title.setText(String.format("[%s] %s",getName(),t));
	}
	public void beep() {
		try { Sound.dong(); } catch (Exception e) {}
	}
	public void cursorLocate(int x, int y) {
		if (x<=1) x=1;
		else if (x > MAX_COL) x=MAX_COL;
		if (y<=0) y=1;
		else if (y > MAX_ROW) x=MAX_ROW;
		cpos.x=x; cpos.y=y;
		Log.debug(1,"setSursor row=%d col=%d", cpos.y,cpos.x);
	}
	public void cursorHome() {
		Log.debug(1,"cursor: home");
		//editor.setCaretPosition(0);
		absCurPos=0;
		cursorLocate(0, 0);
	}
	public void cursorEnd() {
		Log.debug(1,"cursor: end");
		Document doc = editor.getDocument();
		absCurPos=doc.getLength();
	}
	public void cursorMove(int n) {
		Log.debug(1,"cursor: moverel %d (cp=%d)",n,absCurPos);
		int p0 = absCurPos;
		if (p0+n >= 0 && p0+n < editor.getDocument().getLength()) {
			absCurPos = p0+n;
			editor.setCaretPosition(absCurPos);
		}
	}
	public void cursorLineBegin() {
		//Log.debug(1,"cursor: line_begin");
		Document doc = editor.getDocument();
		int p, p0 = absCurPos;
		for (p=p0; p0 > 0; --p0) {
			try {
				if (doc.getText(p0-1, 1).equals("\n")) break;
			} catch (BadLocationException e) {Log.error(e.toString());}
		}

		if (p != p0) {
			absCurPos=p0;
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
		clearAttributes();
		Document doc = editor.getDocument();
		int p0=0, p = doc.getLength();
		if (p != p0) {
			try {
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {Log.error(e.toString());}
			absCurPos=p0;
		}
		cursorLocate(0, 0);
	}
	public void eraseLineAbove() {
		Log.debug("buffer: eraseLineAbove");
		Document doc = editor.getDocument();
		int p, p0 = absCurPos;
		p = 0;
		if (p != p0) {
			try {
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {Log.error(e.toString());}
			absCurPos=p0;
		}
	}
	public void eraseLineBelow() {
		Document doc = editor.getDocument();
		int p, p0 = absCurPos;
		p=doc.getLength();
		if (p > p0) {
			try {
				String txrm=doc.getText(p0, p-p0);
				txrm = txrm.substring(txrm.length()-10, txrm.length());
				Log.debug(1,"eraseLineBelow '%s'",Text.vis(txrm));
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {Log.error(e.toString());}
			absCurPos=p0;
		}
	}
	public void eraseChars(int n) {
		Document doc = editor.getDocument();
		int p = doc.getLength(), p0 = absCurPos;
		if (p0+n < p) p=p0+n;
		if (p > p0) {
			try {
				Log.debug(1,"eraseChars '%s'",doc.getText(p0, p-p0));
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {Log.error(e.toString());}
			absCurPos=p0;
		}
	}
	public void eraseLineLeft() {
		Log.debug("buffer: eraseLineLeft");
		Document doc = editor.getDocument();
		int p, p0 = absCurPos;
		for (p=p0; p0 > 0; --p0) {
			try {
				if (doc.getText(p0-1, 1).equals("\n")) break;
			} catch (BadLocationException e) {Log.error(e.toString());}
		}

		if (p > p0) {
			try {
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {Log.error(e.toString());}
			absCurPos=p0;
		}
	}
	public void eraseLineRight() {
		Log.debug("buffer: eraseLineRight");
		Document doc = editor.getDocument();
		int p, p0 = absCurPos;
		for (p=p0; p < doc.getLength(); ++p) {
			try {
				if (doc.getText(p, 1).equals("\n")) break;
			} catch (BadLocationException e) {Log.error(e.toString());}
		}

		if (p > p0) {
			try {
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {Log.error(e.toString());}
			absCurPos=p0;
		}
	}

	private void readLoop() {
		byte[] buffer = new byte[256];
		int r;
		while (running) {
			try {
				Log.debug("read peer");
				r = peer.read(buffer, 0, buffer.length);
				Log.debug("from peer: %d %s", r, Text.vis(buffer, 0, r));
				if (r > 0) {
					write(buffer, 0, r);
				}
			} catch (IOException e) {
				break;
			}
			flushOutput();
		}
	}
	private void writeLoop() {
		byte[] buffer = new byte[256];
		int r;
		while (running) {
			try {
				r = read(buffer);
				if (r > 0) {
					Log.debug("from trm: %d %s", r, Text.vis(buffer, 0, r));
					peer.write(buffer, 0, r);
				}
			} catch (IOException e) {
				break;
			}
			if (r == 0) Env.sleep(1000);
		}
	}
	public void stop() {
		running = false;
	}
}
