package de.prob.model.eventb.translate;

import java.io.FileNotFoundException;

public class EventBFileNotFoundException extends FileNotFoundException {
	private static final long serialVersionUID = 1L;

	private String path;

	private boolean refreshProject;

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
