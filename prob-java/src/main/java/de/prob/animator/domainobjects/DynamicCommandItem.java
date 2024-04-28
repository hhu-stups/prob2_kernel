package de.prob.animator.domainobjects;

import java.util.List;
import java.util.Objects;

import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;
import de.prob.statespace.Trace;

public class DynamicCommandItem {

	private final Trace trace;
	private final String command;
	private final String name;
	private final String description;
	private final int arity;
	private final List<String> relevantPreferences;
	private final List<PrologTerm> additionalInfo;
	private final String available;
	
	DynamicCommandItem(
			Trace trace,
			String command,
			String name,
			String description,
			int arity,
			List<String> relevantPreferences,
			List<PrologTerm> additionalInfo,
			String available
	) {
		this.trace = trace;
		this.command = command;
		this.name = name;
		this.description = description;
		this.arity = arity;
		this.relevantPreferences = relevantPreferences;
		this.additionalInfo = additionalInfo;
		this.available = available;
	}

	public static DynamicCommandItem fromPrologTerm(final Trace trace, final PrologTerm term) {
		BindingGenerator.getCompoundTerm(term, "command", 7);
		final String command = term.getArgument(1).atomToString();
		final String name = term.getArgument(2).atomToString();
		final String description = term.getArgument(3).atomToString();
		final int arity = BindingGenerator.getAInteger(term.getArgument(4)).intValueExact();
		final List<String> relevantPreferences = PrologTerm.atomsToStrings(BindingGenerator.getList(term.getArgument(5)));
		final List<PrologTerm> additionalInfo = BindingGenerator.getList(term.getArgument(6));
		final String available = term.getArgument(7).atomToString();
		
		return new DynamicCommandItem(
				trace,
				command,
				name,
				description,
				arity,
				relevantPreferences,
				additionalInfo,
				available
		);
	}
	
	public Trace getTrace() {
		return this.trace;
	}
	
	public String getCommand() {
		return command;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getArity() {
		return arity;
	}
	
	public List<String> getRelevantPreferences() {
		return relevantPreferences;
	}
	
	public List<PrologTerm> getAdditionalInfo() {
		return additionalInfo;
	}
	
	public String getAvailable() {
		return available;
	}
	
	public boolean isAvailable() {
		return "available".equals(available);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final DynamicCommandItem other = (DynamicCommandItem)obj;
		return Objects.equals(this.getCommand(), other.getCommand()) && this.getArity() == other.getArity();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getCommand(), this.getArity());
	}
	
	@Override
	public String toString() {
		return name;
	}
}
