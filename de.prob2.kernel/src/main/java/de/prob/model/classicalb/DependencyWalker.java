package de.prob.model.classicalb;

import java.util.HashSet;
import java.util.Set;

import de.be4.classicalb.core.parser.analysis.prolog.MachineReference;
import de.be4.classicalb.core.parser.analysis.prolog.RecursiveMachineLoader;
import de.be4.classicalb.core.parser.analysis.prolog.ReferencedMachines;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.model.representation.DependencyGraph;
import de.prob.model.representation.DependencyGraph.ERefType;
import de.prob.model.representation.ModelElementList;

public final class DependencyWalker {

	private final RecursiveMachineLoader rml;
	private ModelElementList<ClassicalBMachine> machines;
	private DependencyGraph graph;
	private Set<String> machineIds;

	public DependencyWalker(final String machineId,
			final RecursiveMachineLoader rml,
			final ModelElementList<ClassicalBMachine> machines,
			final DependencyGraph graph) {
		this.machineIds = new HashSet<>();
		this.machineIds.add(machineId);
		this.rml = rml;
		this.machines = machines;
		this.graph = graph;
	}

	public void addReferences(final String machineId) {
		final String machineName;
		final String prefix;
		final int lastDot = machineId.lastIndexOf('.');
		if (lastDot == -1) {
			machineName = machineId;
			prefix = null;
		} else {
			machineName = machineId.substring(lastDot + 1);
			prefix = machineId.substring(0, lastDot);
		}

		final ReferencedMachines refMachines = rml.getMachineReferenceInfo().get(machineName);
		for (final MachineReference ref : refMachines.getReferences()) {
			this.machineIds.add(concat(ref.getRenamedName(), ref.getName()));

			String refPrefix = prefix;
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
					refPrefix = concat(prefix, ref.getRenamedName());
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

			final ClassicalBMachine newMachine = makeMachine(ref.getName(), refPrefix);
			machines = machines.addElement(newMachine);
			graph = graph.addEdge(concat(prefix, machineName), newMachine.getName(), refType);
		}
	}

	private ClassicalBMachine makeMachine(final String dest, final String prefix) {
		final DomBuilder builder = new DomBuilder(dest, prefix);
		final Start start = rml.getParsedMachines().get(dest);
		start.apply(builder);
		return builder.getMachine();
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
