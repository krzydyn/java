package gps;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sys.Env;
import sys.Log;

public class KML {
	static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	static DocumentBuilder documentBuilder = null;
	static {
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (Exception e) {}
	}

	Document document = null;
	List<Node> placeMark = new ArrayList<>();

	public KML() throws Exception {
		if (documentBuilder == null)
			throw new RuntimeException("Can't create documentBuilder");
	}

	public void append(String file) throws Exception {
		Log.info("adding file %s", file);
		Document doc = documentBuilder.parse(file);
		NodeList nl = doc.getElementsByTagName("Placemark");
		for (int i = 0; i < nl.getLength(); ++i) {
			Node n = nl.item(i).cloneNode(true);
			placeMark.add(n);
		}
		if (document == null) {
			document = doc;
			for (int i = 0; i < nl.getLength(); ++i) {
				Node n = nl.item(i);
				n.getParentNode().removeChild(n);
			}
		}
	}

	public void save(File file) throws Exception {
		NodeList nl = document.getElementsByTagName("Document");
		if (nl.getLength() == 0)
			throw new RuntimeException("No node Document'");
		Node docnode = nl.item(0);
		for (Node n : placeMark) {
			document.adoptNode(n);
			docnode.appendChild(n);
		}

		document.normalize();

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		//transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		//transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		DOMSource source = new DOMSource(document);
		transformer.transform(source, new StreamResult(file));
	}


	public static void main(String[] args) throws Exception {
		KML kml = new KML();

		for (String f : args) {
			f = Env.expandEnv(f);
			File ff = new File(f);
			if (ff.isDirectory()) {
				String[] files = ff.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".kml");
					}
				});
				Arrays.sort(files);
				for (String x : files) {
					kml.append(f + "/" + x);
				}
			}
			else kml.append(f);
		}

		kml.save(new File("all.kml"));
	}
}
