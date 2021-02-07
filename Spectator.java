import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

public class Spectator {
    Socket socket;
    DatagramSocket ds;
    byte[] buff = new byte[UDP.MAX_DATAGRAM_SIZE];
    DatagramPacket packet = new DatagramPacket(buff, buff.length);
    PrintWriter out;
    BufferedReader in;
    Spectator(String address, int port){
        try {
            socket = new Socket(address,port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            int counter = 0;
            while (counter++<6){
                in.readLine();
            }
            //ignorujemy startowa informacje i mowimy ze jestesmy widzem, chcemy port
            out.println("S");
            int portToOpen = Integer.valueOf(in.readLine());
            System.out.println("Opening port " + portToOpen);
            System.out.println("Starting to spectate...");
            ds = new DatagramSocket(portToOpen);
            socket.close();
            while (true){
                ds.receive(packet);
                String msg = new String(packet.getData());
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if(args.length!=2) {
            System.out.println("ERROR: You need 2 arguments to run this client!");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
        if(!args[1].matches("\\d{1,5}") || args[1].matches("-\\d+")) {
            System.out.println("ERROR: You need to write port as the second argument!");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
        else {
            if(Integer.valueOf(args[1])>65535 || Integer.valueOf(args[1])<1){
                System.out.println("ERROR: You're out of the ports bounds!");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        }
        new Spectator(args[0], Integer.valueOf(args[1]));
    }
}
