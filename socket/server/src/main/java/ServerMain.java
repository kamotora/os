import server.Server;

public class ServerMain {
    public static void main(String[] args) {
        System.out.println("Start application");
        var server = new Server(1234);
        System.out.println("Server starting");
        server.start();
    }
}
