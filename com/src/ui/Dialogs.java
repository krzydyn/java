package ui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class Dialogs {
	static class WrapIoFilter extends FileFilter {
		private java.io.FileFilter filter;
		private String description;
		WrapIoFilter(java.io.FileFilter f, String descr) {
			filter=f;
			description=descr;
		}
		
		public boolean accept(File f) {
			return filter.accept(f);
		}
		public String getDescription() {
			return description;
		}
	};
	
	static public JFileChooser createFileChooser(boolean multsel) {
		JFileChooser chooser = new JFileChooser();
		//chooser.approveSelection();
		chooser.setMultiSelectionEnabled(multsel);
		return chooser;
	}
	static public void addFilter(JFileChooser chooser, java.io.FileFilter filter, String descr) {
		if (chooser.isAcceptAllFileFilterUsed())
			chooser.setAcceptAllFileFilterUsed(false);			
		chooser.addChoosableFileFilter(new WrapIoFilter(filter,descr));
	}
}
