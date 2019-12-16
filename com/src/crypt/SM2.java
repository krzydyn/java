package crypt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.spec.ECPoint;
import text.Text;

/**
 * <p><b>Standards for secure communication defined by the Chinese authorities</b><br>
 * http://www.programmersought.com/article/101352955/<br>
 * https://github.com/riboseinc/rfc-crypto-sm2/tree/master/sections<br>
 * </p>
 * <ul>
 * <li><b>SM2 public key cryptographic algorithm based on elliptic curves</b>br>
 * <pre>
 * 		https://tools.ietf.org/html/draft-shen-sm2-ecdsa-02
 * 		http://www.gmbz.org.cn/upload/2018-07-24/1532401863206085511.pdf
 * 		https://github.com/bcgit/bc-java/tree/master/core/src/test/java/org/bouncycastle/crypto/test
 * 		https://botan.randombit.net/doxygen/sm2_8cpp_source.html
 * 		https://arxiv.org/pdf/1808.02988.pdf
 * 		public key 512 bits and private key 256 bits
 * 		1. SM2DSA - elliptic curve based signature
 * 		2. SM2KEP - key exchange protocol
 * 		3. SM2PKE - public key encryption (pubkey is 512bits, prvkey is 256bits)
 * </pre>
 * <li> <b>SM3 hashing algorithm comparable to SHA-256</b><br>
 * 		output is 256bits
 * <li> <b>SM4 block cipher algorithm for symmetric cryptography comparable to AES-128</b>br>
 * 		https://eprint.iacr.org/2008/281.pdf
 * 		keySize = 128bits
 * 		blockSize = 128bits
 * </ul>
 *
 * <p>The registered OID entry of "SM2 Elliptic Curve Cryptography"<br>
 * <pre>2A 81 1C CF 55 01 82 2D</pre>
 * </p>
 * <p>Initialization Vector IV<br>
 * <pre>
 * 7380166f 4914b2b9 172442d7 da8a0600
 * a96f30bc 163138aa e38dee4d b0fb0e4e</pre>
 * </p>
 *
 * <p><b>Elliptic Curve Formula</b><br>
 *  y^2 = x^3 + ax + b</p>
 *
 * <p><b>Curve Parameters</b><br>
 * <pre>
 * p   = FFFFFFFE FFFFFFFF FFFFFFFF FFFFFFFF
 *       FFFFFFFF 00000000 FFFFFFFF FFFFFFFF
 * a   = FFFFFFFE FFFFFFFF FFFFFFFF FFFFFFFF
 *       FFFFFFFF 00000000 FFFFFFFF FFFFFFFC
 * b   = 28E9FA9E 9D9F5E34 4D5A9E4B CF6509A7
 *       F39789F5 15AB8F92 DDBCBD41 4D940E93
 * n   = FFFFFFFE FFFFFFFF FFFFFFFF FFFFFFFF
 *       7203DF6B 21C6052B 53BBF409 39D54123
 * x_G = 32C4AE2C 1F198119 5F990446 6A39C994
 *       8FE30BBF F2660BE1 715A4589 334C74C7
 * y_G = BC3736A2 F4F6779C 59BDCEE3 6B692153
 *       D0A9877C C62A4740 02DF32E5 2139F0A0
 *
 * p 8542D69E4C044F18E8B92435BF6FF7DE457283915C45517D722EDB8B08F1DFC3
 * a 787968B4FA32C3FD2417842E73BBFEFF2F3C848B6831D7E0EC65228B3937E498
 * b 63E4C6D3B23B0C849CF84241484BFE48F61D59A5B16BA06E6E12D1DA27C5249A
 * xG 421DEBD61B62EAB6746434EBC3CC315E32220B3BADD50BDC4C4E6C147FEDD43D
 * yG 0680512BCBB42C07D47349D2153B70C4E5D7FDFCBFA36EA1A85841B9E46E09A2
 * n: 8542D69E4C044F18E8B92435BF6FF7DD297720630485628D5AE74EE7C32E79B7
 * </pre>
 * </p>
 *
 * @author krzydyn
 *
 */

public class SM2 {
    static class ECCurve {
		private BigInteger p;
		private BigInteger a;
		private BigInteger b;
		static class Fp extends ECCurve {
			Fp(BigInteger ecc_p, BigInteger ecc_a, BigInteger ecc_b) {
			}
		}

