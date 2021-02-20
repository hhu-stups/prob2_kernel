package de.prob;

import com.google.inject.AbstractModule;
import de.prob.MainModule;


public class JsonManagerStubModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new MainModule());
	}
}
