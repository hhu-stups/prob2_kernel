package de.prob.json;

import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.statespace.OperationInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.*;

public class TraceJsonFileTest {

	@Test
	public void reassemble_information_test(){

		OperationInfo operationInfo1 = new OperationInfo("op1", emptyList(), emptyList(), true, OperationInfo.Type.CLASSICAL_B, singletonList("x"), emptyList(), emptyList(), emptyMap());
		OperationInfo operationInfo2 = new OperationInfo("op2", emptyList(), emptyList(), true, OperationInfo.Type.CLASSICAL_B, emptyList(), emptyList(), emptyList(), emptyMap());
		OperationInfo operationInfo3 = new OperationInfo("op3", emptyList(), emptyList(), true, OperationInfo.Type.CLASSICAL_B, emptyList(), emptyList(), emptyList(), emptyMap());

		Map<String, OperationInfo> operationInfoMap = new HashMap<>();
		operationInfoMap.put("op1" , operationInfo1);
		operationInfoMap.put("op2" , operationInfo2);
		operationInfoMap.put("op3" , operationInfo3);


		Map<String, String> globalTypes = new HashMap<>();
		globalTypes.put("x", "integer");

		Map<String, OperationInfo> result = TraceJsonFile.reassembleTypeInfo(globalTypes, operationInfoMap);

		Map<String, String> expected = new HashMap<>(globalTypes);

		Assertions.assertEquals(expected, result.get("op1").getTypeMap());

	}


	@Test
	public void createGlobalIdentifierMap_test(){

		OperationInfo operationInfo1 = new OperationInfo("op1", emptyList(), emptyList(), true, OperationInfo.Type.CLASSICAL_B, singletonList("x"), emptyList(), emptyList(), singletonMap("x", "integer"));
		OperationInfo operationInfo2 = new OperationInfo("op2", emptyList(), emptyList(), true, OperationInfo.Type.CLASSICAL_B, singletonList("y"), emptyList(), emptyList(), singletonMap("y", "boolean"));
		OperationInfo operationInfo3 = new OperationInfo("op3", emptyList(), emptyList(), true, OperationInfo.Type.CLASSICAL_B, emptyList(), emptyList(), emptyList(), emptyMap());

		Map<String, OperationInfo> operationInfoMap = new HashMap<>();
		operationInfoMap.put("op1" , operationInfo1);
		operationInfoMap.put("op2" , operationInfo2);
		operationInfoMap.put("op3" , operationInfo3);


		Map<String, String> expected = new HashMap<>();
		expected.put("x", "integer");
		expected.put("y", "boolean");


		Map<String, String> result = TraceJsonFile.createGlobalIdentifierMap(operationInfoMap);


		Assertions.assertEquals(expected, result);

	}

	@Test
	public void createGlobalIdentifierMap_test_2(){
		Map<String, String> typeMap = new HashMap<>();
		typeMap.put("a", "integer");
		typeMap.put("b", "integer");
		typeMap.put("x", "integer");
		typeMap.put("y", "integer");
		typeMap.put("q", "integer");

		OperationInfo operationInfo1 = new OperationInfo("op1", Arrays.asList("a", "b"), Arrays.asList("x", "y"), true, OperationInfo.Type.CLASSICAL_B, singletonList("q"), emptyList(), emptyList(), typeMap);
		OperationInfo operationInfo2 = new OperationInfo("op2", emptyList(), emptyList(), true, OperationInfo.Type.CLASSICAL_B, singletonList("v"), emptyList(), emptyList(), singletonMap("v", "boolean"));
		OperationInfo operationInfo3 = new OperationInfo("op3", emptyList(), emptyList(), true, OperationInfo.Type.CLASSICAL_B, emptyList(), emptyList(), emptyList(), emptyMap());

		Map<String, OperationInfo> operationInfoMap = new HashMap<>();
		operationInfoMap.put("op1" , operationInfo1);
		operationInfoMap.put("op2" , operationInfo2);
		operationInfoMap.put("op3" , operationInfo3);


		Map<String, String> expectedTypes = new HashMap<>();
		expectedTypes.put("v", "boolean");
		expectedTypes.put("q", "integer");


		Map<String, String> globalTypeMap = TraceJsonFile.createGlobalIdentifierMap(operationInfoMap);

		Assertions.assertEquals(expectedTypes, globalTypeMap);
	}


