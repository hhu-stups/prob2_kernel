package de.prob.cli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class ModuleCli extends AbstractModule {

	@Override
	protected void configure() {
		bind(ProBInstance.class).toProvider(ProBInstanceProvider.class);
		bind(OsSpecificInfo.class).toProvider(OsInfoProvider.class)
				.asEagerSingleton();
		bind(String.class).annotatedWith(OsName.class).toInstance(System.getProperty("os.name"));
		bind(String.class).annotatedWith(OsArch.class).toInstance(System.getProperty("os.arch"));
	}

	@Provides
	private static OsFamily getOsFamily(@OsName final String osName) {
		return OsFamily.fromName(osName);
	}

	@Retention(RUNTIME)
	@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
	@BindingAnnotation
	@interface OsName {
	}

	/**
	 * Currently unused - at the moment we only ship probcli binaries for a single architecture (x86_64).
	 * Might be used again in the future if we support more than one architecture again (e. g. ARM64).
	 */
	@Retention(RUNTIME)
	@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
	@BindingAnnotation
	@interface OsArch {
	}

}
