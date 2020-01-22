package test_jna;

//Copyright 2015 Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland

//www.source-code.biz, www.inventec.ch/chdh
//
//This module is multi-licensed and may be used under the terms of any of the following licenses:
//
//LGPL, GNU Lesser General Public License, V2.1 or later, http://www.gnu.org/licenses/lgpl.html
//EPL, Eclipse Public License, V1.0 or later, http://www.eclipse.org/legal
//
//Please contact the author if you need another license.
//This module is provided "as is", without warranties of any kind.
//
//Home page: http://www.source-code.biz/snippets/java/SerialPortChannel

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.IntegerType;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * A serial port channel for Android.
 *
 * <p>
 * This class uses JNA to access a serial port device (TTY) of the Android
 * operating system.
 */
public class SerialPortChannel implements ByteChannel {

	private static final boolean isBigEndian = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
	private static final int ioBufSize = 512;

	private boolean isOpen;
	private int fileHandle;

	private Memory ioBuf;

	/**
	 * Creates a serial port channel.
	 *
	 * @param fileName
	 *            The file name of the serial port device, e.g. "/dev/ttyO2".
	 * @param baudRate
	 *            The baud rate for the serial port, e.g. 9600.
	 */
	public SerialPortChannel(String fileName, int baudRate) throws IOException {
		staticInit();
		try {
			open(fileName, baudRate);
		} finally {
			if (!isOpen) {
				close2();
			}
		}
	}

	private synchronized void open(String fileName, int baudRate) throws IOException {
		fileHandle = -1;
		int mode = LibcDefs.O_RDWR | LibcDefs.O_NOCTTY | LibcDefs.O_NONBLOCK;
		fileHandle = libc.open(fileName, mode);
		if (fileHandle == -1) {
			throw new IOException("Open of \"" + fileName + "\" failed, errno=" + Native.getLastError() + ".");
		}
		configureSerialPort(baudRate);
		ioBuf = new Memory(ioBufSize);
		isOpen = true;
	}

