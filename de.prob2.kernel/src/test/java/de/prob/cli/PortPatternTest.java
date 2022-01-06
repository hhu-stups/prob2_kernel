package de.prob.cli;

import java.util.regex.Matcher;

import org.junit.Test;

import static org.junit.Assert.*;

public class PortPatternTest {

	@Test
	public void testSuccess1() {
		String line = "Port: 3422";
		final Matcher matcher = ProBInstanceProvider.CLI_PORT_PATTERN.matcher(line);
		assertTrue("Pattern does not match", matcher.matches());
		assertEquals("3422", matcher.group(1));
	}

	@Test
	public void testSuccess2() {
		String line = " s \t  Port: 3422";
		final Matcher matcher = ProBInstanceProvider.CLI_PORT_PATTERN.matcher(line);
		assertTrue("Pattern does not match", matcher.matches());
		assertEquals("3422", matcher.group(1));
	}

	@Test
	public void testFailingMatch1()  {
		String line = "Port: ";
		final Matcher matcher = ProBInstanceProvider.CLI_PORT_PATTERN.matcher(line);
		assertFalse("Pattern matches, but should not", matcher.matches());
	}

	@Test
	public void testEmptyInput()  {
		String line = "";
		final Matcher matcher = ProBInstanceProvider.CLI_PORT_PATTERN.matcher(line);
		assertFalse("Pattern matches empty string", matcher.matches());
	}

	@Test
	public void testTrailingChars()  {
		String line = "    Port: 3422 ";
		final Matcher matcher = ProBInstanceProvider.CLI_PORT_PATTERN.matcher(line);
		assertFalse("Pattern does not match", matcher.matches());
	}

}
