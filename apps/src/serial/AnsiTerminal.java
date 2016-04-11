package serial;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import text.Ansi;
import text.Text;
import ui.MainPanel;

/**
 * Simple Terminal
 * <p>
 * input: swing events system -> sent to remote OutputStream<br>
 * output: JTextComponent <- responses from remote InputStream
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

	private final int MAX_COL=120;
	private final int MAX_ROW=60;

	private JTextComponent editor = new JTextPane();
	private JLabel title = new JLabel();
	private AttributeSet attrib = SimpleAttributeSet.EMPTY;
	private StringBuilder inputBuffer = new StringBuilder();
	private StringBuilder outputBuffer = new StringBuilder();
	private Point cpos = new Point(0, 0);
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
		JButton b=new JButton("C");
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
	}

	private void disableActions(String ...names) {
		ActionMap m = editor.getActionMap();
		for (String n : names) {
	        Action a = m.get(n);
	        if (a != null) a.setEnabled(false);
		}
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
			inputBuffer.setLength(0);
			cursorLineBegin();
		}
		inputBuffer.append(c);
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getModifiers() != 0) return ;

		int code = e.getKeyCode();
		if (code == 38) { //up-arrow
			inputBuffer.append(Ansi.CSI+"A");
			cursorEnd();
			cursorLineBegin();
		}
		else if (code == 40) { //down-arrow
			inputBuffer.append(Ansi.CSI+"B");
			cursorEnd();
			cursorLineBegin();
		}
		else if (code == 39) { //right-arrow
			inputBuffer.append(Ansi.CSI+"C");
		}
		else if (code == 37) { //left-arrow
			inputBuffer.append(Ansi.CSI+"D");
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {}

	private void writeBuffer() {
		if (outputBuffer.length() == 0) return ;
		int p0 = editor.getCaretPosition();
		//Log.debug("Output: %s", Text.vis(outputBuffer));
		try {
			Document doc = editor.getDocument();
			if (p0 < doc.getLength()) {
				int l = Math.min(doc.getLength() - p0, outputBuffer.length());
				doc.remove(p0, l);
			}
			doc.insertString(p0, outputBuffer.toString(), attrib);
			editor.setCaretPosition(p0+outputBuffer.length());
			outputBuffer.setLength(0);
		} catch (BadLocationException e) {Log.error(e.toString());}
	}

	public void flushInput() {
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
			else if (seq.equals(Ansi.ERASE_BELOW)) eraseBelow();
			else if (seq.equals(Ansi.ERASE_ABOVE)) eraseAbove();
			else if (seq.equals(Ansi.ERASE_LN)) eraseLineRight();
			else if (seq.equals(Ansi.CURSOR_POS)) cursorHome();
			else if (seq.equals(Ansi.SGR_RESET)) {
				attrib = SimpleAttributeSet.EMPTY;
			}
			else if (seq.endsWith("C")) {
				int n = Integer.parseInt(seq.substring(Ansi.CSI.length(), seq.length()-1));
				cursorMove(n);
			}
			else if (seq.endsWith("D")) {
				int n = Integer.parseInt(seq.substring(Ansi.CSI.length(), seq.length()-1));
				cursorMove(-n);
			}
			else if (seq.endsWith("H")) { //set cursor position
				int x=-1,y=-1;
				String code = seq.substring(Ansi.CSI.length(), seq.length()-1);
				for (int j,i=0; i < code.length(); i=j+1) {
					j=code.indexOf(';', i);
					if (j<0) j=code.length();
					try {
						y=Integer.parseInt(code.substring(i, j));
						if (x < 0) {x=y; y=-1;}
					} catch (Exception e) {
						Log.error("can't parse seq: '%s'", Text.vis(seq));
					}
				}
				if (x<0) x=1;
				else if (x > MAX_COL) x=MAX_COL;
				if (y<0) y=1;
				else if (y > MAX_ROW) x=MAX_ROW;
				cpos.x = x;
				cpos.y = y;
				cursorHome();
			}
			else if (seq.endsWith("K")) {
				int n = Integer.parseInt(seq.substring(Ansi.CSI.length(), seq.length()-1));
				if (n == 0) eraseLineRight();
				else if (n == 1) eraseLineLeft();
				else if (n == 2) eraseAll();
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
			else if (seq.endsWith("n")) {
				if (seq.equals(Ansi.CSI + "5n")) {
					inputBuffer.append(Ansi.CSI + "0n");
				}
				else if (seq.equals(Ansi.CSI + "6n")) {
					inputBuffer.append(Ansi.CSI + String.format("%d;%dR", cpos.x, cpos.y));
				}
			}
			else done=false;
		}
		else done=false;

		if (!done) Log.warn("%s: seq %s is not handled", getName(), Text.vis(seq));
		else Log.debug("%s: seq %s [OK]", getName(), Text.vis(seq));
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
				if (Character.isLetter(c)) {escSeq=false;c=0;}
				else if ((c=='['||c==']') && outputBuffer.length() > 2) {
					escSeq=false;
				}
			}
			else if (s0.equals(Ansi.OSC)) {
				if (c == Ansi.Code.BEL) {escSeq=false;c=0;}
			}

			if (!escSeq) {
				handleEscSeq(outputBuffer.toString());
				outputBuffer.setLength(0);
				if (c!=0) outputBuffer.append(c);
			}
		}
		else if (c < 0x20) {
			//if (c != Ansi.Code.LF && c != Ansi.Code.CR && c != Ansi.Code.ESC && c != Ansi.Code.ENQ)
			//	Log.debug("ansi %s", Ansi.toString(c));

			if (c == Ansi.Code.CR) {
				writeBuffer();
				attrib = SimpleAttributeSet.EMPTY;
			}
			else if (c == Ansi.Code.BEL) {
				cursorEnd();
				beep();
			}
			else if (c == Ansi.Code.HT) {
				outputBuffer.append(c);
			}
			else if (c == Ansi.Code.VT || c == Ansi.Code.LF) {
				cursorEnd();
				outputBuffer.append('\n');
			}
			else if (c == Ansi.Code.BS) {
				int p = editor.getCaretPosition();
				if (p > 0) {
					editor.setCaretPosition(p-1);
				}
			}
			else if (c == Ansi.Code.ESC) {
				writeBuffer();
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
		if (!escSeq) writeBuffer();
	}
	public void write(CharSequence s) {
		for (int i=0; i<s.length(); ++i) {
			append(s.charAt(i));
		}
		if (!escSeq) writeBuffer();
	}

	public void setTitle(String t) {
		if (t==null || t.isEmpty()) title.setText(getName());
		else title.setText(String.format("[%s] %s",getName(),t));
	}
	public void beep() {
		try { Sound.dong(); } catch (Exception e) {}
	}
	public void cursorHome() {
		editor.setCaretPosition(0);
	}
	public void cursorEnd() {
		Document doc = editor.getDocument();
		int p0 = doc.getLength();
		editor.setCaretPosition(p0);
	}
	public void cursorMove(int n) {
		int p0 = editor.getCaretPosition();
		editor.setCaretPosition(p0+n);
	}
	public void cursorLineBegin() {
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
	public void eraseAll() {
		Document doc = editor.getDocument();
		int p0=0, p = doc.getLength();
		if (p != p0) {
			try {
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {Log.error(e.toString());}
			editor.setCaretPosition(p0);
		}
	}
	public void eraseAbove() {
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
	public void eraseBelow() {
		Document doc = editor.getDocument();
		int p, p0 = editor.getCaretPosition();
		p=doc.getLength();
		if (p != p0) {
			try {
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {Log.error(e.toString());}
			editor.setCaretPosition(p0);
		}
	}
	public void eraseLineLeft() {
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
