package crypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import sys.Log;
import text.Text;

/*
TLV stands for Tag Length Value

Tag Coding
 byte 0 of tag is coded:
   bit
   7 6 5 4 3 2 1 0
   0 0              universal class
   0 1              application class
   1 0              context-specific class
   1 1              private class
       0            primitive data object
       1            constructed data object
         1 1 1 1 1  see subsequent bytes
         x x x x x  tag number

 subsequent bytes of tag is coded:
   bit
   7 6 5 4 3 2 1 0
   0                last byte of tag
   1                another byte follows
     x x x x x x x  (part of) tag number

Length Coding
 byte 0
   bit7   0  length is coded on bits 6-0
          1  length is coded on subsequent (bits 6-0) bytes

Examples of tag coding:
 0x81: context class,primitive (EMV, Amount Binary)
0x9f02: context class,primitive,subsequent (EMV, Amount Numeric)

generally:
0x00-0x1e:      (30 tags) universal class, primitive
0x1f00-0x1f7f: (127 tags) universal class, primitive
0x20-0x3e:      (30 tags) universal class, constructed
0x3f00-0x3f7f: (127 tags) universal class, constructed
0x40-0x5e:      (30 tags) application class, primitive
0x5f00-0x5f7f: (127 tags) application class, primitive
0x60-0x7e:      (30 tags) application class, constructed
0x7f00-0x7f7f: (127 tags) application class, constructed
0x80-0x9e:      (30 tags) context-specific class, primitive
0x9f00-0x9f7f: (127 tags) context-specific class, primitive
0xa0-0xbe:      (30 tags) context-specific class, constructed
0xbf00-0xbf7f: (127 tags) context-specific class, constructed
0xc0-0xde:      (30 tags) private class, primitive
0xdf00-0xdf7f: (127 tags) private class, primitive
0xe0-0xfe:      (30 tags) private class, constructed
0xff00-0xff7f: (127 tags) private class, constructed

universal tags(t0) meaning (ASN.1)
Tag Primitive 	Use
0 	RFU (for BER internal mechanism)
1 	BOOLEAN
2 	INTEGER
3 	BIT STRING
4 	OCTET STRING
5 	NULL
6 	OBJECT IDENTIFIER
7 	ObjectDescriptor
8 	EXTERNAL, INSTANCE OF
9 	REAL
10 	ENUMERATED
11 	EMBEDDED PDV
12 	UTF8String
13 	RELATIVE-OID
14 	RFU
15 	RFU
16 	SEQUENCE, SEQUENCE OF
17 	SET, SET OF
18 	NumericString
19 	PrintableString
20 	TeletexString, T61String
21 	VideotexString
22 	IA5String
23 	UTCTime
24 	GeneralizedTime
25 	GraphicString
26 	VisibleString, ISO646String
27 	GeneralString
28 	UniversalString
29 	CHARACTER STRING
30 	BMPString

31  Card Service Data
*/
public class TLV_BER {
	public enum TYPE {
		RFU, BOOLEAN, INTEGER, REAL, BITSTRING, OCTSTRING, STRING, NULL, OBJID, OBJDESCCR, INSTANCE,
		ENUMERATED, SEQUENCE, SET, EMBEDDED, RELATIVE, TIME
	}
	private TYPE ASN1_Type[] = {
			TYPE.RFU,       //0
			TYPE.BOOLEAN,   //1
			TYPE.INTEGER,   //2
			TYPE.BITSTRING, //3
			TYPE.OCTSTRING, //4
			TYPE.NULL,      //5
			TYPE.OBJID,     //6
			TYPE.OBJDESCCR, //7
			TYPE.INSTANCE,  //8
			TYPE.REAL,      //9
			TYPE.ENUMERATED,//10
			TYPE.EMBEDDED,  //11
			TYPE.STRING,    //12
			TYPE.RELATIVE,  //13
			TYPE.RFU,       //14
			TYPE.RFU,       //15
			TYPE.SEQUENCE,  //16
			TYPE.SET,       //17
			TYPE.STRING,    //18
			TYPE.STRING,    //19
			TYPE.STRING,    //20
			TYPE.STRING,    //21
			TYPE.STRING,    //22
			TYPE.TIME,      //23
			TYPE.TIME,      //24
			TYPE.STRING,    //25
			TYPE.STRING,    //26
			TYPE.STRING,    //27
			TYPE.STRING,    //28
			TYPE.STRING,    //29
			TYPE.STRING,    //30
	};
	static final int TAG_SUBSEQ = 0x1f;
	static final int TAG_CONSTR = 0x20;
	static final int TAG_NEXT  = 0x80;
	static final int LEN_BYTE  = 0x80;

