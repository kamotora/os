package client;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientTest {

    @Test
    void sendMessageTest() {
        String response = sendMsgIntoDefaultPort("client");
        assertEquals(StringUtils.reverse("client"), response);
    }

    @Test
    void sendBlankTest() {
        String response = sendMsgIntoDefaultPort("");
        assertEquals("Bad request", response);

    }

    @Test
    @SneakyThrows
    void sendMessageWithManyThreadsTest() {
        runTaskInManyThread((ignore) -> {
            String name = Thread.currentThread().getName();
            System.out.printf("Thread %s sending name%n", name);
            String response = sendMsgIntoDefaultPort(name);
            System.out.printf("Thread %s receive response %s%n", name, response);
            try {
                Thread.sleep(2000); // чтобы точно выполнилось в разных потоках
            } catch (InterruptedException e) {
                // ignore
            }
        }, 10);
    }

    @Test
    @SneakyThrows
    public void sendMessageInManyProcesses() {
        runTaskInManyThread((i) -> {
            try {
                Process exec = Runtime.getRuntime().exec("/home/kamotora/.jdks/openjdk-17.0.1/bin/java -javaagent:/home/kamotora/jetbrains/apps/IDEA-U/ch-0/213.5744.223/lib/idea_rt.jar=42143:/home/kamotora/jetbrains/apps/IDEA-U/ch-0/213.5744.223/bin -Dfile.encoding=UTF-8 -classpath /home/kamotora/IdeaProjects/os/socket/client/target/classes:/home/kamotora/.m2/repository/org/projectlombok/lombok/1.18.22/lombok-1.18.22.jar:/home/kamotora/.m2/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar ClientMain");
                String processMessage = "pid " + exec.pid();
                try(var outputStream = exec.getOutputStream()) {
                    outputStream.write(processMessage.getBytes(StandardCharsets.UTF_8));
                }
                var processConsoleOutput = new String(exec.getInputStream().readAllBytes());
                System.out.printf("For process %d output: %n%s%n", i, processConsoleOutput.replaceAll("Input request text: ",""));
            } catch (Exception e) {
                System.err.println(e);
            }
        }, 5);
    }
    @Test
    @Timeout(10)
    @SneakyThrows
    public void sendMessageInManyProcessesWithBlock() {
        Process exec = Runtime.getRuntime().exec("/home/kamotora/.jdks/openjdk-17.0.1/bin/java -javaagent:/home/kamotora/jetbrains/apps/IDEA-U/ch-0/213.5744.223/lib/idea_rt.jar=42143:/home/kamotora/jetbrains/apps/IDEA-U/ch-0/213.5744.223/bin -Dfile.encoding=UTF-8 -classpath /home/kamotora/IdeaProjects/os/socket/client/target/classes:/home/kamotora/.m2/repository/org/projectlombok/lombok/1.18.22/lombok-1.18.22.jar:/home/kamotora/.m2/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar ClientMain block" );
        System.out.println(exec.errorReader().lines().collect(Collectors.joining()));
        sendMessageInManyProcesses();
    }


    private static String sendMsgIntoDefaultPort(String msg) {

        try (var socket = new Client(1234)) {
            return socket.sendMessage(msg);
        }
    }

    @SneakyThrows
    private static void runTaskInManyThread(Consumer<Integer> task, int threads) {
        List<Callable<Void>> tasks = IntStream.range(0, threads)
                .parallel()
                .mapToObj((i) -> (Callable<Void>) () -> {
                    task.accept(i);
                    return null;
                })
                .collect(Collectors.toList());
        Executors.newFixedThreadPool(threads).invokeAll(tasks);
    }
}