	@Test
	public void cleanseOperationTest_test(){

		OperationInfo operationInfo1 = new OperationInfo("op1", emptyList(), emptyList(), true, OperationInfo.Type.CLASSICAL_B, singletonList("x"), emptyList(), emptyList(), singletonMap("x", "integer"));
		OperationInfo operationInfo2 = new OperationInfo("op2", emptyList(), emptyList(), true, OperationInfo.Type.CLASSICAL_B, singletonList("y"), emptyList(), emptyList(), singletonMap("y", "boolean"));
		OperationInfo operationInfo3 = new OperationInfo("op3", emptyList(), emptyList(), true, OperationInfo.Type.CLASSICAL_B, emptyList(), emptyList(), emptyList(), emptyMap());

		Map<String, OperationInfo> operationInfoMap = new HashMap<>();
		operationInfoMap.put("op1" , operationInfo1);
		operationInfoMap.put("op2" , operationInfo2);
		operationInfoMap.put("op3" , operationInfo3);


		Map<String, String> toCleanse = new HashMap<>();
		toCleanse.put("y", "boolean");



		OperationInfo operationInfo2Cleansed = new OperationInfo("op2", emptyList(), emptyList(), true, OperationInfo.Type.CLASSICAL_B, singletonList("y"), emptyList(), emptyList(), emptyMap());

		Map<String, OperationInfo> expected = new HashMap<>();
		expected.put("op1" , operationInfo1);
		expected.put("op2" , operationInfo2Cleansed);
		expected.put("op3" , operationInfo3);

		Map<String, OperationInfo> result = TraceJsonFile.cleanseOperationInfo(operationInfoMap, toCleanse);


		Assertions.assertEquals(expected, result);

	}


	@Test
	public void integration_test(){

		Map<String, String> typeMap = new HashMap<>();
		typeMap.put("a", "integer");
		typeMap.put("b", "integer");
		typeMap.put("x", "integer");
		typeMap.put("y", "integer");
		typeMap.put("q", "integer");

		OperationInfo operationInfo1 = new OperationInfo("op1", Arrays.asList("a", "b"), Arrays.asList("x", "y"), true, OperationInfo.Type.CLASSICAL_B, singletonList("q"), emptyList(), emptyList(), typeMap);
		OperationInfo operationInfo2 = new OperationInfo("op2", emptyList(), emptyList(), true, OperationInfo.Type.CLASSICAL_B, singletonList("v"), emptyList(), emptyList(), singletonMap("v", "boolean"));
		OperationInfo operationInfo3 = new OperationInfo("op3", emptyList(), emptyList(), true, OperationInfo.Type.CLASSICAL_B, emptyList(), emptyList(), emptyList(), emptyMap());

		Map<String, OperationInfo> operationInfoMap = new HashMap<>();
		operationInfoMap.put("op1" , operationInfo1);
		operationInfoMap.put("op2" , operationInfo2);
		operationInfoMap.put("op3" , operationInfo3);


		Map<String, String> expectedTypes = new HashMap<>();
		expectedTypes.put("v", "boolean");
		expectedTypes.put("q", "integer");


		Map<String, String> globalTypeMap = TraceJsonFile.createGlobalIdentifierMap(operationInfoMap);

		Assertions.assertEquals(expectedTypes, globalTypeMap);


		Map<String, String> cleansedTypeMapFor1 = new HashMap<>();
		cleansedTypeMapFor1.put("a", "integer");
		cleansedTypeMapFor1.put("b", "integer");
		cleansedTypeMapFor1.put("x", "integer");
		cleansedTypeMapFor1.put("y", "integer");

		OperationInfo operationInfo1Cleansed = new OperationInfo("op1", Arrays.asList("a", "b"), Arrays.asList("x", "y"), true, OperationInfo.Type.CLASSICAL_B, singletonList("q"), emptyList(), emptyList(), cleansedTypeMapFor1);
		OperationInfo operationInfo2Cleansed = new OperationInfo("op2", emptyList(), emptyList(), true, OperationInfo.Type.CLASSICAL_B, singletonList("v"), emptyList(), emptyList(), emptyMap());

		Map<String, OperationInfo> expected = new HashMap<>();
		expected.put("op1" , operationInfo1Cleansed);
		expected.put("op2" , operationInfo2Cleansed);
		expected.put("op3" , operationInfo3);



		Map<String, OperationInfo> result = TraceJsonFile.cleanseOperationInfo(operationInfoMap, globalTypeMap);



		Assertions.assertEquals(expected, result);

	}
}
