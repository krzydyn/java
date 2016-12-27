package so_tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public class OP_41345269 {

	public static void main(String[] args) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		BigDecimal d = BigDecimal.valueOf(-0.00390625);
		dos.writeInt(d.scale());
		dos.write(d.unscaledValue().toByteArray());
		dos.close(); // flush
		byte[] array = bos.toByteArray();

		ByteArrayInputStream bis = new ByteArrayInputStream(array);
		DataInputStream dis = new DataInputStream(bis);
		int sc = dis.readInt(); //grab 4 bytes
		BigInteger unscaledVal = new BigInteger(Arrays.copyOfRange(array, 4, array.length));
		System.out.println(new BigDecimal(unscaledVal, sc));
	}

}
