import client.Client;

import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {

        try (Scanner in = new Scanner(System.in)) {
            while (true) {
                System.out.print("Input request text: ");
                var request = in.nextLine();
                System.out.println("Response: " + sendToServer(request));
            }
        }
    }

    private static String sendToServer(String msg) {
        try (var client = new Client(1234)) {
            return client.sendMessage(msg);
        }
    }
}
