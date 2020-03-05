package unittest;

import concur.RingByteBuffer;
import concur.RingArray;
import sys.Log;
import text.Text;

public class T_Buffers extends UnitTest {

	static void ringbuffer() {
		RingArray<int[]> b=new RingArray<int[]>(10);
	}
	static void bytebuffer() {
		byte[] ibuf=new byte[10];
		byte[] obuf=new byte[10];
		for (int i=0; i < obuf.length; ++i) obuf[i]=(byte)(i+1);
		RingByteBuffer b=new RingByteBuffer(10);
		check("should be 5",b.write(obuf,0,5)==5);
		check("should be 5",b.write(obuf,5,5)==5);
		check("should be 0",b.write(obuf,3,5)==0);
		check("should be 10", b.read(ibuf)==10);
		check(ibuf,obuf);

		int o1=0;
		for (int i=0; i < 10; ++i) {
			if (o1+5 > obuf.length) o1=0;
			int r;
			try {
				r=b.write(obuf,o1,5);
			}
			catch (Throwable e) {
				Log.error(e, "o1=%d l=%d",o1,5);
				break;
			}
			if (r==0) break;
			b.read(ibuf,0,4);
			Log.info(Text.join(",", ibuf,0,4));
		}
	}
}
