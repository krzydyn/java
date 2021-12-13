// Arup Guha
// 11/5/2012
// Simple Example Illustrating Elliptic Curve Cryptography

// Known Bugs: My code for encryption crashes sometimes. I believe this is when the singularity point (origin)
//             is an answer to an intermediate calculation. My best guess is that I am incorrectly defining the
//             origin.

// Improvements to be made: The way this is set up, the user is required to know both the x and y coordinate of
//                          the point they are encrypting. Perhaps this is required, but I don't how to
//                          definitively generate large points on an EC, so my example just uses the textbook
//                          curve E23(1,1). I'd like to find a way to verify and generate points on a large curve
//						    so that I can test out my multiply function truly making use of the fast multiply.

/*** Edited on 11/18/2019 - I think at least that this code won't crash any more!!! ***/

import java.math.*;
import java.util.*;

public class ECC {
	
	// Parts of one ECC system.
	private EllipticCurve curve;
	private Point generator;
	private Point publicKey;
	private BigInteger privateKey;
	
	// We need a curve, a generator point (x,y) and a private key, nA, that will
	// be used to generate the public key.
	public ECC(EllipticCurve c, BigInteger x, BigInteger y, BigInteger nA) {
		
		curve = c;
		generator = new Point(curve, x, y);
		privateKey = nA;
		publicKey = generator.multiply(privateKey);
	}
	
	// Encryption.
	public Point[] encrypt(Point plain) {
		
		// First we must pick a random k, in range.
		int bits = curve.getP().bitLength();
		BigInteger k = new BigInteger(bits, new Random());
		System.out.println("Picked "+k+" as k for encrypting.");
		
		// Our output is an ordered pair, (k*generator, plain + k*publickey)
		Point[] ans = new Point[2];
		ans[0] = generator.multiply(k);
		ans[1] = plain.add(publicKey.multiply(k));
		return ans;
	}
	
	// Decryption - notice the similarity to El Gamal!!!
	public Point decrypt(Point[] cipher) {
		
		// This is what we subtract out.
		Point sub = cipher[0].multiply(privateKey);
		
		// Subtract out and return.
		System.out.println("sub of "+cipher[1]+" - "+sub);
		return cipher[1].subtract(sub);
	}
	
	public String toString() {
		
		return "Gen: "+generator+"\n"+
			   "pri: "+privateKey+"\n"+
			   "pub: "+publicKey;
	}
	
	public static void main(String[] args) {
		
		
		// Just use the book's curve and test.
		EllipticCurve myCurve = new EllipticCurve(new BigInteger("23"), new BigInteger("1"), new BigInteger("1"));
		BigInteger x = new BigInteger("6");
		BigInteger y = new BigInteger("19");
		BigInteger nA = new BigInteger("10");
		ECC Alice = new ECC(myCurve, x, y, nA);
		
		// Calculate all points on the curve.
		Point[] pts = new Point[28];
		pts[0] = new Point(myCurve, BigInteger.ZERO, BigInteger.ZERO);
		Point tmp = new Point(myCurve, new BigInteger("3"), new BigInteger("13"));
		for (int i=1; i<28; i++)
			pts[i] = pts[i-1].add(tmp);
		
		// Try encrypting each one!
		for (int i=1; i<28; i++) {
			
			// Assigning the point to encrypt.
			Point plain = pts[i];
			System.out.println("encrypting "+plain);
		
			// Encrypt and print.
			Point[] cipher = Alice.encrypt(plain);
			System.out.println("cipher first part "+cipher[0]);
			System.out.println("cipher second part "+cipher[1]);
		
			// Decrypt and verify.
			Point recover = Alice.decrypt(cipher);
			System.out.println("recovered "+recover);
		}
	}
}
