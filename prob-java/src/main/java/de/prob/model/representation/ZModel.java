package de.prob.model.representation;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.LoadZCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.FormalismType;
import de.prob.statespace.Language;

public class ZModel extends AbstractModel {
	@Inject
	public ZModel(final StateSpaceProvider ssProvider) {
		this(ssProvider, null);
	}

	public ZModel(final StateSpaceProvider ssProvider, File modelFile) {
		super(ssProvider, Collections.emptyMap(), new DependencyGraph(), modelFile);
	}

	public ZModel create(final File modelFile) {
		return new ZModel(stateSpaceProvider, modelFile);
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
		return FormalismType.Z;
	}

	@Override
	public Language getLanguage() {
		return Language.Z;
	}

	@Override
	public AbstractCommand getLoadCommand() {
		return new LoadZCommand(this.getModelFile().getAbsolutePath());
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
