package de.prob.scripting;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.hhu.stups.alloy2b.translation.Alloy2BParser;
import de.hhu.stups.alloy2b.translation.Alloy2BParserErr;
import de.hhu.stups.alloy2b.translation.ParserResult;
import de.prob.animator.domainobjects.ErrorItem;
import de.prob.exception.ProBError;
import de.prob.model.representation.AlloyModel;
import de.prob.statespace.StateSpace;

public class AlloyFactory implements ModelFactory<AlloyModel> {
	private final Provider<StateSpace> stateSpaceProvider;
	private final Provider<AlloyModel> modelCreator;
	
	@Inject
	AlloyFactory(Provider<StateSpace> stateSpaceProvider, Provider<AlloyModel> modelCreator) {
		this.stateSpaceProvider = stateSpaceProvider;
		this.modelCreator = modelCreator;
	}

	private static List<ErrorItem> convertAlloyExceptionToErrorItems(Alloy2BParserErr e) {
		return Collections.singletonList(
			new ErrorItem(e.getMessage(), ErrorItem.Type.ERROR, Collections.singletonList(
				new ErrorItem.Location(e.getFilename(), e.getRowStart(), e.getColStart(),
						e.getRowEnd(), e.getColEnd())
			))
		);
	}

	@Override
	public ExtractedModel<AlloyModel> extract(final String modelPath) throws IOException {
		final File f = new File(modelPath);
		final AlloyModel alloyModel;
		try {
			final ParserResult parserResult = new Alloy2BParser().parseFromFile(f.getAbsolutePath());
			alloyModel = modelCreator.get().create(f, parserResult.getPrologTerm());
			return new ExtractedModel<>(stateSpaceProvider, alloyModel);
		} catch (final Alloy2BParserErr e) {
			throw new ProBError(null, convertAlloyExceptionToErrorItems(e), e);
		}
	}
}
