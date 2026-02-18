package de.prob.model.eventb.translate;

import de.prob.animator.domainobjects.EventB;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.eventb.ProofObligation;
import de.prob.prolog.term.PrologTerm;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

public class ProofXmlHandler extends DefaultHandler {

	public static class ExtractedSequent {
		private final String description;
		private final List<IEvalElement> hypotheses;
		private final List<IEvalElement> selectedHypotheses;
		private final IEvalElement goal;

		ExtractedSequent(String description, List<IEvalElement> hypotheses, List<IEvalElement> selectedHypotheses, IEvalElement goal) {
			this.description = description;
			this.hypotheses = hypotheses;
			this.selectedHypotheses = selectedHypotheses;
			this.goal = goal;
		}

		ProofObligation toProofObligation(String source, String name, int confidence, List<PrologTerm> sourceInfos) {
			return new ProofObligation(source, name, confidence, description, sourceInfos, hypotheses, selectedHypotheses, goal);
		}
	}

	private static final String PREDICATE_SET_PREFIX = "|org.eventb.core.poPredicateSet#";
	private static final String PREDICATE_PREFIX = "|org.eventb.core.poPredicate#";

	private Map<String,ExtractedSequent> extractedSequents;

	private final Map<String, String> descriptions = new LinkedHashMap<>();
	private final Map<String, List<IEvalElement>> hypotheses = new HashMap<>();
	private final Map<String, List<IEvalElement>> selectedHypotheses = new HashMap<>();
	private final Map<String, IEvalElement> goals = new HashMap<>();

	private final Map<String, String> poPredicateSets = new HashMap<>(); // PO_ID -> predSetName
	private final Map<String, String> predicateSetParentSets = new HashMap<>(); // predSetName -> parentSetName
	private final Map<String, Map<String,IEvalElement>> predicateSetPredicates = new HashMap<>(); // predSetName -> (predName -> predicate)
	private final Map<String, Map<String,String>> singlePredicateSelectionHints = new HashMap<>(); // PO_ID -> (predSetName -> predName)
	private final Map<String, Set<Map.Entry<String,String>>> predicateSetSelectionHints = new HashMap<>(); // PO_ID -> {predSetFstName,predSetSndName}
	private final Map<String, Set<String>> selectedPredicateSets = new HashMap<>(); // PO_ID -> predSetNames

