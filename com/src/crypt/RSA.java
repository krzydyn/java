package crypt;

import java.math.BigInteger;
import java.util.Random;

public class RSA {
    private BigInteger N;  
    private BigInteger e; 
    private BigInteger d;
    
    public RSA(int bitlength) {
    	Random r = new Random(); 
    	BigInteger p = BigInteger.probablePrime(bitlength, r);
        BigInteger q = BigInteger.probablePrime(bitlength, r);
        N = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        e = BigInteger.probablePrime(bitlength/2, r);
        while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0 ) { 
            e.add(BigInteger.ONE); 
        }
        d = e.modInverse(phi);  
    }
    public RSA(BigInteger e, BigInteger d, BigInteger N) {
    	this.e = e; 
        this.d = d; 
        this.N = N; 
    }
    
    public byte[] encrypt(byte[] msg) {
    	return new BigInteger(msg).modPow(e, N).toByteArray();
    }
    
    public byte[] decrypt(byte[] msg) {
    	return new BigInteger(msg).modPow(d, N).toByteArray();
    }
}
