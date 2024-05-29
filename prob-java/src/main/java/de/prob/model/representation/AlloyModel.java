package de.prob.model.representation;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.LoadAlloyTermCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.FormalismType;
import de.prob.statespace.Language;

public class AlloyModel extends AbstractModel {
	private final String term;
	
	@Inject
	public AlloyModel(final StateSpaceProvider ssProvider) {
		this(ssProvider, null, null);
	}

	public AlloyModel(final StateSpaceProvider ssProvider, final File modelFile, final String term) {
		super(ssProvider, Collections.emptyMap(), new DependencyGraph(), modelFile);

		this.term = term;
	}

	public AlloyModel create(final File modelFile, final String term) {
		return new AlloyModel(stateSpaceProvider, modelFile, term);
	}
	
	@Override
	public IEvalElement parseFormula(final String formula, final FormulaExpand expand) {
		return new ClassicalB(formula, expand);
	}

	@Override
	public IEvalElement formulaFromIdentifier(final List<String> identifier, final FormulaExpand expansion) {
		return ClassicalB.fromIdentifier(identifier, expansion);
	}

	@Override
	public FormalismType getFormalismType() {
		return FormalismType.B;
	}

	@Override
	public Language getLanguage() {
		return Language.ALLOY;
	}

	@Override
	public AbstractCommand getLoadCommand() {
		return new LoadAlloyTermCommand(this.term, this.getModelFile().toString());
	}

	@Override
	public AbstractElement getComponent(String name) {
		return null;
	}

	@Override
	public AbstractElement getMainComponent() {
		return null;
	}
}
