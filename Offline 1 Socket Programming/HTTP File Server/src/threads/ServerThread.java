package threads;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class ServerThread implements Runnable {
    private Socket socket;
    private String root;
    private String log;
    private int server_port;
    private static int request_no = 0;
    private Thread thread;

    public ServerThread(Socket socket, String root, String log, int server_port) {
        this.socket = socket;
        this.root = root;
        this.log = log;
        this.server_port = server_port;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        /* instantiating bufferedReader */
        BufferedReader bufferedReader = null;
        String httpRequest = null;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch(IOException e) {
            e.printStackTrace();
        }

        // receiving request message from client
        try {
            httpRequest = bufferedReader.readLine();
        } catch(IOException e) {
            e.printStackTrace();
        }

        //checking whether GET or UPLOAD request)
        if(httpRequest==null || httpRequest.startsWith("GET")) {

            //if no http request line is sent, then terminate
            if(httpRequest == null) {
                try {
                    bufferedReader.close();
                    socket.close();
                } catch(IOException e) {
                    e.printStackTrace();
                } finally {
                    return ;
                }
            }

            PrintWriter printWriter=null, fileWriter=null;

            try {
                printWriter = new PrintWriter(socket.getOutputStream());
                fileWriter = new PrintWriter(log+"\\http_log_"+(++request_no)+".log");
            } catch(IOException e) {
                e.printStackTrace();
            }

            System.out.println(">> http request line from client: "+httpRequest);
            //assert fileWriter != null;
            fileWriter.println("HTTP REQUEST LINE FROM CLIENT:\n"+httpRequest+"\n");

            //valid http request,processing http response
            String path = "";
            String[] array = httpRequest.split("/");
            File fileContent;
            File[] listOfContent;

            for(int i=1; i<array.length-1; i++) {
                if(i == (array.length-2)) {
                    path += array[i].replace(" HTTP","");
                } else {
                    path += array[i]+"\\";
                }
            }

            // creating File object
            if(path.equals("")) {
                fileContent = new File(root);
            } else {
                //to allow file or directory name with space
                path = path.replace("%20", " ")+"\\";
                fileContent = new File(root+"\\"+path);
            }

            //http response body content
            StringBuilder stringBuilder = new StringBuilder();
            if(fileContent.exists()) {
                if(fileContent.isDirectory()) {
                    listOfContent = fileContent.listFiles();
                    stringBuilder.append("<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n<link rel=\"icon\" href=\"data:,\">\n</head>\n<body>\n");

                    for(int i=0; i<listOfContent.length; i++) {
                        /* NOTICE: hyperlink */
                        if(listOfContent[i].isDirectory()) {
                            stringBuilder.append("<font size=\"7\"><b><a href=\"http://localhost:"+server_port+"/"+path.replace("\\", "/")+listOfContent[i].getName()+"\"> "+listOfContent[i].getName()+" </a></b></font><br>\n");
                        }
                        if(listOfContent[i].isFile()) {
                            stringBuilder.append("<font size=\"6\"><a href=\"http://localhost:"+server_port+"/"+path.replace("\\", "/")+listOfContent[i].getName()+"\"> "+listOfContent[i].getName()+" </a></font><br>\n");
                        }
                    }
                    stringBuilder.append("</body>\n</html>");
                }
            }
            else {

                stringBuilder.append("<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n<link rel=\"icon\" href=\"data:,\">\n</head>\n<body>\n");
                stringBuilder.append("<h1> 404: Page not found </h1>\n");
                stringBuilder.append("</body>\n</html>");
            }

            // sending http response line, headers, and body
            fileWriter.println("HTTP RESPONSE TO CLIENT:");
            String httpResponse = "";

            if(httpRequest.length() > 0) {
                if(httpRequest.startsWith("GET")) {
                    if(fileContent.exists() && fileContent.isDirectory()) {
                        httpResponse += "HTTP/1.1 200 OK\r\nServer: Java HTTP Server: 1.0\r\nDate: "+new Date()+"\r\nContent-Type: text/html\r\nContent-Length: "+stringBuilder.toString().length()+"\r\n";
                        //log file will contain request line, response line and response header; not response body
                        fileWriter.println(httpResponse);

                        printWriter.write(httpResponse);
                        printWriter.write("\r\n");
                        printWriter.write(stringBuilder.toString());
                        printWriter.flush();
                    }
                    if(fileContent.exists() && fileContent.isFile()) {
                        //application/force-download
                        httpResponse += "HTTP/1.1 200 OK\r\nServer: Java HTTP Server: 1.0\r\nDate: "+new Date()+"\r\nContent-Type: application/force-download\r\nContent-Length: "+fileContent.length()+"\r\n";
                        fileWriter.println(httpResponse);

                        printWriter.write(httpResponse);
                        printWriter.write("\r\n");
                        printWriter.flush();

                        //sending file using socket
                        int count;
                        byte[] buffer = new byte[1024];

                        try {
                            OutputStream out = socket.getOutputStream();
                            BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileContent));

                            while((count=in.read(buffer)) > 0) {
                                out.write(buffer, 0, count);
                                out.flush();
                            }

                            in.close();
                            out.close();
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(!fileContent.exists()) {
                        httpResponse += "HTTP/1.1 404 NOT FOUND\r\nServer: Java HTTP Server: 1.0\r\nDate: "+new Date()+"\r\nContent-Type: text/html\r\nContent-Length: "+stringBuilder.toString().length()+"\r\n";
                        fileWriter.println(httpResponse);

                        printWriter.write(httpResponse);
                        printWriter.write("\r\n");
                        printWriter.write(stringBuilder.toString());
                        printWriter.flush();

                        System.out.println(">> 404: Page not found");
                    }
                }
            }

            // closing process
            try {
                bufferedReader.close();
                printWriter.close();
                fileWriter.close();
                socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        if(httpRequest.startsWith("UPLOAD")) {

            try {
                String isValid = bufferedReader.readLine();

                if(isValid.equals("invalid")) {
                    System.out.println(">> given file name is invalid");

                    bufferedReader.close();
                    socket.close();
                    return ;
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }

            //valid
            int count;
            byte[] buffer = new byte[1024];

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(new File(root+"\\"+httpRequest.substring(7)));
                InputStream in = socket.getInputStream();

                while((count=in.read(buffer)) > 0){
                    fileOutputStream.write(buffer);
                }

                in.close();
                fileOutputStream.close();
            } catch(IOException e) {
                e.printStackTrace();
            }

            // closing process
            try {
                bufferedReader.close();
                socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        return ;
    }
}
