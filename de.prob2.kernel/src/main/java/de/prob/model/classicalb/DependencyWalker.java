package de.prob.model.classicalb;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.AImplementationMachineParseUnit;
import de.be4.classicalb.core.parser.node.AImportsMachineClause;
import de.be4.classicalb.core.parser.node.AMachineReference;
import de.be4.classicalb.core.parser.node.ARefinementMachineParseUnit;
import de.be4.classicalb.core.parser.node.ASeesMachineClause;
import de.be4.classicalb.core.parser.node.AUsesMachineClause;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.PMachineReference;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.be4.classicalb.core.parser.node.Token;

import de.prob.model.representation.DependencyGraph;
import de.prob.model.representation.DependencyGraph.ERefType;
import de.prob.model.representation.ModelElementList;

public class DependencyWalker extends DepthFirstAdapter {

	private DependencyGraph graph;
	private final String prefix;
	private final String name;
	private final Map<String, Start> parsedMachines;
	private ModelElementList<ClassicalBMachine> machines;
	private Set<String> machineIds;

	public DependencyWalker(final String machineId,
			final ModelElementList<ClassicalBMachine> machines,
			final DependencyGraph graph, final Map<String, Start> parsedMachines) {
		this.machineIds = new HashSet<>();
		this.machineIds.add(machineId);
		final int lastDot = machineId.lastIndexOf('.');
		if (lastDot == -1) {
			this.name = machineId;
			this.prefix = null;
		} else {
			this.name = machineId.substring(lastDot + 1);
			this.prefix = machineId.substring(0, lastDot);
		}
		this.machines = machines;
		this.graph = graph;
		this.parsedMachines = parsedMachines;
	}

	@Override
	public void caseASeesMachineClause(final ASeesMachineClause node) {
		registerMachineNames(node.getMachineNames(), ERefType.SEES);
	}

	@Override
	public void caseAUsesMachineClause(final AUsesMachineClause node) {
		registerMachineNames(node.getMachineNames(), ERefType.USES);
	}

	@Override
	public void caseAImportsMachineClause(final AImportsMachineClause node) {
		for (final PMachineReference r : node.getMachineReferences()) {
			final String dest = extractMachineName(((AMachineReference) r).getMachineName());
			addMachine(dest, prefix, ERefType.IMPORTS);
		}
	}

	@Override
	public void caseAMachineReference(final AMachineReference node) {
		final String dest = extractMachineName(node.getMachineName());
		final String prefix = extractMachinePrefix(node.getMachineName());
		addMachine(dest, concat(this.prefix, prefix), ERefType.INCLUDES);
	}


	@Override
	public void outARefinementMachineParseUnit(
			final ARefinementMachineParseUnit node) {
		registerRefinementMachine(node.getRefMachine());
	}

	@Override
	public void outAImplementationMachineParseUnit(
			final AImplementationMachineParseUnit node) {
		registerRefinementMachine(node.getRefMachine());
	}

	private void registerRefinementMachine(final TIdentifierLiteral refMachine) {
		final String dest = refMachine.getText();
		addMachine(dest, prefix, ERefType.REFINES);
	}

	private void registerMachineNames(final List<PExpression> machineNames,
			final ERefType depType) {
		for (final PExpression machineName : machineNames) {
			if (machineName instanceof AIdentifierExpression) {
				final AIdentifierExpression identifier = (AIdentifierExpression) machineName;
				final String dest = extractMachineName(identifier
						.getIdentifier());
				addMachine(dest, depType == ERefType.USES ? dest : prefix, depType); // TODO test this
			}
		}
	}

	private static String extractIdentifierName(final List<TIdentifierLiteral> nameL) {
		return nameL.stream()
			.map(Token::getText)
			.collect(Collectors.joining("."));
	}

	private String extractMachineName(final LinkedList<TIdentifierLiteral> list) {
		machineIds.add(extractIdentifierName(list));
		return list.getLast().getText();
	}

	private String extractMachinePrefix(LinkedList<TIdentifierLiteral> list) {
		if (list.size() > 1) {
			return extractIdentifierName(list.subList(0, list.size() - 1));
		} else {
			return null;
		}
	}

	private ClassicalBMachine makeMachine(final String dest, final String prefix) {
		final DomBuilder builder = new DomBuilder(dest, prefix);
		final Start start = parsedMachines.get(dest);
		start.apply(builder);
		return builder.getMachine();
	}

	// Takes the name of the destination machine, makes it, and puts it in the
	// graph
	private void addMachine(final String dest, String prefix, final ERefType refType) {
		final ClassicalBMachine newMachine = makeMachine(dest, prefix);
		machines = machines.addElement(newMachine);
		graph = graph.addEdge(concat(this.prefix, this.name), newMachine.getName(), refType);
	}

	public String concat(String prefix, String name) {
		if (prefix == null) {
			return name;
		}
		return prefix + "." + name;
	}

	public ModelElementList<ClassicalBMachine> getMachines() {
		return machines;
	}

	public DependencyGraph getGraph() {
		return graph;
	}

	public Set<String> getMachineIds() {
		return machineIds;
	}

}
