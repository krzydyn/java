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
 * <h3>Padding</h3>
 * <ul>
 * <li>No padding (message length = k*block_size)
 * <li>Zero padding: DATA,0x00...
 * <li>ANSI X.923: DATA,0x00,0x00,...NUM_OF_PAD_BYTES
 * <li>ISO 10126: DATA,RND,RND,...NUM_OF_PAD_BYTES
 * <li>PKCS7/PKCS5: DATA,NUM_OF_PAD_BYTES,...NUM_OF_PAD_BYTES
 * <li>ISO/IEC 7816-4: DATA,0x80,0x00...
 * </ul>
 * <h3>Block Cipher Modes (of chaining blocks)</h3>
 * <b>Note:</b> CFB, OFB, CTR does not require padding
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
 * 		P[i] = C[i] XOR encrypt(K,CTR[i])
 * </ul>
 *
 * @author krzydyn
 *
 */

public interface Cipher {
	final public int ENCRYPT=0;
	final public int DECRYPT=1;
	public void init(int mode);
	public int update(byte[] data, int offs, int len, byte[] out, int outoffs);
	public int finish(byte[] out, int outoffs);
}
