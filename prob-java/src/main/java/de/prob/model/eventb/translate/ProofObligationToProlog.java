package de.prob.model.eventb.translate;

import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.node.*;
import de.prob.animator.domainobjects.IBEvalElement;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.eventb.Context;
import de.prob.model.eventb.EventBMachine;
import de.prob.model.eventb.ProofObligation;
import de.prob.model.representation.Set;
import de.prob.prolog.output.IPrologTermOutput;
import de.tla2bAst.BAstCreator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ProofObligationToProlog {

	public static void toDisproverProlog(final EventBMachine mch, final IPrologTermOutput pto) {
		printMetaInfoFacts(mch.getName(), pto);
		for (ProofObligation po : mch.getProofs()) {
			toDisproverProlog(po, mch, pto);
		}
	}

	public static void toDisproverProlog(final Context ctx, final IPrologTermOutput pto) {
		printMetaInfoFacts(ctx.getName(), pto);
		for (ProofObligation po : ctx.getProofs()) {
			toDisproverProlog(po, ctx, pto);
		}
	}

	public static void toDisproverProlog(final ProofObligation po, final EventBMachine mch, final IPrologTermOutput pto) {
		List<Context> allCtx = new ArrayList<>();
		for (Context ctx : mch.getSees()) {
			allCtx.add(ctx);
			allCtx.addAll(getAllContexts(ctx));
		}
		printPOFact(po, allCtx, pto);
	}

	public static void toDisproverProlog(final ProofObligation po, final Context ctx, final IPrologTermOutput pto) {
		List<Context> contexts = getAllContexts(ctx);
		contexts.add(ctx);
		printPOFact(po, contexts, pto);
	}

	private static List<Context> getAllContexts(final Context ctx) {
		List<Context> contexts = new ArrayList<>();
		for (Context c : ctx.getExtends()) {
			contexts.add(c);
			contexts.addAll(getAllContexts(c));
		}
		return contexts;
	}

	private static void printMetaInfoFacts(final String machineName, final IPrologTermOutput pto) {
		Date date = new Date();
		pto.openTerm("generated");
		pto.printNumber(date.getTime());
		pto.printAtom(date.toString());
		pto.closeTerm();
		pto.fullstop();

		pto.openTerm("project_name");
		pto.printAtom(machineName); // TODO: do we have the project name?
		pto.closeTerm();
		pto.fullstop();

		pto.openTerm("machine_name");
		pto.printAtom(machineName);
		pto.closeTerm();
		pto.fullstop();
	}

	private static void printPOFact(final ProofObligation po, final List<Context> contexts, final IPrologTermOutput pto) {
		pto.openTerm("disprover_po"); // Label,Context,Goal,AllHyps,SelHyps,Status
		pto.printAtom(po.getName());
		createDisproverContext(po, contexts).apply(new ASTProlog(pto, null));
		po.getGoal().printProlog(pto);
		pto.openList();
		for (IEvalElement hyp : po.getHypotheses()) {
			hyp.printProlog(pto);
		}
		pto.closeList();
		pto.openList();
		for (IEvalElement hyp : po.getSelectedHypotheses()) {
			hyp.printProlog(pto);
		}
		pto.closeList();
		pto.printAtom(po.isDischarged() ? "true" : "unknown");
		pto.closeTerm();
		pto.fullstop();
		pto.flush();
	}

	private static AEventBContextParseUnit createDisproverContext(ProofObligation po, List<Context> contexts) {
		// cf. ProB Rodin Plugin: DisproverContextCreator
		List<PExpression> constantsIdentifiers = new ArrayList<>();
		List<PSet> sets = new ArrayList<>();
		List<PPredicate> axioms = new ArrayList<>();

		// sets:
		for (Context ctx : contexts) {
			for (Set set : ctx.getSets()) {
				sets.add(new ADeferredSetSet(BAstCreator.createTIdentifierLiteral(set.getName())));
			}
		}

		// type env:
		for (String id : po.getIdentifiers().keySet()) {
			IEvalElement type = po.getIdentifiers().get(id);
			if (type instanceof IBEvalElement) {
				PExpression identifier = new AIdentifierExpression(BAstCreator.createTIdentifierLiteral(id));
				axioms.add(new AMemberPredicate(identifier, (PExpression) ((IBEvalElement) type).getAst()));
				constantsIdentifiers.add(identifier.clone());
			}
		}

		for (IEvalElement hyp : po.getHypotheses()) {
			if (hyp instanceof IBEvalElement) {
				axioms.add((PPredicate) ((IBEvalElement) hyp).getAst());
			}
		}

		return new AEventBContextParseUnit(
				new TIdentifierLiteral("DisproverContext"),
				Arrays.asList( // in the order used by the ProB Rodin Plugin:
						new AAxiomsContextClause(axioms),
						new AConstantsContextClause(constantsIdentifiers),
						new ASetsContextClause(sets)
				)
		);
	}

}
