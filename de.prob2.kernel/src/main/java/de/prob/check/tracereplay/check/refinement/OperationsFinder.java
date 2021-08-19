package de.prob.check.tracereplay.check.refinement;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OperationsFinder extends DepthFirstAdapter {

	Set<String> promoted = new HashSet<>();
	Map<String, HashSet<String>> used = new HashMap<>();
	private AOperationCallSubstitution currentOperation;

	@Override
	public void caseAPromotesMachineClause(APromotesMachineClause node)
	{

			List<PExpression> copy = new ArrayList<>(node.getOperationNames());
			for(PExpression e : copy)
			{
				e.apply(this);
			}


	}

	@Override
	public void caseAOperationCallSubstitution(AOperationCallSubstitution node)
	{
		currentOperation = node;
		inAOperationCallSubstitution(node);
		{
			List<PExpression> copy = new ArrayList<>(node.getResultIdentifiers());
			for(PExpression e : copy)
			{
				e.apply(this);
			}
		}
		{
			List<TIdentifierLiteral> copy = new ArrayList<>(node.getOperation());
			for(TIdentifierLiteral e : copy)
			{
				e.apply(this);
			}
		}
		{
			List<PExpression> copy = new ArrayList<>(node.getParameters());
			for(PExpression e : copy)
			{
				e.apply(this);
			}
		}
		outAOperationCallSubstitution(node);
	}

	@Override
	public void caseAOperationCallExpression(AOperationCallExpression node)
	{
			List<TIdentifierLiteral> copy = new ArrayList<>(node.getOperation());
			assert !copy.isEmpty();
			String function = copy.get(0).getText();
			if(used.containsKey(function)){
				used.get(function).add(currentOperation.getOperation().get(0).getText());
			}
			else{
				used.put(function, new HashSet<>(Stream.of(currentOperation.getOperation().get(0).getText()).collect(Collectors.toSet())));
			}
	}

}
