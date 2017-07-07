/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.setl.merkle;

import static com.setl.merkle.JavaApplication1.bytesToHex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author nicholas
 */
public class Merkle<K, V> {
	ArrayList<listEntry> MerkleList;
	Map<K, Integer> MerkleIndex;
	ArrayList<ArrayList<String>> HashArray;
	HashSet<Integer> toUpdate;
	MessageDigest digest;
	ExecutorService threadService;

	private class listEntry {
		Integer index;
		V data;

		public listEntry(Integer pIndex, V pData) {
			index = pIndex;
			data = pData;
		}
	}

	private class HashEntriesRange implements Callable {

		private ArrayList<listEntry> ObjectArray;
		private ArrayList<String> HashArray;
		private Integer IndexArray[];
		private Integer startIndex;
		private Integer endIndex;
		private MessageDigest myDigest;

		HashEntriesRange(ArrayList<listEntry> ObjectArray, ArrayList<String> HashArray, Integer IndexArray[],
				Integer startIndex, Integer endIndex) {
			this.ObjectArray = ObjectArray;
			this.HashArray = HashArray;
			this.IndexArray = IndexArray;
			this.startIndex = startIndex;
			this.endIndex = endIndex;

			try {
				this.myDigest = MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException e) {
			}

		}

		public Integer call() {
			Integer hashcount = 0;

			for (int thisIndex = this.startIndex; thisIndex <= this.endIndex; thisIndex++) {
				try {
					myDigest.reset();
					myDigest.update(convertToBytes(this.ObjectArray.get(thisIndex).data));
					byte[] hash = digest.digest();
					this.HashArray.set(thisIndex, bytesToHex(digest.digest()));
				} catch (Exception e) {
				}

				// this.HashArray.set(thisIndex,
				// sha256(this.ObjectArray.get(thisIndex).data));
				hashcount += 1;
			}

			return hashcount;
		}
	}

	public Merkle() {
		this.MerkleList = new ArrayList<>();
		this.MerkleIndex = new HashMap<>();
		this.HashArray = new ArrayList<>();
		this.HashArray.add(new ArrayList<>());

		this.toUpdate = new HashSet<>();

		this.threadService = Executors.newFixedThreadPool(4);

		try {
			this.digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
		}

	}

	private String sha256(ArrayList<String> input) {
		try {
			digest.reset();

			for (String thisIthem : input) {
				digest.update(thisIthem.getBytes("UTF-8"));
			}

			byte[] hash = digest.digest();
			return bytesToHex(hash);
		} catch (UnsupportedEncodingException e) {
		}

		return "";
	}

	private String sha256(String input) {
		try {
			digest.reset();
			digest.update(input.getBytes("UTF-8"));
			byte[] hash = digest.digest();
			return bytesToHex(hash);
		} catch (UnsupportedEncodingException e) {
		}

		return "";
	}

	private String sha256(byte[] input) {
		digest.reset();
		digest.update(input);
		byte[] hash = digest.digest();
		return bytesToHex(hash);
	}

