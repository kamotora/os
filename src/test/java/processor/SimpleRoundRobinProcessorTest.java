package processor;

import org.junit.jupiter.api.Test;
import output.Color;
import task.TaskFactory;

class SimpleRoundRobinProcessorTest {
    @Test
    public void test() {
        SimpleRoundRobinProcessor
                .builder()
                .task(TaskFactory.randomTask(Color.RED, 10, 30))
                .task(TaskFactory.randomTask(Color.RED, 10, 40))
                .task(TaskFactory.randomTask(Color.RED, 10, 50))
                .task(TaskFactory.randomTask(Color.RED, 10, 60))
                .build()
                .processTasksTraceable();
    }
}