package de.prob;

import com.google.inject.Singleton;

@Singleton
public class CliConfiguration {

    public static String serverName = "localhost";
    public static int serverPort = 11312;

    public static void setConfiguration(String sName, int sPort) {
        serverName = sName;
        serverPort = sPort;
    }

}
