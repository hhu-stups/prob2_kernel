package de.prob.check;

import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.check.tracereplay.check.TraceModifier;
import de.prob.statespace.Trace;
import org.apache.groovy.util.Maps;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class TraceModifierUtilsTest {


	@Test
	public void zip_test(){
		List<String> first = Arrays.asList("a", "b", "c");
		List<String> second = Arrays.asList("1", "2", "3");

		Map<String, String> result = TraceCheckerUtils.zip(first, second);

		Map<String, String> expected = Maps.of("a", "1", "b", "2", "c", "3");

		Assertions.assertEquals(expected, result);
	}


	@Test
	public void zip_empty_test(){
		List<String> first = Collections.emptyList();
		List<String> second = Collections.emptyList();

		Map<String, String> result = TraceCheckerUtils.zip(first, second);

		Map<String, String> expected = Collections.emptyMap();

		Assertions.assertEquals(expected, result);
	}

}
