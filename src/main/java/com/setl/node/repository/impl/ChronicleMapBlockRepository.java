package com.setl.node.repository.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import com.setl.node.repository.BlockManagerRepository;
import com.setl.node.wallet.Block;
import com.setl.utils.MessagePackHelper;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

public class ChronicleMapBlockRepository implements BlockManagerRepository{
	@Inject
	public ChronicleMapBlockRepository() {
		this("./wallet/", "./wallet/database.db");
	}

	private static Logger logger = LogManager.getLogger(ChronicleMapBlockRepository.class);
	public ChronicleMap<Integer, String> db = null;
	// private final static String FILENAME = "./wallet/database.db";
	private final String prefix;

	public ChronicleMapBlockRepository(String prefix, String database) {
		this.prefix = prefix;
		try {
			File dbFile = new File(database);
			if (!dbFile.exists() && !dbFile.getParentFile().exists()) {
				dbFile.getParentFile().mkdirs();
			}
			ChronicleMapBuilder<Integer, String> builder = ChronicleMapBuilder.of(Integer.class, String.class)
					.name("hashes").entries(5_000_000L).averageValue("af3df0fd1c3a91842f4fdfd681d04b784c7e28ea6480ed8e7406b502c0e9074a");
			db = builder.createOrRecoverPersistedTo(dbFile);

		} catch (IOException e) {
			logger.error("", e);
		}
	}

	public boolean save(Block s) {
		// 1 calculat hash
		String hash = s.initialHash();
		MessagePacker packer;
		try {
			File file = new File(prefix + hash);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			packer = MessagePack.newDefaultPacker(new FileOutputStream(file));
			MessagePackHelper.packAnything(packer, s.asList());
			packer.flush();
			packer.close();
			db.put(s.getBaseHeight(), hash);
			return true;
		} catch (Exception e) {
			logger.error("", e);
			return false;
		}

	}

	public Optional<Block> load(int height) {
		String hash = db.get(height);
		if (hash == null) {
			logger.warn("Block {}  not found ", height, db.size());
			return Optional.empty();
		}
		return load(hash);
	}

	public Optional<Block> load(String hash) {
		try {
			MessageUnpacker unpacker = MessagePack
					.newDefaultUnpacker(new BufferedInputStream(new FileInputStream(prefix + hash), 1024 * 1024 * 2));

			List<Object> blockAsList = MessagePackHelper.unpackAllObjects(unpacker);

			blockAsList = (List<Object>) blockAsList.get(0);// remove wrapper
			Block block = new Block(blockAsList.toArray());
			return Optional.of(block);

		} catch (Exception e) {
			logger.error("", e);
			return Optional.empty();
		}

	}
}
