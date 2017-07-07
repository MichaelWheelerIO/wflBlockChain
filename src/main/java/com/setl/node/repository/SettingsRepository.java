package com.setl.node.repository;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import com.setl.node.state.SettingsKeys;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

public class SettingsRepository {
	private final String FILE_LOCATION = "settings.db";
	private ChronicleMap<SettingsKeys, Object> db;

	@Inject
	public SettingsRepository() {

		try {
			ChronicleMapBuilder<SettingsKeys, Object> builder = ChronicleMapBuilder.of(SettingsKeys.class, Object.class)
					.name("hashes").actualSegments(5).averageKeySize(10).entries(10).averageKeySize(10)
					.averageValueSize(10);
			;

			db = builder.createPersistedTo(new File(FILE_LOCATION));

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public Object get(Object key) {
		Object value = db.get(key);
		return value;
	}

	public void put(SettingsKeys key, Object value) {
		db.put(key, value);
	}

}
