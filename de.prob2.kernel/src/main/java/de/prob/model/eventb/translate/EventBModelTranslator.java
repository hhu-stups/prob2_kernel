package de.prob.model.eventb.translate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.node.Node;
import de.prob.model.eventb.Context;
import de.prob.model.eventb.EventBConstant;
import de.prob.model.eventb.EventBMachine;
import de.prob.model.eventb.EventBModel;
import de.prob.model.eventb.EventBVariable;
import de.prob.model.eventb.ProofObligation;
import de.prob.model.representation.AbstractElement;
import de.prob.prolog.output.IPrologTermOutput;

public class EventBModelTranslator {
	private final List<EventBMachineTranslator> machineTranslators = new ArrayList<>();
	private final List<ContextTranslator> contextTranslators = new ArrayList<>();
	private final List<ProofObligation> proofObligations = new ArrayList<>();
	private final TheoryTranslator theoryTranslator;
	private final EventBModel model;

	public EventBModelTranslator(final EventBModel model,
			final AbstractElement mainComponent) {
		this.model = model;

		for (EventBMachine machine : extractMachineHierarchy(mainComponent, model)) {
            machineTranslators.add(new EventBMachineTranslator(machine));
			proofObligations.addAll(machine.getProofs());
		}

		for (Context context : extractContextHierarchy(mainComponent, model)) {
			contextTranslators.add(new ContextTranslator(context));
			proofObligations.addAll(context.getProofs());
		}

		theoryTranslator = new TheoryTranslator(model.getTheories());
	}

	public List<EventBMachine> extractMachineHierarchy(
			final AbstractElement mainComponent, EventBModel model) {
		if (mainComponent instanceof Context) {
			return Collections.emptyList();
		}
		List<EventBMachine> machines = new ArrayList<>();
		if (mainComponent instanceof EventBMachine) {
			EventBMachine machine = (EventBMachine) mainComponent;
			machines.add(machine);
			machines.addAll(extractMachines(machine, model));
		}
		return machines;
	}

	private List<EventBMachine> extractMachines(final EventBMachine machine, EventBModel model) {
		final EventBMachine refines = machine.getRefinesMachine();
		if (refines == null) {
			return Collections.emptyList();
		}
		EventBMachine refinedMachine = model.getMachine(refines.getName());
		List<EventBMachine> machines = new ArrayList<>();
		machines.add(refinedMachine);
		machines.addAll(extractMachines(refinedMachine, model));
		return machines;
	}

	public List<Context> extractContextHierarchy(
			final AbstractElement mainComponent, EventBModel model) {
		if (mainComponent instanceof Context) {
			return extractContextHierarchy((Context) mainComponent, model);
		}
		if (mainComponent instanceof EventBMachine) {
			return extractContextHierarchy((EventBMachine) mainComponent, model);
		}
		return Collections.emptyList();
	}

	private List<Context> extractContextHierarchy(final EventBMachine machine, EventBModel model) {
		List<Context> contexts = new ArrayList<>();
		for (Context c : machine.getSees()) {
			Context seenContext = model.getContext(c.getName());
			contexts.add(seenContext);
			List<Context> contextHierarchy = extractContextHierarchy(seenContext, model);
			for (Context context : contextHierarchy) {
				if (!contexts.contains(context)) {
					contexts.add(context);
				}
			}
		}
		return contexts;
	}

	private List<Context> extractContextHierarchy(final Context context, EventBModel model) {
		List<Context> contexts = new ArrayList<>();
		contexts.add(context);
		for (Context c : context.getExtends()) {
			Context extendedContext = model.getContext(c.getName());
			contexts.add(extendedContext);
			List<Context> contextHierarchy = extractContextHierarchy(extendedContext, model);
			for (Context c2 : contextHierarchy) {
				if (!contexts.contains(c2)) {
					contexts.add(c2);
				}
			}
		}
		return contexts;
	}

	public void printProlog(final IPrologTermOutput pto) {
		RodinPosPrinter labelPrinter = new RodinPosPrinter();

		List<Node> machineNodes = new ArrayList<>();
		List<Node> contextNodes = new ArrayList<>();
		for (EventBMachineTranslator trans : machineTranslators) {
			machineNodes.add(trans.translateMachine());
			labelPrinter.addPositions(trans.getPositions());
		}
		for (ContextTranslator t : contextTranslators) {
			contextNodes.add(t.translateContext());
			labelPrinter.addPositions(t.getPositions());
		}

		ASTProlog printer = new ASTProlog(pto, labelPrinter);
		pto.openTerm("load_event_b_project");
		pto.openList();
		for (Node node : machineNodes) {
			node.apply(printer);
		}
		pto.closeList();

		pto.openList();
		for (Node node : contextNodes) {
			node.apply(printer);
		}
		pto.closeList();

		pto.openList();
		pto.openTerm("exporter_version");
		pto.printNumber(3);
		pto.closeTerm();

		for (ProofObligation po : proofObligations) {
			po.toProlog(pto);
		}

		theoryTranslator.toProlog(pto);

		printPragmas(pto);

		pto.closeList();

		pto.printVariable("_Error");
		pto.closeTerm();
	}

	private void printPragmas(final IPrologTermOutput pto) {
		for (EventBMachine machine : model.getMachines()) {
            for (EventBVariable var : machine.getVariables()) {
				if (var.hasUnit()) {
					pto.openTerm("pragma");
					pto.printAtom("unit");
					pto.printAtom(machine.getName());
					pto.printAtom(var.getName());
					pto.openList();
					pto.printAtom(var.getUnit());
					pto.closeList();
					pto.closeTerm();
				}
			}
		}

		for (Context context : model.getContexts()) {
			for (EventBConstant constant : context.getConstants()) {
				if (constant.hasUnit()) {
					pto.openTerm("pragma");
					pto.printAtom("unit");
					pto.printAtom(context.getName());
					pto.printAtom(constant.getName());
					pto.openList();
					pto.printAtom(constant.getUnit());
					pto.closeList();
					pto.closeTerm();
				}
			}
		}
	}

	/**
	 * Variant of {@link #printProlog(IPrologTermOutput)} which prints a complete top-level {@code package/1} fact.
	 * This is the format expected by ProB in .eventb files.
	 * 
	 * @param pto where to output the fact
	 */
	public void printPrologFact(IPrologTermOutput pto) {
		pto.openTerm("package");
		this.printProlog(pto);
		pto.closeTerm();
		pto.fullstop();
		pto.flush();
	}
}
