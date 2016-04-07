package serial;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

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
	private static final long serialVersionUID = 0
			;
	final static Border focusedBorder = BorderFactory.createLineBorder(Color.GRAY, 3);
	final static Border unfocusedBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);

	private JTextComponent editor = new JTextPane();
	private StringBuilder inputBuffer = new StringBuilder();
	private StringBuilder outputBuffer = new StringBuilder();
	private boolean escSeq = false;

	//to get focus component must satisfy: 1.visible, 2.enabled, 3. focusable
	public AnsiTerminal(String t, boolean editable) {
		super(new BorderLayout());
		setName(t);

		setBorder(unfocusedBorder);

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

		add(new JLabel(t), BorderLayout.NORTH);
		add(MainPanel.createScrolledPanel(editor), BorderLayout.CENTER);
	}

	private void disableActions(String ...names) {
		ActionMap m = editor.getActionMap();
		//Log.debug("Action keys: %d %s", m.size(), Text.join(m.allKeys(), "\n"));

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
		Log.debug("key typed %d", (int)e.getKeyChar());
		char c = e.getKeyChar();
		inputBuffer.append(c);
		e.consume();
	}
	@Override
	public void keyPressed(KeyEvent e) {
		Log.debug("%s: key pressed %d", getName(), e.getKeyCode());
		if (e.getKeyCode() == 38) { //up-arrow
			inputBuffer.append(Ansi.CSI+"A");
			cursorEnd();
			eraseLineBegin();
		}
		else if (e.getKeyCode() == 40) { //down-arrow
			inputBuffer.append(Ansi.CSI+"B");
			cursorEnd();
			eraseLineBegin();
		}
		else if (e.getKeyCode() == 39) { //right-arrow
			inputBuffer.append(Ansi.CSI+"C");
		}
		else if (e.getKeyCode() == 37) { //left-arrow
			inputBuffer.append(Ansi.CSI+"D");
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
	}

	private void writeBuffer() {
		if (outputBuffer.length() == 0) return ;
		int p0 = editor.getCaretPosition();
		try {
			Document doc = editor.getDocument();
			if (p0 < doc.getLength()) {
				int l = Math.min(doc.getLength() - p0, outputBuffer.length());
				doc.remove(p0, l);
			}
			doc.insertString(p0, outputBuffer.toString(), null);
			editor.setCaretPosition(p0+outputBuffer.length());
			outputBuffer.setLength(0);
		} catch (BadLocationException e) {}
	}

	public void flushImput() {
		inputBuffer.setLength(0);
		escSeq = false;
	}
	public int getInputBuffer(byte[] b) {
		if (inputBuffer.length() == 0) return 0;

		synchronized (inputBuffer) {
			byte[] bi = inputBuffer.toString().getBytes();
			int l = Math.min(b.length, bi.length);
			for (int i = 0; i < l; ++i)
				b[i] = bi[i];
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
			else if (seq.equals(Ansi.ERASE_ABOVE)) eraseAfter();
			else if (seq.equals(Ansi.CURSOR_POS)) cursorHome();
			else if (seq.endsWith("C")) {
				int n = Integer.parseInt(seq.substring(Ansi.CSI.length(), seq.length()-1));
				cursorMove(n);
			}
			else if (seq.endsWith("D")) {
				int n = Integer.parseInt(seq.substring(Ansi.CSI.length(), seq.length()-1));
				cursorMove(-n);
			}
			else if (seq.endsWith("H")) {
			}
			else done=false;
		}
		else done=false;

		if (done) Log.debug("%s: seq %s", getName(), Text.vis(seq));
		else Log.warn("%s: seq %s is not handled", getName(), Text.vis(seq));
	}

	public void append(char c) {
		if (escSeq) {
			outputBuffer.append(c);
			if (Character.isLetter(c)) {
				handleEscSeq(outputBuffer.toString());
				outputBuffer.setLength(0);
				escSeq=false;
			}
		}
		else if (c < 0x20) {
			if (c != Ansi.Code.LF && c != Ansi.Code.CR && c != Ansi.Code.ESC)
				Log.debug("proc ansi %s", Ansi.codeName(c));

			if (c == Ansi.Code.CR) {
				writeBuffer();
			}
			else if (c == Ansi.Code.BEL) {
				beep();
			}
			else if (c == Ansi.Code.VT) {
				outputBuffer.append('\n');
			}
			else if (c == Ansi.Code.HT || c == Ansi.Code.LF) {
				outputBuffer.append(c);
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
				Log.debug("%s: Ignore %s", getName(), Ansi.codeName(c));
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
	public void eraseAll() {
		Document doc = editor.getDocument();
		int p=0, p0 = doc.getLength();
		try {
			doc.remove(p0, p-p0);
		} catch (BadLocationException e) {}
		if (p != p0) {
			try {
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {}
			editor.setCaretPosition(p0);
			//editor.repaint(500);
		}
	}
	public void eraseAfter() {
		Document doc = editor.getDocument();
		int p = 0,p0 = editor.getCaretPosition();
		if (p != p0) {
			try {
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {}
			editor.setCaretPosition(p0);
			//editor.repaint();
		}
	}
	public void eraseBelow() {
		Document doc = editor.getDocument();
		int p,p0 = editor.getCaretPosition();
		p=doc.getLength();
		if (p != p0) {
			try {
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {}
			editor.setCaretPosition(p0);
			//editor.repaint();
		}
	}
	public void eraseLineBegin() {
		Document doc = editor.getDocument();
		int p, p0 = editor.getCaretPosition();
		for (p=p0; p0 > 0; --p0) {
			try {
				if (doc.getText(p0-1, 1).equals("\n")) break;
			} catch (BadLocationException e1) {}
		}

		if (p != p0) {
			try {
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {}
			editor.setCaretPosition(p0);
			//editor.repaint(500);
		}
	}
	public void eraseLineEnd() {
		Document doc = editor.getDocument();
		int p,p0 = editor.getCaretPosition();
		for (p=p0; p < doc.getLength(); ++p) {
			try {
				if (doc.getText(p, 1).equals("\n")) break;
			} catch (BadLocationException e1) {}
		}

		if (p != p0) {
			try {
				doc.remove(p0, p-p0);
			} catch (BadLocationException e) {}
			editor.setCaretPosition(p0);
			//editor.repaint();
		}
	}

}
