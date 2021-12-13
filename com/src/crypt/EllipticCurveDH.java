// Arup Guha
// 11/5/2012
// Executes a Diffie-Hellman Key Exchange via an Elliptic Curve.
// Edited on 11/18/2019 - Hopefully the bugs are removed.

import java.util.*;
import java.math.*;

public class EllipticCurveDH {

	// Parts of one EC-Diffie Hellman system.
	private EllipticCurve curve;
	private Point generator;
	private Point publicKey;
	private BigInteger privateKey;

	// We need a curve, a generator point (x,y) and a private key, nA, that will
	// be used to generate the public key.
	public EllipticCurveDH(EllipticCurve c, BigInteger x, BigInteger y, BigInteger nA) {
		curve = c;
		generator = new Point(curve, x, y);
		privateKey = nA;
		publicKey = generator.multiply(privateKey);
	}

    public static void main(String[] args) {

    	Scanner stdin = new Scanner(System.in);

		// Set up curve and generator.
        EllipticCurve myCurve = new EllipticCurve(new BigInteger("23"), new BigInteger("1"), new BigInteger("1"));
		Point gen = new Point(myCurve, new BigInteger("3"), new BigInteger("13"));
		gen = new Point(myCurve, new BigInteger("19"), new BigInteger("18"));
		
		// I am testing lots of cases here for private numbers for Alice and Bob.
		for (int a=1; a<30; a++) {
			for (int b=1; b<30; b++) {
				
				BigInteger nAlice = new BigInteger(""+a);
				BigInteger nBob = new BigInteger(""+b);
				
				// Calculate & print public "keys" exchanged.
				Point aliceSend = gen.multiply(nAlice);
				Point bobSend = gen.multiply(nBob);
				
				/*** Comment this back in if you want to see what gets sent.
				//System.out.println("Alice sends to Bob: "+aliceSend);
				//System.out.println("Bob sends ot Alice: "+bobSend);
				***/

				// Make both calculations for the shared key.
				Point aliceGets = bobSend.multiply(nAlice);
				Point bobGets = aliceSend.multiply(nBob);

				/*** Comment this back in if you want to see exchanged key.
				//System.out.println("Alice gets "+aliceGets);
				//System.out.println("Bob gets "+bobGets);
				***/
				
				// Check for errors here.
				if (!aliceGets.equals(bobGets)) {
					System.out.println("ERROR "+a+" "+b);
					System.out.println("Alice sends to Bob: "+aliceSend);
					System.out.println("Bob sends ot Alice: "+bobSend);
					System.out.println("Alice gets "+aliceGets);
					System.out.println("Bob gets "+bobGets);
				}
			}
		}
    }
}
