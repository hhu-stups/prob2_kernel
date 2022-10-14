package de.prob;

import java.nio.file.Path;

import com.google.inject.AbstractModule;

import de.prob.annotations.Home;
import de.prob.annotations.Version;

public class MainConfiguration extends AbstractModule {
	public MainConfiguration() {}

	@Override
	protected void configure() {
		bind(String.class).annotatedWith(Version.class).toInstance(Main.getVersion());
		@SuppressWarnings("deprecation")
		final String proBDirectory = Main.getProBDirectory();
		bind(String.class).annotatedWith(Home.class).toInstance(proBDirectory);
		@SuppressWarnings("deprecation")
		final Path proBHomePath = Main.getProBHomePath();
		bind(Path.class).annotatedWith(Home.class).toInstance(proBHomePath);
	}
}
