package de.prob.model.eventb.translate;

import java.io.FileNotFoundException;

public class EventBFileNotFoundException extends FileNotFoundException {

    private String path;

    private boolean refreshProject;

    public EventBFileNotFoundException(String path, String additionalMsg, boolean refreshProject) {
        super(path + " (No such file)" + "\n" + additionalMsg);
        this.path = path;
        this.refreshProject = refreshProject;
    }

    public String getPath() {
        return path;
    }

    public boolean refreshProject() {
        return refreshProject;
    }

}
