package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.IntegerPrologTerm;
import de.prob.prolog.term.PrologTerm;

import java.math.BigInteger;

public class GetStatisticsCommand extends AbstractCommand {

    public enum StatisticsOption {
        GLOBAL_RUNTIME("global_runtime"),
        GLOBAL_WALLTIME("global_walltime"),
        DELTA_RUNTIME("delta_runtime"),
        DELTA_WALLTIME("delta_walltime"),
        MEMORY_USED("memory_used"),
        MEMORY_FREE("memory_free"),
        GLOBAL_STACK_USED("global_stack_used"),
        LOCAL_STACK_USED("local_stack_used"),
        TRAIL_USED("trail_used"),
        CHOICE_USED("choice_used"),
        ATOMS_USED("atoms_used"),
        ATOMS_NBUSED("atoms_nbused"),
        GC_COUNT("gc_count"),
        GC_TIME("gc_time");

        private final String option;

        StatisticsOption(String option) {
            this.option = option;
        }

        public String getOption() {
            return option;
        }
    }

    private static final String PROLOG_COMMAND_NAME = "get_statistics";

    private static final String RESULT = "V";

    private final StatisticsOption option;

    private BigInteger result;

    public GetStatisticsCommand(StatisticsOption option) {
        this.option = option;
    }

    @Override
    public void writeCommand(IPrologTermOutput pto) {
        pto.openTerm(PROLOG_COMMAND_NAME);
        pto.printAtom(option.getOption());
        pto.printVariable(RESULT);
        pto.closeTerm();
    }

    @Override
    public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
        IntegerPrologTerm valueTerm = (IntegerPrologTerm) bindings.get("V");
        result = valueTerm.getValue();
    }

    public BigInteger getResult() {
        return result;
    }

}
