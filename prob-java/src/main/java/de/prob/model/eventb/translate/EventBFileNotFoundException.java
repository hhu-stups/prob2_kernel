package de.prob.model.eventb.translate;

import java.io.FileNotFoundException;

public final class EventBFileNotFoundException extends FileNotFoundException {
	private static final long serialVersionUID = 1L;

	private final String path;

	private final boolean refreshProject;

	public EventBFileNotFoundException(String path, String additionalMsg, boolean refreshProject, Throwable cause) {
		super(path + " (No such file)" + "\n" + additionalMsg);
		this.path = path;
		this.refreshProject = refreshProject;
		this.initCause(cause);
	}

	public String getPath() {
		return path;
	}

	public boolean refreshProject() {
		return refreshProject;
	}

}
