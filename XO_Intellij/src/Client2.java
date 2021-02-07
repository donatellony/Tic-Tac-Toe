import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;


public class Client2 implements Runnable{
    private volatile boolean running = true;
    Socket socket = null;
    PrintWriter out = null;
    BufferedReader in;
    Client2(String address, int port){
        try {
            socket = new Socket(address, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (UnknownHostException e) {
            System.out.println("Unknown host");
            System.exit(-1);
        }
        catch  (IOException e) {
            System.out.println("No I/O");
            System.exit(-1);
        }
        //start wczytywania informacji z klawiatury
        new Thread(this).start();
        //i wysylania jej do serwera
        try {
            String line;
            while((line = in.readLine()) != null)
            {
                System.out.println(line);
            }
        }
        catch (IOException e) {
            System.out.println("Error during communication");
            System.exit(-1);
        }

        try {
            socket.close();
        }
        catch (IOException e) {
            System.out.println("Cannot close the socket");
            System.exit(-1);
        }

    }
    public static void main(String[] args) {
//        args = new String[]{"localhost", "18917"};
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
        new Client2(args[0], Integer.valueOf(args[1]));
    }
    public void run() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Hello!");
            System.out.println("If you want to see a list of players, type LIST");
            System.out.println("If you want to play, type PLAY");
            System.out.println("If you want to see and update your stats, type STATS");
            System.out.println("If you want to exit, type LOGOUT");
            System.out.println("Good luck!");
            System.out.println("==============================");
            out.println("P");
            while(this.running) {
                String msg = scanner.nextLine();
                if (msg.contains("LOGOUT") || msg.contains("logout")) {
                    System.out.println("CLOSING...");
                    out.println(msg.toUpperCase());
                    //wysylamy ostatnia wiadomosc i zamykamy proces klienta
                    this.running = false;
                    System.exit(0);
                }
                out.println(msg.toUpperCase());
            }
            scanner.close();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }
}
