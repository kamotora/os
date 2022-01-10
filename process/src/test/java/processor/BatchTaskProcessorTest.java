package processor;

import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonConfig;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import output.Color;
import task.DurationWrapper;
import task.OperationFactory;
import task.Task;
import task.TaskFactory;
import util.PlotUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class BatchTaskProcessorTest {
    @Test
    public void fullRandomTest() {
        List<Pair<List<Task>, ProcessorStatistics>> stats = new ArrayList<>();
        IntStream.range(0, 19)
                .parallel()
                .forEach(i -> {
                    var tasks = IntStream.range(0, RandomUtils.nextInt(2, 8)).mapToObj(
                            j -> TaskFactory.randomTask(Integer.toString(j), Color.randomColor(), 5,
                                    RandomUtils.nextInt(30, 100))).collect(Collectors.toList());
                    ProcessorStatistics statistics = RoundRobinTaskProcessor
                            .builder()
                            .tasks(tasks)
                            .build()
                            .processTasksTraceable();
                    stats.add(Pair.of(tasks, statistics));
                });
        ProcessorStatistics sumStat = ProcessorStatistics.sum(stats.stream().map(Pair::getRight).toList());
        AbstractTaskProcessor.printProcessorStatistics(sumStat);
//        List<Pair<Double, Double>> collect = stats.stream()
//                .map(Pair::getRight)
//                .map(stat -> Pair.of((double) (stat.ioOperationsTime()) / stat.totalTime() * 100,
//                        stat.waitTimeStat().getAverage() / stat.totalTime() * 100))
//                .collect(Collectors.toList());
//        PlotUtils.draw(collect, "% IO operations time", "% average waiting time");
    }

    @SneakyThrows
    private void drawPlot(List<Pair<Double, Double>> points, String xLabel, String yLabel) {
        List<Pair<? extends Number, ? extends Number>> sorted = points.stream()
                .sorted(Comparator.comparing(Pair::getLeft))
                .collect(Collectors.toList());
        var x = sorted.stream()
                .map(Pair::getLeft)
                .collect(Collectors.toList());
        var y = sorted.stream()
                .map(Pair::getRight)
                .collect(Collectors.toList());

        Plot plt = Plot.create(PythonConfig.pythonBinPathConfig("/home/kamotora/anaconda3/bin/python"));
        plt.plot()
                .add(x, y, "-ok");
        plt.xlabel(xLabel);
        plt.ylabel(yLabel);
        plt.legend();
        plt.show();
    }

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
    public void onlyIoOperationsTest() {
        BatchTaskProcessor
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
        BatchTaskProcessor
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
        BatchTaskProcessor
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
        BatchTaskProcessor
                .builder()
                .task(TaskFactory
                        .fixedTask("1", Color.RED, Arrays.asList(
                                OperationFactory.calculationOperation(DurationWrapper.millis(5000))/*,
                                OperationFactory.networkOperation(DurationWrapper.millis(2000))*/)))
                .task(TaskFactory
                        .fixedTask("2", Color.GREEN, Arrays.asList(
              /*                  OperationFactory.networkOperation(DurationWrapper.millis(100)),*/
                                OperationFactory.calculationOperation(DurationWrapper.millis(100)))))
                .build()
                .processTasksTraceable();
    }
}