package de.prob.animator.command;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class ComposedCommandTest {
	@Test
	void createPrefixShort() {
		Set<String> prefixes = new HashSet<>();
		for (int i = 0; i < 26; i++) {
			prefixes.add(ComposedCommand.createPrefix(i));
		}
		Assertions.assertEquals(26, prefixes.size());
	}

	@Test
	void createPrefixLong() {
		Set<String> prefixes = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			prefixes.add(ComposedCommand.createPrefix(i));
		}
		Assertions.assertEquals(100, prefixes.size());
	}
}
