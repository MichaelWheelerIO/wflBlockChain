package com.setl.node.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.setl.node.repository.impl.ChronicleMapBlockRepository;
import com.setl.node.wallet.Block;

public class BlockRepositoryTest {

	BlockManagerRepository bm = new ChronicleMapBlockRepository("./test-blocks/", "./test-blocks/00000.db");

	@Test
	public void loadBlock() {
		try (Stream<Path> paths = Files.walk(Paths.get("./test-blocks"))) {
			paths.filter(p -> Files.isRegularFile(p) && !p.toString().toUpperCase().contains(".D")
					&& !p.toString().contains(".git")).forEach(filePath -> {

						Block block = bm.load(filePath.getFileName().toString()).get();
						String expected = filePath.getFileName().toString();
						String value = block.initialHash();
						Assert.assertThat(value, Matchers.is(expected));
					});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
