package de.prob.animator.command;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import org.apache.groovy.util.Maps;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class GetMachineOperationInfosWithTypesTest {

	ProBKernelStub proBKernelStub = null;

	@BeforeEach
	public void createJsonManager() {
		if (proBKernelStub == null) {
			Injector injector1 = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
			this.proBKernelStub = injector1.getInstance(ProBKernelStub.class);
		}
	}


	@Test
	public void get_prepared_operation_test_1() throws IOException, ModelTranslationError {
		proBKernelStub.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "examplesForOperations", "machineWithOneOperation.mch"));

		GetMachineOperationInfosWithTypes getMachineOperationsFull = new GetMachineOperationInfosWithTypes();

		proBKernelStub.executeCommand(getMachineOperationsFull);

		Map<String, String> expected = Maps.of("cars" , "integer", "maxCars", "integer");

		Assertions.assertEquals(1, getMachineOperationsFull.getOperationInfos().size());
		Assertions.assertEquals(expected, getMachineOperationsFull.getOperationInfos().get(0).getTypeMap());

	}



	@Test
	public void get_prepared_operation_test_2() throws IOException, ModelTranslationError {
		//StateSpace statespace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "Lift.mch"));

		StateSpace statespace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "tropical_island", "version_1", "Island.mch"));

		GetMachineOperationInfosWithTypes getMachineOperationsFull = new GetMachineOperationInfosWithTypes();

		statespace.execute(getMachineOperationsFull);

		Map<String, String> arriveByBoatTypeMap = new HashMap<>();
		arriveByBoatTypeMap.put("persons", "integer");
		arriveByBoatTypeMap.put("maxPerson", "integer");
		arriveByBoatTypeMap.put("currentPersons", "integer");
		OperationInfo arriveByBoat = new OperationInfo("arrive_by_boat",
				singletonList("persons"),
				emptyList(),
				true,
				OperationInfo.Type.CLASSICAL_B,
				Arrays.asList("currentPersons", "maxPerson"),
				singletonList("currentPersons"),
				emptyList(),
				arriveByBoatTypeMap);

		Map<String, String> arriveByFootTypeMap = new HashMap<>();
		arriveByFootTypeMap.put("maxPerson", "integer");
		arriveByFootTypeMap.put("currentPersons", "integer");
		OperationInfo arriveByFoot = new OperationInfo("arrive_by_foot",
				emptyList(),
				emptyList(),
				true,
				OperationInfo.Type.CLASSICAL_B,
				Arrays.asList("currentPersons", "maxPerson"),
				singletonList("currentPersons"),
				emptyList(), arriveByFootTypeMap);


		Map<String, String> leaveMap = new HashMap<>();
		leaveMap.put("currentPersons", "integer");

		OperationInfo leave = new OperationInfo("leave",
				emptyList(),
				emptyList(),
				true,
				OperationInfo.Type.CLASSICAL_B,
				singletonList("currentPersons"),
				singletonList("currentPersons"),
				emptyList(),
				leaveMap);


		Assertions.assertEquals(leave, statespace.getLoadedMachine().getMachineOperationInfo("leave"));
		Assertions.assertEquals(arriveByBoat, statespace.getLoadedMachine().getMachineOperationInfo("arrive_by_boat"));
		Assertions.assertEquals(arriveByFoot, statespace.getLoadedMachine().getMachineOperationInfo("arrive_by_foot"));

	}
}
