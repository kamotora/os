package server;

import exception.ServerException;
import lombok.Getter;
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
    @Getter
    private final ServerSocket serverSocket;
    private final ExecutorService executor;

    @SneakyThrows
    public Server(int port) {
        serverSocket = new ServerSocket(port, 1);
        this.executor = Executors.newFixedThreadPool(3);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @SneakyThrows
    public void start() {
        while (true) {
            executor.submit(new Connection(serverSocket.accept()));
        }
    }

    @SneakyThrows
    @Override
    public void close() {
        serverSocket.close();
    }

    private static class Connection implements AutoCloseable, Runnable {
        private final PrintWriter outWriter;
        private final BufferedReader inReader;
        private final Socket socket;

        @SneakyThrows
        public Connection(Socket socket) {
            this.socket = socket;
            socket.setSoTimeout(5000);
            try {
                outWriter = new PrintWriter(socket.getOutputStream(), true);
                inReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                var addr = socket.getInetAddress();
                var ex = new ServerException("Error to create streams for %s:%s".formatted(addr.getHostAddress(), socket.getPort()), e);
                System.err.println(ex.getLocalizedMessage());
                throw ex;
            }
        }

        @SneakyThrows
        public void run() {
            var request = inReader.readLine();
            var response  = StringUtils.isNotBlank(request) ? StringUtils.reverse(request) : "Bad request";
            System.out.printf("Client info: %s. Request: '%s'. Response: '%s'%n", socket.toString(), request, response);
            outWriter.println(response);
        }

        @SneakyThrows
        @Override
        public void close() {
            inReader.close();
            outWriter.close();
            socket.close();
        }
    }
}
