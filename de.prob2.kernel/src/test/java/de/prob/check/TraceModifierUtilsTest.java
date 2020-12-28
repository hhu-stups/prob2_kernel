package de.prob.check;

import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.check.tracereplay.check.TraceModifier;
import de.prob.statespace.Trace;
import org.apache.groovy.util.Maps;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.*;

public class TraceModifierUtilsTest {


	@Test
	public void generate_perm(){
		List<String> bla = Arrays.asList("hallo", "wie", "geht", "es", "mir");
		List<List<String>> result = TraceCheckerUtils.generatePerm(bla, 0, 3, new ArrayList<>(2));
		System.out.println(result);
		Assert.assertEquals(60, result.size());
	}

	@Test
	public void generate_perm_2(){
		List<String> bla = Arrays.asList("hallo", "wie", "geht", "es", "mir", "?", "Geht", "es", "dir", "auch", "gut?", "Ja,", "das", "ist", "aber", "nett", "ich", "haue", "dich ", "dann ", "mal");
		List<List<String>> result = TraceCheckerUtils.generatePerm(bla, 0, 3, new ArrayList<>(2));
		Assert.assertEquals(7980, result.size());
	}

	@Test
	public void generate_perm_3(){
		List<String> bla = Arrays.asList("hallo", "wie", "geht", "es", "mir", "?", "Geht", "es", "dir", "auch", "gut?", "Ja,", "das", "ist", "aber", "nett", "ich", "haue", "dich ", "dann ", "mal");
		List<List<String>> result = TraceCheckerUtils.generatePerm(bla, 0, 9, new ArrayList<>(2));
		Assert.assertEquals(7980, result.size());
	}


	@Test
	public void generate_perm_4(){
		List<String> bla = Arrays.asList("a", "b", "c");
		List<List<String>> result = TraceCheckerUtils.generatePerm(bla, 0, 2, new ArrayList<>(2));
		System.out.println(result);
	}

	@Test
	public void zip_test(){
		List<String> first = Arrays.asList("a", "b", "c");
		List<String> second = Arrays.asList("1", "2", "3");

		Map<String, String> result = TraceCheckerUtils.zip(first, second);

		Map<String, String> expected = Maps.of("a", "1", "b", "2", "c", "3");

		Assert.assertEquals(expected, result);
	}


	@Test
	public void zip_empty_test(){
		List<String> first = Collections.emptyList();
		List<String> second = Collections.emptyList();

		Map<String, String> result = TraceCheckerUtils.zip(first, second);

		Map<String, String> expected = Collections.emptyMap();

		Assert.assertEquals(expected, result);
	}

}
