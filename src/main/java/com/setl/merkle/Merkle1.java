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
import java.util.Arrays;
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
 * 
 *         Variable collapse of hash tree. byte array hash tree single byte
 *         array per layer
 * 
 */
public class Merkle1<K, V> {
	ArrayList<listEntry> MerkleList;
	Map<K, Integer> MerkleIndex;
	ArrayList<byte[]> HashArray;
	HashSet<Integer> toUpdate;
	MessageDigest digest;
	final Integer HASH_SIZE = 32; // Sizeof return from sha256
	final Integer TREE_STEP = 8;
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
		private byte[] HashArray;
		private Integer startIndex;
		private Integer endIndex;
		private MessageDigest myDigest;

		HashEntriesRange(ArrayList<listEntry> ObjectArray, byte[] HashArray, Integer IndexArray[], Integer startIndex,
				Integer endIndex) {
			this.ObjectArray = ObjectArray;
			this.HashArray = HashArray;
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

					System.arraycopy(digest.digest(), 0, HashArray, thisIndex * HASH_SIZE, HASH_SIZE);
				} catch (Exception e) {
				}

				hashcount += 1;
			}

			return hashcount;
		}
	}

	public Merkle1() {
		this.MerkleList = new ArrayList<>();
		this.MerkleIndex = new HashMap<>();
		this.HashArray = new ArrayList<>();

		this.toUpdate = new HashSet<>();

		this.threadService = Executors.newFixedThreadPool(4);

		try {
			this.digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
		}

	}

	private byte[] sha256(ArrayList<byte[]> input) {

		digest.reset();

		for (byte[] thisItem : input) {
			if (thisItem != null) {
				digest.update(thisItem);
			}
		}

		byte[] hash = digest.digest();
		// return bytesToHex(hash);
		return hash;

	}

	private byte[] sha256(String input) {
		try {
			digest.reset();
			digest.update(input.getBytes("UTF-8"));
			byte[] hash = digest.digest();
			// return bytesToHex(hash);
			return hash;

		} catch (UnsupportedEncodingException e) {
		}

		return new byte[0];
	}

	private byte[] sha256(byte[] input) {
		digest.reset();
		digest.update(input);
		byte[] hash = digest.digest();
		// return bytesToHex(hash);
		return hash;
	}

	private byte[] convertToBytes(Object object) throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(object);
			return bos.toByteArray();
		}
	}

	private byte[] sha256(Object input) {
		try {
			digest.reset();
			digest.update(convertToBytes(input));
			byte[] hash = digest.digest();
			// return bytesToHex(hash);
			return hash;
		} catch (Exception e) {
		}
		return new byte[0];
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

		if (this.HashArray.size() == 0) {
			return "";
		}

		byte[] hashArray = this.HashArray.get(this.HashArray.size() - 1);

		if ((hashArray == null) || (hashArray.length < this.HASH_SIZE)) {
			return "";
		}

		if (hashArray.length == this.HASH_SIZE) {
			return bytesToHex(hashArray);
		} else {
			return bytesToHex(Arrays.copyOf(hashArray, this.HASH_SIZE));
		}
	}

	public int commit_all() {
		int hashcount = 0;

		// Extend Base layer ?
		// Add New items to hashlist[0] and ensure that those items are in
		// _uncommited, except
		// where an empty element is added to hashlist[0] to ensure an even
		// number of elements.

		int layerItemLength = Math.max(this.MerkleList.size(), 1);
		int layerIndex = 0;
		int targetByteLength;
		byte[] thisLayer;
		byte[] baseLayer;

		// Add / Resize
		while (layerItemLength > 0) {
			targetByteLength = layerItemLength * this.HASH_SIZE;

			if (HashArray.size() <= layerIndex) {
				HashArray.add(new byte[targetByteLength]);
			} else {
				thisLayer = HashArray.get(layerIndex);
				if (thisLayer.length != targetByteLength) {
					HashArray.set(layerIndex, Arrays.copyOf(thisLayer, targetByteLength));
				}
			}

			if (layerItemLength <= 1) {
				layerItemLength = 0;
			} else {
				layerItemLength = Math.toIntExact(Math.round(Math.ceil(layerItemLength / (double) this.TREE_STEP)));
			}
			layerIndex++;
		}

		if (toUpdate.size() > 0) {

			int merkleLength = this.MerkleList.size();
			int thisUpdateIndex = 0;
			int lastUpdateIndex = -1;

			HashSet<Integer> pendingIndexToUpdate = new HashSet<>();

			// Update base layer using indices from self._uncommited

			baseLayer = HashArray.get(0);
			long start = System.currentTimeMillis();

			if (false || (this.toUpdate.size() < 1000)) {
				for (int index : this.toUpdate) {
					if (index < merkleLength) {
						V item = this.MerkleList.get(index).data;

						System.arraycopy(sha256(item), 0, baseLayer, index * HASH_SIZE, HASH_SIZE);

						hashcount += 1;
						thisUpdateIndex = (index / TREE_STEP);
						if (thisUpdateIndex != lastUpdateIndex) {
							pendingIndexToUpdate.add(thisUpdateIndex);
						}
						lastUpdateIndex = thisUpdateIndex;
					}
				}
			} else {
				Object[] hashObjects = new Object[4];
				Object[] futures = new Object[4];

				Integer[] updateIndices = new Integer[this.toUpdate.size()];
				this.toUpdate.toArray(updateIndices);

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
					thisUpdateIndex = (index / TREE_STEP);
					if (thisUpdateIndex != lastUpdateIndex) {
						pendingIndexToUpdate.add(thisUpdateIndex);
					}
					lastUpdateIndex = thisUpdateIndex;
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

			/* */
			/*
			 * // Hash entire base layer for root hash :
			 * 
			 * if (this.HashArray.size() > 1) { baseLayer =
			 * this.HashArray.get(0); thisLayer =
			 * this.HashArray.get(this.HashArray.size() -1);
			 * 
			 * this.HashArray.set(this.HashArray.size() -1, sha256(baseLayer));
			 * }
			 */

			int byteChunkSize = HASH_SIZE * TREE_STEP;
			int baseLayerLength;
			byte[] tempArray = new byte[byteChunkSize];
			int offset;

			for (layerIndex = 1; layerIndex < this.HashArray.size(); layerIndex++) {
				thisLayer = this.HashArray.get(layerIndex);
				baseLayer = this.HashArray.get(layerIndex - 1);
				baseLayerLength = baseLayer.length;

				HashSet<Integer> thisIndexToUpdate = pendingIndexToUpdate;
				pendingIndexToUpdate = new HashSet<>();

				for (int index : thisIndexToUpdate) {
					offset = index * HASH_SIZE;

					System.arraycopy(baseLayer, offset * TREE_STEP, tempArray, 0,
							Math.min((baseLayerLength - (offset * TREE_STEP)), byteChunkSize));

					System.arraycopy(sha256(tempArray), 0, thisLayer, offset, this.HASH_SIZE);

					hashcount += 1;
					thisUpdateIndex = (index / TREE_STEP);
					if (thisUpdateIndex != lastUpdateIndex) {
						pendingIndexToUpdate.add(thisUpdateIndex);
					}
					lastUpdateIndex = thisUpdateIndex;
				}

				/*
				 * for (offset = 0; offset < thisLayer.length; offset +=
				 * HASH_SIZE) { System.arraycopy(baseLayer, offset * TREE_STEP,
				 * tempArray, 0, Math.min((baseLayerLength - (offset *
				 * TREE_STEP)), byteChunkSize));
				 * 
				 * System.arraycopy(sha256(tempArray), 0, thisLayer, offset,
				 * this.HASH_SIZE);
				 * 
				 * hashcount += 1; }
				 */
			}
			/*
			 * Integer layerHashcount;
			 * 
			 * for (int layer= 1; layer < this.HashArray.size(); layer++) {
			 * baseLayer = this.HashArray.get(layer - 1); thisLayer =
			 * this.HashArray.get(layer); layerHashcount = 0;
			 * 
			 * HashSet<Integer> thisIndexToUpdate = pendingIndexToUpdate;
			 * pendingIndexToUpdate = new HashSet<>();
			 * 
			 * byte[] newHash; byte[] hash1; byte[] hash2;
			 * 
			 * for (int index2 : thisIndexToUpdate) { hash1 =
			 * baseLayer.get(index2*2); hash2 = baseLayer.get((index2*2) + 1);
			 * if (hash1 == null) hash1 = new byte[0]; if (hash2 == null) hash2
			 * = new byte[0];
			 * 
			 * newHash = new byte[hash1.length + hash2.length];
			 * System.arraycopy(hash1, 0, newHash, 0, hash1.length);
			 * System.arraycopy(hash2, 0, newHash, hash1.length, hash2.length);
			 * 
			 * thisLayer.set(index2, sha256(newHash));
			 * 
			 * pendingIndexToUpdate.add((int)(index2 / TREE_STEP)); hashcount +=
			 * 1; //layerHashcount += 1; }
			 * 
			 * //System.out.println("Layer " + layer + ", LayerHashcount = " +
			 * layerHashcount);
			 * 
			 * }
			 * 
			 * /*
			 */

			long end2 = System.currentTimeMillis();
			System.out.println("Hashcount = " + hashcount + ", in " + (end2 - end));

			// Clear uncommitted.

			this.toUpdate.clear();
		}

		return hashcount;

	}

}
