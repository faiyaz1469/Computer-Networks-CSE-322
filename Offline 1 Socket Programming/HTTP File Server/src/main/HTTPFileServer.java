package main;

import threads.ServerThread;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class HTTPFileServer {
    private static final String PATH_TO_ROOT = "G:\\Chrome Downloads\\Computer Networks\\root";
    private static final String PATH_TO_LOG = "G:\\Chrome Downloads\\Computer Networks\\log directory";
    private static final int SERVER_PORT = 8082;

    public static void main(String[] args) throws IOException {
        File logDirectory = new File(PATH_TO_LOG);

        if(logDirectory.exists()) {
            String[] entries = logDirectory.list();

            assert entries != null;
            for(String entry: entries){
                File toBeDeleted = new File(logDirectory.getPath(), entry);
                toBeDeleted.delete();
            }
            logDirectory.delete();
        }
        logDirectory.mkdir();

        // starting listening on port 8082
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println(">> http file server waiting for connection on port no: "+SERVER_PORT+"\n");

        // starting accepting http requests
        while(true) {
            new ServerThread(serverSocket.accept(), PATH_TO_ROOT, PATH_TO_LOG, SERVER_PORT);
        }
    }
}
