
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server {
    // Creating replica of the data store
    private static int var=0;
    private static ServerSocket ss;


    public static void main(String args[]){

        try{
            // Setting up Primary
            if(args.length==1) {
                int primaryPort = Integer.parseInt(args[0]);
                ss = new ServerSocket(primaryPort);
                System.out.println("I am the primary!");
                System.out.println("Data Server is listening on port " + primaryPort);
                createPrimaryServer(primaryPort);
            }
            // Setting up backUp
            else if(args.length==2){
                int backupPort = Integer.parseInt(args[0]);
                int primaryPort = Integer.parseInt(args[1]);

                ss = new ServerSocket(backupPort);
                String message="JOIN:"+backupPort;
                System.out.println("I am a backup with port: "+backupPort);
                System.out.println("Primary Port: " + primaryPort);
                // Sending message to primary server
                transfer(primaryPort,message);
                System.out.println("Data Server is listening on port " + backupPort);
                createBackupServer(backupPort,primaryPort);
            }
            else {
                System.out.println("Enter valid arguments for port numbers");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void createBackupServer(int backupPort, int primaryPort){

        try {


            while(true){
                // a blocking call to wait for incoming client connections
                Socket s = ss.accept();
                InputStream input = s.getInputStream();
                // Creating an input stream object to read data sent from client
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                OutputStream output = s.getOutputStream();
                // Creating an output stream object to send data to client
                PrintWriter writer=new PrintWriter(output,true);
                // readLine will get the client response and store it in the String 'request'
                String request = reader.readLine();
                //System.out.println("READER: "+ request);
                String command=request.split(":")[0];

                // If READ request -> send current value of replica at backup
                if(command.equals("READ")){
                    writer.println("COMPLETE_"+command+":"+var);
                }
                // If WRITE or UPDATE request
                else if(command.equals("UPDATE") || command.equals("WRITE")) {
                    int temp = Integer.parseInt(request.split(":")[1]);
                    if (var != temp) {
                        var = temp;
                        String message="UPDATE:"+var;
                        writer.println("COMPLETE_"+command);
                        // Send an update to primary and wait for acknowledgement
                        transfer(primaryPort,message);
                    }
                    // Send acknowledgement to client
                    writer.println("COMPLETE_"+command);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void createPrimaryServer(int primaryPort){
        try{

            ArrayList<Integer> backupServers = new ArrayList<Integer>();

            while(true) {
                // a blocking call to wait for incoming client connections
                Socket s = ss.accept();
                InputStream input = s.getInputStream();
                // Creating an input stream object to read data sent from client
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                OutputStream output = s.getOutputStream();
                // Creating an output stream object to send data to client
                PrintWriter writer = new PrintWriter(output, true);
                // readLine will get the client response and store it in the String 'request'
                String request = reader.readLine();

                String command=request.split(":")[0];

                // If READ request from client -> return current value of replica
                if(command.equals("READ")){
                    writer.println("COMPLETE_"+command+":"+var);
                }
                // If WRITE or UPDATE request
                else if(command.equals("UPDATE") || command.equals("WRITE")){

                    int temp = Integer.parseInt(request.split(":")[1]);
                    if (var != temp) {
                        // Update its own replica to new value
                        var = temp;
                        // Sending update request to each backup server
                        for(int i=0; i < backupServers.size(); i++){
                            //System.out.println(backupServers.get(i));
                            String message="UPDATE:"+var;
                            writer.println("COMPLETE_"+command);
                            transfer(backupServers.get(i),message);
                        }
                    }
                    // Sending acknowledgement to client
                    writer.println("COMPLETE_"+command);
                }
                // If JOIN request from backup server -> record backup port and send acknowledgement
                else if(request.startsWith("JOIN")){
                    //System.out.println("IN JOIN");
                    backupServers.add(Integer.parseInt(request.split(":")[1]));
                    //writer.println("COMPLETE_"+command);
                    //System.out.println(var);
                    writer.println("JOIN:"+var);
                }
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void transfer(int port, String message) {
        Socket socket=null;
        try {
            socket = new Socket("localhost",port);
            InputStream input = socket.getInputStream();
            BufferedReader read = new BufferedReader(new
                    InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            PrintWriter write=new PrintWriter(output,true);
            //send message
            write.println(message);
            System.out.println("Just sent out: " + message + " to port: " + port);
            //get response
            String line = read.readLine();
            //System.out.println("RESPONSE for JOIN "+line);
            //System.out.println("Got response: " + line);
            if(line.contains("JOIN")){
                line = line.split(":")[1];
                var = Integer.parseInt(line);
                write.println(var);
            }

            //close the socket

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
