package de.prob.model.eventb.translate;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.prob.model.eventb.Context;
import de.prob.model.eventb.Event;
import de.prob.model.eventb.EventBGuard;
import de.prob.model.eventb.EventBMachine;
import de.prob.model.eventb.ProofObligation;
import de.prob.model.representation.ModelElementList;
import de.prob.util.Tuple2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ProofExtractor {
	private static final Logger logger = LoggerFactory.getLogger(ProofExtractor.class);

	private Map<String, String> descriptions;
	private Map<String, Integer> proofConfidences;

	private final List<ProofObligation> proofs = new ArrayList<>();

	public ProofExtractor(final Context c, final String baseFileName)
			throws SAXException {
		extractProofs(baseFileName);
		addProofs(c);
	}

	public ProofExtractor(final EventBMachine m, final String baseFileName)
			throws SAXException {
		extractProofs(baseFileName);
		addProofs(m);
	}

	private void extractProofs(final String baseFileName) throws SAXException {
		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			SAXParser saxParser = parserFactory.newSAXParser();

			String bpoFileName = baseFileName + ".bpo";
			File bpoFile = new File(bpoFileName);
			// Use LinkedHashMap to preserve the order of proof descriptions from the Rodin project.
			descriptions = new LinkedHashMap<>();
			if (bpoFile.exists()) {
				saxParser.parse(bpoFile, new DefaultHandler() {
					@Override
					public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
						if ("org.eventb.core.poSequent".equals(qName)) {
							String name = attributes.getValue("name");
							String desc = attributes.getValue("org.eventb.core.poDesc");
							descriptions.put(name, desc);
						}
					}
				});
			} else {
				logger.info("Could not find file {}. Assuming that no proofs have been generated for model element.", bpoFileName);
			}

			String bpsFileName = baseFileName + ".bps";
			File bpsFile = new File(bpsFileName);
			proofConfidences = new HashMap<>();
			if (bpsFile.exists()) {
				saxParser.parse(bpsFile, new DefaultHandler() {
					@Override
					public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
						if ("org.eventb.core.psStatus".equals(qName)) {
							String name = attributes.getValue("name");
							proofConfidences.put(name, Integer.parseInt(attributes.getValue("org.eventb.core.confidence")));
						}
					}
				});
			} else {
				logger.info("Could not find file {}. Assuming that no proofs are discharged for model element.", bpsFileName);
			}
		} catch (ParserConfigurationException e) {
			throw new SAXException(e);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void addProofs(final Context c) {
		for (Map.Entry<String, String> entry : descriptions.entrySet()) {
			String name = entry.getKey();
			String desc = entry.getValue();
			final int confidence = proofConfidences.getOrDefault(name, 0);

			String[] split = name.split("/");
			String type;
			if (split.length == 1) {
				type = split[0];
			} else if (split.length == 2) {
				type = split[1];
			} else {
				type = split[2];
			}
			String source = c.getName();

			List<Tuple2<String, String>> elements = new ArrayList<>();
			if ("THM".equals(type) || "WD".equals(type)) {
				elements.add(new Tuple2<>("axiom", split[0]));
			}
			proofs.add(new ProofObligation(source, name, confidence, desc, elements));
		}
	}

	private void addProofs(final EventBMachine m) {
		for (Map.Entry<String, String> entry : descriptions.entrySet()) {
			String name = entry.getKey();
			String desc = entry.getValue();
			final int confidence = proofConfidences.getOrDefault(name, 0);

			String[] split = name.split("/");
			String type;
			if (split.length == 1) {
				type = split[0];
			} else if (split.length == 2) {
				type = split[1];
			} else {
				type = split[2];
			}
			String source = m.getName();

			List<Tuple2<String, String>> elements = new ArrayList<>();
			if ("GRD".equals(type)) {
				Event concreteEvent = m.getEvent(split[0]);
				final Event event = concreteEvent.getParentEvent();
				if (event != null && event.getGuards().getElement(split[1]) != null) {
					EventBGuard guard = event.getGuards().getElement(split[1]);
					elements.add(new Tuple2<>("event", event.getName()));
					elements.add(new Tuple2<>("guard", guard.getName()));
				}
				elements.add(new Tuple2<>("event", concreteEvent.getName()));
				proofs.add(new ProofObligation(source, name, confidence, desc, elements));
			} else if ("INV".equals(type)) {
				elements.add(new Tuple2<>("event", "invariant"));
				proofs.add(new ProofObligation(source, name, confidence, desc, elements));
			} else if ("THM".equals(type)) {
				if (split.length == 2) {
					elements.add(new Tuple2<>("invariant", split[0]));
				} else {
					elements.add(new Tuple2<>("guard", split[1]));
					elements.add(new Tuple2<>("event", split[0]));
				}
				proofs.add(new ProofObligation(source, name, confidence, desc, elements));
			} else if ("WD".equals(type)) {
				if (split.length == 2) {
					elements.add(new Tuple2<>("invariant", split[0]));
				} else {
					Event event = m.getEvent(split[0]);
					if (event.getActions().getElement(split[1]) != null) {
						elements.add(new Tuple2<>("event", event.getName()));
						elements.add(new Tuple2<>("action", split[1]));
					} else {
						elements.add(new Tuple2<>("event", event.getName()));
						elements.add(new Tuple2<>("guard", split[1]));
					}
					proofs.add(new ProofObligation(source, name, confidence, desc, elements));
				}
			} else {
				proofs.add(new ProofObligation(source, name, confidence, desc, elements));
			}
		}

	}

	public ModelElementList<ProofObligation> getProofs() {
		return new ModelElementList<>(proofs);
	}
}