	    public ECPoint decodePoint(byte[] encoded)
	    {
	        ECPoint p = null;

	        byte type = encoded[0];
	        switch (type)
	        {
	        case 0x00: // infinity
	        {
	            if (encoded.length != 1)
	            {
	                throw new IllegalArgumentException("Incorrect length for infinity encoding");
	            }

	            p = ECPoint.POINT_INFINITY;
	            break;
	        }
	        case 0x02: // compressed
	        case 0x03: // compressed
	        {
	            int yTilde = type & 1;
	            int len = encoded.length-1;
	            BigInteger X = new BigInteger(encoded, 1, len);

	            p = decompressPoint(yTilde, X);
	            break;
	        }
	        case 0x04: // uncompressed
	        {
	        	int len = (encoded.length - 1)/2;
	            BigInteger X = new BigInteger(encoded, 1, len);
	            BigInteger Y = new BigInteger(encoded, 1+len, len);
	            p = new ECPoint(X, Y);
	            break;
	        }
	        case 0x06: // hybrid
	        case 0x07: // hybrid
	        {
	        	int len = (encoded.length - 1)/2;
	            BigInteger X = new BigInteger(encoded, 1, len);
	            BigInteger Y = new BigInteger(encoded, 1+len, len);

	            if (Y.testBit(0) != (type == 0x07))
	            {
	                throw new IllegalArgumentException("Inconsistent Y coordinate in hybrid encoding");
	            }

	            p = new ECPoint(X, Y);
	            break;
	        }
	        default:
	            throw new IllegalArgumentException("Invalid point encoding 0x" + Integer.toString(type, 16));
	        }

	        if (type != 0x00 && p.equals(ECPoint.POINT_INFINITY))
	        {
	            throw new IllegalArgumentException("Invalid infinity encoding");
	        }

	        return p;
	    }
        protected ECPoint decompressPoint(int yTilde, BigInteger x)
        {
        	BigInteger rhs = x.multiply(x).add(a).multiply(x).add(b);
        	BigInteger y = rhs.sqrt();

            /*
             * If y is not a square, then we haven't got a point on the curve
             */
            if (y == null)
            {
                throw new IllegalArgumentException("Invalid point compression");
            }

            if (y.testBit(0) != (yTilde == 1))
            {
                // Use the other root
                y = y.negate();
            }

            return new ECPoint(x, y);
        }

		public ECPoint multiply(ECPoint ecc_point_g, BigInteger k) {
			// TODO Auto-generated method stub
			return null;
		}

		public ECPoint add(ECPoint x1y1, ECPoint multiply) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	static class ECDomainParameters {

		public ECDomainParameters(ECCurve ecc_curve, ECPoint ecc_point_g, BigInteger ecc_n) {
			// TODO Auto-generated constructor stub
		}

	}
	static class ECKeyPairGenerator {

		public void init(ECKeyGenerationParameters ecc_ecgenparam) {
			// TODO Auto-generated method stub

		}

	}
	static class ECKeyGenerationParameters {

		public ECKeyGenerationParameters(ECDomainParameters ecc_bc_spec, SecureRandom secureRandom) {
			// TODO Auto-generated constructor stub
		}

	}
	static class SM2Result {
		public BigInteger r;
		public BigInteger s;
		public BigInteger R;

	}
	static class Util {

		public static byte[] byteConvert32Bytes(BigInteger bi) {
			// TODO Auto-generated method stub
			return null;
		}

	}

    public static final String[] ECC_PARAM = {
            "8542D69E4C044F18E8B92435BF6FF7DE457283915C45517D722EDB8B08F1DFC3",
            "787968B4FA32C3FD2417842E73BBFEFF2F3C848B6831D7E0EC65228B3937E498",
            "63E4C6D3B23B0C849CF84241484BFE48F61D59A5B16BA06E6E12D1DA27C5249A",
            "8542D69E4C044F18E8B92435BF6FF7DD297720630485628D5AE74EE7C32E79B7",
            "421DEBD61B62EAB6746434EBC3CC315E32220B3BADD50BDC4C4E6C147FEDD43D",
            "0680512BCBB42C07D47349D2153B70C4E5D7FDFCBFA36EA1A85841B9E46E09A2"
    };

