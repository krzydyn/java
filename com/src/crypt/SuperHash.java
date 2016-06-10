package crypt;
// Based on article by Paul Hsieh
// http://www.azillionmonkeys.com/qed/hash.html

public class SuperHash implements Digest {
	int hash; //32-bit hash
	byte[] hashrep = new byte[4];

	@Override
	public void init(byte[] initval) {
		hash=0;
		if (initval!=null) {
			for (int i=0; i < initval.length && i < 4; ++i) {
				hash <<= 8;
				hash=initval[i];
			}
		}
	}

	private int get16bits(byte[] data, int offs) {
		return (data[offs]<<8) | data[offs+1];
	}

	@Override
	public void update(byte[] data, int offs, int len) {
		int rem=len&3;
		len>>=2;

		int tmp;
		 /* Main loop */
	    for (;len > 0; len--) {
	        hash  += get16bits (data,offs);
	        tmp    = (get16bits (data,offs+2) << 11) ^ hash;
	        hash   = (hash << 16) ^ tmp;
	        hash  += hash >> 11;
	        offs  += 4;
	    }

	    /* Handle end cases */
	    switch (rem) {
	        case 3: hash += get16bits(data, offs);
	                hash ^= hash << 16;
	                hash ^= data[offs+2] << 18;
	                hash += hash >> 11;
	                break;
	        case 2: hash += get16bits(data, offs);
	                hash ^= hash << 11;
	                hash += hash >> 17;
	                break;
	        case 1: hash += data[offs];
	                hash ^= hash << 10;
	                hash += hash >> 1;
	    }

	    /* Force "avalanching" of final 127 bits */
	    hash ^= hash << 3;
	    hash += hash >> 5;
	    hash ^= hash << 4;
	    hash += hash >> 17;
	    hash ^= hash << 25;
	    hash += hash >> 6;
	}

	@Override
	public byte[] finish() {
		for (int i=0; i < hashrep.length; ++i) {
			hashrep[i] = (byte)((hash>>i)&0xff);
		}
		return hashrep;
	}

}
