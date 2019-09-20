package de.prob.scripting;

import java.io.File;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.hhu.stups.alloy2b.translation.Alloy2BParser;
import de.hhu.stups.alloy2b.translation.Alloy2BParserErr;
import de.hhu.stups.alloy2b.translation.ParserResult;
import de.prob.exception.ProBError;
import de.prob.model.representation.AlloyModel;

public class AlloyFactory implements ModelFactory<AlloyModel> {
	private final Provider<AlloyModel> modelCreator;
	
	@Inject
	public AlloyFactory(final Provider<AlloyModel> modelCreator) {
		this.modelCreator = modelCreator;
	}

	@Override
	public ExtractedModel<AlloyModel> extract(final String modelPath) throws IOException, ModelTranslationError {
		final File f = new File(modelPath);
		final AlloyModel alloyModel;
		try {
			final ParserResult parserResult = new Alloy2BParser().parseFromFile(f.getAbsolutePath());
			alloyModel = modelCreator.get().create(f, parserResult.getPrologTerm());
			return new ExtractedModel<>(alloyModel, null);
		} catch (final Alloy2BParserErr e) {
			throw new ProBError(e);
		}
	}
}
