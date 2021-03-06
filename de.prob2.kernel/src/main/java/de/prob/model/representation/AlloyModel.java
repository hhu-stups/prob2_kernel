package de.prob.model.representation;

import java.io.File;

import com.github.krukow.clj_lang.PersistentHashMap;
import com.google.inject.Inject;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.LoadAlloyTermCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EvaluationException;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.FormalismType;

public class AlloyModel extends AbstractModel {
	private final String term;
	
	@Inject
	public AlloyModel(final StateSpaceProvider ssProvider) {
		this(ssProvider, null, null);
	}

	public AlloyModel(final StateSpaceProvider ssProvider, final File modelFile, final String term) {
		super(ssProvider, PersistentHashMap.emptyMap(), new DependencyGraph(), modelFile);

		this.term = term;
	}

	public AlloyModel create(final File modelFile, final String term) {
		return new AlloyModel(getStateSpaceProvider(), modelFile, term);
	}
	
	@Override
	public IEvalElement parseFormula(final String formula, final FormulaExpand expand) {
		return new ClassicalB(formula, expand);
	}

	@Override
	public FormalismType getFormalismType() {
		return FormalismType.B;
	}

	@Override
	public AbstractCommand getLoadCommand(final AbstractElement mainComponent) {
		return new LoadAlloyTermCommand(this.term, this.getModelFile().toString());
	}

	@Override
	public AbstractElement getComponent(String name) {
		return null;
	}
}
