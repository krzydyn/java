package serial;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import sys.Log;
import ui.MainPanel;

@SuppressWarnings("serial")
public class EditorUI extends JPanel implements FocusListener {
	final static Border focusedBorder = BorderFactory.createLineBorder(Color.GRAY, 3);
	final static Border unfocusedBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);

	private JTextPane editor = new JTextPane();


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

		editor.setEditable(editable);
		//editor.setFocusable(editable);
		editor.setEnabled(false); //disabled editor does not receive focus
		editor.setBackground(Color.DARK_GRAY);
		editor.setForeground(Color.LIGHT_GRAY);
		editor.addMouseListener(ml);
		addMouseListener(ml);

		addFocusListener(this);

		add(new JLabel(t), BorderLayout.NORTH);
		add(MainPanel.createScrolledPanel(editor), BorderLayout.CENTER);
	}

	@Override
	public void addKeyListener(KeyListener l) {
		super.addKeyListener(l);
	}

	public void append(String s) {
		try {
			Document doc = editor.getDocument();
			doc.insertString(doc.getLength(), s, null);
			editor.setCaretPosition(doc.getLength());
		} catch (BadLocationException e) {}
	}
	public void append(byte[] b, int off, int len) {
		append(new String(b,off,len));
	}

	@Override
	public void focusGained(FocusEvent e) {
		setBorder(focusedBorder);
	}

	@Override
	public void focusLost(FocusEvent e) {
		setBorder(unfocusedBorder);
	}
}
