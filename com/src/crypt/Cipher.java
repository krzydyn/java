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

/**
 * <h2>Theory</h2>
 * <b>Block Ciphers</b>
 * <ul>
 * <li> DES (56)
 * <li> 3DES (112,168)
 * <li> AES (128,192,256)
 * </ul><br><br>
 * <b>Padding</b>
 * <ul>
 * <li>No padding (message length = k*block_size)
 * <li>Zero padding: DATA,0x00...
 * <li>ANSI X.923: DATA,0x00,0x00,...NUM_OF_PAD_BYTES
 * <li>ISO 10126: DATA,RND,RND,...NUM_OF_PAD_BYTES
 * <li>PKCS5/PKCS7: DATA,NUM_OF_PAD_BYTES,...NUM_OF_PAD_BYTES<br>
 * 		PKCS5:8 bytes block, PKCS7: 16 bytes block
 * <li>ISO/IEC 7816-4: DATA,0x80,0x00...
 * <li>ISO/IEC 9797/M1: DATA,0x00,0x00,... (same as ZeroPading)
 * <li>ISO/IEC 9797/M2: DATA,0x80,0x00,....(same as ISO/IEC 7816-4)
 * <li>ISO/IEC 9797/M3: pad 0, des CBC, des3 on last block
 * </ul>
 * <h3>Block Cipher Modes (of chaining blocks)</h3>
 * <b>NOTE:</b> CFB, OFB, CTR does not require padding and return same length as input
 * <ul>
 * <li>ECB (Electronic Codebook)<br>
 *		C[i] = encrypt(K,P[i])<br>
 *		P[i] = decrypt(K,C[i])<br>
 *
 * <li>CBC (Cipher Block Chaining)<br>
 * 		C[i]=encrypt(K,P[i] XOR C[i-1]), C[0]=IV<br>
 * 		P[i]=decrypt(K,C[i]) XOR C[i-1], C[0]=IV
 *
 * <li>PCBC (Propagating Cipher Block Chaining)<br>
 * 		C[i]=encrypt(K,P[i] XOR P[i-1] XOR C[i-1]), P[0] XOR C[0]=IV<br>
 * 		P[i]=decrypt(K,C[i]) XOR P[i-1] XOR C[i-1], P[0] XOR C[0]=IV
 *
 * <li>CFB (Cipher Feedback)<br>
 *		C[i] = encrypt(K,C[i-1]) XOR P[i-1], C[0]=IV<br>
 *		P[i] = decrypt(K,C[i-1]) XOR C[i], C[0]=IV
 *
 * <li>OFB (Output Feedback)<br>
 * 		O[i] = encrypt(K,O[i-1]), O[0] = IV<br>
 * 		C[i] = P[i] XOR O[i]<br>
 * 		P[i] = C[i] XOR O[i]
 *
 * <li>CTR (Counter, Integer Counter Mode)<br>
 * 		CTR[i] = CTR[i-1]+1, CTR[0]=0<br>
 * 		C[i] = P[i] XOR encrypt(K,CTR[i])<br>
 * 		P[i] = C[i] XOR decrypt(K,CTR[i])
 *
 * <li>GCM (Galois Counter Mode) https://en.wikipedia.org/wiki/Galois/Counter_Mode<br>
 * 		CTR[i] = CTR[i-1]+1, CTR[0]=0<br>
 * 		C[i] = P[i] XOR encrypt(K,CTR[i])<br>
 * 		P[i] = C[i] XOR encrypt(K,CTR[i])<br>
 * 		H[i] = C[i] XOR H[i-1], H[0]=AuthData1  (AAD)<br>
 * 		AuthTag = H[n] XOR (len(P)||len(C)) XOR encrypt(CTR[0])
 *
 * <li>CCM (Counter with CBC-MAC)<br>
 *		P' = Flags || Nonce || P
 *		Flags = 0(1bit) || Adata(1bit) || M'(3bits) || L'(3bits)
 *		Adata bit: 0 if len(A)==0, 1 if len(A)>0
 *		M' = (M-2)/2, where 4 <= M <= 16, M=len(AUTH_FIELD)
 *		L' = L-1, where 2 <= L <= 8, L=len(LENGTH_FIELD)
 * </ul>
 *
 * @author krzydyn
 *
 */

public interface Cipher extends CipherBlock {
	public int update(byte[] data, int offs, int len, byte[] out, int outoffs);
	public int finish(byte[] out, int outoffs);
}
