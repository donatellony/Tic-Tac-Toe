import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.regex.Pattern;


public class MainServerThread1 extends Thread {
    private Socket socket;
    boolean logout;
    boolean playingState;
    int Player_id;
    BufferedReader in;
    PrintWriter out;

    public MainServerThread1(Socket socket, int Player_id) {
        super();
        this.socket = socket;
        this.Player_id = Player_id;
        logout = false;
        playingState = false;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            out.println("==============================\nYour game data: \n" + MyServer1.baza.get(Player_id) + "\n==============================");
            while (!logout) {
                String line;
                line = in.readLine();
                String[] tmp = line.split(" ");
                switch (tmp[0]) {
                    case "PLAY":
                        if (tmp.length == 1) {
                            try {
                                MyServer1.FindOpponent(socket, Player_id);
                                MyServer1.baza.get(Player_id).isInGame = true;
                                while (MyServer1.baza.get(Player_id).isInGame){;}
                            } catch (Exception e) {
                                e.printStackTrace();
                                out.println("Error: PLAY");
                            }
                            break;
                        } else {
                            out.println("Error: PLAY");
                            break;
                        }
                    case "LOGOUT":
                        if (tmp.length == 1) {
                            try {
                                logout = true;
                                out.println("Logging out...");
                                out.println();
                                out.println();
                                socket.close();
                                MyServer1.baza.remove(Player_id);
                            } catch (Exception e) {
                                out.println("Error: LOGOUT");
                            }
                            break;
                        } else {
                            out.println("Error: LOGOUT");
                            break;
                        }
                    case "LIST":
                        if (tmp.length == 1) {
                            try {
                                out.println(MyServer1.baza.toString());
                            } catch (Exception e) {
                                out.println("Error: LIST");
                            }
                            break;
                        } else {
                            out.println("Error: LIST");
                            break;
                        }
                    case "STATS":
                        if (tmp.length == 1) {
                            try {
                                out.println(MyServer1.baza.get(Player_id).getStats());
                            } catch (Exception e) {
                                out.println("Error: STATS");
                            }
                            break;
                        } else {
                            out.println("Error: STATS");
                            break;
                        }
                    case "P":
                        if(MyServer1.baza.get(Player_id).getPlayerType()==0)
                        MyServer1.baza.get(Player_id).setPlayerType(1);
                        break;
                    case "S":
                        if(MyServer1.baza.get(Player_id).getPlayerType()==0) {
//                            MyServer1.baza.get(Player_id).setPlayerType(2);
//                            MyServer1.baza.get(Player_id).setNickname("Spectator");
                            MyServer1.baza.remove(Player_id); //to, co jest zakomentowane - moje niepewnosci w pewnych momentach.
                            //mozna zakomentowac powyzsza linijke i rozkomentowac te, ktore sa na niej
                            //wtedy serwer nie bedzie usuwac z glownej bazy danych widzow
                            MyServer1.spectators.put(Player_id, socket.getInetAddress());
                            out.println(MyServer1.translationPort);
                            //takze mozna zakomentowac ponizsze 4 linijki kodu:
                            logout = true;
                            out.println();
                            out.println();
                            socket.close();
                            //do tego miejsca, i rozkomentowac ponizsza klauzure IF
                            //ale wtedy Widz ciagle bedzie polaczony przez TCP do serwera
                            //nie jest napisane ze tak powinno byc, dlatego zakomentowalem to :\
                        }
                        break;
                    default:
                        out.println("ERROR: WRONG COMMAND");
                        break;
                }
//                if(socket.isClosed()){
//                    logout = true;
//                    out.println();
//                    out.println();
//                    socket.close();
//                    MyServer1.spectators.remove(Player_id);
//                    MyServer1.baza.remove(Player_id);
//                }
            }
            out.println();
            out.println();
        } catch (IOException e1) {
            // do nothing
            try {
                socket.close();
            } catch (IOException e) {
                // do nothing
            }
        }


    }
}