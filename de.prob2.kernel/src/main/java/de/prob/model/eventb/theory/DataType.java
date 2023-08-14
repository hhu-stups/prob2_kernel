package de.prob.model.eventb.theory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.prob.model.representation.AbstractElement;

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
