package de.prob.cli;

import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InterruptRefPatternTest {

	@Test
	public void testSuccess1() {
		String line = "user interrupt reference id: 3422";
		final Matcher matcher = ProBInstanceProvider.CLI_USER_INTERRUPT_REFERENCE_PATTERN.matcher(line);
		assertTrue(matcher.matches(), "Pattern does not match");
		assertEquals("3422", matcher.group(1));
	}

	@Test
	public void testSuccess2() {
		String line = "   \t   s  user interrupt reference id: 3422";
		final Matcher matcher = ProBInstanceProvider.CLI_USER_INTERRUPT_REFERENCE_PATTERN.matcher(line);
		assertTrue(matcher.matches(), "Pattern does not match");
		assertEquals("3422", matcher.group(1));
	}

	@Test
	public void testSuccessInterruptsOff() {
		String line = "   \t   s  user interrupt reference id: off";
		final Matcher matcher = ProBInstanceProvider.CLI_USER_INTERRUPT_REFERENCE_PATTERN.matcher(line);
		assertTrue(matcher.matches(), "Pattern does not match");
		assertEquals("off", matcher.group(1));
	}

	@Test
	public void testInvalidReference() {
		String line = "   \t   s  user interrupt reference id: deadbeef";
		final Matcher matcher = ProBInstanceProvider.CLI_USER_INTERRUPT_REFERENCE_PATTERN.matcher(line);
		assertFalse(matcher.matches(), "Pattern does not match");
	}

	@Test
	public void testFailingMatch1() {
		String line = "user interrupt reference id:      \t";
		final Matcher matcher = ProBInstanceProvider.CLI_USER_INTERRUPT_REFERENCE_PATTERN.matcher(line);
		assertFalse(matcher.matches(), "Pattern does not match");
	}

	@Test
	public void testEmptyInput() {
		String line = "";
		final Matcher matcher = ProBInstanceProvider.CLI_USER_INTERRUPT_REFERENCE_PATTERN.matcher(line);
		assertFalse(matcher.matches(), "Pattern does not match");
	}

	@Test
	public void testTrailingChars() {
		String line = "    Port: 3422 ";
		final Matcher matcher = ProBInstanceProvider.CLI_USER_INTERRUPT_REFERENCE_PATTERN.matcher(line);
		assertFalse(matcher.matches(), "Pattern does not match");
	}

}
