package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class ExportVisBForHistoryCommand extends AbstractCommand {

    private static final String PROLOG_COMMAND_NAME = "prob2_export_visb_html_for_history";

    private final String stateID;

    private final String path;

    public ExportVisBForHistoryCommand(final String stateID, final String path) {
        this.stateID = stateID;
        this.path = path;
    }

    @Override
    public void writeCommand(IPrologTermOutput pto) {
        pto.openTerm(PROLOG_COMMAND_NAME);
        pto.printAtomOrNumber(stateID);
        pto.printAtom(path);
        pto.closeTerm();
    }

    @Override
    public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
        // There are no output variables.
    }

}