	private void configureSerialPort(int baudRate) throws IOException {
		Termios cfg = new Termios();
		LibcDefs.tcgetattr(fileHandle, cfg);
		LibcDefs.cfmakeraw(cfg);
		int baudRateCode = encodeBaudRate(baudRate);
		LibcDefs.cfsetospeed(cfg, baudRateCode);
		LibcDefs.tcsetattr(fileHandle, cfg);
	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public void close() throws IOException {
		if (!isOpen) {
			return;
		}
		isOpen = false;
		close2();
	}

	private void close2() throws IOException {
		if (fileHandle != -1) {
			libc.close(fileHandle);
		}
	}

	// Reads from the channel without blocking.
	// Returns the number of bytes actually read.
	public synchronized int read(byte[] buf, int pos, int len) throws IOException {
		int reqLen = Math.min(len, ioBufSize);
		if (reqLen <= 0) {
			return 0;
		}
		NativeSizeT trLen0 = libc.read(fileHandle, ioBuf, new NativeSizeT(reqLen));
		int trLen = trLen0.intValue();
		if (trLen == -1) {
			int errno = Native.getLastError();
			if (errno == LibcDefs.EAGAIN) {
				return 0;
			}
			throw new IOException("Serial port read error, errno=" + errno + ".");
		}
		if (trLen == 0) {
			return 0;
		}
		if (trLen < 0 || trLen > reqLen) {
			throw new RuntimeException("Invalid length returned from read().");
		}
		ioBuf.read(0, buf, pos, trLen);
		return trLen;
	}

	// Reads from the channel without blocking.
	// Returns the number of bytes actually read.
	public int read(byte[] buf) throws IOException {
		return read(buf, 0, buf.length);
	}

	// Reads from the channel without blocking.
	// Returns the number of bytes actually read.
	@Override
	public int read(ByteBuffer buf) throws IOException {
		int trLen = read(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining());
		buf.position(buf.position() + trLen);
		return trLen;
	}

	// Writes to the channel without blocking.
	// Returns the number of bytes actually written.
	public synchronized int write(byte[] buf, int pos, int len) throws IOException {
		int reqLen = Math.min(len, ioBufSize);
		if (reqLen <= 0) {
			return 0;
		}
		ioBuf.write(0, buf, pos, reqLen);
		NativeSizeT trLen0 = libc.write(fileHandle, ioBuf, new NativeSizeT(reqLen));
		int trLen = trLen0.intValue();
		if (trLen == -1) {
			int errno = Native.getLastError();
			if (errno == LibcDefs.EAGAIN) {
				return 0;
			}
			throw new IOException("Serial port write error, errno=" + errno + ".");
		}
		if (trLen < 0 || trLen > reqLen) {
			throw new RuntimeException("Invalid length returned from write().");
		}
		return trLen;
	}

	// Writes to the channel without blocking.
	// Returns the number of bytes actually written.
	public int write(byte[] buf) throws IOException {
		return write(buf, 0, buf.length);
	}

	// Writes to the channel without blocking.
	// Returns the number of bytes actually written.
	@Override
	public int write(ByteBuffer buf) throws IOException {
		int trLen = write(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining());
		buf.position(buf.position() + trLen);
		return trLen;
	}

	// Waits until any data has been received and is ready to be read.
	// @param timeoutMs
	// Timeout value in milliseconds. 0 to return immediately. -1 to wait endlessly.
	// @return
	// true if data should be ready to be read.
	public boolean waitInputReady(int timeoutMs) throws IOException {
		TimeVal timeVal = (timeoutMs < 0) ? null : new TimeVal(timeoutMs);
		FdSet rxSet = new FdSet();
		FdSet errorSet = new FdSet();
		rxSet.set(fileHandle);
		errorSet.set(fileHandle);
		int rc = libc.select(fileHandle + 1, rxSet.a, null, errorSet.a, timeVal);
		checkSelectErrors(rc, errorSet);
		if (rc == 0) {
			return false;
		}
		if (!rxSet.isSet(fileHandle)) {
			throw new RuntimeException("rxSet bit is not set after select().");
		}
		return true;
	}

	// Waits until the channel is ready to accept new data to be transmitted.
	// @param timeoutMs
	// Timeout value in milliseconds. 0 to return immediately. -1 to wait endlessly.
	// @return
	// true if the channel should be ready for writing data.
	public boolean waitOutputReady(int timeoutMs) throws IOException {
		TimeVal timeVal = (timeoutMs < 0) ? null : new TimeVal(timeoutMs);
		FdSet txSet = new FdSet();
		FdSet errorSet = new FdSet();
		txSet.set(fileHandle);
		errorSet.set(fileHandle);
		int rc = libc.select(fileHandle + 1, null, txSet.a, errorSet.a, timeVal);
		checkSelectErrors(rc, errorSet);
		if (rc == 0) {
			return false;
		}
		if (!txSet.isSet(fileHandle)) {
			throw new RuntimeException("txSet bit is not set after select().");
		}
		return true;
	}

	private void checkSelectErrors(int rc, FdSet errorSet) throws IOException {
		if (rc == -1) {
			throw new IOException("Error in select(), errno=" + Native.getLastError() + ".");
		}
		boolean error = errorSet.isSet(fileHandle);
		if (!(rc == 0 && !error || rc == 1 || rc == 2 && error)) {
			throw new RuntimeException(
					"Invalid return code received from select(), rc=" + rc + ", error=" + error + ".");
		}
		if (error) {
			throw new IOException("Channel error state detected");
		}
	}

	private static int encodeBaudRate(int baudRate) {
		switch (baudRate) {
		case 0:
			return 0000000;
		case 50:
			return 0000001;
		case 75:
			return 0000002;
		case 110:
			return 0000003;
		case 134:
			return 0000004;
		case 150:
			return 0000005;
		case 200:
			return 0000006;
		case 300:
			return 0000007;
		case 600:
			return 0000010;
		case 1200:
			return 0000011;
		case 1800:
			return 0000012;
		case 2400:
			return 0000013;
		case 4800:
			return 0000014;
		case 9600:
			return 0000015;
		case 19200:
			return 0000016;
		case 38400:
			return 0000017;
		case 57600:
			return 0010001;
		case 115200:
			return 0010002;
		case 230400:
			return 0010003;
		case 460800:
			return 0010004;
		case 500000:
			return 0010005;
		case 576000:
			return 0010006;
		case 921600:
			return 0010007;
		case 1000000:
			return 0010010;
		case 1152000:
			return 0010011;
		case 1500000:
			return 0010012;
		case 2000000:
			return 0010013;
		case 2500000:
			return 0010014;
		case 3000000:
			return 0010015;
		case 3500000:
			return 0010016;
		case 4000000:
			return 0010017;
		default:
			throw new IllegalArgumentException("Unsupported baud rate " + baudRate + ".");
		}
	}

	// ------------------------------------------------------------------------------

	private static Libc libc;
	private static boolean staticInitDone;

	private static interface Libc extends Library {

		// fcntl.h
		int open(String path, int mode);

		// select.h
		int select(int nfds, byte[] readfds, byte[] writefds, byte[] errfds, TimeVal timeout);

		// unistd.h
		int close(int fileHandle);

		int ioctl(int fileHandle, int request, Pointer addr);

		NativeSizeT read(int fileHandle, Pointer addr, NativeSizeT length);

		NativeSizeT write(int fileHandle, Pointer addr, NativeSizeT length);
	}

	private static class LibcDefs {

		// errno-base.h
		static final int EAGAIN = 11;

		// fcntl.h
		static final int O_RDWR = 00000002;
		static final int O_NOCTTY = 00000400;
		static final int O_NONBLOCK = 00004000;

		// ioctls.h
		static final int TCGETS = 0x5401; // for ARM and X86, for Mips is 0x540d!
		static final int TCSETS = 0x5402;

		// posix_types.h
		static final int FD_SETSIZE = 1024;

		// termbits.h
		// static final int VTIME = 5;
		// static final int VMIN = 6; // for ARM and X86, fĂ�ÂĽr MIPS this is 4!

		// termios.h
		static final int IGNBRK = 0000001;
		static final int BRKINT = 0000002;
		static final int PARMRK = 0000010;
		static final int ISTRIP = 0000040;
		static final int INLCR = 0000100;
		static final int IGNCR = 0000200;
		static final int ICRNL = 0000400;
		static final int IXON = 0002000;
		static final int OPOST = 0000001;
		static final int ISIG = 0000001;
		static final int ICANON = 0000002;
		static final int ECHO = 0000010;
		static final int ECHONL = 0000100;
		static final int IEXTEN = 0100000;
		static final int CBAUD = 0010017;
		static final int CSIZE = 0000060;
		static final int PARENB = 0000400;
		static final int CS8 = 0000060;

		static void tcgetattr(int fileHandle, Termios cfg) throws IOException {
			if (libc.ioctl(fileHandle, TCGETS, cfg.getPointer()) == -1) {
				throw new IOException("tcgetattr failed, errno=" + Native.getLastError() + ".");
			}
			cfg.read();
		}

		static void tcsetattr(int fileHandle, Termios cfg) throws IOException {
			cfg.write();
			if (libc.ioctl(fileHandle, TCSETS, cfg.getPointer()) == -1) {
				throw new IOException("tcsetattr failed, errno=" + Native.getLastError() + ".");
			}
		}

		static void cfmakeraw(Termios cfg) {
			cfg.c_iflag &= ~(IGNBRK | BRKINT | PARMRK | ISTRIP | INLCR | IGNCR | ICRNL | IXON);
			cfg.c_oflag &= ~OPOST;
			cfg.c_lflag &= ~(ECHO | ECHONL | ICANON | ISIG | IEXTEN);
			cfg.c_cflag &= ~(CSIZE | PARENB);
			cfg.c_cflag |= CS8;
		}

		static void cfsetospeed(Termios cfg, int baudRateCode) {
			cfg.c_cflag = (cfg.c_cflag & ~CBAUD) | (baudRateCode & CBAUD);
		}
	}

	private static class Termios extends Structure {
		public int c_iflag;
		public int c_oflag;
		public int c_cflag;
		public int c_lflag;
		public byte c_line;
		public byte[] c_cc = new byte[32]; // actual length depends on platform (currently 19 for ARM and X86, 23 for
											// Mips)

		@Override
		protected List getFieldOrder() {
			return Arrays.asList("c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_line", "c_cc");
		}
	}

	// (Should be excluded from JavaDoc)
	public static class NativeSizeT extends IntegerType { // must be public for JNA
		public NativeSizeT() {
			this(0);
		}

		public NativeSizeT(long value) {
			super(Native.SIZE_T_SIZE, value);
		}
	}

	private static class TimeVal extends Structure { // represents struct timeval in linux/time.h
		public NativeLong tv_sec; // time_t
		public NativeLong tv_usec; // suseconds_t

		TimeVal(int ms) {
			set(ms);
		}

		void set(int ms) {
			tv_sec.setValue(ms / 1000);
			tv_usec.setValue(ms % 1000 * 1000);
		}

		@Override
		protected List getFieldOrder() {
			return Arrays.asList("tv_sec", "tv_usec");
		}
	}

	private static class FdSet { // represents an fd_set structure
		byte[] a; // bitmap corresponding to file handles

		FdSet() {
			a = new byte[LibcDefs.FD_SETSIZE / 8];
		}

		void set(int fd) {
			a[getBytePos(fd)] |= getBitMask(fd);
		}

		boolean isSet(int fd) {
			return (a[getBytePos(fd)] & getBitMask(fd)) != 0;
		}

		private static int getBytePos(int fd) {
			if (fd < 0 || fd >= LibcDefs.FD_SETSIZE) {
				throw new RuntimeException("File handle out of range for fd_set.");
			}
			if (isBigEndian) {
				return (fd / 8 / Native.LONG_SIZE + 1) * Native.LONG_SIZE - 1 - fd / 8 % Native.LONG_SIZE;
			} else {
				return fd / 8;
			}
		}

		private static int getBitMask(int fd) {
			return 1 << (fd % 8);
		}
	}

	private static synchronized void staticInit() {
		if (staticInitDone) {
			return;
		}
		libc = (Libc)Native.loadLibrary("c", Libc.class);
		staticInitDone = true;
	}

}
