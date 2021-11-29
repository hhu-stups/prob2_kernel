package de.prob.model.classicalb;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.be4.classicalb.core.parser.analysis.prolog.MachineReference;
import de.be4.classicalb.core.parser.analysis.prolog.ReferencedMachines;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.model.representation.DependencyGraph;
import de.prob.model.representation.DependencyGraph.ERefType;
import de.prob.model.representation.ModelElementList;

public final class DependencyWalker {

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

	public void addReferences(final ReferencedMachines refMachines) {
		for (final MachineReference ref : refMachines.getReferences()) {
			this.machineIds.add(concat(ref.getRenamedName(), ref.getName()));

			String refPrefix = this.prefix;
			final ERefType refType;
			switch (ref.getType()) {
				case SEES:
					refType = ERefType.SEES;
					break;
				
				case USES:
					refPrefix = ref.getName(); // TODO test this
					refType = ERefType.USES;
					break;
				
				case REFINES:
					refType = ERefType.REFINES;
					break;
				
				case INCLUDES:
					refPrefix = concat(this.prefix, ref.getRenamedName());
					refType = ERefType.INCLUDES;
					break;
				
				case EXTENDS:
					refType = ERefType.EXTENDS;
					break;
				
				case IMPORTS:
					refType = ERefType.IMPORTS;
					break;
				
				default:
					continue;
			}

			addMachine(ref.getName(), refPrefix, refType);
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
