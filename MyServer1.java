import java.io.*;
import java.net.*;
import java.util.*;

public class MyServer1 {
    static LinkedHashMap<Integer, IP_Port> baza; //glowna baza danych
    public static ArrayList<Socket> queue; //kolej soketow dla poczatku gry
    public static ArrayList<Integer> idqueue; //kolej id dla poczatku gry
    public static HashMap<Integer, InetAddress> spectators; //baza danych widzow
    public static int MAIN_SERVER_PORT; //dalej sa intuicyjnie zrozumiale rzeczy
    public static DatagramSocket translation;
    public static int translationPort;

    MyServer1(int port, int translationPort) {
        MAIN_SERVER_PORT = port;
        this.translationPort = translationPort;
    }

    public void listenSocket() {
        ServerSocket server = null;
        spectators = new HashMap<>();
        Socket client = null;
        queue = new ArrayList<>();
        idqueue = new ArrayList<>();
        baza = new LinkedHashMap<Integer, IP_Port>() {
            //troche przerpbilem toString() zeby bylo lepiej widac co sie dzieje po komandzie LIST
            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                this.forEach((o1, o2) -> sb.append("ID: ").append(o1).append(", Nickname: ").append(o2.getNickname()).append(", IP: ").append(o2.getIp()).append(", Port: ").append(o2.getPort()).append("\n"));
                return sb.toString().trim();
            }
        };
        try {
            //wystartujemy nasze serwery TCP i UDP
            server = new ServerSocket(MAIN_SERVER_PORT);
            translation = new DatagramSocket();
        } catch (IOException e) {
            System.out.println("Could not listen");
            System.exit(-1);
        }
        System.out.println("Server listens on port: " + server.getLocalPort());
        System.out.println("Server translates on port: " + translation.getLocalPort() + ", to ports: " + translationPort);
        while (true) {
            try {
                client = server.accept();
            } catch (IOException e) {
                System.out.println("Accept failed");
                System.exit(-1);
            }
            //dodajemy klienta do bazy i przenosimy do oddzielnego watku
            IP_Port player = new IP_Port(client.getInetAddress(), client.getPort());
            baza.put(player.id, player);

            (new MainServerThread1(client, player.id)).start();
        }

    }

    public static void main(String[] args) {
//        args = new String[]{"18917", "20402"};
        if(args.length!=2){
            System.out.println("ERROR: You need 2 arguments to run this server!");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
        for (int i = 0; i < args.length; i++) {
            if(!args[i].matches("\\d{1,5}") || args[i].matches("-\\d+")) {
                System.out.println("ERROR: You need to write ports at the arguments!");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
            else {
                if(Integer.valueOf(args[i])>65535 || Integer.valueOf(args[i])<1){
                    System.out.println("ERROR: You're out of the ports bounds!");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                }
            }
        }
        MyServer1 server = new MyServer1(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
        server.listenSocket();
    }

    public static void FindOpponent(Socket socket, int Player_id) {
        queue.add(socket);
        idqueue.add(Player_id);
        //dodajemy gracza do kolei i czekamy na opponenta
        while (queue.size() < 2 && queue.size()!=0) {
            try {
                PrintWriter out0 = new PrintWriter(queue.get(0).getOutputStream(), true);
                out0.println("Waiting for the opponent...");
                Thread.sleep(5000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Player_id==idqueue.get(1) - latwy sposob pozbyc sie bledu z startowaniem dwoch watkow z gra
        if (queue.size()>=2 && Player_id == idqueue.get(1)) {
            try {
                PrintWriter out0 = new PrintWriter(queue.get(0).getOutputStream(), true);
                PrintWriter out1 = new PrintWriter(queue.get(1).getOutputStream(), true);
                //wszedzie wysylamy info o poczatku gry
                out0.println("Your opponent is:\n" + baza.get(idqueue.get(1)));
                out1.println("Your opponent is:\n" + baza.get(idqueue.get(0)));
                System.out.println("//////////\nMatch between\n" + baza.get(idqueue.get(1)) + "\nand\n" + baza.get(idqueue.get(0)) + "\nhas started!\n//////////");
                stream(translation, spectators, "//////////\nMatch between\n" + baza.get(idqueue.get(1)) + "\nand\n" + baza.get(idqueue.get(0)) + "\nhas started!\n//////////");
            } catch (IOException e) {
                e.printStackTrace();
            }
            StartGame(queue.get(0), queue.get(1), idqueue.get(0), idqueue.get(1));
            queue.remove(0);
            idqueue.remove(0);
            queue.remove(0);
            idqueue.remove(0);
        }
    }

    public static void StartGame(Socket p1, Socket p2, int idp1, int idp2) {
        //tworzymy nowy watek dla meczu
        new Thread(() -> {
            BufferedReader inp1;
            BufferedReader inp2;
            PrintWriter outp1;
            PrintWriter outp2;
            //nasza deska dla gry
            String board = "   1  2  3\n" +
                    "A|| || || ||\n" +
                    "-----------\n" +
                    "B|| || || ||\n" +
                    "-----------\n" +
                    "C|| || || ||";
            char[][] playField = new char[3][3];
            startGame(playField, board);
            int turnCounter = 1;
            String str ="";
            int[] turn = new int[2];
            try {
                inp1 = new BufferedReader(new InputStreamReader(p1.getInputStream()));
                inp2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));
                outp1 = new PrintWriter(p1.getOutputStream(), true);
                outp2 = new PrintWriter(p2.getOutputStream(), true);
                //ciagle sprawdzamy czy gra jeszcze nie jest zakonczona
                while (!isGameEnded(playField) && !isDraw(playField)) {
                    if(turnCounter%2==1) {
                        //zaczyna sie ruch X
                        boolean isRightMove;
                        boolean isCellFree = false;
                            outp1.println("Now it's YOUR (X's) turn!");
                            outp2.println("Now it's X's turn!");
                            outp1.println(board);
                            outp2.println(board);
                            //sprawdzamy legalnosc ruchu, a juz potem robimy go
                        do {
                            str = inp1.readLine().toUpperCase().trim();
                            while (!isRightMove(str)){
                                outp1.println("Please, enter the right combination of symbols (example: A1)");
                                str = inp1.readLine().toUpperCase().trim();
                            }
                            isRightMove = true;
                            switch (str.charAt(0)) {
                                case 'A':
                                    turn[0] = 0;
                                    break;
                                case 'B':
                                    turn[0] = 1;
                                    break;
                                case 'C':
                                    turn[0] = 2;
                            }
                            turn[1] = Integer.valueOf(String.valueOf(str.charAt(1))) - 1;
                            if (!isCellFree(playField, turn)){
                                outp1.println("This cell is already taken!\nChoose one of the others");
                                isRightMove = false;
                            }
                            else
                                isCellFree = true;
                        }while (!isTurnDone(isRightMove, isCellFree));
                        playField[turn[0]][turn[1]] = 'X';
                        board = refreshBoard(playField);
                        turnCounter++;
                    }
                    else {
                        //zaczyna sie ruch O
                        boolean isRightMove;
                        boolean isCellFree = false;
                        outp1.println("Now it's O's turn!");
                        outp2.println("Now it's YOUR (O's) turn!");
                        outp1.println(board);
                        outp2.println(board);
                        //tu tez sprawdzamy legalnosc
                        do {
                            str = inp2.readLine().toUpperCase().trim();
                            while (!isRightMove(str)) {
                                outp2.println("Please, enter the right combination of symbols (example: A1)");
                                str = inp2.readLine().toUpperCase().trim();
                            }
                            isRightMove = true;
                            switch (str.charAt(0)) {
                                case 'A':
                                    turn[0] = 0;
                                    break;
                                case 'B':
                                    turn[0] = 1;
                                    break;
                                case 'C':
                                    turn[0] = 2;
                            }
                            turn[1] = Integer.valueOf(String.valueOf(str.charAt(1))) - 1;
                            if (!isCellFree(playField, turn)){
                                outp2.println("This cell is already taken!\nChoose one of the others");
                                isRightMove = false;
                            }
                            else
                                isCellFree = true;
                        }while (!isTurnDone(isRightMove, isCellFree));
                        playField[turn[0]][turn[1]] = 'O';
                        board = refreshBoard(playField);
                        turnCounter++;
                    }
                    //po kazdym ruchu wypisujemy do widza co sie zmienilo
                    stream(translation, spectators, "//////////\n"+baza.get(idp1) + "\n(X) VS (O)\n" + baza.get(idp2) + "\n"+board +"\nTurn: " +(turnCounter-1)+"\n//////////");
                }
                //na tym momencie spelnia sie jeden z warunkow zakonczenia gry
                outp1.println(board);
                outp2.println(board);
                outp1.println("THE MATCH HAS BEEN ENDED");
                outp2.println("THE MATCH HAS BEEN ENDED");
                if(isDraw(playField)){
                    //remis nie zmienia statystyki graczy
                    outp1.println("DRAW!");
                    outp2.println("DRAW!");
                }
                else {
                    //jesli nie bylo remisu, wszedzie wypisujemy kto ma wygrane i przegrane i zmieniamy statystyki
                    if (turnCounter % 2 == 0) {
                        outp1.println("THE WINNER IS " + baza.get(idp1).getNickname().toUpperCase() + "!");
                        baza.get(idp1).victories++;
                        outp1.println("CONGRATULATIONS!");
                        outp2.println("THE WINNER IS " + baza.get(idp1).getNickname().toUpperCase() + "!");
                        baza.get(idp2).defeats++;
                        outp2.println("BETTER LUCK NEXT TIME ;)");
                        System.out.println("//////////\nMatch between\n" + baza.get(idp1) + "(Victory)\nand\n" + baza.get(idp2) + "(Defeat)\nhas ended\n//////////");
                        stream(translation, spectators, "//////////\nMatch between\n" + baza.get(idp1) + "(Victory)\nand\n" + baza.get(idp2) + "(Defeat)\nhas ended\n//////////");
                    }
                    if (turnCounter % 2 == 1) {
                        outp1.println("THE WINNER IS " + baza.get(idp2).getNickname().toUpperCase() + "!");
                        baza.get(idp1).defeats++;
                        outp1.println("BETTER LUCK NEXT TIME ;)");
                        outp2.println("THE WINNER IS " + baza.get(idp2).getNickname().toUpperCase() + "!");
                        baza.get(idp2).victories++;
                        outp2.println("CONGRATULATIONS!");
                        System.out.println("//////////\nMatch between\n" + baza.get(idp1) + "(Defeat)\nand\n" + baza.get(idp2) + "(Victory)\nhas ended\n//////////");
                        stream(translation, spectators, "//////////\nMatch between\n" + baza.get(idp1) + "(Defeat)\nand\n" + baza.get(idp2) + "(Victory)\nhas ended\n//////////");
                    }
                }
                //gracze wracaja do swojego oddzielnego watku
                MyServer1.baza.get(idp1).isInGame = false;
                MyServer1.baza.get(idp2).isInGame = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public static void startGame(char[][] playField, String board) {
        for (int i = 0; i < playField.length; i++)
            for (int j = 0; j < playField[i].length; j++) {
                playField[i][j] = ' ';
            }
        board = refreshBoard(playField);
            //ta metoda potrzebna jest do startu gry(logicznie :D)
    }

    public static String refreshBoard(char[][] playField) {
        return "   1  2  3\n" +
                "A||" + playField[0][0] + "||" + playField[0][1] + "||" + playField[0][2] + "||\n" +
                " -----------\n" +
                "B||" + playField[1][0] + "||" + playField[1][1] + "||" + playField[1][2] + "||\n" +
                " -----------\n" +
                "C||" + playField[2][0] + "||" + playField[2][1] + "||" + playField[2][2] + "||";
        //szybka aktualizacja deski
    }

    public static boolean isGameEnded(char[][] playField) {
        //nazwa juz prawie wszystko powiedziala. Zwraca true kiedy ktos wygrywa
        int counter1 = 1;
        int counter2 = 1;
        for (int i = 0; i < playField.length; i++) {
            char char1 = playField[i][0];
            char char2 = playField[0][i];
            for (int j = 0; j < playField[i].length - 1; j++) {
                if (char1 != ' ' && char1 == playField[i][j + 1])
                    ++counter1;
                if (char2 != ' ' && char2 == playField[j + 1][i])
                    ++counter2;
            }
            if (counter1 < playField.length)
                counter1 = 1;
            else
                return true;
            if (counter2 < playField.length)
                counter2 = 1;
            else
                return true;
        }
        int i = 1;
        char charV1 = playField[0][0];
        char charV2 = playField[0][playField.length - 1];
        while (i < playField.length) {
            if (charV1 == playField[i][i] && charV1 != ' ')
                ++counter1;
            if (charV2 == playField[i][playField.length - 1 - i] && charV2 != ' ')
                ++counter2;
            ++i;
        }
        return counter2 >= playField.length || counter1 >= playField.length;
    }
    public static boolean isDraw(char[][] playField){
        int counter3 = 0;
        for (int i = 0; i < playField.length; i++) {
            for (int j = 0; j < playField.length; j++) {
                if(playField[i][j]!=' ')
                    ++counter3;
            }
        }
        return counter3 == playField.length * playField.length;
    }
    public static boolean isRightMove(String str){
        //sprawdzamy legalnosc ruchu
        if(str.length()>2)
            return false;
        if(!str.substring(0,1).matches("[ABC]"))
            return false;
        return str.substring(1, 2).matches("[123]");
    }

    public static boolean isCellFree(char[][]playField, int[] turn){
        return playField[turn[0]][turn[1]] == ' ';
    }
    public static boolean isTurnDone(boolean isRightMove, boolean isCellFree){
        return isRightMove && isCellFree;
    }
    //wysylamy wszystkim widzom rozna informacje
    public static void stream(DatagramSocket translation, HashMap<Integer, InetAddress> spectators, String str){
        spectators.forEach((o1,o2)-> {
            try {
                translation.send(new DatagramPacket(str.getBytes(), str.getBytes().length,o2, translationPort));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}