    public static SM2 Instance() {
        return new SM2();
    }

    public final BigInteger ecc_p;
    public final BigInteger ecc_a;
    public final BigInteger ecc_b;
    public final BigInteger ecc_n;
    public final BigInteger ecc_gx;
    public final BigInteger ecc_gy;
    public final ECCurve ecc_curve;
    public final ECPoint ecc_point_g;
    public final ECDomainParameters ecc_bc_spec;
    public final ECKeyPairGenerator ecc_key_pair_generator;

    public SM2() {
        this.ecc_p = new BigInteger(ECC_PARAM[0], 16);
        this.ecc_a = new BigInteger(ECC_PARAM[1], 16);
        this.ecc_b = new BigInteger(ECC_PARAM[2], 16);
        this.ecc_n = new BigInteger(ECC_PARAM[3], 16);
        this.ecc_gx = new BigInteger(ECC_PARAM[4], 16);
        this.ecc_gy = new BigInteger(ECC_PARAM[5], 16);

        this.ecc_curve = new ECCurve.Fp(this.ecc_p, this.ecc_a, this.ecc_b);
        this.ecc_point_g = new ECPoint(this.ecc_gx, this.ecc_gy);

        this.ecc_bc_spec = new ECDomainParameters(this.ecc_curve, this.ecc_point_g, this.ecc_n);

        ECKeyGenerationParameters ecc_ecgenparam;
        ecc_ecgenparam = new ECKeyGenerationParameters(this.ecc_bc_spec, new SecureRandom());

        this.ecc_key_pair_generator = new ECKeyPairGenerator();
        this.ecc_key_pair_generator.init(ecc_ecgenparam);
    }

    public byte[] sm2GetZ(byte[] userId, ECPoint userPubKey) {
        SM3Digest sm3 = new SM3Digest();

        int len = userId.length * 8;
        sm3.update((byte) (len >> 8 & 0xFF));
        sm3.update((byte) (len & 0xFF));
        sm3.update(userId, 0, userId.length);

        byte[] p = Util.byteConvert32Bytes(ecc_a);
        sm3.update(p, 0, p.length);

        p = Util.byteConvert32Bytes(ecc_b);
        sm3.update(p, 0, p.length);

        p = Util.byteConvert32Bytes(ecc_gx);
        sm3.update(p, 0, p.length);

        p = Util.byteConvert32Bytes(ecc_gy);
        sm3.update(p, 0, p.length);

        p = Util.byteConvert32Bytes(userPubKey.getAffineX());
        sm3.update(p, 0, p.length);

        p = Util.byteConvert32Bytes(userPubKey.getAffineY());
        sm3.update(p, 0, p.length);

        byte[] md = new byte[sm3.getDigestSize()];
        sm3.doFinal(md, 0);
        return md;
    }

    public void sm2Sign(byte[] md, BigInteger userPrivKey, SM2Result sm2Result) {
        BigInteger e = new BigInteger(1, md);
        BigInteger k = null;
        ECPoint kp = null;
        BigInteger r = null;
        BigInteger s = null;
        do {
            do {
                String kS = "6CB28D99385C175C94F94E934817663FC176D925DD72B727260DBAAE1FB2F96F";
                k = new BigInteger(kS, 16);
                kp = this.ecc_curve.multiply(ecc_point_g, k);

                // r
                r = e.add(kp.getAffineX());
                r = r.mod(ecc_n);
            } while (r.equals(BigInteger.ZERO) || r.add(k).equals(ecc_n));

            // (1 + dA)~-1
            BigInteger da_1 = userPrivKey.add(BigInteger.ONE);
            da_1 = da_1.modInverse(ecc_n);

            // s
            s = r.multiply(userPrivKey);
            s = k.subtract(s).mod(ecc_n);
            s = da_1.multiply(s).mod(ecc_n);
        } while (s.equals(BigInteger.ZERO));

        sm2Result.r = r;
        sm2Result.s = s;
    }

