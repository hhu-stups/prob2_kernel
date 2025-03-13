package de.prob.cli;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

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
}
