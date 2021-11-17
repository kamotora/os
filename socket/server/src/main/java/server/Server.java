package server;

import exception.ServerException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements AutoCloseable {
    private final ServerSocket serverSocket;
    private final ExecutorService executor;

    @SneakyThrows
    public Server(int port) {
        serverSocket = new ServerSocket(port);
        this.executor = Executors.newFixedThreadPool(3);
    }

    @SneakyThrows
    public void start() {
        while (true) {
            System.out.println("Wait connection....");
            executor.submit(new Connection(serverSocket.accept()));
        }
    }

    @SneakyThrows
    @Override
    public void close() {
        serverSocket.close();
    }

    private static class Connection implements AutoCloseable, Runnable {
        private final PrintWriter out;
        private final BufferedReader in;
        private final Socket socket;

        @SneakyThrows
        public Connection(Socket socket) {
            this.socket = socket;
            socket.setSoTimeout(5000);
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                var addr = socket.getInetAddress();
                var ex = new ServerException("Error to create streams for %s:%s".formatted(addr.getHostAddress(), socket.getPort()), e);
                System.err.println(ex.getLocalizedMessage());
                throw ex;
            }
        }

        @SneakyThrows
        public void run() {
            String request = in.readLine();
            if (StringUtils.isNotBlank(request)) {
                System.out.println("Client info: " + socket.toString());
                System.out.printf("Request: %s%n", request);
                var response = StringUtils.reverse(request);
                System.out.printf("Response: %s%n", response);
                out.println(response);
            } else {
                System.out.println("Receive blank string, return 'Bad request' msg...");
                out.println("Bad request");
            }
        }

        @SneakyThrows
        @Override
        public void close() {
            in.close();
            out.close();
            socket.close();
        }
    }
}
