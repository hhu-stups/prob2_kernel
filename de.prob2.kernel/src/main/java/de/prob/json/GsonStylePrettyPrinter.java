package de.prob.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Instantiatable;

/**
 * Custom {@link PrettyPrinter} that formats the JSON output exactly like Gson
 * (which uses the de facto standard JSON formatting style
 * also used by most other JSON libraries and tools).
 * This avoids unnecessary formatting changes
 * when re-saving existing JSON files that were previously written by Gson.
 */
public final class GsonStylePrettyPrinter implements PrettyPrinter, Instantiatable<GsonStylePrettyPrinter> {
	private final DefaultPrettyPrinter.Indenter indenter;
	
	private int currentIndentLevel;
	
	public GsonStylePrettyPrinter() {
		this.indenter = new DefaultIndenter("  ", "\n");
		
		this.currentIndentLevel = 0;
	}
	
	public GsonStylePrettyPrinter(final GsonStylePrettyPrinter base) {
		this.indenter = base.indenter;
		
		this.currentIndentLevel = base.currentIndentLevel;
	}
	
	@Override
	public GsonStylePrettyPrinter createInstance() {
		return new GsonStylePrettyPrinter(this);
	}
	
	@Override
	public void writeRootValueSeparator(final JsonGenerator gen) throws IOException {
		// No root value separator,
		// i. e. multiple root values are directly concatenated.
	}
	
	@Override
	public void writeStartObject(final JsonGenerator g) throws IOException {
		g.writeRaw('{');
		this.currentIndentLevel++;
	}
	
	@Override
	public void beforeObjectEntries(final JsonGenerator g) throws IOException {
		this.indenter.writeIndentation(g, this.currentIndentLevel);
	}
	
	@Override
	public void writeObjectFieldValueSeparator(final JsonGenerator g) throws IOException {
		g.writeRaw(": ");
	}
	
	@Override
	public void writeObjectEntrySeparator(final JsonGenerator g) throws IOException {
		g.writeRaw(',');
		this.indenter.writeIndentation(g, this.currentIndentLevel);
	}
	
	@Override
	public void writeEndObject(final JsonGenerator g, final int nrOfEntries) throws IOException {
		this.currentIndentLevel--;
		if (nrOfEntries > 0) {
			this.indenter.writeIndentation(g, this.currentIndentLevel);
		}
		g.writeRaw('}');
	}
	
	@Override
	public void writeStartArray(final JsonGenerator g) throws IOException {
		g.writeRaw('[');
		this.currentIndentLevel++;
	}
	
	@Override
	public void beforeArrayValues(final JsonGenerator g) throws IOException {
		this.indenter.writeIndentation(g, this.currentIndentLevel);
	}
	
	@Override
	public void writeArrayValueSeparator(final JsonGenerator g) throws IOException {
		g.writeRaw(',');
		this.indenter.writeIndentation(g, this.currentIndentLevel);
	}
	
	@Override
	public void writeEndArray(final JsonGenerator g, final int nrOfValues) throws IOException {
		this.currentIndentLevel--;
		if (nrOfValues > 0) {
			this.indenter.writeIndentation(g, this.currentIndentLevel);
		}
		g.writeRaw(']');
	}
}
