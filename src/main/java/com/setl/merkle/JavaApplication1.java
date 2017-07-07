/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.setl.merkle;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 *
 * @author nicholas
 */
public class JavaApplication1 {

	final protected static char[] hexArray = "0123456789abcdef".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static String sha256(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(input.getBytes("UTF-8"));
			byte[] hash = digest.digest();
			return bytesToHex(hash);

		} catch (NoSuchAlgorithmException e) {
		} catch (UnsupportedEncodingException e) {
		}

		return "";
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		// String result = LibClass.acrostic(args);
		// System.out.println("Result = " + result);
		System.out.println("Cores = " + Runtime.getRuntime().availableProcessors());

		Random rand = new Random();

		int addressCount = 1000;
		int updateCount = 2000;

		Merkle2<String, Map<String, Long>> myMerkle = new Merkle2<>();
		Merkle2<String, Long> balanceMerkle = new Merkle2<>();

		ArrayList al = new ArrayList();
		ArrayList hashes = new ArrayList();

		Map<String, Integer> M = new HashMap<String, Integer>();

		Map<String, Long> tempMap;

		String thisHash;

		for (int i = 0; i < addressCount; i++) {
			tempMap = new TreeMap<String, Long>();

			tempMap.put("AAA|111", (long) 5000);
			tempMap.put("AAB|111", (long) 5000);
			tempMap.put("AAC|111", (long) 5000);
			tempMap.put("AAD|111", (long) 5000);
			tempMap.put("AAE|111", (long) 5000);
			tempMap.put("AAF|111", (long) 5000);
			tempMap.put("AAG|111", (long) 5000);
			tempMap.put("AAH|111", (long) 5000);
			tempMap.put("AAI|111", (long) 5000);
			tempMap.put("AAJ|111", (long) 5000);
			tempMap.put("BAA|111", (long) 5000);
			tempMap.put("BAB|111", (long) 5000);
			tempMap.put("BAC|111", (long) 5000);
			tempMap.put("BAD|111", (long) 5000);
			tempMap.put("BAE|111", (long) 5000);
			tempMap.put("BAF|111", (long) 5000);
			tempMap.put("BAG|111", (long) 5000);
			tempMap.put("BAH|111", (long) 5000);
			tempMap.put("BAI|111", (long) 5000);
			tempMap.put("BAJ|111", (long) 5000);

			thisHash = sha256(Integer.toString(i));
			hashes.add(thisHash);
			M.put(thisHash, al.size());
			al.add(tempMap);

			myMerkle.set_item(thisHash, tempMap);

			balanceMerkle.set_item(thisHash, 0L);
		}

		myMerkle.root_hash();

		long start = System.currentTimeMillis();

		for (int i = 0; i < updateCount; i++) {
			thisHash = (String) hashes.get(rand.nextInt(addressCount));

			// tempMap = (Map<String, Long>)al.get(M.get(thisHash));
			// tempMap.put("BAA|111", tempMap.getOrDefault("BAA|111", 0L) + 1);
			tempMap = myMerkle.get_item(thisHash);
			tempMap.put("BAA|111", tempMap.getOrDefault("BAA|111", 0L) + 1);
			myMerkle.set_item(thisHash, tempMap);

			balanceMerkle.set_item(thisHash, balanceMerkle.get_item(thisHash) + 1);

			thisHash = (String) hashes.get(rand.nextInt(addressCount));

			tempMap = myMerkle.get_item(thisHash);
			tempMap.put("BAA|111", tempMap.getOrDefault("BAA|111", 0L) - 1);
			myMerkle.set_item(thisHash, tempMap);

		}

		myMerkle.root_hash();

		long end = System.currentTimeMillis();

		System.out.println(end - start);
		System.out.println(updateCount * 1000.0 / (end - start));

		myMerkle.threadService.shutdown();

	}

}