    public void sm2Verify(byte md[], ECPoint userKey, BigInteger r, BigInteger s, SM2Result sm2Result) {
        sm2Result.R = null;
        BigInteger e = new BigInteger(1, md);
        BigInteger t = r.add(s).mod(ecc_n);
        if (t.equals(BigInteger.ZERO)) {
            return;
        } else {
            ECPoint x1y1 = ecc_curve.multiply(ecc_point_g, sm2Result.s);
            System.out.println("X0: " + x1y1.getAffineX().toString(16));
            System.out.println("Y0: " + x1y1.getAffineY().toString(16));
            System.out.println("");

            x1y1 = ecc_curve.add(x1y1, ecc_curve.multiply(userKey, t));
            System.out.println("X1: " + x1y1.getAffineX().toString(16));
            System.out.println("Y1: " + x1y1.getAffineY().toString(16));
            System.out.println("");
            sm2Result.R = e.add(x1y1.getAffineX()).mod(ecc_n);
            System.out.println("R: " + sm2Result.R.toString(16));
            return;
        }
    }


    public static byte[] sign(byte[] userId, byte[] privateKey, byte[] sourceData) {
        if (privateKey == null || privateKey.length == 0) {
            return null;
        }

        if (sourceData == null || sourceData.length == 0) {
            return null;
        }

        SM2 sm2 = SM2.Instance();
        BigInteger userD = new BigInteger(privateKey);


        ECPoint userKey = sm2.ecc_curve.multiply(sm2.ecc_point_g,userD);

        SM3Digest sm3 = new SM3Digest();
        byte[] z = sm2.sm2GetZ(userId, userKey);

        sm3.update(z, 0, z.length);
        sm3.update(sourceData, 0, sourceData.length);
        byte[] md = new byte[32];
        sm3.doFinal(md, 0);


        SM2Result sm2Result = new SM2Result();
        sm2.sm2Sign(md, userD, sm2Result);
        System.out.println("r: " + sm2Result.r.toString(16));
        System.out.println("s: " + sm2Result.s.toString(16));
        System.out.println("");

		TLV der = TLV.create(0x30);
		der.add(TLV.create(0x02).setValue(sm2Result.r.toByteArray()));
		der.add(TLV.create(0x02).setValue(sm2Result.s.toByteArray()));
		return der.toByteArray();
        /*
        DERInteger dR = new DERInteger(sm2Result.r);
        DERInteger dS = new DERInteger(sm2Result.s);
        ASN1EncodableVector v2 = new ASN1EncodableVector();
        v2.add(dR);
        v2.add(dS);
        DERObject sign = new DERSequence(v2);
        byte[] signdata = sign.getDEREncoded();
        return signdata;
        */
    }

    public static boolean verifySign(byte[] userId, byte[] publicKey, byte[] sourceData, byte[] signData) {
        if (publicKey == null || publicKey.length == 0) {
            return false;
        }

        if (sourceData == null || sourceData.length == 0) {
            return false;
        }

        SM2 sm2 = SM2.Instance();
        ECPoint userKey = sm2.ecc_curve.decodePoint(publicKey);

        SM3Digest sm3 = new SM3Digest();
        byte[] z = sm2.sm2GetZ(userId, userKey);
        sm3.update(z, 0, z.length);
        sm3.update(sourceData, 0, sourceData.length);
        byte[] md = new byte[32];
        sm3.doFinal(md, 0);
        System.out.println("SM3" + Text.hex(md));
        System.out.println("");

		TLV der = null;
		try {
			der = TLV.load(new ByteArrayInputStream(signData));
		} catch (IOException e) {
			return false;
		}
		BigInteger r = new BigInteger(der.get(0).value());
		BigInteger s = new BigInteger(der.get(1).value());

		/*
        ByteArrayInputStream bis = new ByteArrayInputStream(signData);
        ASN1InputStream dis = new ASN1InputStream(bis);
        DERObject derObj = dis.readObject();
        Enumeration<DERInteger> e = ((ASN1Sequence) derObj).getObjects();
        BigInteger r = ((DERInteger) e.nextElement()).getValue();
        BigInteger s = ((DERInteger) e.nextElement()).getValue();
        */
        SM2Result sm2Result = new SM2Result();
        sm2Result.r = r;
        sm2Result.s = s;

        /*
        * debug
         */
        System.out.println("r: " + sm2Result.r.toString(16));
        System.out.println("s: " + sm2Result.s.toString(16));
        System.out.println("");

        sm2.sm2Verify(md, userKey, sm2Result.r, sm2Result.s, sm2Result);
        return sm2Result.r.equals(sm2Result.R);

    }
}

