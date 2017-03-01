package test_SO;

import java.io.File;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class AnimationTest {
	public static void main(String[] args) {
		for (String a:args){
			File f=new File(a);
			System.out.printf("Processing %s [%d]\n",a,f.length());
			try {
				ImageInputStream stream = ImageIO.createImageInputStream(f);
				Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
				while (readers.hasNext()) {
					ImageReader reader=readers.next();
					reader.setInput(stream);
					int fnum;

					//fnum=reader.getNumImages(true);
					// not all formats support getNumImages
					// iterate over images (frames)
					for (fnum=0; ; ++fnum) {
						try {
							reader.read(fnum);//read BufferedImage
							IIOMetadata imd =  reader.getImageMetadata(fnum);
							String mfn = imd.getNativeMetadataFormatName();
							IIOMetadataNode root = (IIOMetadataNode)imd.getAsTree(mfn);
							//IIOMetadataNode gce=getNode(root,"GraphicControlExtension");
							IIOMetadataNode gce = (IIOMetadataNode) root.
									getElementsByTagName("GraphicControlExtension").item(0);
							int delay=Integer.parseInt(gce.getAttribute("delayTime"))*10;
							System.out.printf("frame[%d] delay %d ms\n",fnum,delay);
							printNode(gce);
						} catch (IndexOutOfBoundsException e) {
							//no more images available
							break;
						} catch (Exception e) {
							e.printStackTrace();
							break;
						}
					}
					System.out.printf("gif frames: %d\n",fnum);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	private static void printNode(IIOMetadataNode node) {
		System.out.printf("Node %s, type %d\n", node.getNodeName(), node.getNodeType());
		NamedNodeMap attrs = node.getAttributes();
		int n=attrs.getLength();
		if (n>0) {
			System.out.printf("Attributes:\n");
			for (int i = 0; i < n; i++) {
				Node ni=attrs.item(i);
				System.out.printf("%s=%s\n",ni.getNodeName(),ni.getNodeValue());
			}
		}
		n=node.getLength();
		if (n>0) {
			System.out.printf("SubNodes:\n");
			for (int i = 0; i < n; i++) {
				Node ni=node.item(i);
				printNode((IIOMetadataNode)ni);
			}
		}
	}
}
