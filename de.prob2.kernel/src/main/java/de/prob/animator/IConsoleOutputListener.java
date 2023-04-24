package de.prob.animator;

@FunctionalInterface
public interface IConsoleOutputListener {
	void lineReceived(final String line);
}
