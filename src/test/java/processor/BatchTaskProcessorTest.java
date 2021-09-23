package processor;

import org.junit.jupiter.api.Test;
import output.Color;
import task.DurationWrapper;
import task.OperationFactory;
import task.TaskFactory;

import java.util.Arrays;

class BatchTaskProcessorTest {
    @Test
    public void test() {
        BatchTaskProcessor
                .builder()
                .task(TaskFactory.randomTask("1", Color.RED, 10, 30))
                .task(TaskFactory.randomTask("2", Color.GREEN, 10, 40))
                .task(TaskFactory.randomTask("3", Color.BLUE, 10, 50))
                .task(TaskFactory.randomTask("4", Color.CYAN, 10, 60))
                .build()
                .processTasksTraceable();
    }


    @Test
    public void fixedValuesTest() {
        BatchTaskProcessor
                .builder()
                .task(TaskFactory
                        .fixedTask("1", Color.RED, Arrays.asList(
                                OperationFactory.calculationOperation(DurationWrapper.millis(100)),
                                OperationFactory.networkOperation(DurationWrapper.millis(100)))))
                .task(TaskFactory
                        .fixedTask("2", Color.GREEN, Arrays.asList(
                                OperationFactory.calculationOperation(DurationWrapper.millis(100)),
                                OperationFactory.calculationOperation(DurationWrapper.millis(100)))))
                .task(TaskFactory
                        .fixedTask("3", Color.CYAN, Arrays.asList(
                                OperationFactory.networkOperation(DurationWrapper.millis(100)),
                                OperationFactory.guiOperation(DurationWrapper.millis(100)))))
                .task(TaskFactory
                        .fixedTask("4", Color.BLUE, Arrays.asList(
                                OperationFactory.calculationOperation(DurationWrapper.millis(100)),
                                OperationFactory.networkOperation(DurationWrapper.millis(100)),
                                OperationFactory.calculationOperation(DurationWrapper.millis(100)),
                                OperationFactory.guiOperation(DurationWrapper.millis(100)))))
                .task(TaskFactory
                        .fixedTask("5", Color.YELLOW, Arrays.asList(
                                OperationFactory.networkOperation(DurationWrapper.millis(1000)),
                                OperationFactory.networkOperation(DurationWrapper.millis(1000)),
                                OperationFactory.guiOperation(DurationWrapper.millis(1000)),
                                OperationFactory.guiOperation(DurationWrapper.millis(1000)))))
                .build()
                .processTasksTraceable();
    }
}