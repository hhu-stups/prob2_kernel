package de.prob;

import com.google.inject.AbstractModule;

import de.prob.annotations.Version;

public class MainConfiguration extends AbstractModule {
	public MainConfiguration() {}

	@Override
	protected void configure() {
		bind(String.class).annotatedWith(Version.class).toInstance(Main.getVersion());
	}
}
