package de.prob.check.traceCheck;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.prob.check.tracereplay.check.CheckerUtils;
import de.prob.statespace.OperationInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CheckerUtilsTest {

	@Test
	public void listComparator_test_true(){
		List<String> first = Arrays.asList("hallo", "welt");
		List<String> second = Arrays.asList("hallo", "welt");

		boolean result = CheckerUtils.listComparator(first, second);


		Assertions.assertTrue(result);
	}

	@Test
	public void listComparator_test_false1(){
		List<String> first = Arrays.asList("hallo", "welt");
		List<String> second = Collections.singletonList("hallo");

		boolean result = CheckerUtils.listComparator(first, second);


		Assertions.assertFalse(result);
	}

	@Test
	public void listComparator_test_false2(){
		List<String> first = Arrays.asList("hallo", "welt");
		List<String> second = Arrays.asList("hallo", "welt2");

		boolean result = CheckerUtils.listComparator(first, second);


		Assertions.assertFalse(result);
	}

	@Test
	public void operationInfo_equivalent_test_true(){
		OperationInfo operationInfo1 = new OperationInfo("test", Collections.singletonList("test"),
				Collections.emptyList(), false, OperationInfo.Type.CLASSICAL_B, Collections.emptyList(),
				Collections.emptyList(), Collections.emptyList());
		OperationInfo operationInfo2 = new OperationInfo("test", Collections.singletonList("test"),
				Collections.emptyList(), false, OperationInfo.Type.CLASSICAL_B, Collections.emptyList(),
				Collections.emptyList(), Collections.emptyList());

		boolean result = CheckerUtils.equals(operationInfo1, operationInfo2);

		Assertions.assertTrue(result);

	}

	@Test
	public void operationInfo_equivalent_test_false(){
		OperationInfo operationInfo1 = new OperationInfo("test", Arrays.asList("test", "test2"),
				Collections.emptyList(), false, OperationInfo.Type.CLASSICAL_B, Collections.emptyList(),
				Collections.emptyList(), Collections.emptyList());
		OperationInfo operationInfo2 = new OperationInfo("test", Collections.singletonList("test"),
				Collections.emptyList(), false, OperationInfo.Type.CLASSICAL_B, Collections.emptyList(),
				Collections.emptyList(), Collections.emptyList());

		boolean result = CheckerUtils.equals(operationInfo1, operationInfo2);

		Assertions.assertFalse(result);

	}
}
