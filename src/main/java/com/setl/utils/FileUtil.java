package com.setl.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;

public enum FileUtil {
	;
	public static void write(Object object, String filename) {

		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {

			oos.writeObject(object);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static Optional<Object> read(String filename) {

		try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(filename))) {

			return Optional.of(oos.readObject());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return Optional.empty();

	}

	public static Optional<Object> read(File file) {
		try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(file))) {

			return Optional.of(oos.readObject());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return Optional.empty();
	}
}
