package io;

import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Serial2 {
	static public class Params {
		final static public int DATA7 = SerialPort.DATABITS_7;
		final static public int DATA8 = SerialPort.DATABITS_8;
		final static public int STOP0 = 0;
		final static public int STOP1 = SerialPort.STOPBITS_1;
	}
	private String portName;

	static final void loadDriver() {
		//System.loadLibrary("LinuxSerialParallel");
	}

	public Serial2(String n) {
		portName = n;
	}

	public String getName() { return portName; }

	public void open() throws IOException {

	}

    public static List<String> listPorts()
    {
        java.util.Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();
        ArrayList<String> list = new ArrayList<String>();
        while (portEnum.hasMoreElements())
        {
            CommPortIdentifier portIdentifier = (CommPortIdentifier)portEnum.nextElement();
            list.add(portIdentifier.getName());
        }
        Collections.sort(list);
        return list;
    }
}
