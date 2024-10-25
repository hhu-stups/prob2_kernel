package de.prob.model.representation;

import com.google.inject.Inject;
import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.analysis.prolog.RecursiveMachineLoader;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.animator.command.AbstractCommand;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.animator.domainobjects.TLA;
import de.prob.model.classicalb.ClassicalBModel;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.FormalismType;
import de.prob.statespace.Language;
import de.tla2bAst.Translator;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class TLAModel extends AbstractModel {

	private final ClassicalBModel internalModel;
	private final Translator translator;

	@Inject
	public TLAModel(final StateSpaceProvider ssProvider, final ClassicalBModel internalModel) {
		this(ssProvider, null, internalModel, null);
	}

	public TLAModel(final StateSpaceProvider ssProvider, final File modelFile, final ClassicalBModel internalModel,
	                final Translator translator) {
		super(ssProvider, internalModel.getChildren(), internalModel.getGraph(), modelFile);
		this.internalModel = internalModel;
		this.translator = translator;
	}

	public TLAModel create(final Start mainAST, final RecursiveMachineLoader rml, final File modelFile, final BParser bparser,
	                       final Translator translator) {
		return new TLAModel(stateSpaceProvider, modelFile, internalModel.create(mainAST, rml, modelFile, bparser), translator);
	}

	@Override
	public List<Path> getAllFiles() {
		return this.translator.getModuleFiles().stream()
			.map(File::toPath)
			.collect(Collectors.toList());
	}

	@Override
	public AbstractElement getComponent(String name) {
		return internalModel.getComponent(name);
	}

	@Override
	public AbstractElement getMainComponent() {
		return internalModel.getMainComponent();
	}

	@Override
	public IEvalElement parseFormula(final String formula, final FormulaExpand expand) {
		return new TLA(formula, expand, translator);
		// TODO: tests (TLA + classical B expressions)?
	}

	public IEvalElement parseFormulaAsClassicalB(final String formula, final FormulaExpand expand) {
		return internalModel.parseFormula(formula, expand);
	}

	@Override
	public IEvalElement formulaFromIdentifier(List<String> identifier, FormulaExpand expansion) {
		return internalModel.formulaFromIdentifier(identifier, expansion);
	}

	@Override
	public FormalismType getFormalismType() {
		return FormalismType.B;
	}

	@Override
	public Language getLanguage() {
		return Language.TLA;
	}

	@Override
	public AbstractCommand getLoadCommand() {
		return internalModel.getLoadCommand();
	}
}
