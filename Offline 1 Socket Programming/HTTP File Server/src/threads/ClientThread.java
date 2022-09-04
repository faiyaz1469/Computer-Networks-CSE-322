package threads;

import java.io.*;
import java.net.Socket;

public class ClientThread implements Runnable {
    private Socket socket;
    private File inputFile;
    private Thread thread;

    public ClientThread(String filename) {
        inputFile = new File(filename);
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        // connecting to web server (server is listening on port 8082)
        try {
            socket = new Socket("localhost", 8082);
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        PrintWriter printWriter = null;
        //sending file upload request message to web server
        try {
            printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.write("UPLOAD " + inputFile.getName()+"\r\n");
            printWriter.flush();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        //checking if given filename is valid or not
        try {
            if(!inputFile.exists()) {
                printWriter.write("invalid\r\n");
                printWriter.flush();
                System.out.println(">> given file name is invalid");

                printWriter.close();
                socket.close();
                return ;
            }
            else {
                printWriter.write("valid\r\n");
                printWriter.flush();
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        // uploading file to root directory of web server
        int count;
        byte[] buffer = new byte[1024];

        try {
            OutputStream out = socket.getOutputStream();
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(inputFile));

            while((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
                out.flush();
            }

            in.close();
            out.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

        // closing process
        try {
            printWriter.close();
            socket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return ;
    }
}
