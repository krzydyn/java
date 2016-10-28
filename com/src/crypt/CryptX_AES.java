package crypt;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Provider.Service;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import sys.Log;
import text.Text;

// http://codeandme.blogspot.com/2013/07/writing-your-own-jca-extensions-full.html

public class CryptX_AES extends CipherSpi {
	//javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("");

	@Override
	protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen)
			throws IllegalBlockSizeException, BadPaddingException {
		return engineUpdate(input, inputOffset, inputLen);
	}

	@Override
	protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
				throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
		return engineUpdate(input, inputOffset, inputLen, output, outputOffset);
	}

	@Override
	protected int engineGetBlockSize() {
		return 1;
	}

	@Override
	protected byte[] engineGetIV() {
		return null;
	}

	@Override
	protected int engineGetOutputSize(int inputLen) {
		return 0;
	}

	@Override
	protected AlgorithmParameters engineGetParameters() {
		return null;
	}

	@Override
	protected void engineInit(int opmode, Key key, SecureRandom random)
			throws InvalidKeyException {
		Log.debug("opmode=%d key, rand", opmode);
	}

	@Override
	protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException,
			InvalidAlgorithmParameterException {
		engineInit(opmode, key, random);
	}

	@Override
	protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException,
			InvalidAlgorithmParameterException {
		engineInit(opmode, key, random);
	}

	@Override
	protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
		Log.debug("mode=%s",mode);
	}

	@Override
	protected void engineSetPadding(String padding) throws NoSuchPaddingException {
		Log.debug("padding=%s",padding);
	}

	@Override
	protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException {
		// TODO Auto-generated method stub
		return 0;
	}


    private final static String ATTR_MODE = "SupportedModes";
    // Provider attribute name for supported padding names
    private final static String ATTR_PAD  = "SupportedPaddings";

    private final static int S_NO    = 0;       // does not support
    private final static int S_MAYBE = 1;       // unable to determine
    private final static int S_YES   = 2;       // does support

    private static class Transform {
        // transform string to lookup in the provider
        final String transform;
        // the mode/padding suffix in upper case. for example, if the algorithm
        // to lookup is "DES/CBC/PKCS5Padding" suffix is "/CBC/PKCS5PADDING"
        // if loopup is "DES", suffix is the empty string
        // needed because aliases prevent straight transform.equals()
        final String suffix;
        // value to pass to setMode() or null if no such call required
        final String mode;
        // value to pass to setPadding() or null if no such call required
        final String pad;
        Transform(String alg, String suffix, String mode, String pad) {
            this.transform = alg + suffix;
            this.suffix = suffix.toUpperCase(Locale.ENGLISH);
            this.mode = mode;
            this.pad = pad;
        }
        @Override
		public String toString() {
        	return String.format("%s", transform);
        }
        // set mode and padding for the given SPI
        void setModePadding(CipherSpi spi) throws NoSuchAlgorithmException,
                NoSuchPaddingException {
            if (mode != null) {
            	((CryptX_AES)spi).engineSetMode(mode);
            }
            if (pad != null) {
            	((CryptX_AES)spi).engineSetPadding(pad);
            }
        }
        // check whether the given services supports the mode and
        // padding described by this Transform
        int supportsModePadding(Service s) {
            int smode = supportsMode(s);
            if (smode == S_NO) {
                return smode;
            }
            int spad = supportsPadding(s);
            // our constants are defined so that Math.min() is a tri-valued AND
            return Math.min(smode, spad);
        }

        // separate methods for mode and padding
        // called directly by Cipher only to throw the correct exception
        int supportsMode(Service s) {
            return supports(s, ATTR_MODE, mode);
        }
        int supportsPadding(Service s) {
            return supports(s, ATTR_PAD, pad);
        }

        private static int supports(Service s, String attrName, String value) {
            if (value == null) {
                return S_YES;
            }
            String regexp = s.getAttribute(attrName);
            if (regexp == null) {
                return S_MAYBE;
            }
            return matches(regexp, value) ? S_YES : S_NO;
        }

        // Map<String,Pattern> for previously compiled patterns
        // XXX use ConcurrentHashMap once available
        private final static Map<String,Pattern> patternCache =
            Collections.synchronizedMap(new HashMap<String,Pattern>());

        private static boolean matches(String regexp, String str) {
            Pattern pattern = patternCache.get(regexp);
            if (pattern == null) {
                pattern = Pattern.compile(regexp);
                patternCache.put(regexp, pattern);
            }
            return pattern.matcher(str.toUpperCase(Locale.ENGLISH)).matches();
        }

    }

    private static String[] tokenizeTransformation(String transformation)
            throws NoSuchAlgorithmException {
        if (transformation == null) {
            throw new NoSuchAlgorithmException("No transformation given");
        }
        /*
         * array containing the components of a Cipher transformation:
         *
         * index 0: algorithm component (e.g., DES)
         * index 1: feedback component (e.g., CFB)
         * index 2: padding component (e.g., PKCS5Padding)
         */
        String[] parts = new String[3];
        int count = 0;
        StringTokenizer parser = new StringTokenizer(transformation, "/");
        try {
            while (parser.hasMoreTokens() && count < 3) {
                parts[count++] = parser.nextToken().trim();
            }
            if (count == 0 || count == 2 || parser.hasMoreTokens()) {
                throw new NoSuchAlgorithmException("Invalid transformation"
                                               + " format:" +
                                               transformation);
            }
        } catch (NoSuchElementException e) {
            throw new NoSuchAlgorithmException("Invalid transformation " +
                                           "format:" + transformation);
        }
        if ((parts[0] == null) || (parts[0].length() == 0)) {
            throw new NoSuchAlgorithmException("Invalid transformation:" +
                                   "algorithm not specified-"
                                   + transformation);
        }
        return parts;
    }

    private static List<Transform> getTransforms(String transformation)
            throws NoSuchAlgorithmException {
        String[] parts = tokenizeTransformation(transformation);

        String alg = parts[0];
        String mode = parts[1];
        String pad = parts[2];
        if ((mode != null) && (mode.length() == 0)) {
            mode = null;
        }
        if ((pad != null) && (pad.length() == 0)) {
            pad = null;
        }

        if ((mode == null) && (pad == null)) {
            // DES
            Transform tr = new Transform(alg, "", null, null);
            return Collections.singletonList(tr);
        } else { // if ((mode != null) && (pad != null)) {
            // DES/CBC/PKCS5Padding
            List<Transform> list = new ArrayList<Transform>(4);
            list.add(new Transform(alg, "/" + mode + "/" + pad, null, null));
            list.add(new Transform(alg, "/" + mode, null, pad));
            list.add(new Transform(alg, "//" + pad, mode, null));
            list.add(new Transform(alg, "", mode, pad));
            return list;
        }
    }

	static public void test() {
		try {
			List<Transform> transforms = getTransforms("AES/ECB/ISO9797_1PADDING");
			Log.debug("transforms: %s", Text.join(",", transforms));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}
