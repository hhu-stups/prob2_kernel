package de.prob.animator.domainobjects;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

import de.prob.animator.command.ComposedCommand;
import de.prob.animator.command.ExpandFormulaCommand;
import de.prob.animator.command.ExpandFormulaNonrecursiveCommand;
import de.prob.animator.command.GetTopLevelFormulasCommand;
import de.prob.animator.command.InsertFormulaForVisualizationCommand;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;

/**
 * <p>A formula inserted into ProB's BVisual2 mechanism.</p>
 * <p>This mechanism allows expanding a formula into a tree of subformulas and efficiently evaluating each part of the tree. It is also possible to render the structure of a machine (variables, invariants, operation/event guards, etc.) as a tree of BVisual2 formulas. This is used to implement the state view in the ProB 2 UI (and also in the ProB Rodin plugin, although not through this Java API).</p>
 */
public final class BVisual2Formula {
	private final StateSpace stateSpace;
	private final String id;
	
	private BVisual2Formula(final StateSpace stateSpace, final String id) {
		this.stateSpace = Objects.requireNonNull(stateSpace, "stateSpace");
		this.id = Objects.requireNonNull(id, "id");
	}
	
	/**
	 * Get an existing {@link BVisual2Formula} by ID from a state space.
	 * 
	 * @param stateSpace the state space in which the formula is present
	 * @param formulaId the ID of the formula
	 * @return the formula corresponding to the given ID
	 */
	public static BVisual2Formula fromFormulaId(final StateSpace stateSpace, final String formulaId) {
		return new BVisual2Formula(stateSpace, formulaId);
	}
	
	/**
	 * Insert a formula as a {@link BVisual2Formula} in a state space.
	 * 
	 * @param stateSpace the state space in which the formula should be inserted
	 * @param formula the formula to insert
	 * @return the inserted formula
	 */
	public static BVisual2Formula fromFormula(final StateSpace stateSpace, final IEvalElement formula) {
		final InsertFormulaForVisualizationCommand cmd = new InsertFormulaForVisualizationCommand(formula);
		stateSpace.execute(cmd);
		return BVisual2Formula.fromFormulaId(stateSpace, cmd.getId());
	}
	
	/**
	 * Get all top-level {@link BVisual2Formula}s in a state space.
	 * 
	 * @param stateSpace the state space from which to get the top-level formulas
	 * @return a list of all top-level formulas in the state space
	 */
	public static List<BVisual2Formula> getTopLevel(final StateSpace stateSpace) {
		final GetTopLevelFormulasCommand cmd = new GetTopLevelFormulasCommand();
		stateSpace.execute(cmd);
		return cmd.getIds().stream()
			.map(id -> BVisual2Formula.fromFormulaId(stateSpace, id))
			.collect(Collectors.toList());
	}
	
	public StateSpace getStateSpace() {
		return this.stateSpace;
	}
	
	public String getId() {
		return this.id;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final BVisual2Formula other = (BVisual2Formula)obj;
		return this.getStateSpace().equals(other.getStateSpace())
			&& this.getId().equals(other.getId());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getStateSpace(), this.getId());
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("stateSpace", this.getStateSpace())
			.add("id", this.getId())
			.toString();
	}
	
	/**
	 * <p>Expand and evaluate multiple formulas non-recursively in the given state. All formulas must belong to the same state space as the state.</p>
	 * <p>To fully expand a formula recursively, {@link #expandMultiple(List, State)} should be used.</p>
	 * 
	 * @param formulas the formulas to expand and evaluate
	 * @param state the state in which to expand and evaluate the formulas
	 * @return the expanded and evaluated formulas
	 * 
	 * @see #expandNonrecursive(State)
	 */
	public static List<ExpandedFormula> expandNonrecursiveMultiple(final List<BVisual2Formula> formulas, final State state) {
		Objects.requireNonNull(formulas, "formulas");
		Objects.requireNonNull(state, "state");
		
		final List<ExpandFormulaNonrecursiveCommand> expandCommands = formulas.stream()
			.peek(Objects::requireNonNull)
			.peek(formula -> {
				if (!formula.getStateSpace().equals(state.getStateSpace())) {
					throw new IllegalArgumentException(String.format("Formula %s does not belong to the state space of the given state: %s", formula, state.getStateSpace()));
				}
			})
			.map(BVisual2Formula::getId)
			.map(id -> new ExpandFormulaNonrecursiveCommand(id, state))
			.collect(Collectors.toList());
		
		state.getStateSpace().execute(new ComposedCommand(expandCommands));
		
		return expandCommands.stream()
			.map(ExpandFormulaNonrecursiveCommand::getResult)
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>Expand and evaluate this formula non-recursively in the given state. This formula must belong to the same state space as the state.</p>
	 * <p>To expand many formulas in the same state, {@link #expandNonrecursiveMultiple(List, State)} should be used for better performance. To fully expand a formula recursively, {@link #expand(State)} should be used.</p>
	 * 
	 * @param state the state in which to expand and evaluate the formula
	 * @return the expanded and evaluated formula
	 * 
	 * @see #expandNonrecursiveMultiple(List, State)
	 */
	public ExpandedFormula expandNonrecursive(final State state) {
		return expandNonrecursiveMultiple(Collections.singletonList(this), state).get(0);
	}
	
	/**
	 * <p>Expand and evaluate multiple formulas recursively in the given state. All formulas must belong to the same state space as the state.</p>
	 * <p>If the formulas' children are not used (or only partially), {@link #expandNonrecursiveMultiple(List, State)} should be used to avoid recursively evaluating all children when not needed.</p>
	 * 
	 * @param formulas the formulas to expand and evaluate
	 * @param state the state in which to expand and evaluate the formulas
	 * @return the expanded and evaluated formulas
	 * 
	 * @see #expand(State)
	 */
	public static List<ExpandedFormula> expandMultiple(final List<BVisual2Formula> formulas, final State state) {
		Objects.requireNonNull(formulas, "formulas");
		Objects.requireNonNull(state, "state");
		
		final List<ExpandFormulaCommand> expandCommands = formulas.stream()
			.peek(Objects::requireNonNull)
			.peek(formula -> {
				if (!formula.getStateSpace().equals(state.getStateSpace())) {
					throw new IllegalArgumentException(String.format("Formula %s does not belong to the state space of the given state: %s", formula, state.getStateSpace()));
				}
			})
			.map(BVisual2Formula::getId)
			.map(id -> new ExpandFormulaCommand(id, state))
			.collect(Collectors.toList());
		
		state.getStateSpace().execute(new ComposedCommand(expandCommands));
		
		return expandCommands.stream()
			.map(ExpandFormulaCommand::getResult)
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>Expand and evaluate this formula recursively in the given state. This formula must belong to the same state space as the state.</p>
	 * <p>To expand many formulas in the same state, {@link #expandMultiple(List, State)} should be used for better performance. If the formula's children are not used (or only partially), {@link #expandNonrecursive(State)} should be used to avoid recursively evaluating all children when not needed.</p>
	 * 
	 * @param state the state in which to expand and evaluate the formula
	 * @return the expanded and evaluated formula
	 * 
	 * @see #expandMultiple(List, State)
	 */
	public ExpandedFormula expand(final State state) {
		return expandMultiple(Collections.singletonList(this), state).get(0);
	}
}
