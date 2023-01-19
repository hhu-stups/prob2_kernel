package de.prob.model.eventb.translate;

import java.util.LinkedHashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class ProofDescriptionExtractor extends DefaultHandler {
	// Use LinkedHashMap to preserve the order of proof descriptors from the Rodin project.
	private final Map<String, String> proofDescriptions = new LinkedHashMap<>();

	@Override
	public void startElement(final String uri, final String localName,
			final String qName, final Attributes attributes) {
		if ("org.eventb.core.poSequent".equals(qName)) {
			String name = attributes.getValue("name");
			String desc = attributes.getValue("org.eventb.core.poDesc");
			proofDescriptions.put(name, desc);
		}
	}

	public Map<String, String> getProofDescriptions() {
		return proofDescriptions;
	}

}
