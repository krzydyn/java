package SOtests;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.JFileChooser;

public class FileOps {

	public static void main(String[] args) {
		/*final JFileChooser c = Dialogs.createFileChooser(true);
		Dialogs.addFilter(c, new FileFilter() {
					public boolean accept(File f) {
						if (f.isDirectory()) return true;
						return f.getName().toLowerCase().endsWith(".txt");
					}
				}, "Text files");
		*/
		final JFileChooser c = new JFileChooser();
		c.setMultiSelectionEnabled(true);
		c.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		c.addPropertyChangeListener(new PropertyChangeListener() {
	        public void propertyChange(PropertyChangeEvent evt) {
	            if (JFileChooser.SELECTED_FILES_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
	                File[] selectedFiles = c.getSelectedFiles();
	                if (selectedFiles.length > 1) {
	                	File f=c.getSelectedFile();
	                	if (f.isDirectory())
	                		c.setSelectedFiles(new File[] {f});
	                }
	            }
	        }
	    });
		c.showDialog(null,"Apply");
		System.out.println(Arrays.asList(c.getSelectedFiles()));
	}

}
