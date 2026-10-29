package de.prob.scripting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EventBPackageFactoryTest {

	@Test
	void extractsWholeTerm() {
		assertEquals("load_event_b_project([],[],[],_Error)",
			EventBPackageFactory.extractLoadCommand("package(load_event_b_project([],[],[],_Error)).\n"));
	}

	@Test
	void ignoresClosingParenDotInsideQuotedAtom() {
		String cmd = "load_event_b_project([description_text(none,'apr\\350\\s (r\\351\\alis\\351\\).')],[],[],_Error)";
		assertEquals(cmd, EventBPackageFactory.extractLoadCommand("package(" + cmd + ").\n\n"));
	}

	@Test
	void skipsLeadingLines() {
		assertEquals("x", EventBPackageFactory.extractLoadCommand("% comment\npackage(x).\n"));
	}

	@Test
	void rejectsMissingPackageTerm() {
		assertNull(EventBPackageFactory.extractLoadCommand("foo(x).\n"));
		assertNull(EventBPackageFactory.extractLoadCommand("package(x"));
	}
}
