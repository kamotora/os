import client.Client;
import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;

public class ClientMain {
    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) {
        if (args.length != 0) {
            System.out.println("Response: " + sendToServer(StringUtils.join(args, " ")));
        } else {
            try (Scanner in = new Scanner(System.in)) {
                while (true) {
                    System.out.print("Input request text: ");
                    var request = in.nextLine();
                    System.out.println("Response: " + sendToServer(request));
                }
            }
        }
    }

    private static String sendToServer(String msg) {
        try (var client = new Client(1234)) {
            return client.sendMessage(msg);
        }
    }
}
