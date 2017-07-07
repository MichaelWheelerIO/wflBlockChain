package com.setl.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;

/**
 * This holds useful methods for manipulating strings and bytes in order to
 * author messages
 */
public class MessageUtils {

	// available in Java 8 only!
	public static class Base64 {
		private static final java.util.Base64.Decoder m_decoder = java.util.Base64.getDecoder();
		private static final java.util.Base64.Encoder m_encoder = java.util.Base64.getEncoder();

		public static String encode(byte[] data) {
			return m_encoder.encodeToString(data);
		}

		public static byte[] decode(String base64) throws IOException {
			return m_decoder.decode(base64);
		}
	}

	public static class Base16 {

		final protected static char[] hexArray = "0123456789abcdef".toCharArray();

		public static String encode(byte[] bytes) {
			char[] hexChars = new char[bytes.length * 2];
			for (int j = 0; j < bytes.length; j++) {
				int v = bytes[j] & 0xFF;
				hexChars[j * 2] = hexArray[v >>> 4];
				hexChars[j * 2 + 1] = hexArray[v & 0x0F];
			}
			return new String(hexChars);
		}

		private static byte[] decode(final String encoded) throws IllegalArgumentException {
			if ((encoded.length() % 2) != 0)
				throw new IllegalArgumentException("Input string must contain an even number of characters");

			final byte result[] = new byte[encoded.length() / 2];
			final char enc[] = encoded.toCharArray();
			for (int i = 0; i < enc.length; i += 2) {
				StringBuilder curr = new StringBuilder(2);
				curr.append(enc[i]).append(enc[i + 1]);
				result[i / 2] = (byte) Integer.parseInt(curr.toString(), 16);
			}
			return result;
		}
	}

	public static byte[] PeerMessage(Long chainID, Integer msgID, Object... args) throws Exception {
		/*
		 * Peer message as actually transmitted between nodes, after being B64
		 * Encoded of course.
		 *
		 * Messages that are put on the peer_msg_queue are further wrapped, see
		 */
		ArrayList<Object> messageList = new ArrayList<>();
		messageList.add(chainID);
		messageList.add(msgID);

		messageList.addAll(Arrays.asList(args));

		ByteArrayOutputStream mem_out = new ByteArrayOutputStream();
		MessagePacker packer = MessagePack.newDefaultPacker(mem_out);
		MessagePackHelper.packAnything(packer, messageList);
		packer.close(); // Never forget to close (or flush) the buffer
		mem_out.close();

		return mem_out.toByteArray();
	}

	final protected static char[] hexArray = "0123456789abcdef".toLowerCase().toCharArray();

	public static String bytesToHexString(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static byte[] hexToBytes(String s) {
		int len = s.length();
		s = s.toUpperCase();

		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public String b64encode(byte[] b) {
		return java.util.Base64.getEncoder().encodeToString(b);
	}

	/**
	 * compress bytes
	 */
	public static byte[] compress(byte[] content) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
			gzipOutputStream.write(content);
			gzipOutputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.printf("Compression %f\n", (1.0f * content.length / byteArrayOutputStream.size()));
		return byteArrayOutputStream.toByteArray();
	}

}
