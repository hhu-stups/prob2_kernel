package de.prob.cli;

import com.google.inject.Guice;
import com.google.inject.Injector;

import de.prob.MainModule;

public final class CliTestCommon {
	/**
	 * Shared injector used by all tests that interact with probcli.
	 * By reusing a single injector for all tests,
	 * probcli only has to be extracted/installed a single time instead of once per test,
	 * which makes the tests run faster.
	 */
	private static final Injector injector = Guice.createInjector(new MainModule());
	
	private CliTestCommon() {
		throw new AssertionError("Utility class");
	}
	
	public static Injector getInjector() {
		return injector;
	}
}
