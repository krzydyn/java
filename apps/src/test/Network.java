package test;

import java.net.NetworkInterface;
import java.util.Enumeration;

public class Network {

	public static void main(String[] args) throws Exception {
		Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
		while (ifs.hasMoreElements()) {
			NetworkInterface ni = ifs.nextElement();
			System.out.println(ni.toString());
		}
	}

}
