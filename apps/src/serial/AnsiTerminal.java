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
	private static final long serialVersionUID = -1;
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

		add(new JLabel(t), BorderLayout.NORTH);
		add(MainPanel.createScrolledPanel(editor), BorderLayout.CENTER);
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
		}
		else if (e.getKeyCode() == 40) { //down-arrow
			inputBuffer.append(Ansi.CSI+"B");
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

	private void writeBuffer() {
		if (outputBuffer.length() == 0) return ;
		try {
			Document doc = editor.getDocument();
			doc.insertString(doc.getLength(), outputBuffer.toString(), null);
			editor.setCaretPosition(doc.getLength());
			outputBuffer.setLength(0);
		} catch (BadLocationException e) {}
	}

	public void append(char c) {
		if (escSeq) {
			outputBuffer.append(c);
			if (Character.isLetter(c)) {
				Log.debug("%s: seq=%s", getName(), Text.vis(outputBuffer.toString()));
				outputBuffer.setLength(0);
				escSeq=false;
			}
		}
		else if (c < 0x20) {
			if (c == Ansi.Code.CR) {
				writeBuffer();
			}
			else if (c == Ansi.Code.BEL) {
				bel();
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
					--p;
					Document doc = editor.getDocument();
					editor.setCaretPosition(p);
					try {
						doc.remove(p, 1);
					} catch (BadLocationException e) {}
					editor.repaint();
				}
			}
			else if (c == Ansi.Code.ESC) {
				writeBuffer();
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


	public void bel() {
		try { Sound.dong(); } catch (Exception e) {}
	}
	public void clearBoL() {

	}
	public void clearEoL() {
		Document doc = editor.getDocument();
		//Log.debug("clear: %s", Text.vis(doc.));
		int p = doc.getLength();
		while (p > 0) {
			try {
				if (doc.getText(p-1, 1).equals("\n")) break;
			} catch (BadLocationException e1) {}
			--p;
		}

		if (p != doc.getLength()) {
			try {
				doc.remove(p, doc.getLength()-p);
			} catch (BadLocationException e) {}
			editor.setCaretPosition(p);
			editor.repaint();
		}
	}

}
