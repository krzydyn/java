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

universal tags(t0) meaning
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
*/
public class TLV {
	static final int TAG_SEQ   = 0x1f;
	static final int TAG_NEXT  = 0x80;
	static final int TAG_CONST = 0x20;
	static final int LEN_BYTE  = 0x80;
	final int fixedtag;
	final int fixedlen;
	int t;
	int l;
	int vi;
	byte[] buf;

	TLV() {fixedtag=0; fixedlen=0;}
	TLV(int ft,int fl) {fixedtag=ft; fixedlen=fl;}

	public int read(byte[] b, int offs, int len) {
		int i=0;
		t=0; vi=-1; l=0;
		while (i < len && b[offs+i]==0) ++i;
		if (i >= len) return i;
		buf = b;
		t = b[offs+i];

		if (fixedtag > 0) {
			for (int ii=1; ii < fixedtag; ++ii) {
				t <<= 8; t |= b[offs+i]&0xff; ++i;
			}
			Log.debug("fixed tag = %x", t);
		}
		else if ((t&TAG_SEQ) == TAG_SEQ) {
			Log.debug("long tag = %x", t);
			do
				{ ++i; t <<= 8; t |= b[offs+i]&0xff; }
			while(offs+i < len && (b[offs+i]&TAG_NEXT) != 0);
		}
		else {
			Log.debug("short tag = %x", t);
		}
		++i;
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
			Log.debug("long len = %d (ll=%d)", l, ll);
		}
		else {
			Log.debug("short len = %d", l);
		}
		vi = offs+i;
		return i+l;
	}

	public boolean isConstructed() {
		int t0 = t <= 0xff ? t : (t >> 8)&0xff;
		return (t0&TAG_CONST) != 0;
	}

	public int getValIdx() {
		return vi;
	}

	@Override
	public String toString() {
		Integer x = 5;
		x += 10;
		return String.format("T=%02x L=%d V=%s",t,l,Text.hex(buf,vi,l));
	}
}
