package client;

import exception.ClientException;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements AutoCloseable {
    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;


    public Client(int port) {
        this("127.0.0.1", port);
    }

    public Client(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            clientSocket.setSoTimeout(5000);
        } catch (Exception e) {
            var ex = new ClientException("Error to create socket with %s:%s".formatted(ip, port), e);
            System.err.println(ex.getLocalizedMessage());
            throw ex;
        }
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            var ex = new ClientException("Error to create streams for %s:%s".formatted(ip, port), e);
            System.err.println(ex.getLocalizedMessage());
            throw ex;
        }
    }

    @SneakyThrows
    public String sendMessage(String msg) {
        out.println(msg);
        var response = in.readLine();
        return response;
    }

    @SneakyThrows
    @Override
    public void close() {
        in.close();
        out.close();
        clientSocket.close();
    }
}
