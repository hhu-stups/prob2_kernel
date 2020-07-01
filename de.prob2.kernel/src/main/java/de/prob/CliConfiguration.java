package de.prob;

import com.google.inject.Singleton;

@Singleton
public class CliConfiguration {

    private String serverName = "localhost";
    private int serverPort = 11312;

    public void setConfiguration(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public String getServerName() {
        return serverName;
    }

    public int getServerPort() {
        return serverPort;
    }
}
