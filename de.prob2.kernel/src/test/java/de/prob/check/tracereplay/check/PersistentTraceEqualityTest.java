package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTransition;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class PersistentTraceEqualityTest {

	@Test
	public void isEquals_test(){

		PersistentTransition persistentTransition1 = new PersistentTransition("hallo", Collections.singletonMap("a", "b"), Collections.singletonMap("c", "d"),
				Collections.singletonMap("e", "f"), Collections.singleton("g"), Collections.emptyList(), Collections.emptyList(), "");

		PersistentTransition persistentTransition2 = new PersistentTransition("hallo", Collections.singletonMap("a", "b"), Collections.singletonMap("c", "d"),
				Collections.singletonMap("e", "f"), Collections.singleton("g"), Collections.emptyList(), Collections.emptyList(), "");

		Assertions.assertEquals(persistentTransition1, persistentTransition2);

	}
}
