package de.prob.cli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

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
	@DebuggingKey
	private static String createDebuggingKey() {
		return Long.toHexString(new SecureRandom().nextLong());
	}

	@Retention(RUNTIME)
	@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
	@BindingAnnotation
	@interface OsName {
	}

	@Retention(RUNTIME)
	@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
	@BindingAnnotation
	@interface DebuggingKey {
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
