package com.setl.node.repository;

import java.util.Optional;

import com.setl.node.wallet.Block;

public interface BlockManagerRepository {

	boolean save(Block s);

	Optional<Block> load(int height);

	Optional<Block> load(String hash);

}
