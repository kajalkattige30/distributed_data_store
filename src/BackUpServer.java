import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class BackUpServer {
    public  static  void main(String args[]){
        int port = Integer.parseInt(args[0]);
        BackUpServer bs= new BackUpServer();

        bs.openBackUpConnection(port);


    }

    public void openBackUpConnection(int port){
        try {
            ServerSocket ss = new ServerSocket(port);
            while(true){
                Socket s = ss.accept();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
