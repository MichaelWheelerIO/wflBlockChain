package com.setl.node.dao;

import java.util.List;

/**
 * Created by christhornley on 07/07/2017.
 */
public interface TransactionDao {
  void update(int height, List<String> transactionHashes);
}
