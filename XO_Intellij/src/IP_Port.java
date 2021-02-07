import java.net.InetAddress;

public class IP_Port {
    //tak naprawde, sa tu nie tylko IP i Port, ale glownie przechowuje ona IP i Port
    private static int ID = 0;
    private int playerType; //0 - unsigned, 1 - player, 2 - spectator
    private static String[] name1 = {"Mighty", "Fat", "Big", "Deadly", "Legendary", "Gorgeous", "Battle", "Bizarre", "Weird", "Godly", "Angry", "Dangerous"};
    private static String[] name2 = {"Horse", "Unknown", "Pig", "Knight", "Smoke", "Tomaszew", "Cock", "Gus", "Dog", "Cat", "Crocodile", "Flamingo", "Chicken"};
    private InetAddress ip;
    double victories;
    double defeats;
    int id;
    private int port;
    private String nickname;
    boolean isInGame;
    IP_Port(InetAddress ip, int port){
        playerType = 0;
        isInGame = false;
        id = ID++;
        this.ip = ip;
        this.port = port;
        int word1 = (int)(Math.random()*name1.length);
        int word2 = (int)(Math.random()*name2.length);
        nickname = "The " + name1[word1] + " " + name2[word2];
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setPlayerType(int playerType) {
        this.playerType = playerType;
    }

    public int getPlayerType() {
        return playerType;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getPort() {
        return port;
    }

    public String getNickname() {
        return nickname;
    }

    public String getStats(){
        String winratestr ="";
        double winrate = victories/(victories+defeats);
        if(winrate>=0.9)
            winratestr = "Destroyer";
        if(0.9>winrate && winrate>=0.7)
            winratestr = "Enlightened";
        if(0.7>winrate && winrate>=0.5)
            winratestr = "Experienced";
        if(0.5>winrate && winrate>=0.3)
            winratestr = "Weak";
        if(0.3>winrate && defeats!=0)
            winratestr = "Noob";
        nickname = winratestr +", " + nickname;
        if(victories != 0 || defeats !=0)
        return "Victories: " + victories + ", Defeats: " + defeats +"\nWinrate: " + winrate + ", Prefix: " + winratestr;
        return "Play at least one game to get your prefix!\nIt will change as your winrate changes.";
    }

    @Override
    public String toString() {
        return "Ip:"+ip+"\nPort:"+port+"\nNickname:"+nickname;
    }
}
