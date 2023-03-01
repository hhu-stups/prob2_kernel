package de.prob.model.eventb.theory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.prob.model.representation.AbstractElement;
import de.prob.util.Tuple2;

import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.GivenType;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.datatype.IConstructorBuilder;
import org.eventb.core.ast.datatype.IDatatypeBuilder;
import org.eventb.core.ast.extension.IFormulaExtension;

public class DataType extends AbstractElement {

	final String identifierString;
	private final Map<String, DataTypeConstructor> constructorsByName;
	private final List<String> typeArguments;

	public DataType(final String identifierString, final List<DataTypeConstructor> constructors, final List<String> typeArguments) {
		this.identifierString = identifierString;
		this.constructorsByName = constructors.stream().collect(Collectors.toMap(DataTypeConstructor::getName, c -> c));
		this.typeArguments = typeArguments;
	}

	/**
	 * @deprecated Use {@link #DataType(String, List, List)} instead.
	 */
	@Deprecated
	public DataType(final String identifier,
			Map<String, List<Tuple2<String, String>>> constructors,
			List<String> types) {
		this(identifier, constructorsFromMap(constructors), types);
	}

	@Deprecated
	private static List<DataTypeConstructor> constructorsFromMap(final Map<String, List<Tuple2<String, String>>> constructors) {
		final List<DataTypeConstructor> convertedConstructors = new ArrayList<>();
		constructors.forEach((name, args) -> {
			final List<DataTypeDestructor> convertedArgs = args.stream()
				.map(tuple -> new DataTypeDestructor(tuple.getFirst(), tuple.getSecond()))
				.collect(Collectors.toList());
			convertedConstructors.add(new DataTypeConstructor(name, convertedArgs));
		});
		return convertedConstructors;
	}

	public String getTypeIdentifier() {
		return identifierString;
	}

	@Override
	public String toString() {
		return identifierString;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof DataType) {
			return identifierString.equals(obj.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return identifierString.hashCode();
	}

	/**
	 * @deprecated Use {@link #getConstructorsByName()} instead.
	 */
	@Deprecated
	public Map<String, List<Tuple2<String, String>>> getConstructors() {
		final Map<String, List<Tuple2<String, String>>> constructors = new HashMap<>();
		for (final DataTypeConstructor constructor : this.getConstructorsByName().values()) {
			final List<Tuple2<String, String>> args = constructor.getArguments().stream()
				.map(arg -> new Tuple2<>(arg.getName(), arg.getType()))
				.collect(Collectors.toList());
			constructors.put(constructor.getName(), args);
		}
		return constructors;
	}

	public Map<String, DataTypeConstructor> getConstructorsByName() {
		return Collections.unmodifiableMap(this.constructorsByName);
	}

	public Set<IFormulaExtension> getFormulaExtensions(final FormulaFactory ff) {
		List<GivenType> types = new ArrayList<>();
		for (String type : typeArguments) {
			IParseResult parseType = ff.parseType(type);
			if (parseType.getParsedType() instanceof GivenType) {
				types.add((GivenType) parseType.getParsedType());
			}
		}
		if (types.size() != typeArguments.size()) {
			throw new IllegalStateException("Types size (" + types.size() + ") should match Argument numbers (" + typeArguments.size() + ")");
		}
		IDatatypeBuilder builder = ff.makeDatatypeBuilder(identifierString,
				types.toArray(new GivenType[typeArguments.size()]));
		for (final DataTypeConstructor constructor : this.getConstructorsByName().values()) {
			IConstructorBuilder consBuilder = builder.addConstructor(constructor.getName());
			for (final DataTypeDestructor destructor : constructor.getArguments()) {
				IParseResult parseType = ff.parseType(destructor.getType());
				consBuilder.addArgument(destructor.getName(),
						parseType.getParsedType());
			}
		}

		return builder.finalizeDatatype().getExtensions();
	}

	public List<String> getTypeArguments() {
		return typeArguments;
	}
}
