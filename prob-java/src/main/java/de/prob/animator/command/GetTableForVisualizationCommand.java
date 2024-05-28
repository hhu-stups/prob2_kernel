package de.prob.animator.command;

import java.util.List;

import de.prob.animator.domainobjects.DynamicCommandItem;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.animator.domainobjects.TableData;
import de.prob.animator.domainobjects.TableVisualizationCommand;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;
import de.prob.statespace.Trace;

public class GetTableForVisualizationCommand extends AbstractDynamicVisualizationCommand<TableVisualizationCommand> {

	private static final String PROLOG_COMMAND_NAME = "call_table_command_with_trace";
	private static final String TABLE_VAR = "TABLE";

	private TableData table;

	@Deprecated
	public GetTableForVisualizationCommand(State state, DynamicCommandItem item, List<IEvalElement> formulas) {
		this(state.getStateSpace().getTrace(state.getId()), (TableVisualizationCommand) item, formulas);
	}

	public GetTableForVisualizationCommand(Trace trace, TableVisualizationCommand item, List<IEvalElement> formulas) {
		super(PROLOG_COMMAND_NAME, trace, item, null, formulas);
	}

	@Override
	protected void writeExtra(IPrologTermOutput pto) {
		pto.printVariable(TABLE_VAR);
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		this.table = TableData.fromProlog(bindings.get(TABLE_VAR));
	}

	public TableData getTable() {
		return table;
	}
}
