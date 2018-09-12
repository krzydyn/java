package crypt;

import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;

/*
ANS.1 = Abstract Syntax Notation One (BER or DER)
BER - Basic Encoding Rules
DER - Distinguished Encoding Representation (subset of BER)
PKCS - Public Key Cryptography Standards
CMS - Cryptographic Message Syntax

Any DER or BER object can be PEM formatted
PEM = DER or BER encoded with base64

https://www.programcreek.com/java-api-examples/?api=sun.security.pkcs.PKCS7 (last rfc: 5652)

 */

public class PKCS7 {

	static void load(String file) throws Exception {
		FileInputStream is = new FileInputStream("cert.pkcs7");
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		Iterator<?> i = cf.generateCertificates(is).iterator();
		while (i.hasNext()) {
			//java.security.cert.Certificate c = (Certificate)i.next();
			X509Certificate c = (X509Certificate) i.next();
			System.out.println(c.toString());
		}
	}
}
