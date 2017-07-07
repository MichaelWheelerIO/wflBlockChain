package com.setl.node;

public final class Launcher {

	public static void main(String[] args) throws Exception {
		com.setl.node.DaggerApplication //jp-dev.opencsd.io
				.builder().build().getSimpleNode().start("jp-dev.opencsd.io", 13510, 13410);
		Thread.sleep(60 * 1000);
	}
}
