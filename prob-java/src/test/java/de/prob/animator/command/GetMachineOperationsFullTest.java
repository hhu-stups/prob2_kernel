package de.prob.animator.command;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import de.prob.cli.CliTestCommon;
import de.prob.prolog.term.PrologTerm;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;

public class GetMachineOperationsFullTest {
	private static Api api;

	@BeforeAll
	static void beforeAll() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}
	
	@Test
	public void machine_with_one_operation() throws IOException {
		final StateSpace stateSpace = api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "examplesForOperations", "machineWithOneOperation.mch").toString());
		GetMachineOperationsFull getMachineOperationsFull = new GetMachineOperationsFull();
		stateSpace.execute(getMachineOperationsFull);
		String element1 = "b(initialisation(b(assign_single_id(b(identifier(cars),integer,[nodeid(p3(13,5,9)),loc(local,'ISLAND',abstract_variables),not_initialised]),b(integer(0),integer,[nodeid(p3(13,13,14))])),subst,[nodeid(p3(13,5,14))])))";
		String element2 = "b(operation(on,[],[],b(precondition(b(less(b(identifier(cars),integer,[nodeid(p3(16,14,18)),loc(local,'ISLAND',abstract_variables)]),b(identifier(maxCars),integer,[nodeid(p3(16,21,28)),loc(local,'ISLAND',concrete_constants),readonly])),pred,[nodeid(p3(16,14,28))]),b(assign_single_id(b(identifier(cars),integer,[nodeid(p3(16,34,38)),loc(local,'ISLAND',abstract_variables)]),b(add(b(identifier(cars),integer,[nodeid(p3(16,42,46)),loc(local,'ISLAND',abstract_variables)]),b(integer(1),integer,[nodeid(p3(16,49,50))])),integer,[nodeid(p3(16,42,50))])),subst,[nodeid(p3(16,34,50))])),subst,[nodeid(p3(16,10,54))])))";
		Assertions.assertEquals(Arrays.asList(element1, element2), getMachineOperationsFull.getOperations().stream().map(PrologTerm::toString).collect(toList()));
		stateSpace.kill();
	}
	
	@Test
	public void machine_with_no_operation() throws IOException {
		final StateSpace stateSpace = api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "machineWithNoOperation.mch").toString());
		GetMachineOperationsFull getMachineOperationsFull = new GetMachineOperationsFull();
		stateSpace.execute(getMachineOperationsFull);
		List<String> expected = Collections.singletonList("b(initialisation(b(assign_single_id(b(identifier(floors),integer,[nodeid(p3(7,16,22)),loc(local,machineWithNoOperation,abstract_variables),not_initialised]),b(integer(0),integer,[nodeid(p3(7,26,27))])),subst,[nodeid(p3(7,16,27))])))");
		Assertions.assertEquals(expected, getMachineOperationsFull.getOperations().stream().map(PrologTerm::toString).collect(toList()));
		stateSpace.kill();
	}
	
	@Test
	public void machine_with_many_operations() throws IOException {
		final StateSpace stateSpace = api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "testTraceMachine.mch").toString());
		GetMachineOperationsFull getMachineOperationsFull = new GetMachineOperationsFull();
		stateSpace.execute(getMachineOperationsFull);
		String element1 = "b(operation(inccc,[],[],b(precondition(b(less(b(identifier(floors),integer,[nodeid(p3(11,14,20)),loc(local,'Lift',abstract_variables)]),b(integer(2),integer,[nodeid(p3(11,21,22))])),pred,[nodeid(p3(11,14,22))]),b(assign_single_id(b(identifier(floors),integer,[nodeid(p3(11,28,34)),loc(local,'Lift',abstract_variables)]),b(add(b(identifier(floors),integer,[nodeid(p3(11,38,44)),loc(local,'Lift',abstract_variables)]),b(integer(1),integer,[nodeid(p3(11,47,48))])),integer,[nodeid(p3(11,38,48))])),subst,[nodeid(p3(11,28,48))])),subst,[nodeid(p3(11,10,52))])))";
		String element2 = "b(operation(dec,[],[],b(precondition(b(greater(b(identifier(floors),integer,[nodeid(p3(12,12,18)),loc(local,'Lift',abstract_variables)]),b(integer(0),integer,[nodeid(p3(12,19,20))])),pred,[nodeid(p3(12,12,20))]),b(assign_single_id(b(identifier(floors),integer,[nodeid(p3(12,26,32)),loc(local,'Lift',abstract_variables)]),b(minus(b(identifier(floors),integer,[nodeid(p3(12,36,42)),loc(local,'Lift',abstract_variables)]),b(integer(1),integer,[nodeid(p3(12,45,46))])),integer,[nodeid(p3(12,36,46))])),subst,[nodeid(p3(12,26,46))])),subst,[nodeid(p3(12,8,50))])))";
		String element3 = "b(operation(getfloors,[b(identifier(out),integer,[nodeid(p3(13,2,5)),introduced_by(operation)])],[],b(assign_single_id(b(identifier(out),integer,[nodeid(p3(13,28,31)),introduced_by(operation)]),b(identifier(floors),integer,[nodeid(p3(13,35,41)),loc(local,'Lift',abstract_variables)])),subst,[nodeid(p3(13,28,41))])))";
		String element4 = "b(initialisation(b(assign_single_id(b(identifier(floors),integer,[nodeid(p3(7,16,22)),loc(local,'Lift',abstract_variables),not_initialised]),b(integer(0),integer,[nodeid(p3(7,26,27))])),subst,[nodeid(p3(7,16,27))])))";
		List<String> toCompare = Arrays.asList(element1, element2, element3, element4);
		List<String> result = getMachineOperationsFull.getOperations().stream().map(PrologTerm::toString).collect(toList());


		Assertions.assertEquals(new HashSet<>(toCompare), new HashSet<>(result));

		stateSpace.kill();
	}


	@Test
	public void get_full_map() throws IOException {
		final StateSpace stateSpace = api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "testTraceMachine.mch").toString());
		GetMachineOperationsFull getMachineOperationsFull = new GetMachineOperationsFull();
		stateSpace.execute(getMachineOperationsFull);
		Assertions.assertEquals(4, getMachineOperationsFull.getOperationsWithNames().size());
		stateSpace.kill();
	}
}
