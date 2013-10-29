package de.prob.web.views;

import java.util.Map;
import java.util.UUID;

import javax.servlet.AsyncContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.be4.classicalb.core.parser.exceptions.BException;
import de.prob.model.representation.AbstractModel;
import de.prob.statespace.AnimationSelector;
import de.prob.statespace.IAnimationChangeListener;
import de.prob.statespace.Trace;
import de.prob.visualization.AnimationNotLoadedException;
import de.prob.web.AbstractSession;
import de.prob.web.WebUtils;

public class BMotionStudioSession extends AbstractSession implements
		IAnimationChangeListener {

	Logger logger = LoggerFactory.getLogger(BMotionStudioSession.class);

	private final Trace currentTrace;

	private AbstractModel model;

	private final AnimationSelector selector;

	private String templatePath;

	@Inject
	public BMotionStudioSession(final AnimationSelector selector) {
		this.selector = selector;
		currentTrace = selector.getCurrentTrace();
		if (currentTrace == null) {
			throw new AnimationNotLoadedException(
					"Please load model before opening a BMotion Studio visualization and than reload page.");
		} else {
			model = currentTrace.getModel();
			selector.registerAnimationChangeListener(this);
		}
	}

	@Override
	public String html(final String clientid,
			final Map<String, String[]> parameterMap) {
		Object scope = WebUtils.wrap("clientid", clientid, "id", UUID
				.randomUUID().toString(), "workspace", model.getModelFile()
				.getParent() + "/");
		return WebUtils.render("/ui/bmsview/index.html", scope);
	}

	public Object forcerendering(final Map<String, String[]> params) {
		return WebUtils.wrap("cmd", "bms.renderVisualization");
	}

	public Object executeOperation(final Map<String, String[]> params) {
		String op = params.get("op")[0];
		String predicate = "1=1";
		if (params.get("predicate") != null) {
			predicate = params.get("predicate")[0];
		}
		Trace currentTrace = selector.getCurrentTrace();
		try {
			if (predicate == null) {
				predicate = "1=1";
			}
			Trace newTrace = currentTrace.add(op, predicate);
			selector.replaceTrace(currentTrace, newTrace);
		} catch (BException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Object setTemplate(final Map<String, String[]> params) {
		templatePath = params.get("path")[0];
		return WebUtils.wrap("cmd", "bms.setTemplate", "templatefile",
				templatePath);
	}

	@Override
	public void reload(final String client, final int lastinfo,
			final AsyncContext context) {
		super.reload(client, lastinfo, context);
		submit(WebUtils.wrap("cmd", "bms.setTemplate", "templatefile",
				templatePath));
	}

	@Override
	public void traceChange(final Trace trace) {
		submit(WebUtils.wrap("cmd", "bms.renderVisualization"));
	}

}
