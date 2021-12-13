// Arup Guha
// 11/5/2012
// Stores a single point on a specific Elliptic Curve
/*** Edited on 11/18/2019 - hopefully I fixed most of the crashing issues!!! ***/

import java.math.*;

public class Point {

	// Store the x, y and curve.
	private BigInteger x;
	private BigInteger y;
	private EllipticCurve curve;

	// Precondition: (myX, myY) must lie on the curve c. I don't check that here!!!
	public Point(EllipticCurve c, BigInteger myX, BigInteger myY) {
		x = myX;
		y = myY;
		curve = c;
	}

	// Copy constructor.
	public Point(Point copy) {
		x = new BigInteger(copy.x.toString());
		y = new BigInteger(copy.y.toString());
		curve = new EllipticCurve(copy.curve);
	}

	// Returns 0. Not sure if this is the proper way to store the "origin".
	public Point(EllipticCurve c) {
		curve = c;
		x = BigInteger.ZERO;
		y = BigInteger.ZERO;
	}

	// All components must be equal...
	public boolean equals(Point other) {
		if (this.isOrigin() && other.isOrigin()) return true;
		return x.equals(other.x) && y.equals(other.y) && curve.equals(other.curve);
	}

	// Returns true iff other is this point's reflection over the line y = p/2 (real division)
	public boolean mirror(Point other) {
		if (this.isOrigin() && other.isOrigin()) return true;
		return x.equals(other.x) && curve.equals(other.curve) && y.equals(other.curve.getP().subtract(other.y));
	}

	// Returns the negative of this point, which is its mirror.
	public Point negate() {
		if (isOrigin()) return this;
		BigInteger newY = curve.getP().subtract(y);
		return new Point(curve, x, newY);
	}

	// Adds this to other and returns the answer, using the formulas in Stallings (5th edition)
	public Point add(Point other) {

		// Can't add points on different curves.
		if (!curve.equals(other.curve)) return null;

		if (this.equals(other)) {

			// Avoid adding to the origin...
			if (this.y.equals(BigInteger.ZERO)) return new Point(curve);

			// We need these to calculate lambda.
			BigInteger three = new BigInteger("3");
			BigInteger two = new BigInteger("2");
			BigInteger temp = new BigInteger(x.toString());

			// Splitting up the calculation of lambda into all of these steps...
			BigInteger lambda = temp.modPow(two, curve.getP());
			lambda = three.multiply(lambda);
			lambda = lambda.add(curve.getA());
			BigInteger den = two.multiply(y);
			lambda = lambda.multiply(den.modInverse(curve.getP()));

			// Once we have lambda, just plug into these equations.
			BigInteger newX = lambda.multiply(lambda).subtract(x).subtract(x).mod(curve.getP());
			BigInteger newY = (lambda.multiply(x.subtract(newX))).subtract(y).mod(curve.getP());
			return new Point(curve, newX, newY);
		}

		// Returns the origin...not sure if my origin is correct.
		else if (this.mirror(other)) {
			return new Point(curve);
		}

		// Standard case.
		else {

			// Avoid adding by the identity element.
			if (this.isOrigin()) return new Point(other);
			if (other.isOrigin()) return new Point(this);
			
			// We need these to calculate lambda.
			BigInteger three = new BigInteger("3");
			BigInteger two = new BigInteger("2");
			BigInteger temp = new BigInteger(x.toString());

			// Lambda's a bit easier here...
			BigInteger lambda = other.y.subtract(y);
			BigInteger den = other.x.subtract(x);
			lambda = lambda.multiply(den.modInverse(curve.getP()));

			// This calculation is roughly the same as above.
			BigInteger newX = lambda.multiply(lambda).subtract(x).subtract(other.x).mod(curve.getP());
			BigInteger newY = (lambda.multiply(x.subtract(newX))).subtract(y).mod(curve.getP());
			return new Point(curve, newX, newY);
		}
	}

	// Subtraction is just adding the negative.
	public Point subtract(Point other) {
		other = other.negate();
		return this.add(other);
	}

	// Uses "fast multiplication" to multiply this point by factor.
	public Point multiply(BigInteger factor) {
		
		BigInteger two = new BigInteger("2");

		// Base cases.
		if (factor.equals(BigInteger.ZERO))
			return new Point(curve);		
		if (factor.equals(BigInteger.ONE))
			return new Point(this);
		if (factor.equals(two))
			return this.add(this);

		// Even case where we can calculate half of our answer and multiply by 2!
		if (factor.mod(two).equals(BigInteger.ZERO)) {
			Point sqrt = multiply(factor.divide(two));
			return sqrt.add(sqrt);
		}

		// No speed up here, but this recursive call will lead to one.
		else {
			factor = factor.subtract(BigInteger.ONE);
			return this.add(multiply(factor));
		}
	}

	public boolean isOrigin() {
		return x.equals(BigInteger.ZERO) && y.equals(BigInteger.ZERO);
	}

	public String toString() {
		return "(" + x +", "+y+")";
	}

	public static void main(String[] args) {

		// Test out Point arithmetic.
		EllipticCurve myCurve = new EllipticCurve(new BigInteger("23"), new BigInteger("1"), new BigInteger("1"));
		Point p = new Point(myCurve, new BigInteger("3"), new BigInteger("10"));
		Point q = new Point(myCurve, new BigInteger("13"), new BigInteger("16"));
		
		Point res = new Point(p);
		for (int i=1; i<=30; i++) {
			System.out.println(i+": "+res);
			res = res.add(p);
		}

	}
}
