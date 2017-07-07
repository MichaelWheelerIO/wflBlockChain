package com.setl.node.repository.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import com.setl.node.NodeConfig;
import com.setl.node.repository.BlockManagerRepository;
import com.setl.node.wallet.Block;
import com.setl.utils.MessagePackHelper;

public class SqliteBlockRepository implements BlockManagerRepository {


	private static final String TABLE_LIST = "SELECT name FROM sqlite_master WHERE type='table' AND name='blockdata';";
	private static final String TABLE_CREATE = "CREATE TABLE blockdata(blockheight integer PRIMARY KEY, blockhash text)";
	private static final String TABLE_INSERT = "INSERT INTO blockdata(blockheight, blockhash) VALUES(?,?);";
	private static final String TABLE_FIND = "SELECT blockhash from blockdata where blockheight = ?;";
	// private static final String TABLE_LIST_TASK = "SELECT id, command,state
	// from task;";

	private static Logger logger = LogManager.getLogger(SqliteBlockRepository.class);

	final private String prefix;
	final private String connectionString;
	final private String user, password;

	@Inject
	public SqliteBlockRepository(NodeConfig config) {
		this.prefix = config.getBlocksPath();
		connectionString = config.getWalletDbUrl();
		user = config.getWalletDbUser();
		password = config.getWalletDbPassword();
	}

	public SqliteBlockRepository(String prefix, String connectionString, String user, String password) {
		this.prefix = prefix;
		this.connectionString = connectionString;
		this.user = user;
		this.password = password;
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
			save(s.getBaseHeight(), hash);
			return true;
		} catch (Exception e) {
			logger.error("", e);
			return false;
		}

	}

	private PreparedStatement prepareStatement(Connection dbConnection, String query, List<Object> args)
			throws SQLException {
		PreparedStatement stmt = dbConnection.prepareStatement(query);

		for (int i = 0; i < args.size(); i++) {
			Object argument = args.get(i);
			if (argument instanceof String) {
				stmt.setString(i + 1, argument.toString());
			} else if (argument instanceof Integer) {
				stmt.setInt(i + 1, (Integer) argument);
			} else if (argument instanceof Long) {
				stmt.setLong(i + 1, (Long) argument);
			} else {
				stmt.setObject(i + 1, argument);
			}

		}
		return stmt;
	}

	private int executeUpdate(Connection dbConnection, String query, List<Object> args) throws SQLException {

		return prepareStatement(dbConnection, query, args).executeUpdate();
	}

	private ResultSet executeQuery(Connection dbConnection, String query, List<Object> args) throws SQLException {
		return prepareStatement(dbConnection, query, args).executeQuery();
	}

	private Connection createConnection() throws SQLException {
		Connection dbConnection = DriverManager.getConnection(connectionString, user, password);
		dbConnection.setAutoCommit(false);
		if (!executeQuery(dbConnection, TABLE_LIST, Collections.emptyList()).next()) {
			executeUpdate(dbConnection, TABLE_CREATE, Collections.emptyList());
		}
		return dbConnection;
	}

	private void save(int baseHeight, String hash) throws SQLException {
		Connection connection = createConnection();
		try {
			int state = executeUpdate(connection, TABLE_INSERT, Arrays.asList(baseHeight, hash));
			logger.trace(" {} rows updated.", state);
			connection.commit();
		} finally {
			connection.close();

		}
	}

	private String loadHash(int height) throws SQLException {

		Connection connection = createConnection();
		try {
			ResultSet resultRow = executeQuery(connection, TABLE_FIND, Arrays.asList(height));
			connection.commit();
			if (resultRow.next()) {
				return resultRow.getString(1);
			}
			return null;
		} finally {
			connection.close();

		}
	}

	public Optional<Block> load(int height) {
		String hash = null;
		try {
			hash = loadHash(height);
		} catch (SQLException e) {
			logger.catching(e);
		}
		if (hash == null) {
			logger.warn("Block {}  not found ", height);
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
