import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class DataServer {
    private static int var=0;


    public static void main(String args[]){

            try{
                   if(args.length==1) {
                       int port = Integer.parseInt(args[0]);
                       openPrimaryConnection(port);
                   }
                   else{
                       int port = Integer.parseInt(args[0]);
                       int port1 = Integer.parseInt(args[1]);
                       openBackUpConnection(port,port1);

                   }
            }
                   catch (Exception e) {
                    e.printStackTrace();
                }


    }

    public static void openBackUpConnection(int myPort, int primaryPort){
        Socket socket=null;

        try {
            ServerSocket ss = new ServerSocket(myPort);
            socket = new Socket("localhost",primaryPort);

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            PrintWriter writer=new PrintWriter(output,true);

            //send message
            String message="JOIN:"+myPort;
            writer.println(message);

            System.out.println("Just send out: " + message + " to port: " + primaryPort);

            //get response
            String line = reader.readLine();

            System.out.println("Got response: " + line);
            System.out.println("Data Server is listening on port "+myPort);
            socket.close();
            while(true){
                Socket s = ss.accept();
                System.out.println("connected!");
                InputStream in = s.getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                OutputStream out = s.getOutputStream();
                PrintWriter w = new PrintWriter(out, true);

                //String req =
                String req = r.readLine();

                System.out.println(req);

                //writer.println("ACK");

//                    String msg = reader.readLine();
//                    System.out.println(msg);
                if(req.contains("READ")){
                    System.out.println("Current Data value: "+ var);
                    w.println(var);
                }
                else if(req.startsWith("UPDATE") || req.startsWith("WRITE")) {
                    int temp = Integer.parseInt(req.split(":")[1]);
                    if (var != temp) {

                        // Updating its own replica
                        var = temp;
                        // Request each backup server to update its replica and get acknowledgement
                        System.out.println("Current Data value: " + var);
                        // Reply to the requesting client

                        Socket soc = new Socket("localhost",primaryPort);
                        OutputStream out1 = soc.getOutputStream();
                        PrintWriter write=new PrintWriter(out1,true);
                        write.println("UPDATE:"+var);
                        soc.close();
                    }
                    w.println("ACK");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void openPrimaryConnection(int primaryPort){
        try{
            //System.out.println(args.length);
            ServerSocket ss = new ServerSocket(primaryPort);

            ArrayList<Integer> backupServers = new ArrayList<Integer>();

            while(true) {
                Socket s = ss.accept();
                System.out.println("Client connected!");
                InputStream input = s.getInputStream();
                BufferedReader readerClient = new BufferedReader(new InputStreamReader(input));
                //BufferedReader readerBackup = new BufferedReader(new InputStreamReader(input));
                OutputStream output = s.getOutputStream();
                PrintWriter writerClient = new PrintWriter(output, true);

                String req = readerClient.readLine();

                System.out.println(req);

                //writer.println("ACK");

//                    String msg = reader.readLine();
//                    System.out.println(msg);
                if(req.contains("READ")){
                    System.out.println("Current Data value: "+ var);
                    writerClient.println(var);
                }
                else if(req.startsWith("UPDATE") || req.startsWith("WRITE")){

                    int temp = Integer.parseInt(req.split(":")[1]);
                    if (var != temp) {
                        var = temp;
                        System.out.println("Current Data value: " + var);
                        System.out.println(backupServers.get(0));
                        System.out.println(backupServers.size());
                        for(int i=0; i < backupServers.size(); i++){
                            System.out.println(backupServers.get(i));
                            Socket soc = new Socket("localhost", backupServers.get(i));
                            OutputStream out1 = soc.getOutputStream();
                            PrintWriter write=new PrintWriter(out1,true);
                            write.println("UPDATE:"+var);
                            soc.close();
                        }
                    }
                    writerClient.println("ACK");

                }
                else if(req.startsWith("JOIN")){

                    backupServers.add(Integer.parseInt(req.split(":")[1]));
                    writerClient.println("ACK");
                }
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
