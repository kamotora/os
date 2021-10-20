package processor;

import output.*;
import task.Operation;
import task.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

public abstract class AbstractTaskProcessor<T extends Task> {
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
        // todo перенести вывод статистики
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
                            "\n\t Общее время IO задач (ms): " + ioTime +
                            "\n\t Общее время ожидания (ms): " + waitingTime,
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
        RichConsole.print("Время, затраченное на диспетчеризацию (ms): %d".formatted(dispatcherTime), statConfig);
        RichConsole.print("Время от старта системы до завершения всех задач (ms): %d".formatted(totalTime), statConfig);
        RichConsole.print("Общее время вычислительных операций (ms): %d".formatted(calcOperationsTime), statConfig);
        RichConsole.print("%% Времени вычислительных операций от времени работы системы: %d".formatted((int) (((double) calcOperationsTime) / totalTime * 100)), statConfig);
        RichConsole.print("Общее время IO операций (ms): %d".formatted(ioProcessor.getTime()), statConfig);
        RichConsole.print("%% времени IO операций от времени работы системы: %d".formatted((int) (((double) ioProcessor.getTime()) / totalTime * 100)), statConfig);
        printStat("Рабочее время задачи", execTimeStat, statConfig);
        printStat("Время от начала старта системы до завершения задачи", fromStartStat, statConfig);
        printStat("Время от начала старта задачи до завершения задачи", fromExecStat, statConfig);
        printStat("Общее время ожидания задачи", waitTimeStat, statConfig);
        RichConsole.print("%% Среднего времени ожидания от времени работы системы: %d".formatted((int) (waitTimeStat.getAverage() / totalTime * 100)), statConfig);
        return new ProcessorStatistics(execTimeStat, fromExecStat, fromStartStat, waitTimeStat, dispatcherTime, totalTime, calcOperationsTime, ioProcessor.getTime());

    }

    private void printStat(String statName, LongSummaryStatistics stat, RichTextConfig config) {
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
