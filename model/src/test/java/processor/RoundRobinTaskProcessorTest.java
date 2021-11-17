package processor;

import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import output.Color;
import output.RichConsole;
import output.RichTextConfig;
import task.DurationWrapper;
import task.OperationFactory;
import task.Task;
import task.TaskFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class RoundRobinTaskProcessorTest {

    @Test
    @SneakyThrows
    public void fullRandomTest() {
        var tasks = IntStream.range(0, 19)
                .mapToObj(i ->
                        IntStream
                                .range(0, RandomUtils.nextInt(2, 8))
                                .mapToObj(j ->
                                        TaskFactory.randomTask(Integer.toString(j), Color.randomColor(), 5,
                                                RandomUtils.nextInt(30, 100))
                                )
                                .collect(Collectors.toList())
                ).collect(Collectors.toList());
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        executorService.submit(() -> randomTest(tasks, null));
        var batch = executorService.submit(() -> randomTest(tasks, null));
        var robin2ms = executorService.submit(() -> randomTest(tasks, DurationWrapper.millis(2)));
        var robin10ms = executorService.submit(() -> randomTest(tasks, DurationWrapper.millis(10)));
        var robin150ms = executorService.submit(() -> randomTest(tasks, DurationWrapper.millis(150)));
        var robin500ms = executorService.submit(() -> randomTest(tasks, DurationWrapper.millis(500)));
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);
        Thread.sleep(100);
        RichConsole.print("#### BATCH", RichTextConfig.metaMessageStyle());
        AbstractTaskProcessor.printProcessorStatistics(batch.get());
        RichConsole.print("#### ROBIN 2 ms", RichTextConfig.metaMessageStyle());
        AbstractTaskProcessor.printProcessorStatistics(robin2ms.get());
        RichConsole.print("#### ROBIN 10 ms", RichTextConfig.metaMessageStyle());
        AbstractTaskProcessor.printProcessorStatistics(robin10ms.get());
        RichConsole.print("#### ROBIN 150 ms", RichTextConfig.metaMessageStyle());
        AbstractTaskProcessor.printProcessorStatistics(robin150ms.get());
        RichConsole.print("#### ROBIN 500 ms", RichTextConfig.metaMessageStyle());
        AbstractTaskProcessor.printProcessorStatistics(robin500ms.get());
//        List<Pair<Double, Double>> collect = stats.stream()
//                .map(Pair::getRight)
//                .map(stat -> Pair.of((double) (stat.ioOperationsTime()) / stat.totalTime() * 100,
//                        stat.waitTimeStat().getAverage() / stat.totalTime() * 100))
//                .collect(Collectors.toList());
//        PlotUtils.draw(collect, "% IO operations time", "% average waiting time");
    }

    private ProcessorStatistics randomTest(List<List<Task>> tasks, DurationWrapper quantum) {
        return tasks.stream()
                .parallel()
                .map(taskList -> {
                            var cloned = taskList.stream().map(Task::clone).toList();
                            if (quantum != null)
                                return RoundRobinTaskProcessor.builder()
                                        .tasks(cloned)
                                        .quantum(quantum)
                                        .build()
                                        .processTasksTraceable();
                            else
                                return BatchTaskProcessor.builder()
                                        .tasks(cloned)
                                        .build()
                                        .processTasksTraceable();
                        }
                ).reduce(ProcessorStatistics.createZero(), ProcessorStatistics::sum);
    }


    @Test
    public void test() {
        RoundRobinTaskProcessor
                .builder()
                .task(TaskFactory.randomTask("1", Color.RED, 10, 30))
                .task(TaskFactory.randomTask("2", Color.GREEN, 10, 40))
                .task(TaskFactory.randomTask("3", Color.BLUE, 10, 50))
                .task(TaskFactory.randomTask("4", Color.CYAN, 10, 60))
                .build()
                .processTasksTraceable();
    }

    @Test
    public void onlyIoOperationsTest() {
        RoundRobinTaskProcessor
                .builder()
                .task(TaskFactory
                        .fixedTask("1", Color.RED, Arrays.asList(
                                OperationFactory.guiOperation(DurationWrapper.millis(100)),
                                OperationFactory.networkOperation(DurationWrapper.millis(200)))))
                .task(TaskFactory
                        .fixedTask("2", Color.GREEN, Arrays.asList(
                                OperationFactory.guiOperation(DurationWrapper.millis(50)),
                                OperationFactory.networkOperation(DurationWrapper.millis(100)))))
                .task(TaskFactory
                        .fixedTask("3", Color.CYAN, Arrays.asList(
                                OperationFactory.guiOperation(DurationWrapper.millis(100)),
                                OperationFactory.networkOperation(DurationWrapper.millis(100)))))
                .build()
                .processTasksTraceable();
    }

    @Test
    public void fixedValuesTest() {
        RoundRobinTaskProcessor
                .builder()
                .task(TaskFactory
                        .fixedTask("1 task", Color.RED, Arrays.asList(
                                OperationFactory.calculationOperation(DurationWrapper.millis(500)),
                                OperationFactory.networkOperation(DurationWrapper.millis(500)))))
                .task(TaskFactory
                        .fixedTask("2 task", Color.GREEN, Arrays.asList(
                                OperationFactory.calculationOperation(DurationWrapper.millis(500)),
                                OperationFactory.calculationOperation(DurationWrapper.millis(500)))))
                .task(TaskFactory
                        .fixedTask("3 task", Color.CYAN, Arrays.asList(
                                OperationFactory.networkOperation(DurationWrapper.millis(500)),
                                OperationFactory.guiOperation(DurationWrapper.millis(500)))))
                .task(TaskFactory
                        .fixedTask("4 task", Color.BLUE, Arrays.asList(
                                OperationFactory.calculationOperation(DurationWrapper.millis(500)),
                                OperationFactory.networkOperation(DurationWrapper.millis(500)),
                                OperationFactory.calculationOperation(DurationWrapper.millis(500)),
                                OperationFactory.guiOperation(DurationWrapper.millis(500)))))
                .task(TaskFactory
                        .fixedTask("5 task", Color.YELLOW, Arrays.asList(
                                OperationFactory.networkOperation(DurationWrapper.millis(1000)),
                                OperationFactory.calculationOperation(DurationWrapper.millis(1000)),
                                OperationFactory.calculationOperation(DurationWrapper.millis(1000)),
                                OperationFactory.guiOperation(DurationWrapper.millis(1000)))))
                .build()
                .processTasksTraceable();
    }

    @Test
    public void fixedValuesTest2() {
        RoundRobinTaskProcessor
                .builder()
                .task(TaskFactory
                        .fixedTask("1 task", Color.RED, Arrays.asList(
                                OperationFactory.calculationOperation(DurationWrapper.millis(750)),
                                OperationFactory.networkOperation(DurationWrapper.millis(250)))))
                .task(TaskFactory
                        .fixedTask("2 task", Color.GREEN, Arrays.asList(
                                OperationFactory.calculationOperation(DurationWrapper.millis(750)),
                                OperationFactory.calculationOperation(DurationWrapper.millis(750)))))
                .task(TaskFactory
                        .fixedTask("3 task", Color.CYAN, Arrays.asList(
                                OperationFactory.networkOperation(DurationWrapper.millis(250)),
                                OperationFactory.guiOperation(DurationWrapper.millis(250)))))
                .task(TaskFactory
                        .fixedTask("4 task", Color.BLUE, Arrays.asList(
                                OperationFactory.calculationOperation(DurationWrapper.millis(750)),
                                OperationFactory.networkOperation(DurationWrapper.millis(250)),
                                OperationFactory.calculationOperation(DurationWrapper.millis(750)),
                                OperationFactory.guiOperation(DurationWrapper.millis(250)))))
                .task(TaskFactory
                        .fixedTask("5 task", Color.YELLOW, Arrays.asList(
                                OperationFactory.networkOperation(DurationWrapper.millis(500)),
                                OperationFactory.calculationOperation(DurationWrapper.millis(1500)),
                                OperationFactory.calculationOperation(DurationWrapper.millis(1500)),
                                OperationFactory.guiOperation(DurationWrapper.millis(500)))))
                .build()
                .processTasksTraceable();
    }


    @Test
    public void longTaskTest() {
        RoundRobinTaskProcessor
                .builder()
                .task(TaskFactory
                        .fixedTask("1", Color.RED, Arrays.asList(
                                OperationFactory.calculationOperation(DurationWrapper.millis(5000))/*,
                                OperationFactory.networkOperation(DurationWrapper.millis(100))*/)))
                .task(TaskFactory
                        .fixedTask("2", Color.GREEN, Arrays.asList(
                                OperationFactory.calculationOperation(DurationWrapper.millis(100))/*,
                                OperationFactory.networkOperation(DurationWrapper.millis(100))*/)))
                .build()
                .processTasksTraceable();
    }
}