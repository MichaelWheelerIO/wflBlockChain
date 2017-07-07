package com.setl.utils;

//import com.setl.ed25519.Ed25519DonnaJNI;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by nicholas on 18/10/2016.
 */

public enum SHA256 {
	;

	private static final MessageDigest shaDigest;
	static {
		try {
			shaDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	private static ThreadLocal<MessageDigest> SHA256Digest = null;

	protected static MessageDigest getSHA256Digest() throws NoSuchAlgorithmException {
		if (SHA256Digest != null)
			return SHA256Digest.get();

		SHA256Digest = new ThreadLocal<MessageDigest>();
		SHA256Digest.set(MessageDigest.getInstance("SHA-256"));

		return SHA256Digest.get();
	}

	// private static final char[] hexArrayLower =
	// "0123456789abcdef".toCharArray();
	// private static final char[] hexArrayUpper =
	// "0123456789ABCDEF".toCharArray();

	private static final char[] HEX_DIGITSLOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',
			'd', 'e', 'f' };
	private static final char[] HEX_DIGITSUPPER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
			'D', 'E', 'F' };

	public static String bytesToHex(byte[] data) {
		return bytesToHex(data, 0, data.length, false);
	}

	public static String bytesToHex(byte[] data, int offset, int length, Boolean upperCase) {
		/**
		 * Created by wolong on 22/07/16.
		 *
		 * Really good HEX encoder. fastest so far.
		 */

		char[] hexArray = (upperCase ? HEX_DIGITSUPPER : HEX_DIGITSLOWER);
		char[] out = new char[length << 1];
		for (int i = 0, j = 0; i < length; i++) {
			out[j++] = hexArray[(0xF0 & data[offset + i]) >>> 4];
			out[j++] = hexArray[0x0F & data[offset + i]];
		}
		return new String(out);
	}

	// private static String bytesToHex_old(byte[] bytes) {
	// return bytesToHex_old(bytes, false);
	// }

	// private static String bytesToHex_old(byte[] bytes, Boolean upperCase) {
	// /* Perfectly good hex encoder, just slower than the other one. */
	// char[] hexArray = (upperCase ? hexArrayUpper : hexArrayLower);
	//
	// char[] hexChars = new char[bytes.length * 2];
	// for (int j = 0; j < bytes.length; j++) {
	// int v = bytes[j] & 0xFF;
	// hexChars[j * 2] = hexArray[v >>> 4];
	// hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	// }
	// return new String(hexChars);
	// }

	public static byte[] HexToBytes(String string) {
		/**
		 * Created by wolong on 22/07/16.
		 *
		 * Fast Hex decoder, not case sensitive.
		 */

		char[] data = string.toCharArray();
		int len = data.length;

		if ((len & 0x01) != 0) {
			return null;
		}

		byte[] out = new byte[len >> 1];

		for (int i = 0, j = 0; j < len; i++) {
			out[i] = (byte) (((Character.digit(data[j++], 16) << 4) | Character.digit(data[j++], 16)) & 0xFF);
		}

		/*
		 * Was this ...
		 * 
		 * for (int i = 0, j = 0; j < len; i++) { int f =
		 * Character.digit(data[j], 16) << 4; j++; f = f |
		 * Character.digit(data[j], 16); j++; out[i] = (byte) (f & 0xFF); }
		 */

		return out;
	}

	// private static byte[] HexToBytes_old(String s) {
	// /* OK, but not as fast. */
	// int len = s.length();
	// s = s.toUpperCase();
	//
	// byte[] data = new byte[len / 2];
	// for (int i = 0; i < len; i += 2) {
	// data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) +
	// Character.digit(s.charAt(i + 1), 16));
	// }
	// return data;
	// }

	public static byte[] sha256(String input) {
		MessageDigest thisDigest = shaDigest; // TX.getSHA256Digest();

		try {
			thisDigest.reset();
			return thisDigest.digest(input.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
		}

		return new byte[0];
	}

	public static byte[] sha256(byte[] input) {
		MessageDigest thisDigest = shaDigest; // TX.getSHA256Digest();

		thisDigest.reset();
		byte[] hash = thisDigest.digest(input);
		// return bytesToHex(hash);
		return hash;
	}

}
