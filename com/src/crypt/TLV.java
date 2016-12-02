package crypt;

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

	public int read(byte[] b, int i, int j) {
		return 0;
	}

	public boolean isComplex() {
		// TODO Auto-generated method stub
		return false;
	}

	public byte[] getVal() {
		// TODO Auto-generated method stub
		return null;
	}

}
