package de.prob.model.eventb.translate;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
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
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.PrologTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ProofExtractor {
	private static final Logger logger = LoggerFactory.getLogger(ProofExtractor.class);

	private Path proofObligationsFile;
	private Path proofStatusFile;

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

	public Path getProofObligationsFile() {
		return proofObligationsFile;
	}

	public Path getProofStatusFile() {
		return proofStatusFile;
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
				proofObligationsFile = bpoFile.toPath();
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
				proofObligationsFile = null;
			}

			String bpsFileName = baseFileName + ".bps";
			File bpsFile = new File(bpsFileName);
			proofConfidences = new HashMap<>();
			if (bpsFile.exists()) {
				proofStatusFile = bpsFile.toPath();
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
				proofStatusFile = null;
			}
		} catch (ParserConfigurationException e) {
			throw new SAXException(e);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static CompoundPrologTerm makeSource(final String type, final String label) {
		return new CompoundPrologTerm(type, new CompoundPrologTerm(label));
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

			List<PrologTerm> sourceInfos = new ArrayList<>();
			if ("THM".equals(type) || "WD".equals(type)) {
				sourceInfos.add(makeSource("axiom", split[0]));
			}
			proofs.add(new ProofObligation(source, name, confidence, desc, sourceInfos));
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

			List<PrologTerm> sourceInfos = new ArrayList<>();
			if ("GRD".equals(type)) {
				Event concreteEvent = m.getEvent(split[0]);
				final Event event = concreteEvent.getParentEvent();
				if (event != null && event.getGuards().getElement(split[1]) != null) {
					EventBGuard guard = event.getGuards().getElement(split[1]);
					sourceInfos.add(makeSource("event", event.getName()));
					sourceInfos.add(makeSource("guard", guard.getName()));
				}
				sourceInfos.add(makeSource("event", concreteEvent.getName()));
			} else if ("INV".equals(type)) {
				sourceInfos.add(makeSource("event", "invariant"));
			} else if ("THM".equals(type)) {
				if (split.length == 2) {
					sourceInfos.add(makeSource("invariant", split[0]));
				} else {
					sourceInfos.add(makeSource("guard", split[1]));
					sourceInfos.add(makeSource("event", split[0]));
				}
			} else if ("WD".equals(type)) {
				if (split.length == 2) {
					sourceInfos.add(makeSource("invariant", split[0]));
				} else {
					Event event = m.getEvent(split[0]);
					if (event.getActions().getElement(split[1]) != null) {
						sourceInfos.add(makeSource("event", event.getName()));
						sourceInfos.add(makeSource("action", split[1]));
					} else {
						sourceInfos.add(makeSource("event", event.getName()));
						sourceInfos.add(makeSource("guard", split[1]));
					}
				}
			}
			proofs.add(new ProofObligation(source, name, confidence, desc, sourceInfos));
		}

	}

	public ModelElementList<ProofObligation> getProofs() {
		return new ModelElementList<>(proofs);
	}
}