	private String currSequent = null;
	private String currHypSet = null;

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
		switch (qName) {
			case "org.eventb.core.poSequent": {
				String name = attributes.getValue("name");
				descriptions.put(name, attributes.getValue("org.eventb.core.poDesc"));
				currSequent = name;
				singlePredicateSelectionHints.put(name, new HashMap<>());
				predicateSetSelectionHints.put(name, new HashSet<>());
				break;
			}

			case "org.eventb.core.poPredicateSet": {
				String parentSetAttr = attributes.getValue("org.eventb.core.parentSet");
				if (currSequent == null) {
					currHypSet = attributes.getValue("name");
					predicateSetPredicates.put(currHypSet, new HashMap<>());
					if (parentSetAttr != null) {
						String parentSetName = extractPredicateSetName(parentSetAttr).getValue();
						predicateSetParentSets.put(currHypSet, parentSetName);
					}
				} else {
					String hypSetName = extractPredicateSetName(parentSetAttr).getValue();
					poPredicateSets.put(currSequent, hypSetName);
				}
				break;
			}

			case "org.eventb.core.poPredicate": {
				if (currHypSet != null) {
					String name = attributes.getValue("name");
					String hyp = attributes.getValue("org.eventb.core.predicate");
					predicateSetPredicates.get(currHypSet).put(name, new EventB(hyp));
				} else if (currSequent != null) {
					String goal = attributes.getValue("org.eventb.core.predicate");
					goals.put(currSequent, new EventB(goal));
				}
				break;
			}

			case "org.eventb.core.poSelHint": {
				if (currSequent != null) {
					String firstHint = attributes.getValue("org.eventb.core.poSelHintFst");
					String secondHint = attributes.getValue("org.eventb.core.poSelHintSnd");

					if (secondHint == null) {
						Map.Entry<Integer,String> fstPredName = extractName(firstHint, PREDICATE_PREFIX, null);
						String fstSetName = extractName(firstHint, PREDICATE_SET_PREFIX, fstPredName.getKey()).getValue();
						singlePredicateSelectionHints.get(currSequent).put(fstSetName,fstPredName.getValue());
					} else {
						String fstSetName = extractPredicateSetName(firstHint).getValue();
						String sndSetName = extractPredicateSetName(secondHint).getValue();
						predicateSetSelectionHints.get(currSequent).add(new AbstractMap.SimpleEntry<>(fstSetName,sndSetName));
					}
				}
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		if (currSequent == null && "org.eventb.core.poPredicateSet".equals(qName)) {
			currHypSet = null;
		} else if ("org.eventb.core.poSequent".equals(qName)) {
			currSequent = null;
		}
	}

	private String getInternalName(String attributeValue) {
		String internalName = attributeValue;
		if (internalName.endsWith("\\\\")) {
			internalName = internalName.substring(0, internalName.length() - 1);
		} else {
			internalName = internalName.replace("\\", "");
		}
		return internalName;
	}

	private Map.Entry<Integer,String> extractPredicateSetName(String attributeValue) {
		return extractName(attributeValue, PREDICATE_SET_PREFIX, null);
	}

	private Map.Entry<Integer,String> extractName(String attributeValue, String namePrefix, Integer endIdx) {
		int idx = attributeValue.indexOf(namePrefix);
		if (idx == -1) {
			throw new RuntimeException("Could not extract name from attribute: " + attributeValue);
		}
		int start = idx + namePrefix.length();
		String extractedValue = (endIdx == null)
				? attributeValue.substring(start)
				: attributeValue.substring(start, endIdx);
		return new AbstractMap.SimpleEntry<>(idx, getInternalName(extractedValue));
	}

	public Map<String,ExtractedSequent> getExtractedSequents() {
		if (extractedSequents != null) {
			return extractedSequents;
		}

		poPredicateSets.keySet().forEach(poLabel -> hypotheses.put(poLabel, getHypotheses(poPredicateSets.get(poLabel))));

		// compute selected predicate sets from selection hints
		for (String poLabel : predicateSetSelectionHints.keySet()) {
			Set<Map.Entry<String,String>> poPredSets = predicateSetSelectionHints.get(poLabel);
			selectedPredicateSets.put(poLabel, new HashSet<>());
			for (Map.Entry<String,String> startEnd : poPredSets) {
				String start = startEnd.getKey();
				String end = startEnd.getValue();
				while (end != null && !end.equals(start)) {
					// iterate from second to first hint (second set is included, first excluded), cf. org.eventb.internal.core.pom.POLoader
					if ("SEQHYP".equals(end)) { // TODO: check if local predicateSet always has the name "SEQHYP"
						end = poPredicateSets.get(poLabel);
					}
					selectedPredicateSets.get(poLabel).add(end);
					end = predicateSetParentSets.get(end);
				}
			}
		}

		// collect selected predicates
		for (String poLabel : poPredicateSets.keySet()) {
			List<IEvalElement> selectedHypotheses = new ArrayList<>();
			for (String predSet : selectedPredicateSets.get(poLabel)) {
				if (predicateSetPredicates.get(predSet) == null) continue;
				selectedHypotheses.addAll(predicateSetPredicates.get(predSet).values());
			}
			singlePredicateSelectionHints.get(poLabel).forEach((predSet, predName) -> {
				IEvalElement pred = predicateSetPredicates.get(predSet).get(predName);
				selectedHypotheses.add(pred); // TODO: check if pred is in all hypotheses?
			});
			this.selectedHypotheses.put(poLabel,selectedHypotheses);
		}

		Map<String, ExtractedSequent> extractedSequents = new LinkedHashMap<>(); // keep order
		for (String poLabel : descriptions.keySet()) {
			extractedSequents.put(poLabel, new ExtractedSequent(descriptions.get(poLabel), hypotheses.get(poLabel),
					selectedHypotheses.get(poLabel), goals.get(poLabel)));
		}
		return this.extractedSequents = extractedSequents;
	}

	private List<IEvalElement> getHypotheses(String predicateSetName) {
		List<IEvalElement> hypotheses = new ArrayList<>();
		if (predicateSetPredicates.get(predicateSetName) == null) {
			return hypotheses;
		}
		hypotheses.addAll(getHypotheses(predicateSetParentSets.get(predicateSetName)));
		hypotheses.addAll(predicateSetPredicates.get(predicateSetName).values());
		return hypotheses;
	}

}
