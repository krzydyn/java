package crypt;

public class AES3 {
	protected static int[] rCon = new int[]{0x8d, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20,
	      0x40, 0x80, 0x1b, 0x36, 0x6c, 0xd8, 0xab, 0x4d, 0x9a, 0x2f, 0x5e, 0xbc,
	      0x63, 0xc6, 0x97, 0x35, 0x6a, 0xd4, 0xb3, 0x7d, 0xfa, 0xef, 0xc5, 0x91,
	      0x39, 0x72, 0xe4, 0xd3, 0xbd, 0x61, 0xc2, 0x9f, 0x25, 0x4a, 0x94, 0x33,
	      0x66, 0xcc, 0x83, 0x1d, 0x3a, 0x74, 0xe8, 0xcb, 0x8d, 0x01, 0x02, 0x04,
	      0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36, 0x6c, 0xd8, 0xab, 0x4d, 0x9a,
	      0x2f, 0x5e, 0xbc, 0x63, 0xc6, 0x97, 0x35, 0x6a, 0xd4, 0xb3, 0x7d, 0xfa,
	      0xef, 0xc5, 0x91, 0x39, 0x72, 0xe4, 0xd3, 0xbd, 0x61, 0xc2, 0x9f, 0x25,
	      0x4a, 0x94, 0x33, 0x66, 0xcc, 0x83, 0x1d, 0x3a, 0x74, 0xe8, 0xcb, 0x8d,
	      0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36, 0x6c, 0xd8,
	      0xab, 0x4d, 0x9a, 0x2f, 0x5e, 0xbc, 0x63, 0xc6, 0x97, 0x35, 0x6a, 0xd4,
	      0xb3, 0x7d, 0xfa, 0xef, 0xc5, 0x91, 0x39, 0x72, 0xe4, 0xd3, 0xbd, 0x61,
	      0xc2, 0x9f, 0x25, 0x4a, 0x94, 0x33, 0x66, 0xcc, 0x83, 0x1d, 0x3a, 0x74,
	      0xe8, 0xcb, 0x8d, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b,
	      0x36, 0x6c, 0xd8, 0xab, 0x4d, 0x9a, 0x2f, 0x5e, 0xbc, 0x63, 0xc6, 0x97,
	      0x35, 0x6a, 0xd4, 0xb3, 0x7d, 0xfa, 0xef, 0xc5, 0x91, 0x39, 0x72, 0xe4,
	      0xd3, 0xbd, 0x61, 0xc2, 0x9f, 0x25, 0x4a, 0x94, 0x33, 0x66, 0xcc, 0x83,
	      0x1d, 0x3a, 0x74, 0xe8, 0xcb, 0x8d, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20,
	      0x40, 0x80, 0x1b, 0x36, 0x6c, 0xd8, 0xab, 0x4d, 0x9a, 0x2f, 0x5e, 0xbc,
	      0x63, 0xc6, 0x97, 0x35, 0x6a, 0xd4, 0xb3, 0x7d, 0xfa, 0xef, 0xc5, 0x91,
	      0x39, 0x72, 0xe4, 0xd3, 0xbd, 0x61, 0xc2, 0x9f, 0x25, 0x4a, 0x94, 0x33,
	      0x66, 0xcc, 0x83, 0x1d, 0x3a, 0x74, 0xe8, 0xcb};
	protected static int[] sBox = new int[] {0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b,
	      0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76, 0xca, 0x82,
	      0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4,
	      0x72, 0xc0, 0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5,
	      0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15, 0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96,
	      0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75, 0x09, 0x83,
	      0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3,
	      0x2f, 0x84, 0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb,
	      0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf, 0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d,
	      0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8, 0x51, 0xa3,
	      0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff,
	      0xf3, 0xd2, 0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7,
	      0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73, 0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a,
	      0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb, 0xe0, 0x32,
	      0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95,
	      0xe4, 0x79, 0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56,
	      0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08, 0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6,
	      0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a, 0x70, 0x3e,
	      0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1,
	      0x1d, 0x9e, 0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e,
	      0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf, 0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6,
	      0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16};
	protected static int[] invSBox = new int[] {0x52, 0x09, 0x6a, 0xd5, 0x30,
		  0x36, 0xa5, 0x38, 0xbf, 0x40, 0xa3, 0x9e, 0x81, 0xf3, 0xd7, 0xfb, 0x7c,
	      0xe3, 0x39, 0x82, 0x9b, 0x2f, 0xff, 0x87, 0x34, 0x8e, 0x43, 0x44, 0xc4,
	      0xde, 0xe9, 0xcb, 0x54, 0x7b, 0x94, 0x32, 0xa6, 0xc2, 0x23, 0x3d, 0xee,
	      0x4c, 0x95, 0x0b, 0x42, 0xfa, 0xc3, 0x4e, 0x08, 0x2e, 0xa1, 0x66, 0x28,
	      0xd9, 0x24, 0xb2, 0x76, 0x5b, 0xa2, 0x49, 0x6d, 0x8b, 0xd1, 0x25, 0x72,
	      0xf8, 0xf6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xd4, 0xa4, 0x5c, 0xcc, 0x5d,
	      0x65, 0xb6, 0x92, 0x6c, 0x70, 0x48, 0x50, 0xfd, 0xed, 0xb9, 0xda, 0x5e,
	      0x15, 0x46, 0x57, 0xa7, 0x8d, 0x9d, 0x84, 0x90, 0xd8, 0xab, 0x00, 0x8c,
	      0xbc, 0xd3, 0x0a, 0xf7, 0xe4, 0x58, 0x05, 0xb8, 0xb3, 0x45, 0x06, 0xd0,
	      0x2c, 0x1e, 0x8f, 0xca, 0x3f, 0x0f, 0x02, 0xc1, 0xaf, 0xbd, 0x03, 0x01,
	      0x13, 0x8a, 0x6b, 0x3a, 0x91, 0x11, 0x41, 0x4f, 0x67, 0xdc, 0xea, 0x97,
	      0xf2, 0xcf, 0xce, 0xf0, 0xb4, 0xe6, 0x73, 0x96, 0xac, 0x74, 0x22, 0xe7,
	      0xad, 0x35, 0x85, 0xe2, 0xf9, 0x37, 0xe8, 0x1c, 0x75, 0xdf, 0x6e, 0x47,
	      0xf1, 0x1a, 0x71, 0x1d, 0x29, 0xc5, 0x89, 0x6f, 0xb7, 0x62, 0x0e, 0xaa,
	      0x18, 0xbe, 0x1b, 0xfc, 0x56, 0x3e, 0x4b, 0xc6, 0xd2, 0x79, 0x20, 0x9a,
	      0xdb, 0xc0, 0xfe, 0x78, 0xcd, 0x5a, 0xf4, 0x1f, 0xdd, 0xa8, 0x33, 0x88,
	      0x07, 0xc7, 0x31, 0xb1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xec, 0x5f, 0x60,
	      0x51, 0x7f, 0xa9, 0x19, 0xb5, 0x4a, 0x0d, 0x2d, 0xe5, 0x7a, 0x9f, 0x93,
	      0xc9, 0x9c, 0xef, 0xa0, 0xe0, 0x3b, 0x4d, 0xae, 0x2a, 0xf5, 0xb0, 0xc8,
	      0xeb, 0xbb, 0x3c, 0x83, 0x53, 0x99, 0x61, 0x17, 0x2b, 0x04, 0x7e, 0xba,
	      0x77, 0xd6, 0x26, 0xe1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0c, 0x7d};

