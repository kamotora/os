package processor;

import output.*;
import task.Operation;
import task.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

public abstract class AbstractTaskProcessor<T extends Task> {
    public static final RichTextConfig UNDERLINE_CONFIG = RichTextConfig.metaMessageStyle();

    protected final List<T> tasks;
    // Общее время на обработку операций НЕ ввода/вывода
    protected long timer = 0L;
    protected final IOProcessor ioProcessor = new IOProcessor();

    public long startOfSystemTime;

    protected AbstractTaskProcessor(List<T> tasks) {
        this.tasks = tasks;
    }

    protected abstract void processTasks(List<Task> tasks);

    public ProcessorStatistics processTasksWithStatistics() {
        startOfSystemTime = System.currentTimeMillis();
        ArrayList<Task> processingTasks = new ArrayList<>(this.tasks);
        processTasks(processingTasks);
        long totalTime = System.currentTimeMillis() - startOfSystemTime;
        // output stats
        RichConsole.print("Tasks performed:\n", null);
        RichConsole.print("Tasks per second: %.3f".formatted(this.tasks.size() / (totalTime * 0.001)), null);
        var calcOperationsTime = 0L;
        for (Task task : this.tasks) {
            long ioTime = task.getOperations().stream()
                    .filter(oper -> oper.getType() == Operation.Type.IO)
                    .mapToLong(Operation::getExecutionTime)
                    .sum();
            long calcTime = task.getOperations().stream()
                    .filter(oper -> oper.getType() != Operation.Type.IO)
                    .mapToLong(Operation::getExecutionTime)
                    .sum();
            calcOperationsTime += calcTime;
            long waitingTime = task.getOperations().stream()
                    .mapToLong(Operation::getWaitingTime)
                    .sum();
            RichConsole.print(task.getName() +
                            ":\n\t Рабочее время задачи (ms): " + task.getTotalExecutionTime() +
                            "\n\t Время от начала старта системы до завершения задачи (ms): " + (task.getEnd() - startOfSystemTime) +
                            "\n\t Время от начала старта задачи до завершения задачи (ms): " + task.getTimeFromStartExecution() +
                            "\n\t Общее время вычислительных задач (ms): " + calcTime +
                            "\n\t Общее время IO операций (ms): " + ioTime +
                            "\n\t Общее время ожидания IO операций (ms): " + waitingTime,
                    task.getDecoration());
        }
        LongSummaryStatistics execTimeStat = this.tasks.stream()
                .mapToLong(Task::getTotalExecutionTime)
                .summaryStatistics();
        LongSummaryStatistics fromExecStat = this.tasks.stream()
                .mapToLong(Task::getTimeFromStartExecution)
                .summaryStatistics();
        LongSummaryStatistics fromStartStat = this.tasks.stream()
                .mapToLong(task -> (task.getEnd() - startOfSystemTime))
                .summaryStatistics();
        LongSummaryStatistics waitTimeStat = this.tasks.stream()
                .mapToLong(Task::getWaitingTime)
                .summaryStatistics();
        RichTextConfig statConfig = RichTextConfig.builder()
                .decoration(Decoration.UNDERLINE)
                .build();
        RichConsole.print("Статистика:\n\t", statConfig);
        long dispatcherTime = timer - calcOperationsTime;
        var stat = new ProcessorStatistics(execTimeStat, fromExecStat, fromStartStat,
                waitTimeStat, dispatcherTime, totalTime, calcOperationsTime, ioProcessor.getTime());
        printProcessorStatistics(stat);
        return stat;
//        todo delete
//        RichConsole.print("Время, затраченное на диспетчеризацию (ms): %d".formatted(dispatcherTime), statConfig);
//        RichConsole.print("Время от старта системы до завершения всех задач (ms): %d".formatted(totalTime), statConfig);
//        RichConsole.print("Общее время вычислительных операций (ms): %d".formatted(calcOperationsTime), statConfig);
//        RichConsole.print("%% Времени вычислительных операций от времени работы системы: %d".formatted((int) (((double) calcOperationsTime) / totalTime * 100)), statConfig);
//        RichConsole.print("Общее время IO операций (ms): %d".formatted(ioProcessor.getTime()), statConfig);
//        RichConsole.print("%% времени IO операций от времени работы системы: %d".formatted((int) (((double) ioProcessor.getTime()) / totalTime * 100)), statConfig);
//        printStat("Рабочее время задачи", execTimeStat, statConfig);
//        printStat("Время от начала старта системы до завершения задачи", fromStartStat, statConfig);
//        printStat("Время от начала старта задачи до завершения задачи", fromExecStat, statConfig);
//        printStat("Общее время ожидания IO операций", waitTimeStat, statConfig);
//        RichConsole.print("%% Среднего времени ожидания от времени работы системы: %d".formatted((int) (waitTimeStat.getAverage() / totalTime * 100)), statConfig);
    }

