package de.prob.cli;

import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PortPatternTest {

	@Test
	public void testSuccess1() {
		String line = "Port: 3422";
		final Matcher matcher = ProBInstanceProvider.CLI_PORT_PATTERN.matcher(line);
		assertTrue(matcher.matches(), "Pattern does not match");
		assertEquals("3422", matcher.group(1));
	}

	@Test
	public void testSuccess2() {
		String line = " s \t  Port: 3422";
		final Matcher matcher = ProBInstanceProvider.CLI_PORT_PATTERN.matcher(line);
		assertTrue(matcher.matches(), "Pattern does not match");
		assertEquals("3422", matcher.group(1));
	}

	@Test
	public void testFailingMatch1()  {
		String line = "Port: ";
		final Matcher matcher = ProBInstanceProvider.CLI_PORT_PATTERN.matcher(line);
		assertFalse(matcher.matches(), "Pattern matches, but should not");
	}

	@Test
	public void testEmptyInput()  {
		String line = "";
		final Matcher matcher = ProBInstanceProvider.CLI_PORT_PATTERN.matcher(line);
		assertFalse(matcher.matches(), "Pattern matches empty string");
	}

	@Test
	public void testTrailingChars()  {
		String line = "    Port: 3422 ";
		final Matcher matcher = ProBInstanceProvider.CLI_PORT_PATTERN.matcher(line);
		assertFalse(matcher.matches(), "Pattern does not match");
	}

}
