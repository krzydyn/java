package serial;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
 *
 * @author k.dynowski
 *
 */

//TODO rename it to AnsiTerminal
// input: swing events system -> sent to remote OutputStream
// output: JTextComponent <- responses from remote InputStream
@SuppressWarnings("serial")
public class EditorUI extends JPanel implements FocusListener {
	final static Border focusedBorder = BorderFactory.createLineBorder(Color.GRAY, 3);
	final static Border unfocusedBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);

	private JTextComponent editor = new JTextPane();

	//to get focus component must satisfy: 1.visible, 2.enabled, 3. focusable
	public EditorUI(String t, boolean editable) {
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

		//don't traversal
		Set<KeyStroke> emptyset = new HashSet<KeyStroke>();
		editor.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,emptyset);
		editor.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,emptyset);

		add(new JLabel(t), BorderLayout.NORTH);
		add(MainPanel.createScrolledPanel(editor), BorderLayout.CENTER);
	}

	@Override
	public void addKeyListener(KeyListener l) {
		editor.addKeyListener(l);
	}
	@Override
	public void addFocusListener(final FocusListener l) {
		editor.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				e.setSource(EditorUI.this);
				l.focusLost(e);
			}
			@Override
			public void focusGained(FocusEvent e) {
				e.setSource(EditorUI.this);
				l.focusGained(e);
			}
		});
	}

	private void dong() {
		try { Sound.dong(); } catch (Exception e) {}
	}


	public void append(String s) {
		if (s.length() == 0) return ;
		try {
			Document doc = editor.getDocument();

			doc.insertString(doc.getLength(), s, null);
			editor.setCaretPosition(doc.getLength());
		} catch (BadLocationException e) {}
	}
	private StringBuilder textcache = new StringBuilder(1024);
	public void append(byte[] b, int off, int len) {
		//Log.debug("appending: %s", Text.vis(b, off, len));
		boolean escseq = textcache.length()>0;
		for (int i=0; i<len; ++i) {

			char c = (char)(b[off+i]&0xff);
			if (escseq) {
				textcache.append(c);
				if (Character.isLetter(c)) {
					Log.debug("%s: seq=%s", getName(), Text.vis(textcache.toString()));
					textcache.setLength(0);
					escseq=false;
				}
			}
			else if (c < 0x20) {
				if (c == Ansi.Code.CR) {
					append(textcache.toString());
					textcache.setLength(0);
				}
				else if (c == Ansi.Code.BEL) {
					dong();
				}
				else if (c == Ansi.Code.VT) {
					textcache.append('\n');
				}
				else if (c == Ansi.Code.HT || c == Ansi.Code.LF) {
					textcache.append(c);
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
					escseq=true;
					append(textcache.toString());
					textcache.setLength(0);
					textcache.append(c);
				}
				else {
					Log.debug("%s: Ignore %s", getName(), Ansi.codeName(c));
				}
			}
			else {
				textcache.append(c);
			}
		}
		if (!escseq) {
			append(textcache.toString());
			textcache.setLength(0);
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

	public void clear() {
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
