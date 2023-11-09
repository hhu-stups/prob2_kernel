package de.prob.animator.command;

import java.util.Collections;
import java.util.List;

import de.prob.animator.domainobjects.IBEvalElement;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class UnsatCoreCommand extends AbstractCommand {

	private final UnsatCoreBaseCommand realCommand;

	public UnsatCoreCommand(IBEvalElement pred) {
		this(pred, Collections.emptyList());
	}

	public UnsatCoreCommand(IBEvalElement pred, boolean minimumCore) {
		this(pred, Collections.emptyList(), minimumCore);
	}

	public UnsatCoreCommand(IBEvalElement pred, List<IBEvalElement> fixedPreds) {
		this(pred, fixedPreds, false);
	}

	public UnsatCoreCommand(IBEvalElement pred, List<IBEvalElement> fixedPreds,
			boolean minimumCore) {
		if (minimumCore)
			realCommand = new UnsatMinimalCoreCommand(pred, fixedPreds);
		else 
			realCommand = new UnsatRegularCoreCommand(pred, fixedPreds);
	}

	@Override
	public void writeCommand(IPrologTermOutput pout) {
		realCommand.writeCommand(pout);
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		realCommand.processResult(bindings);
	}

	public IBEvalElement getCore() {
		return realCommand.getCore();
	}

}