	private byte[] convertToBytes(Object object) throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(object);
			return bos.toByteArray();
		}
	}

	private String sha256(Object input) {
		try {
			digest.reset();
			digest.update(convertToBytes(input));
			byte[] hash = digest.digest();
			return bytesToHex(hash);
		} catch (Exception e) {
		}
		return "";
	}

	public Boolean set_item(K key, V item) {
		int Index;
		int NewIndex;

		Index = MerkleIndex.getOrDefault(key, -1);

		if (Index >= 0) {
			// Update
			toUpdate.add(Index);
			MerkleList.get(Index).data = item;
		} else {
			// Add
			NewIndex = MerkleList.size();
			MerkleList.add(new listEntry(NewIndex, item));
			MerkleIndex.put(key, NewIndex);
			toUpdate.add(NewIndex);
		}

		return true;
	}

	public V get_item(K key) {
		int Index = MerkleIndex.getOrDefault(key, -1);

		if (Index >= 0) {
			return MerkleList.get(Index).data;
		} else {
			return null;
		}
	}

	public String root_hash() {
		this.commit_all();

		if (this.HashArray.get(0).size() == 0) {
			return "";
		} else {
			return this.HashArray.get(this.HashArray.size() - 1).get(0);
		}
	}

	public int commit_all() {
		int hashcount = 0;

		// Extend Base layer ?
		// Add New items to hashlist[0] and ensure that those items are in
		// _uncommited, except
		// where an empty element is added to hashlist[0] to ensure an even
		// number of elements.

		int targetLength = this.MerkleList.size();

		// Ensure even number of merklelist elements
		// The update code will not try to hash a data item for the 'extra' hash
		// item.
		if ((targetLength % 2) == 1) {
			targetLength += 1;
		}

		if (HashArray.size() == 0) {
			HashArray.add(new ArrayList<>());
		}

		ArrayList<String> baseLayer = HashArray.get(0);
		ArrayList<String> thisLayer;

		int baseLength = baseLayer.size();
		int adjustment = targetLength - baseLength;

		if (adjustment > 0) {
			// Set new elements as uncommitted
			for (int counter = baseLength; counter < targetLength; counter++) {
				toUpdate.add(counter);
				baseLayer.add("");
			}
		}

		if (toUpdate.size() > 0) {
			int newLength;

			if (this.HashArray.get(0).size() > 1) {
				// Add in placeholder entries in the Merkle tree as necessary.

				// For each combined hash layer (1+), check the length.

				for (int i = 1; i < this.HashArray.size(); i++) {
					// If this layer needs to be longer, add dummy entry(s
					// (sic)).
					thisLayer = HashArray.get(i);

					newLength = (int) ((this.HashArray.get(i - 1).size() + 1) / 2);

					if ((newLength % 2) == 1) {
						newLength++;
						adjustment = newLength - thisLayer.size();
					}

					if (adjustment > 0) {
						while (thisLayer.size() < adjustment)
							thisLayer.add("");
					}
				}

				// if the root has two elements, add new root(s).

				while (this.HashArray.get(this.HashArray.size() - 1).size() > 1) {

					newLength = (int) ((this.HashArray.get(this.HashArray.size() - 1).size() + 1) / 2);

					if ((newLength > 1) && ((newLength % 2) == 1))
						newLength += 1;

					this.HashArray.add(new ArrayList<String>(Collections.nCopies(newLength, "")));
				}

			}

			// Update hashes
			// Hash tree is updated a layer at a time. This should cause the
			// minimum amount of hashing.

			int merkleLength = this.MerkleList.size();

			HashSet<Integer> pendingIndexToUpdate = new HashSet<>();

			// Update base layer using indices from self._uncommited
			Integer[] updateIndices = new Integer[this.toUpdate.size()];
			this.toUpdate.toArray(updateIndices);

			long start = System.currentTimeMillis();

			if (false || (this.toUpdate.size() < 1000)) {
				for (int index : updateIndices) {
					if (index < merkleLength) {
						V item = this.MerkleList.get(index).data;

						baseLayer.set(index, sha256(item));

						hashcount += 1;
						pendingIndexToUpdate.add((int) (index / 2));

						// update higher layers using 'pendingIndexToUpdate'

					}
				}
			} else {
				Object[] hashObjects = new Object[4];
				Object[] futures = new Object[4];

				Integer chunkSize = (Integer) (updateIndices.length / 4);

				hashObjects[0] = new HashEntriesRange(this.MerkleList, baseLayer, updateIndices, 0, chunkSize);
				hashObjects[1] = new HashEntriesRange(this.MerkleList, baseLayer, updateIndices, chunkSize + 1,
						(2 * chunkSize));
				hashObjects[2] = new HashEntriesRange(this.MerkleList, baseLayer, updateIndices, (2 * chunkSize) + 1,
						(3 * chunkSize));
				hashObjects[3] = new HashEntriesRange(this.MerkleList, baseLayer, updateIndices, (3 * chunkSize) + 1,
						(updateIndices.length - 1));

				futures[0] = this.threadService.submit((HashEntriesRange) hashObjects[0]);
				futures[1] = this.threadService.submit((HashEntriesRange) hashObjects[1]);
				futures[2] = this.threadService.submit((HashEntriesRange) hashObjects[2]);
				futures[3] = this.threadService.submit((HashEntriesRange) hashObjects[3]);

				for (int index : updateIndices) {
					pendingIndexToUpdate.add((int) (index / 2));
				}

				for (int index = 0; index < 4; index++) {
					try {
						hashcount += ((Future<Integer>) futures[index]).get();
					} catch (Exception e) {
					}
				}
			}

			long end = System.currentTimeMillis();
			System.out.println("Hashcount = " + hashcount + ", in " + (end - start));
			hashcount = 0;
			Integer layerHashcount;

			/*
			 * 
			 * baseLayer = this.HashArray.get(0); thisLayer =
			 * this.HashArray.get(this.HashArray.size() -1);
			 * 
			 * String newHash = sha256(baseLayer); thisLayer.set(0, newHash);
			 * 
			 * // ---- OR ----
			 * 
			 * StringBuilder thisSB = new
			 * StringBuilder((int)(baseLayer.get(0).length() * baseLayer.size()
			 * * 1.01)); for (String thisItem : baseLayer) {
			 * thisSB.append(thisItem); }
			 * 
			 * String newHash = sha256(thisSB.toString()); thisLayer.set(0,
			 * newHash);
			 */

			for (int layer = 1; layer < this.HashArray.size(); layer++) {
				baseLayer = this.HashArray.get(layer - 1);
				thisLayer = this.HashArray.get(layer);
				layerHashcount = 0;

				HashSet<Integer> thisIndexToUpdate = pendingIndexToUpdate;
				pendingIndexToUpdate = new HashSet<>();

				for (int index2 : thisIndexToUpdate) {
					String newHash = sha256(baseLayer.get(index2 * 2) + baseLayer.get((index2 * 2) + 1));
					thisLayer.set(index2, newHash);

					pendingIndexToUpdate.add((int) (index2 / 2));
					hashcount += 1;
					// layerHashcount += 1;
				}

				// System.out.println("Layer " + layer + ", LayerHashcount = " +
				// layerHashcount);

			}

			long end2 = System.currentTimeMillis();
			System.out.println("Hashcount = " + hashcount + ", in " + (end2 - end));

			// Clear uncommitted.

			this.toUpdate.clear();
		}

		return hashcount;

	}

}
