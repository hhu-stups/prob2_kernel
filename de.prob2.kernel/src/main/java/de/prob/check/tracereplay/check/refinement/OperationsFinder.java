package de.prob.check.tracereplay.check.refinement;

import com.sun.org.apache.xpath.internal.functions.WrongNumberArgsException;
import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.*;
import de.prob.exception.ProBError;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OperationsFinder extends DepthFirstAdapter {

	private Set<RenamingContainer> promoted = new HashSet<>();
	private final Map<String, HashSet<String>> used = new HashMap<>();
	private AOperation currentOperation;
	private final String sourceMachine;
	private final Start node;
	private boolean extendsSourceMachine = false;
	private final List<RenamingContainer> extendedMachines = new ArrayList<>();
	private final List<RenamingContainer> includedImportedMachines = new ArrayList<>();


	public static class RenamingContainer {
		final String prefix;
		final String suffix;

		public RenamingContainer(String prefix, String suffix){
			this.prefix = prefix;
			this.suffix = suffix;
		}

		public RenamingContainer(List<TIdentifierLiteral> list) throws ProBError {
			if(list.size() == 2) {
				this.prefix = list.get(0).getText();
				this.suffix = list.get(1).getText();
			}else if(list.size() == 1){
				this.prefix = "";
				this.suffix = list.get(0).getText();
			}else{
				throw new ProBError(new WrongNumberArgsException("Expects a list of length two"));
			}
		}


		public boolean complies(String name){
			return suffix.equals(name);
		}

		@Override
		public boolean equals(Object o){
			if(o instanceof RenamingContainer)
			{
				return ((RenamingContainer) o).suffix.equals(this.suffix) && ((RenamingContainer) o).prefix.equals(this.prefix);
			}
			return false;
		}

		@Override
		public String toString(){
			if(prefix.equals("")){
				return suffix;
			}else {
				return prefix + "." + suffix;
			}
		}
	}



	public OperationsFinder(String sourceMachine, Start node){
		this.sourceMachine = sourceMachine;
		this.node = node;
	}

	public void explore(){
		node.apply(this);
	}


	public Map<String, Set<String>> usedOperationsReversed(){
		return used.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()).stream().collect(Collectors.toMap(entry -> entry, entry ->
			 used.entrySet().stream().filter(innerEntry -> innerEntry.getValue().contains(entry))
					 .map(Map.Entry::getKey).collect(Collectors.toSet())
		));
	}

	@Override
	public void caseAPromotesMachineClause(APromotesMachineClause node)
	{
		Set<RenamingContainer> result = new HashSet<>();
		for(PExpression expression : node.getOperationNames()){
			if(expression instanceof AIdentifierExpression){
				LinkedList<TIdentifierLiteral> identifier = ((AIdentifierExpression) expression).getIdentifier();
				if(identifier.size() == 1){
					result.add(new RenamingContainer("", identifier.get(0).getText()));
				}else if(identifier.size() == 2){
					result.add(new RenamingContainer(identifier.get(0).getText(), identifier.get(1).getText()));
				}
			}
		}

		promoted = result;
	}



	@Override
	public void caseAOperation(AOperation node)
	{
		currentOperation = node;
		if(node.getOperationBody() != null)
		{
			node.getOperationBody().apply(this);
		}
	}

	@Override
	public void caseAOpSubstitution(AOpSubstitution node)
	{
		String function = node.getName().toString().trim();
		if(used.containsKey(function)){
			used.get(function).add(currentOperation.getOpName().getFirst().getText());
		}
		else{
			used.put(currentOperation.getOpName().getFirst().getText(), new HashSet<>(Stream.of(function).collect(Collectors.toSet())));
		}
	}

	@Override
	public void caseAExtendsMachineClause(AExtendsMachineClause node)
	{

		List<PMachineReference> copy = new ArrayList<>(node.getMachineReferences());
		for(PMachineReference reference : copy){
			reference.apply(this);
		}

	}

	@Override
	public void caseAMachineReference(AMachineReference node)
	{
		if(node.parent() instanceof AExtendsMachineClause){
			boolean sourceMachineExtended = node.getMachineName().stream().anyMatch(entry -> entry.toString().trim().equals(sourceMachine));
			extendsSourceMachine = sourceMachineExtended || extendsSourceMachine;
		    extendedMachines.add(new RenamingContainer(node.getMachineName()));

		}else if(node.parent() instanceof  AIncludesMachineClause || node.parent() instanceof  AImportsMachineClause){
			includedImportedMachines.add(new RenamingContainer(node.getMachineName()));
		}

	}

	public List<RenamingContainer> getExtendedMachines() {
		return extendedMachines;
	}

	public List<RenamingContainer> getIncludedImportedMachines() {
		return includedImportedMachines;
	}

	public Set<RenamingContainer> getPromoted() {
		return promoted;
	}

	public Map<String, HashSet<String>> getUsed() {
		return used;
	}

	public boolean isExtendsSourceMachine() {
		return extendsSourceMachine;
	}

}
