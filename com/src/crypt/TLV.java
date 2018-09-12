package crypt;

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
public class TLV {
	enum TYPE {
		RFU, BOOLEAN, INTEGER, BITSTRING, STRING, NULL, OBJID, OBJDESCCR
	}
	TYPE tlvType[] = {
			TYPE.RFU,       //0
			TYPE.BOOLEAN,   //1
			TYPE.INTEGER,   //2
			TYPE.BITSTRING, //3
			TYPE.STRING,    //4
			TYPE.NULL,      //5
			TYPE.OBJID,     //6
			TYPE.OBJDESCCR, //7
			TYPE.RFU,       //8
			TYPE.RFU,       //9
			TYPE.RFU,       //10
			TYPE.RFU,       //11
			TYPE.STRING,    //12
			TYPE.RFU,       //13
			TYPE.RFU,       //14
			TYPE.RFU,       //15
			TYPE.RFU,       //16
			TYPE.RFU,       //17
			TYPE.STRING,    //18
			TYPE.STRING,    //19
			TYPE.STRING,    //20
			TYPE.STRING,    //21
			TYPE.STRING,    //22
			TYPE.STRING,    //23
			TYPE.STRING,    //24
			TYPE.STRING,    //25
			TYPE.STRING,    //26
			TYPE.STRING,    //27
			TYPE.STRING,    //28
			TYPE.STRING,    //29
			TYPE.STRING,    //30
	};
	static final int TAG_SEQ   = 0x1f;
	static final int TAG_NEXT  = 0x80;
	static final int TAG_CONSTR = 0x20;
	static final int LEN_BYTE  = 0x80;
	final int fixedtag;
	final int fixedlen;
	int ti;
	int l;
	int vi;
	byte[] buf;

	TLV() {fixedtag=0; fixedlen=0;}
	TLV(int ft,int fl) {fixedtag=ft; fixedlen=fl;}

	long tag() {
		if (ti < 0) return -1;
		long t = buf[ti]&0xff;
		if (fixedtag > 0) {
			for (int i = 1; i < fixedtag; ++i) {
				t <<= 8; t |= buf[ti+i]&0xff;
			}
		}
		else if ((buf[ti]&TAG_SEQ) == TAG_SEQ) {
			int l=1;
			do {
				t <<= 8; t |= buf[ti+l]&0xff;
				++l;
			}
			while ((buf[ti+l]&TAG_NEXT) != 0);
		}
		return t;
	}
	byte tagByte(int i) {
		if (fixedtag > 0) {
			if (i < fixedtag) return buf[ti+i];
			return -1;
		}
		else if ((buf[ti]&TAG_SEQ) == TAG_SEQ) {
			if (i == 0) return buf[ti];
			int l=1;
			do {
				if (i == l) return buf[ti+i];
			} while ((buf[ti+l]&TAG_NEXT) != 0);
			return -1;
		}
		else {
			if (i == 0) return buf[ti+i];
			return -1;
		}
	}

	public int read(byte[] b, int offs, int len) {
		int i=0;
		ti=-1; vi=-1; l=0;
		while (i < len && b[offs+i]==0) ++i;
		if (i >= len) return 0;
		buf = b; ti = offs + i;

		if (fixedtag > 0) {
			i += fixedtag;
			Log.debug("fixed tag = %x", tag());
		}
		else if ((buf[offs+i]&TAG_SEQ) == TAG_SEQ) {
			for (++i; (b[offs+i]&TAG_NEXT) != 0; ++i) ;
			++i;
			//Log.debug("long tag = %x", tag());
		}
		else {
			++i;
			//Log.debug("short tag = %x", tag());
		}
		l = b[offs+i]&0xff; ++i;
		if (fixedlen > 0) {
			for (int ii=1; ii < fixedlen; ++ii) {
				l <<= 8; l |= b[offs+i]&0xff; ++i;
			}
			Log.debug("fixed len = %d", l);
		}
		else if ((l&LEN_BYTE) != 0) {
			int ll = l&0x7f;
			l=0;
			for (int ii=0; ii < ll; ++ii) {
				l <<= 8; l |= b[offs+i]&0xff; ++i;
			}
			if (i+l > len) l = len-i;
			//Log.debug("long len = %d (ll=%d)", l, ll);
		}
		else {
			//Log.debug("short len = %d", l);
		}
		vi = offs+i;
		return i+l;
	}

	public boolean isConstructed() {
		if (ti < 0) return false;
		return (buf[ti]&TAG_CONSTR) != 0;
	}

	public int getValueOffset() {
		return vi;
	}

	@Override
	public String toString() {
		if ((buf[ti]&TAG_SEQ) < tlvType.length && tlvType[buf[ti]&TAG_SEQ] == TYPE.STRING)
			return String.format("T=%02x L=%d V=%s",tag(),l,Text.vis(buf,vi,l));
		else
			return String.format("T=%02x L=%d V=%s",tag(),l,Text.hex(buf,vi,l));

	}
}
