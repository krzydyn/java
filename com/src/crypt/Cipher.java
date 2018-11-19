/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package crypt;

// https://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/HowToImplAProvider.html

/**
 * <h2>Theory</h2>
 * <b>Block Ciphers</b>
 * <ul>
 * <li> DES (56)
 * <li> 3DES (112,168)
 * <li> AES (128,192,256)
 * </ul><br><br>
 * <b>Padding modes</b>
 * <ul>
 * <li><b>No padding</b> (message length = k*block_size)
 * <li><b>Zero padding</b> DATA,0x00...
 * <li><b>ANSI X.923</b> DATA,0x00,0x00,...NUM_OF_PAD_BYTES
 * <li><b>ISO 10126</b> DATA,RND,RND,...NUM_OF_PAD_BYTES
 * <li><b>PKCS5/PKCS7</b> DATA,NUM_OF_PAD_BYTES,...NUM_OF_PAD_BYTES<br>
 * 			PKCS5 only block_size 8, PKCS7 for any block_size 1..255
 * <li><b>ISO/IEC 7816-4</b> DATA,0x80,0x00...
 * <li><b>ISO/IEC 9797/M1</b> DATA,0x00,0x00,... (same as ZeroPading)
 * <li><b>ISO/IEC 9797/M2</b> DATA,0x80,0x00,....(same as ISO/IEC 7816-4)
 * <li><b>ISO/IEC 9797/M3</b> pad 0, des CBC, des3 on last block
 * <li><b>CTS</b> pad last block with tail of encrypted prev block, encrypt last, and swap
 *
 * </ul>
 * <h3>Block Cipher Modes (of chaining blocks)</h3>
 * <b>NOTE:</b> CFB, OFB, CTR does not require padding and return same length as input
 * <ul>
 * <li><b>ECB (Electronic Codebook)</b><br>
 *		C[i] = encrypt(K,P[i])<br>
 *		P[i] = decrypt(K,C[i])<br>
 *
 * <li><b>CBC (Cipher Block Chaining)</b><br>
 * 		C[i]=encrypt(K,P[i] XOR C[i-1]), C[0]=IV<br>
 * 		P[i]=decrypt(K,C[i]) XOR C[i-1], C[0]=IV
 *
 * <li><b>PCBC (Propagating Cipher Block Chaining)</b><br>
 * 		C[i]=encrypt(K,P[i] XOR P[i-1] XOR C[i-1]), P[0] XOR C[0]=IV<br>
 * 		P[i]=decrypt(K,C[i]) XOR P[i-1] XOR C[i-1], P[0] XOR C[0]=IV
 *
 * <li><b>CFB (Cipher Feedback)</b><br>
 *		C[i] = encrypt(K,C[i-1]) XOR P[i-1], C[0]=IV<br>
 *		P[i] = decrypt(K,C[i-1]) XOR C[i], C[0]=IV
 *
 * <li><b>OFB (Output Feedback)</b><br>
 * 		O[i] = encrypt(K,O[i-1]), O[0] = IV<br>
 * 		C[i] = P[i] XOR O[i]<br>
 * 		P[i] = C[i] XOR O[i]
 *
 * <li><b>CTR (Counter, Integer Counter Mode)</b><br>
 * 		CTR[i] = CTR[i-1]+1, CTR[0]=0<br>
 * 		C[i] = P[i] XOR encrypt(K,CTR[i])<br>
 * 		P[i] = C[i] XOR decrypt(K,CTR[i])
 *
 * <li><b>GCM (Galois Counter Mode)</b> https://en.wikipedia.org/wiki/Galois/Counter_Mode<br>
 * 		CTR[i] = CTR[i-1]+1, CTR[0]= IV if len(IV)=12, otherwise CTR[0]=GHASH(H,{},IV)<br>
 * 		C[i] = P[i] XOR encrypt(K,CTR[i])<br>
 * 		P[i] = C[i] XOR encrypt(K,CTR[i])<br>
 * 		H[i] = C[i] XOR H[i-1], H[0]=AuthData1  (AAD)<br>
 * 		AuthTag = H[n] XOR (len(P)||len(C)) XOR encrypt(CTR[0])<br>
 * 		Note: || = concatenation<br>
 * 		(Java appends AuthTag on the end of encrypted buffer)
 *
 * <li><b>CCM (Counter with CBC-MAC)</b><br>
 *		P' = Flags || Nonce || P
 *		Flags = 0(1bit) || Adata(1bit) || M'(3bits) || L'(3bits)
 *		Adata bit: 0 if len(A)==0, 1 if len(A)>0
 *		M' = (M-2)/2, where 4 <= M <= 16, M=len(AUTH_FIELD)
 *		L' = L-1, where 2 <= L <= 8, L=len(LENGTH_FIELD)
 *		Note: || = concatenation<br>
 * </ul>
 *
 * @author krzydyn
 *
 */

public interface Cipher extends CipherBlock {
	public int update(byte[] data, int offs, int len, byte[] out, int outoffs);
	public int finish(byte[] out, int outoffs);
}
