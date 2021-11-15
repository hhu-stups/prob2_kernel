package de.prob.model.representation;

import java.io.File;

import com.github.krukow.clj_lang.PersistentHashMap;
import com.google.inject.Inject;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.LoadZCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EvaluationException;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.FormalismType;

public class ZModel extends AbstractModel {
	@Inject
	public ZModel(final StateSpaceProvider ssProvider) {
		this(ssProvider, null);
	}

	public ZModel(final StateSpaceProvider ssProvider, File modelFile) {
		super(ssProvider, PersistentHashMap.emptyMap(), new DependencyGraph(), modelFile);
	}

	public ZModel create(final File modelFile) {
		return new ZModel(getStateSpaceProvider(), modelFile);
	}
	
	@Override
	public IEvalElement parseFormula(final String formula, final FormulaExpand expand) {
		return new ClassicalB(formula, expand);
	}

	@Override
	public IEvalElement formulaFromIdentifier(final String identifier, final FormulaExpand expansion) {
		return ClassicalB.fromIdentifier(identifier, expansion);
	}

	@Override
	public FormalismType getFormalismType() {
		return FormalismType.Z;
	}

	@Override
	public AbstractCommand getLoadCommand(final AbstractElement mainComponent) {
		return new LoadZCommand(this.getModelFile().getAbsolutePath());
	}

	@Override
	public AbstractElement getComponent(String name) {
		return null;
	}
}