	public static int tagBytes(int t) {
		int l = 0;
		if (t < 0x100) l = 1;
		else if (t < 0x10000) l = 2;
		else if (t < 0x1000000) l = 3;
		else l = 4;
		return l;
	}
	public static int lengthBytes(int len) {
		int l = 0;
		if (len < 0x80) l = 1;
		else if (len < 0x100) l = 2;
		else if (len < 0x10000) l = 3;
		else l = 4;
		return l;
	}


	final int fixedtag;
	final int fixedlen;

	byte[] buf;
	int bufOffs;
	int tl;
	int vi;
	int vl;

	public TLV_BER() {fixedtag=0; fixedlen=0;}
	public TLV_BER(int ft,int fl) {fixedtag=ft; fixedlen=fl;}

	public long tagAsLong() {
		if (bufOffs < 0) return -1;
		long t = buf[bufOffs]&0xff;
		if (fixedtag > 0) {
			for (int i = 1; i < fixedtag; ++i) {
				t <<= 8; t |= buf[bufOffs+i]&0xff;
			}
		}
		else if ((buf[bufOffs]&TAG_SUBSEQ) == TAG_SUBSEQ) {
			int l=1;
			do {
				t <<= 8; t |= buf[bufOffs+l]&0xff;
				++l;
			}
			while ((buf[bufOffs+l]&TAG_NEXT) != 0);
		}
		return t;
	}
	public byte tagByte(int i) {
		if (i < tl) return buf[bufOffs+i];
		return -1;
	}

	private void makeTag(int tag) {
		if (fixedtag > 0) tl = fixedtag;
		else tl = tagBytes(tag);
		for (int i = 0; i < tl; ++i)
			buf[bufOffs+i] = (byte)(tag >> (8*(tl - i -1)));
	}
	private void makeLength(int l) {
		int lb = lengthBytes(l);
		int li = bufOffs + tl;
		if (lb == 1) buf[li++] = (byte)l;
		else {
			buf[li++] = (byte)(0x80 | (lb-1));
			for (int i = 1; i < lb; ++i) {
				buf[li + lb - i] = (byte)l;
				l >>>= 8;
			}
			li += lb - 1;
		}

		vi = li - bufOffs;
		vl = l;
	}

	public void create(int capa) {
		bufOffs = 0; tl = 0; vl = 0;
		if (buf == null || buf.length < capa)
			buf = new byte[capa];
	}

	public void set(int t, byte[] v, int offs, int len) {
		makeTag(t);
		makeLength(len);
		System.arraycopy(v, offs, buf, bufOffs + vi, len);

		Log.debug("tlv.set %s", toString());
	}

	public void set(int t, byte[] v) {
		set(t, v, 0, v.length);
	}

