package de.prob.cli;

import java.io.File;
import java.nio.file.Path;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import de.prob.annotations.Home;

public final class ModuleCli extends AbstractModule {

	@Override
	protected void configure() {
		bind(ProBInstance.class).toProvider(ProBInstanceProvider.class);
	}

	@Provides
	@Singleton
	private static OsSpecificInfo getOsSpecificInfo() {
		return OsSpecificInfo.detect();
	}

	@Provides
	@Singleton
	private static ProBInstanceProvider getProBInstanceProvider(OsSpecificInfo osInfo) {
		return ProBInstanceProvider.defaultProvider(osInfo);
	}

	@Provides
	@Singleton
	@Home
	private static Path getProBHomePath(ProBInstanceProvider proBInstanceProvider) {
		return proBInstanceProvider.getProBDirectory();
	}

	@Provides
	@Singleton
	@Home
	private static String getProBHomeString(ProBInstanceProvider proBInstanceProvider) {
		return proBInstanceProvider.getProBDirectory() + File.separator;
	}
}