    public static void printProcessorStatistics(ProcessorStatistics stat) {
        RichConsole.print("Время, затраченное на диспетчеризацию (ms): %d".formatted(stat.dispatcherTime()), UNDERLINE_CONFIG);
        RichConsole.print("Время от старта системы до завершения всех задач (ms): %d".formatted(stat.totalTime()), UNDERLINE_CONFIG);
        RichConsole.print("Общее время вычислительных операций (ms): %d".formatted(stat.calcOperationsTime()), UNDERLINE_CONFIG);
        RichConsole.print("%% Времени вычислительных операций от времени работы системы: %d".formatted((int) (((double) stat.calcOperationsTime()) / stat.totalTime() * 100)), UNDERLINE_CONFIG);
        RichConsole.print("Общее время IO операций (ms): %d".formatted(stat.ioOperationsTime()), UNDERLINE_CONFIG);
        RichConsole.print("%% времени IO операций от времени работы системы: %d".formatted((int) (((double) stat.ioOperationsTime()) / stat.totalTime() * 100)), UNDERLINE_CONFIG);
        printStat("Рабочее время задачи", stat.execTimeStat(), UNDERLINE_CONFIG);
        printStat("Время от начала старта системы до завершения задачи", stat.fromStartStat(), UNDERLINE_CONFIG);
        printStat("Время от начала старта задачи до завершения задачи", stat.fromExecStat(), UNDERLINE_CONFIG);
        printStat("Общее время ожидания IO операций", stat.waitTimeStat(), UNDERLINE_CONFIG);
        RichConsole.print("%% Среднего времени ожидания от времени работы системы: %d".formatted((int) (stat.waitTimeStat().getAverage() / stat.totalTime() * 100)), UNDERLINE_CONFIG);

    }

    private static void printStat(String statName, LongSummaryStatistics stat, RichTextConfig config) {
        RichConsole.print(config,
                "Average %s (ms): %.2f".formatted(statName, stat.getAverage()),
                "Max %s (ms): %d".formatted(statName, stat.getMax()),
                "Min %s (ms): %d".formatted(statName, stat.getMin()),
                "Sum %s (ms): %d".formatted(statName, stat.getSum()));
    }

    protected ProcessorStatistics processTasksTraceable() {
        RichTextConfig richTextConfig = RichTextConfig.builder()
                .background(Background.BLACK)
                .color(Color.WHITE)
                .decoration(Decoration.BOLD)
                .build();
        String processorName = getClass().getSimpleName();
        printTasksBeforeStart();
        RichConsole.print(richTextConfig, "%s has started task processing...".formatted(processorName));
        ProcessorStatistics processorStatistics = processTasksWithStatistics();
        RichConsole.print(richTextConfig, "%s has finished task processing...".formatted(processorName));
        return processorStatistics;
    }

    protected void printTasksBeforeStart() {
        tasks.forEach(task -> {
            RichConsole.print("'%s' info".formatted(task.getName()), task.getDecoration());
            task.getOperations().forEach(operation -> {
                RichConsole.print("\tOperation '%s': type: %s, ioOperationsTime: %s".formatted(operation.getName(), operation.getType(), operation.getTime().toString()), task.getDecoration());
            });
        });
    }
}