	public int read(byte[] b, int offs, int len) {
		int i=0;
		bufOffs = 0; tl = 0; vl = 0;
		while (i < len && b[offs+i]==0) ++i;
		if (i >= len) return i;
		buf = b; bufOffs = offs + i;

		// TAG
		if (fixedtag > 0) {
			i += fixedtag;
		}
		else if ((buf[offs+i]&TAG_SUBSEQ) == TAG_SUBSEQ) {
			for (++i; (b[offs+i]&TAG_NEXT) != 0; ++i) ;
			++i;
		}
		else {
			++i;
		}
		tl = offs + i - bufOffs;

		// LENGTH
		vl = b[offs+i]&0xff; ++i;
		if (fixedlen > 0) {
			for (int ii=1; ii < fixedlen; ++ii) {
				vl <<= 8; vl |= b[offs+i]&0xff; ++i;
			}
			Log.debug("fixed len = %d", vl);
		}
		else if ((vl&LEN_BYTE) != 0) {
			int ll = vl&0x7f;
			vl=0;
			for (int ii=0; ii < ll; ++ii) {
				vl <<= 8; vl |= b[offs+i]&0xff; ++i;
			}
			if (i+vl > len) vl = len-i;
			//Log.debug("long len = %d (ll=%d)", l, ll);
		}
		else {
			//Log.debug("short len = %d", l);
		}

		// VALUE
		vi = offs+i - bufOffs;
		return i+vl;
	}

	public int read(InputStream is) throws IOException {
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		bufOffs = 0; tl = 0; vl = 0;
		int r, rd=0;;
		while ((r=is.read())==0) ++rd;
		if (r == -1) return rd;

		// TAG
		ba.write(r); ++rd;
		if (fixedtag > 0) {
			for (int i=1; i < fixedtag && (r=is.read()) >= 0; ++i) {
				ba.write(r); ++rd;
			}
		}
		else if ((r&TAG_SUBSEQ) == TAG_SUBSEQ){
			while ((r=is.read()) >= 0) {
				ba.write(r); ++rd;
				if ((r&TAG_NEXT) == 0) break;
			}
		}
		tl = ba.size();
		buf = ba.toByteArray();
		if (r == -1) return rd;

		// LENGTH
		r=is.read();
		if (r == -1) return rd;
		ba.write(r); ++rd;
		vl = r;
		if (fixedlen > 0) {
			for (int i=1; i < fixedtag && (r=is.read()) >= 0; ++i) {
				ba.write(r); ++rd;
				vl <<= 8; vl |= r;
			}
		}
		else if ((vl&LEN_BYTE) != 0) {
			int ll = vl&0x7f;
			vl=0;
			for (int i=0; i < ll && (r=is.read()) >= 0; ++i) {
				ba.write(r); ++rd;
				vl <<= 8; vl |= r;
			}
		}
		buf = ba.toByteArray();
		if (r == -1) return rd;

		// VALUE
		vi = ba.size();
		for (int i=0; i < vl && (r=is.read()) >= 0; ++i) {
			ba.write(r); ++rd;
		}
		buf = ba.toByteArray();
		return rd;
	}

	public int write(OutputStream os) throws IOException {
		int len = vi + vl;
		os.write(buf, bufOffs, len);
		return len;
	}

	public int write(byte[] b, int offs) {
		int n = vi + vl;
		System.arraycopy(buf, bufOffs, b, offs, n);
		return n;
	}

	public boolean isConstructed() {
		if (bufOffs < 0) return false;
		return (buf[bufOffs]&TAG_CONSTR) != 0;
	}

	public int getValueOffset() {
		return bufOffs + vi;
	}

	@Override
	public String toString() {
		if ((buf[bufOffs]&TAG_SUBSEQ) < ASN1_Type.length) {
			TYPE type = ASN1_Type[buf[bufOffs]&TAG_SUBSEQ];
			if (type == TYPE.STRING)
				return String.format("T=%s L=%d V=%s",Text.hex(buf,bufOffs,tl),vl,Text.vis(buf,vi,vl));
		}
		return String.format("T=%s L=%d V=%s",Text.hex(buf,bufOffs,tl),vl,Text.hex(buf,vi,vl));
	}
	public byte[] toByteArray() {
		return Arrays.copyOfRange(buf, bufOffs, vi+vl);
	}
}
