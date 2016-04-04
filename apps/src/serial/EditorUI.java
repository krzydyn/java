package serial;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import sys.Log;
import text.Ansi;
import text.Text;
import ui.MainPanel;

@SuppressWarnings("serial")
public class EditorUI extends JPanel implements FocusListener {
	final static Border focusedBorder = BorderFactory.createLineBorder(Color.GRAY, 3);
	final static Border unfocusedBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);

	private JEditorPane editor = new JEditorPane();


	//to get focus component must satisfy: 1.visible, 2.enabled, 3. focusable
	public EditorUI(String t, boolean editable) {
		super(new BorderLayout());
		setName(t);
		setFocusable(true);

		MouseListener ml = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (isFocusOwner()) return ;
				if (!requestFocusInWindow()) {
					Log.error("can't focus on %s\n", getName());
				}
			}
		};

		setBorder(unfocusedBorder);

		editor.setFont(Font.decode(Font.MONOSPACED));
		editor.setEditable(editable);
		editor.setFocusable(editable);
		editor.setEnabled(false); //disabled editor does not receive focus
		editor.setBackground(Color.DARK_GRAY);
		editor.setForeground(Color.LIGHT_GRAY);
		editor.setCaretColor(Color.WHITE);
		editor.addMouseListener(ml);
		addMouseListener(ml);
		addFocusListener(this);

		//don't traversal
		Set<KeyStroke> emptyset = new HashSet<KeyStroke>();
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,emptyset);
		setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,emptyset);

		add(new JLabel(t), BorderLayout.NORTH);
		add(MainPanel.createScrolledPanel(editor), BorderLayout.CENTER);
	}

	public void append(String s) {
		if (s.length() == 0) return ;
		try {
			Document doc = editor.getDocument();
			doc.insertString(doc.getLength(), s, null);
			editor.setCaretPosition(doc.getLength());
		} catch (BadLocationException e) {}
	}
	public void append(byte[] b, int off, int len) {
		StringBuilder s = new StringBuilder(len);
		boolean escseq = false;
		for (int i=0; i<len; ++i) {
			if (off+i >= b.length) throw new IndexOutOfBoundsException();
			char c = (char)(b[off+i]&0xff);
			if (escseq) {
				s.append(c);
				if (Character.isLowerCase(c)) {
					Log.debug("eseq=%s", Text.vis(s.toString()));
					s.setLength(0);
					escseq=false;
				}
			}
			else if (c < 0x20 || c > 0x80) {
				if (c == '\t'||c=='\n'||c=='\r') s.append(c);
				else if (c == '\u0007' || c == '\u0008') { //backspace
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
					if (s.length() > 0) {
						append(s.toString());
						s.setLength(0);
					}
				}
				else {
					s.append(String.format("<%x>",(int)c));
				}
			}
			else {
				s.append(c);
			}
		}
		if (s.length() > 0) {
			if (escseq) Log.debug("eseq=%s", Text.vis(s.toString()));
			else append(s.toString());
			s.setLength(0);
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
}