    protected int actual;
    protected static int Nb = 4;
    protected int Nk;
    protected int Nr;
    protected int[][][] estado;
    protected int[] w;
    protected int[] llave;

    public AES3(byte[] entrada) {
        llave = new int[entrada.length];
        for (int i = 0; i < entrada.length; i++) {
            llave[i] = entrada[i];
        }
        Nb = 4;
        switch (entrada.length) {
            case 16:
                Nr = 10;
                Nk = 4;
                break;
            case 24:
                Nr = 12;
                Nk = 6;
                break;

            case 32:
                Nr = 14;
                Nk = 8;
                break;
            default:
                throw new IllegalArgumentException("Solamente soporta llaves de 128,192 y 256 bits");
        }
        estado = new int[2][4][Nb];
        w = new int[Nb * (Nr + 1)];
        expandirLlave();
    }
    protected int[][] addRoundKey(int[][] s, int ronda) {
        for (int c = 0; c < Nb; c++) {
            for (int r = 0; r < 4; r++) {
                s[r][c] = s[r][c] ^ ((w[ronda * Nb + c] << (r * 8)) >>> 24);
            }
        }
        return s;
    }
    protected int[][] cifrado(int[][] entrada, int[][] salida) {
        for (int i = 0; i < entrada.length; i++) {
            for (int j = 0; j < entrada[0].length; j++) {
                salida[i][j] = entrada[i][j];
            }
        }
        actual = 0;//Ronda 0
        addRoundKey(salida, actual);

        for (actual = 1; actual < Nr; actual++) {
            subBytes(salida);
            shiftRows(salida);
            mixColumns(salida);
            addRoundKey(salida, actual);
        }
        subBytes(salida);
        shiftRows(salida);
        addRoundKey(salida, actual);
        return salida;
    }
    protected int[][] inversoCifrar(int[][] entrada, int[][] salida) {
        for (int i = 0; i < entrada.length; i++) {
            for (int j = 0; j < entrada.length; j++) {
                salida[i][j] = entrada[i][j];
            }
        }
        actual = Nr;
        addRoundKey(salida, actual);

        for (actual = Nr - 1; actual > 0; actual--) {
            invShiftRows(salida);
            invSubBytes(salida);
            addRoundKey(salida, actual);
            invMixColumnas(salida);
        }
        invShiftRows(salida);
        invSubBytes(salida);
        addRoundKey(salida, actual);
        return salida;

    }
    public byte[] encrypt(byte[] bloque) {
        if (bloque.length != 16) {
            throw new IllegalArgumentException("Solamente se pueden cifrar bloques de 16byte");
        }
        byte[] almacena = new byte[bloque.length];

        for (int i = 0; i < Nb; i++) {//columnas
            for (int j = 0; j < 4; j++) {//filas
                estado[0][j][i] = bloque[i * Nb + j] & 0xff;
            }
        }
        //cifra dentro de s[2];
        cifrado(estado[0], estado[1]);
        for (int i = 0; i < Nb; i++) {
            for (int j = 0; j < 4; j++) {
                almacena[i * Nb + j] = (byte) (estado[1][j][i] & 0xff);
            }
        }
        return almacena;
    }
    public byte[] decrypt(byte[] bloque) {
        if (bloque.length != 16) {
            throw new IllegalArgumentException("Solamente se pueden decifrar bloques de 16byte");
        }
        byte[] almacena = new byte[bloque.length];

        for (int i = 0; i < Nb; i++) {//Columnas
            for (int j = 0; j < 4; j++) {//Filas
                estado[0][j][i] = bloque[i * Nb + j] & 0xff;
            }
        }
        //decifrar en s[2]
        inversoCifrar(estado[0], estado[1]);
        for (int i = 0; i < Nb; i++) {
            for (int j = 0; j < 4; j++) {
                almacena[i * Nb + j] = (byte) (estado[1][j][i] & 0xff);
            }
        }
        return almacena;

    }
    protected int[][] invMixColumnas(int[][] est) {
        int temp0, temp1, temp2, temp3;
        for (int c = 0; c < Nb; c++) {
            temp0 = mult(0x0e, est[0][c]) ^ mult(0x0b, est[1][c]) ^ mult(0x0d, est[2][c]) ^ mult(0x09, est[3][c]);
            temp1 = mult(0x09, est[0][c]) ^ mult(0x0e, est[1][c]) ^ mult(0x0b, est[2][c]) ^ mult(0x0d, est[3][c]);
            temp2 = mult(0x0d, est[0][c]) ^ mult(0x09, est[1][c]) ^ mult(0x0e, est[2][c]) ^ mult(0x0b, est[3][c]);
            temp3 = mult(0x0b, est[0][c]) ^ mult(0x0d, est[1][c]) ^ mult(0x09, est[2][c]) ^ mult(0x0e, est[3][c]);

            est[0][c] = temp0;
            est[1][c] = temp1;
            est[2][c] = temp2;
            est[3][c] = temp3;
        }
        return est;
    }
    protected int[][] invShiftRows(int[][] est) {
        int temp1, temp2, temp3, i; //Temporales usados para los desplazamientos
        //fila 1;
        temp1 = est[1][Nb - 1];
        for (i = Nb - 1; i > 0; i--) {
            est[1][i] = est[1][(i - 1) % Nb];
        }
        est[1][0] = temp1;
        //fila 2
        temp1 = est[2][Nb - 1];
        temp2 = est[2][Nb - 2];
        for (i = Nb - 1; i > 1; i--) {
            est[2][i] = est[2][(i - 2) % Nb];
        }
        est[2][1] = temp1;
        est[2][0] = temp2;
        //fila 3
        temp1 = est[3][Nb - 3];
        temp2 = est[3][Nb - 2];
        temp3 = est[3][Nb - 1];
        for (i = Nb - 1; i > 2; i--) {
            est[3][i] = est[3][(i - 3) % Nb];
        }
        est[3][0] = temp1;
        est[3][1] = temp2;
        est[3][2] = temp3;
        return est;
    }
    protected int[][] invSubBytes(int[][] est) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < Nb; j++) {
                est[i][j] = invSubWord(est[i][j]) & 0xFF;
            }
        }
        return est;
    }
    protected static int invSubWord(int palabra) {
        int subPalabra = 0;
        for (int i = 24; i >= 0; i -= 8) {
            int in = palabra << i >>> 24;
            subPalabra |= invSBox[in] << (24 - i);
        }
        return subPalabra;
    }
    protected int[] expandirLlave() {
        int temp, i = 0;
        while (i < Nk) {
            w[i] = 0x00000000;
            w[i] |= llave[4 * i] << 24;
            w[i] |= llave[4 * i + 1] << 16;
            w[i] |= llave[4 * i + 2] << 8;
            w[i] |= llave[4 * i + 3];
            i++;
        }
        i = Nk;
        while (i < Nb * (Nr + 1)) {
            temp = w[i - 1];
            if (i % Nk == 0) {
                //Aplica un XOR con una ronda constante rCon.
                temp = subWord(rotWord(temp)) ^ (rCon[i / Nk] << 24);
            } else if (Nk > 6 && (i % Nk == 4)) {
                temp = subWord(temp);
            } else {
            }
            w[i] = w[i - Nk] ^ temp;
            i++;
        }
        return w;
    }
    protected int[][] mixColumns(int[][] est) {
        int temp0, temp1, temp2, temp3;
        for (int c = 0; c < Nb; c++) {

            temp0 = mult(0x02, est[0][c]) ^ mult(0x03, est[1][c]) ^ est[2][c] ^ est[3][c];
            temp1 = est[0][c] ^ mult(0x02, est[1][c]) ^ mult(0x03, est[2][c]) ^ est[3][c];
            temp2 = est[0][c] ^ est[1][c] ^ mult(0x02, est[2][c]) ^ mult(0x03, est[3][c]);
            temp3 = mult(0x03, est[0][c]) ^ est[1][c] ^ est[2][c] ^ mult(0x02, est[3][c]);

            est[0][c] = temp0;
            est[1][c] = temp1;
            est[2][c] = temp2;
            est[3][c] = temp3;
        }

        return est;
    }
    protected static int mult(int part_a, int part_b) {
        int sum = 0;

        while (part_a != 0) { // Mientras no sea 0

            if ((part_a & 1) != 0) // Checar si el primer bit es 1
            {
                sum = sum ^ part_b; // Sumar b desde el bit mas pequeño
            }
            part_b = xtime(part_b); // bit shift left mod 0x11b si es necesario;

            part_a = part_a >>> 1; // lowest bit of a was used so shift right
        }
        return sum;

    }
    protected static int rotWord(int word) {
        return (word << 8) | ((word & 0xFF000000) >>> 24);
    }
    protected int[][] shiftRows(int[][] est) {
        int temp1, temp2, temp3, i;
        //fila 1
        temp1 = est[1][0];
        for (i = 0; i < Nb - 1; i++) {
            est[1][i] = est[1][(i + 1) % Nb];
        }
        est[1][Nb - 1] = temp1;

        // fila 2, se desplaza 1-byte
        temp1 = est[2][0];
        temp2 = est[2][1];
        for (i = 0; i < Nb - 2; i++) {
            est[2][i] = est[2][(i + 2) % Nb];
        }
        est[2][Nb - 2] = temp1;
        est[2][Nb - 1] = temp2;

        // fila 3, se desplaza 2-bytes
        temp1 = est[3][0];
        temp2 = est[3][1];
        temp3 = est[3][2];
        for (i = 0; i < Nb - 3; i++) {
            est[3][i] = est[3][(i + 3) % Nb];
        }
        est[3][Nb - 3] = temp1;
        est[3][Nb - 2] = temp2;
        est[3][Nb - 1] = temp3;

        return est;
    }
    protected int[][] subBytes(int[][] est) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < Nb; j++) {
                est[i][j] = subWord(est[i][j]) & 0xFF;
            }
        }
        return est;

    }
    protected static int subWord(int word) {
        int subWord = 0;
        for (int i = 24; i >= 0; i -= 8) {
            int in = word << i >>> 24;
            subWord |= sBox[in] << (24 - i);
        }
        return subWord;
    }
    /**
    * return: xb(x) mod x8+x4+x3+x+1.
    */
   protected static int xtime(int b) {
       if ((b & 0x80) == 0) {
           return b << 1;
       }
       return (b << 1) ^ 0x11b;
   }

}