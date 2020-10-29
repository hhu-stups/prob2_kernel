package de.prob.animator.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

import java.util.Map;

/**
 * Hands down two operations to prolog and as a result gets a delta when both operation are similar in name, parameter name
 * or a no when operations are not similar according to these metrics.
 */
public class CompareTwoOperations extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "compare_operations";
	public static final String VARIABLE = "DELTA";
	private Map<String, String> delta;
	

	private final CompoundPrologTerm operationOld;
	private final CompoundPrologTerm operationNew;
	private final ObjectMapper objectMapper;
	private final ListPrologTerm foundVariables;
	private final ListPrologTerm freeVariables;


	public CompareTwoOperations(CompoundPrologTerm operationOld, CompoundPrologTerm operationNew, ListPrologTerm foundVars, ListPrologTerm freeVars, ObjectMapper objectMapper){
		this.operationNew = operationNew;
		this.operationOld = operationOld;
		this.objectMapper = objectMapper;
		this.foundVariables = foundVars;
		this.freeVariables = freeVars;
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME).printTerm(operationOld).printTerm(operationNew).printTerm(foundVariables).printTerm(freeVariables).printVariable(VARIABLE).closeTerm();
		System.out.println(pto);
	}


	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		TypeReference<Map<String, String>> typeReference = new TypeReference<Map<String, String>>() {};
		try {
			delta = objectMapper.readValue(bindings.get(VARIABLE).toString().replace("'", ""), typeReference);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
	
	public Map<String, String> getDelta(){
		return delta;
	}
}
