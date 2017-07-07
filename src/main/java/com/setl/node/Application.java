package com.setl.node;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { SimpleNodeModule.class })
public interface Application {
	SimpleNode getSimpleNode();

}