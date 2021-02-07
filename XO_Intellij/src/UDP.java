public class UDP {
    private static int MIN_MTU = 576;
    private static int MAX_IP_HEADER_SIZE = 60;
    private static int UDP_HEADER_SIZE = 8;
    public static int MAX_DATAGRAM_SIZE;

    static {
        MAX_DATAGRAM_SIZE = MIN_MTU - MAX_IP_HEADER_SIZE - UDP_HEADER_SIZE;
    }
}
