package de.prob.animator.command;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import de.prob.ProBKernelStub;
import de.prob.cli.CliTestCommon;
import de.prob.prolog.term.PrologTerm;
import de.prob.scripting.ModelTranslationError;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;

public class GetMachineOperationsFullTest {

	ProBKernelStub proBKernelStub = null;

	@BeforeEach
	public void createJsonManager(){
		if(proBKernelStub==null) {
			this.proBKernelStub = CliTestCommon.getInjector().getInstance(ProBKernelStub.class);
		}
	}
	
	@Test
	public void machine_with_one_operation() throws IOException, ModelTranslationError {
		proBKernelStub.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "examplesForOperations", "machineWithOneOperation.mch"));
		GetMachineOperationsFull getMachineOperationsFull = new GetMachineOperationsFull();
		proBKernelStub.executeCommand(getMachineOperationsFull);
		String element1 = "b(initialisation(b(assign_single_id(b(identifier(cars),integer,[nodeid(pos(18,1,13,5,13,9)),loc(local,'ISLAND',abstract_variables),not_initialised]),b(integer(0),integer,[nodeid(pos(19,1,13,13,13,14))])),subst,[nodeid(pos(17,1,13,5,13,14))])))";
		String element2 = "b(operation(on,[],[],b(precondition(b(less(b(identifier(cars),integer,[nodeid(pos(24,1,16,14,16,18)),loc(local,'ISLAND',abstract_variables)]),b(identifier(maxCars),integer,[nodeid(pos(25,1,16,21,16,28)),loc(local,'ISLAND',concrete_constants),readonly])),pred,[nodeid(pos(23,1,16,14,16,28))]),b(assign_single_id(b(identifier(cars),integer,[nodeid(pos(27,1,16,34,16,38)),loc(local,'ISLAND',abstract_variables)]),b(add(b(identifier(cars),integer,[nodeid(pos(29,1,16,42,16,46)),loc(local,'ISLAND',abstract_variables)]),b(integer(1),integer,[nodeid(pos(30,1,16,49,16,50))])),integer,[nodeid(pos(28,1,16,42,16,50))])),subst,[nodeid(pos(26,1,16,34,16,50))])),subst,[nodeid(pos(22,1,16,10,16,54))])))";
		Assertions.assertEquals(Arrays.asList(element1, element2), getMachineOperationsFull.getOperations().stream().map(PrologTerm::toString).collect(toList()));
	}
	
	@Test
	public void machine_with_no_operation() throws IOException, ModelTranslationError {
		proBKernelStub.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "machineWithNoOperation.mch"));
		GetMachineOperationsFull getMachineOperationsFull = new GetMachineOperationsFull();
		proBKernelStub.executeCommand(getMachineOperationsFull);
		List<String> expected = Collections.singletonList("b(initialisation(b(assign_single_id(b(identifier(floors),integer,[nodeid(pos(14,1,7,16,7,22)),loc(local,machineWithNoOperation,abstract_variables),not_initialised]),b(integer(0),integer,[nodeid(pos(15,1,7,26,7,27))])),subst,[nodeid(pos(13,1,7,16,7,27))])))");
		Assertions.assertEquals(expected, getMachineOperationsFull.getOperations().stream().map(PrologTerm::toString).collect(toList()));
	}
	
	@Test
	public void machine_with_many_operations() throws IOException, ModelTranslationError {
		proBKernelStub.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "testTraceMachine.mch"));
		GetMachineOperationsFull getMachineOperationsFull = new GetMachineOperationsFull();
		proBKernelStub.executeCommand(getMachineOperationsFull);
		String element1 = "b(operation(inccc,[],[],b(precondition(b(less(b(identifier(floors),integer,[nodeid(pos(20,1,11,14,11,20)),loc(local,'Lift',abstract_variables)]),b(integer(2),integer,[nodeid(pos(21,1,11,21,11,22))])),pred,[nodeid(pos(19,1,11,14,11,22))]),b(assign_single_id(b(identifier(floors),integer,[nodeid(pos(23,1,11,28,11,34)),loc(local,'Lift',abstract_variables)]),b(add(b(identifier(floors),integer,[nodeid(pos(25,1,11,38,11,44)),loc(local,'Lift',abstract_variables)]),b(integer(1),integer,[nodeid(pos(26,1,11,47,11,48))])),integer,[nodeid(pos(24,1,11,38,11,48))])),subst,[nodeid(pos(22,1,11,28,11,48))])),subst,[nodeid(pos(18,1,11,10,11,52))])))";
		String element2 = "b(operation(dec,[],[],b(precondition(b(greater(b(identifier(floors),integer,[nodeid(pos(30,1,12,12,12,18)),loc(local,'Lift',abstract_variables)]),b(integer(0),integer,[nodeid(pos(31,1,12,19,12,20))])),pred,[nodeid(pos(29,1,12,12,12,20))]),b(assign_single_id(b(identifier(floors),integer,[nodeid(pos(33,1,12,26,12,32)),loc(local,'Lift',abstract_variables)]),b(minus(b(identifier(floors),integer,[nodeid(pos(35,1,12,36,12,42)),loc(local,'Lift',abstract_variables)]),b(integer(1),integer,[nodeid(pos(36,1,12,45,12,46))])),integer,[nodeid(pos(34,1,12,36,12,46))])),subst,[nodeid(pos(32,1,12,26,12,46))])),subst,[nodeid(pos(28,1,12,8,12,50))])))";
		String element3 = "b(operation(getfloors,[b(identifier(out),integer,[nodeid(pos(38,1,13,2,13,5)),introduced_by(operation)])],[],b(assign_single_id(b(identifier(out),integer,[nodeid(pos(41,1,13,28,13,31)),introduced_by(operation)]),b(identifier(floors),integer,[nodeid(pos(42,1,13,35,13,41)),loc(local,'Lift',abstract_variables)])),subst,[nodeid(pos(40,1,13,28,13,41))])))";
		String element4 = "b(initialisation(b(assign_single_id(b(identifier(floors),integer,[nodeid(pos(14,1,7,16,7,22)),loc(local,'Lift',abstract_variables),not_initialised]),b(integer(0),integer,[nodeid(pos(15,1,7,26,7,27))])),subst,[nodeid(pos(13,1,7,16,7,27))])))";
		List<String> toCompare = Arrays.asList(element1, element2, element3, element4);
		List<String> result = getMachineOperationsFull.getOperations().stream().map(PrologTerm::toString).collect(toList());


		Assertions.assertEquals(new HashSet<>(toCompare), new HashSet<>(result));
	}


	@Test
	public void get_full_map() throws IOException, ModelTranslationError {
		proBKernelStub.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "testTraceMachine.mch"));
		GetMachineOperationsFull getMachineOperationsFull = new GetMachineOperationsFull();
		proBKernelStub.executeCommand(getMachineOperationsFull);
		Assertions.assertEquals(4, getMachineOperationsFull.getOperationsWithNames().size());
	}
}
