package de.prob.animator.command;

import de.prob.animator.domainobjects.FormulaTranslationMode;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public final class GetInternalRepresentationCommand extends AbstractCommand {
	public enum TypeInfos {
		// These correspond to the modes accepted by translate:set_print_type_infos/1 in probcli.
		NONE("none"),
		NEEDED("needed"),
		ALL("all"),
		;
		
		private final String prologName;
		
		TypeInfos(final String prologName) {
			this.prologName = prologName;
		}
		
		public String getPrologName() {
			return this.prologName;
		}
	}
	
	private static final String PROLOG_COMMAND_NAME = "get_machine_internal_representation";
	private static final String PRETTY_PRINT = "PP";
	
	private FormulaTranslationMode translationMode;
	private GetInternalRepresentationCommand.TypeInfos typeInfos;
	private String prettyPrint;
	
	public GetInternalRepresentationCommand() {
		this.translationMode = FormulaTranslationMode.ASCII;
		this.typeInfos = GetInternalRepresentationCommand.TypeInfos.NONE;
	}
	
	public FormulaTranslationMode getTranslationMode() {
		return this.translationMode;
	}
	
	/**
	 * Change the syntax mode to use when pretty-printing the internal representation.
	 * Currently only {@link FormulaTranslationMode#ASCII} and {@link FormulaTranslationMode#UNICODE} are properly supported.
	 * 
	 * @param translationMode syntax to use when pretty-printing the internal representation
	 */
	public void setTranslationMode(final FormulaTranslationMode translationMode) {
		this.translationMode = translationMode;
	}
	
	public GetInternalRepresentationCommand.TypeInfos getTypeInfos() {
		return typeInfos;
	}
	
	public void setTypeInfos(final GetInternalRepresentationCommand.TypeInfos typeInfos) {
		this.typeInfos = typeInfos;
	}
	
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.openList();
		
		pto.openTerm("translation_mode");
		pto.printAtom(this.getTranslationMode().getPrologName());
		pto.closeTerm();
		
		pto.openTerm("type_infos");
		pto.printAtom(this.getTypeInfos().getPrologName());
		pto.closeTerm();
		
		pto.closeList();
		pto.printVariable(PRETTY_PRINT);
		pto.closeTerm();
	}
	
	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.prettyPrint = bindings.get(PRETTY_PRINT).atomToString();
	}
	
	public String getPrettyPrint() {
		return this.prettyPrint;
	}
}
