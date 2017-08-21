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

package ui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class Dialogs {
	static class WrapIoFilter extends FileFilter {
		private final java.io.FileFilter filter;
		private final String description;
		WrapIoFilter(java.io.FileFilter f, String descr) {
			filter=f;
			description=descr;
		}

		@Override
		public boolean accept(File f) {
			return filter.accept(f);
		}
		@Override
		public String getDescription() {
			return description;
		}
	};

	static public JFileChooser createFileChooser(boolean multsel) {
		JFileChooser chooser = new JFileChooser(new File("."));
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
