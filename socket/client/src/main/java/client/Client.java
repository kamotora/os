package client;

import exception.ClientException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements AutoCloseable {
    private final Socket clientSocket;
    private final PrintWriter outWriter;
    private final BufferedReader inReader;


    public Client(int port) {
        this("127.0.0.1", port);
    }

    public Client(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            clientSocket.setSoTimeout(2000);
        } catch (Exception e) {
            var ex = new ClientException("Error to create socket with %s:%s".formatted(ip, port), e);
            System.err.println(ex.getLocalizedMessage());
            throw ex;
        }
        try {
            outWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            inReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            var ex = new ClientException("Error to create streams for %s:%s".formatted(ip, port), e);
            System.err.println(ex.getLocalizedMessage());
            throw ex;
        }
    }

    @SneakyThrows
    public String sendMessage(String msg) {
        System.out.printf("Sending message %s on %s%n", msg, clientSocket.getRemoteSocketAddress());
        if (msg.contains("block")) {
            // block socket
            while (true)
                outWriter.write(RandomUtils.nextInt());
        } else
            outWriter.println(msg);
        return inReader.readLine();
    }

    @SneakyThrows
    @Override
    public void close() {
        inReader.close();
        outWriter.close();
        clientSocket.close();
    }
}
