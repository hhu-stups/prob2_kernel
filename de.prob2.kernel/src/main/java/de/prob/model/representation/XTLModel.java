package de.prob.model.representation;

import java.io.File;
import java.util.List;

import com.github.krukow.clj_lang.PersistentHashMap;
import com.google.inject.Inject;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.LoadXTLCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.FormalismType;

public class XTLModel extends AbstractModel {
	@Inject
	public XTLModel(final StateSpaceProvider ssProvider) {
		this(ssProvider, null);
	}

	public XTLModel(final StateSpaceProvider ssProvider, File modelFile) {
		super(ssProvider, PersistentHashMap.emptyMap(), new DependencyGraph(), modelFile);
	}

	public XTLModel create(final File modelFile) {
		return new XTLModel(getStateSpaceProvider(), modelFile);
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
		return FormalismType.XTL;
	}

	@Override
	public AbstractCommand getLoadCommand(final AbstractElement mainComponent) {
		return new LoadXTLCommand(this.getModelFile().getAbsolutePath());
	}

	@Override
	public AbstractElement getComponent(String name) {
		return null;
	}
}
