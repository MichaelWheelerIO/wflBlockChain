package com.setl.utils;

import java.io.UnsupportedEncodingException;

import com.setl.ed25519.Ed25519DonnaJNI;

public enum TransactionUtils {
	;
	private static Ed25519DonnaJNI donnalib = new Ed25519DonnaJNI();

	static public String signTransaction(String hash, byte[] privKey, byte[] pubKey) {
		try {
			return MessageUtils.Base64.encode(donnalib.createSignature(hash.getBytes("UTF-8"), privKey, pubKey));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

	}

	static public boolean verifyTransaction(String hash, byte[] publicKey, byte[] signature) {
		try {
			return donnalib.verifySignature(hash.getBytes("UTF-8"), publicKey, signature);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

	}

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

	private static final char[] hexArrayLower = "0123456789abcdef".toCharArray();
	private static final char[] hexArrayUpper = "0123456789ABCDEF".toCharArray();

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
}
