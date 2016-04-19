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

package io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class Serial {
	static public class Param {
		final static public int DATA_5 = SerialPort.DATABITS_5;
		final static public int DATA_6 = SerialPort.DATABITS_6;
		final static public int DATA_7 = SerialPort.DATABITS_7;
		final static public int DATA_8 = SerialPort.DATABITS_8;
		final static public int STOP_1 = SerialPort.STOPBITS_1;
		final static public int STOP_2 = SerialPort.STOPBITS_2;
		final static public int FLOW_NONE = SerialPort.FLOWCONTROL_NONE;
		final static public int FLOW_RTSCTS = SerialPort.FLOWCONTROL_RTSCTS_IN|SerialPort.FLOWCONTROL_RTSCTS_OUT;
		final static public int FLOW_XONXOFF = SerialPort.FLOWCONTROL_XONXOFF_IN|SerialPort.FLOWCONTROL_XONXOFF_OUT;
	}
	private String portName;
	private CommPort commPort;

	public Serial(String n) {
		portName = n;
	}

	public String getName() { return portName; }

	public void open() throws IOException {
		CommPortIdentifier portIdentifier;
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
	        if (portIdentifier.isCurrentlyOwned()){
	        	System.out.println("Error: Port is currently in use");
	        } else {
	        	commPort = portIdentifier.open(this.getClass().getName(),2000);
	        }
	        //setup default reading timeout
	        commPort.enableReceiveTimeout(100);
		} catch (Throwable e) {
			throw new IOException("open " + portName, e);
		}
	}

	public void close() {
		if (commPort != null) commPort.close();
		commPort=null;
	}

	public boolean isOpen() {
		return commPort != null;
	}

	public void setParams(int speed, int databits, int stopbits, int flowcontrol) {
		try {
	        if (commPort instanceof SerialPort) {
	            SerialPort serialPort = (SerialPort) commPort;
	            serialPort.setSerialPortParams(speed,databits,stopbits,flowcontrol);
	        } else {
	            throw new RuntimeException("Must be serial port");
	        }
		} catch (Throwable e) {
		}
	}

	public int read(byte[] b, int off, int len) throws IOException {
		try {
			return commPort.getInputStream().read(b, off, len);
		}catch (IOException e) {
			throw e;
		}catch (Throwable e) {
			throw new IOException("read", e);
		}
	}
	public void write(byte[] b, int off, int len) throws IOException {
		commPort.getOutputStream().write(b, off, len);
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
