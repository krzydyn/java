package test;

import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 *
 * @author Alessandro
 */
public class NewJFrame extends javax.swing.JFrame {

    /** Creates new form NewJFrame */
    public NewJFrame() {
        final StyleContext cont = StyleContext.getDefaultStyleContext();
        final AttributeSet attrGray = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.gray);
        final AttributeSet attrGreen = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.GREEN);
        final AttributeSet attrBlue = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.BLUE);
        final AttributeSet attrRed = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.RED);
        final AttributeSet attrBlack = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
        DefaultStyledDocument doc = new DefaultStyledDocument() {
            @Override
            public void insertString (int offset, String str, AttributeSet a) throws BadLocationException {
                super.insertString(offset, str, a);

                String text = getText(0, getLength());
                int before = findLastNonWordChar(text, offset);
                if (before < 0) before = 0;
                int after = findFirstNonWordChar(text, offset + str.length());
                int wordL = before;
                int wordR = before;

                while (wordR <= after) {
                    if (wordR == after || String.valueOf(text.charAt(wordR)).matches("\\W")) {
                    	System.out.println("testing:"+text.substring(wordL, wordR));
                        if (text.substring(wordL, wordR).matches("(\\W)*(System)"))
                            setCharacterAttributes(wordL, wordR - wordL, attrBlue, false);
                        else if (text.substring(wordL, wordR).matches("(\\W)*(Mobile)"))
                            setCharacterAttributes(wordL, wordR - wordL, attrBlue, false);
                        else if (text.substring(wordL, wordR).matches("(\\W)*(heaven)"))
                            setCharacterAttributes(wordL, wordR - wordL, attrBlue, false);
                        else if (text.substring(wordL, wordR).matches("(\\W)*(calc)"))
                            setCharacterAttributes(wordL, wordR - wordL, attrBlue, false);
                        else if (text.substring(wordL, wordR).matches("(\\W)*(privaNewJFramete)"))
                            setCharacterAttributes(wordL, wordR - wordL, attrBlue, false);
                        else if (text.substring(wordL, wordR).matches("(\\W)*(public)"))
                            setCharacterAttributes(wordL, wordR - wordL, attrGray, false);
                        else if (text.substring(wordL, wordR).matches("(\\W)*(party)"))
                            setCharacterAttributes(wordL, wordR - wordL, attrRed, false);

                        else
                            setCharacterAttributes(wordL, wordR - wordL, attrBlack, false);
                        wordL = wordR;
                    }
                    wordR++;
                }
            }

            @Override
            public void remove (int offs, int len) throws BadLocationException {
                super.remove(offs, len);

                String text = getText(0, getLength());
                int before = findLastNonWordChar(text, offs);
                if (before < 0) before = 0;
                int after = findFirstNonWordChar(text, offs);

                if (text.substring(before, after).matches("(\\W)*(System)"))
                    setCharacterAttributes(before, after - before, attrBlue, false);
                else if (text.substring(before, after).matches("(\\W)*(PlasmaMembrane)"))
                    setCharacterAttributes(before, after - before, attrBlue, false);
                else if (text.substring(before, after).matches("(\\W)*(Cytosol)"))
                    setCharacterAttributes(before, after - before, attrBlue, false);
                else if (text.substring(before, after).matches("(\\W)*(Organelle)"))
                    setCharacterAttributes(before, after - before, attrBlue, false);
                else if (text.substring(before, after).matches("(\\W)*(Nucleous)"))
                    setCharacterAttributes(before, after - before, attrBlue, false);
                else if (text.substring(before, after).matches("(\\W)*(volume)"))
                    setCharacterAttributes(before, after - before, attrGray, false);
                else if (text.substring(before, after).matches("(\\W)*(rate)"))
                    setCharacterAttributes(before, after - before, attrRed, false);
                 else {
                    setCharacterAttributes(before, after - before, attrBlack, false);
                }
            }
        };
        initComponents(doc);
        //JTextPane txt = new JTextPane(doc);
        //setPreferredSize(new Dimension(200, 200));
        //pack();
        setLocationRelativeTo(null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     * @param doc
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents(DefaultStyledDocument doc) {

        openBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        textModel = new javax.swing.JTextPane(doc);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        openBtn.setText("Open");
        openBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                openBtnActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(textModel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(216, 216, 216)
                        .addComponent(openBtn))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(openBtn)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>
String spec;
File f;
String filename;
JFileChooser chooser;

private void openBtnActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
    chooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter(".txt files", "txt", "text");
    chooser.setFileFilter(filter);
    int option = chooser.showOpenDialog(null);
    if (option == JFileChooser.APPROVE_OPTION) {
    f = chooser.getSelectedFile();
    filename = f.getAbsolutePath();
    String modelSpec = readSpecification();
    spec = modelSpec;
    textModel.setText(spec);
    }
}


public String readSpecification() {
        String spec1 = "";

        // trying to read from file the specification...
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while(line!=null) {
                spec1 += line + "\n";
                line = reader.readLine();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return spec1;
    }

private int findLastNonWordChar (String text, int index) {
        while (--index >= 0) {
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
        }
        return index;
    }

    private int findFirstNonWordChar (String text, int index) {
        while (index < text.length()) {
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
            index++;
        }
        return index;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
			public void run() {
                new NewJFrame().setVisible(true);
                //new NewJFrame();
            }
        });
    }
    // Variables declaration - do not modify
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton openBtn;
    private javax.swing.JTextPane textModel;
    // End of variables declaration
}