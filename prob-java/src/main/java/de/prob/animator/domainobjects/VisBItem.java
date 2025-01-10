package de.prob.animator.domainobjects;

import java.util.Locale;
import java.util.Objects;

import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.PrologTerm;

/**
 * The VisBItem is designed for the JSON / VisB file
 */
public final class VisBItem {

	public static final class VisBItemKey {

		private final String id;
		private final String attribute;

		public VisBItemKey(final String id, final String attribute) {
			this.id = id;
			this.attribute = attribute;
		}

		public String getId() {
			return id;
		}

		public String getAttribute() {
			return attribute;
		}

		@Override
		public String toString() {
			return String.format(Locale.ROOT, "VisBItemKey{%s, %s}", id, attribute);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			VisBItemKey that = (VisBItemKey) o;
			return Objects.equals(id, that.id) && Objects.equals(attribute, that.attribute);
		}

		@Override
		public int hashCode() {
			return Objects.hash(id, attribute);
		}
	}

	private final VisBItemKey key;
	private final String id;
	private final String attribute;
	private final String expression; // B Formula to compute value of attribute for SVG object id
	private final String description;
	private final String source;

	/**
	 *
	 * @param id this has to be the id used in the svg file to correspond with that svg element
	 * @param attribute this has to be an actual svg attribute
	 * @param expression this formula has to provide a valid B expression
	 * @param description this String is the description of the VisB item
	 * @param source this String is the source of the VisB item
	 */
	public VisBItem(String id, String attribute, String expression, String description, String source) {
		this.key = new VisBItemKey(id, attribute);
		this.id = id;
		this.attribute = attribute.toLowerCase();
		this.expression = expression;
		this.description = description;
		this.source = source;
	}

	public VisBItemKey getKey() {
		return key;
	}

	public String getId() {
		return id;
	}

	public String getAttribute() {
		return attribute;
	}

	public String getExpression() {
		return expression;
	}

	public String getDescription() {
		return description;
	}

	public String getSource() {
		return source;
	}

	public static VisBItem fromPrologTerm(final PrologTerm term) {
		BindingGenerator.getCompoundTerm(term, "visb_item", 5);
		final String id = term.getArgument(1).atomicToString();
		final String attribute = term.getArgument(2).atomToString();
		final String expression = term.getArgument(3).atomToString();
		final String description = term.getArgument(4).atomToString();
		// TODO: Implement VisB Source
		final String source = term.getArgument(5).toString();
		return new VisBItem(id, attribute, expression, description, source);
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "VisBItem{%s, %s, %s, %s, %s}", id, attribute, expression, description, source);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VisBItem visBItem = (VisBItem) o;
		return Objects.equals(id, visBItem.id) && Objects.equals(attribute, visBItem.attribute) && Objects.equals(expression, visBItem.expression) && Objects.equals(description, visBItem.description) && Objects.equals(source, visBItem.source);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, attribute, expression, description, source);
	}
}
