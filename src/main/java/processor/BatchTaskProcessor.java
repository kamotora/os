package processor;

import lombok.Builder;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import output.Decoration;
import output.RichConsole;
import output.RichTextConfig;
import task.Operation;
import task.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

@SuperBuilder
public class BatchTaskProcessor implements ITaskProcessor<Task> {
    public static long startOfSystem;
    @Singular
    protected List<Task> tasks;

    @Builder.Default
    private final IOProcessor ioProcessor = new IOProcessor();

    @Override
    public void processTasks() {
        startOfSystem = System.currentTimeMillis();
        ArrayList<Task> processingTasks = new ArrayList<>(this.tasks);
        long timer = System.currentTimeMillis();
        // processing
        while (!processingTasks.isEmpty()) {
            var iterator = processingTasks.iterator();
            while (iterator.hasNext()) {
                var task = iterator.next();
                if (task.getStatus() == Task.Status.CREATED) {
                    task.setStart(timer);
                    RichConsole.print("%s start execution".formatted(task.getName()), task.getDecoration());
                }
                timer += processTask(task);
                if (task.isDone()) {
                    task.setEnd(timer);
                    iterator.remove();
                }
            }
        }
        // output stats
        RichConsole.print("Tasks performed:\n", null);
        RichConsole.print("Tasks per second: %.3f".formatted(Math.max(ioProcessor.getTime() - startOfSystem, timer - startOfSystem) * 0.001 / this.tasks.size()), null);
        for (Task task : this.tasks) {
            long ioTime = task.getOperations().stream()
                    .filter(oper -> oper.getType() == Operation.Type.IO)
                    .mapToLong(Operation::getExecutionTime)
                    .sum();
            long calcTime = task.getOperations().stream()
                    .filter(oper -> oper.getType() != Operation.Type.IO)
                    .mapToLong(Operation::getExecutionTime)
                    .sum();
            long waitingTime = task.getOperations().stream()
                    .mapToLong(Operation::getWaitingTime)
                    .sum();
            RichConsole.print(task.getName() +
                            ":\n\t Execution time (ms): " + task.getTotalExecutionTime() +
                            "\n\t Time from start system (ms): " + task.getTimeFromStartSystem() +
                            "\n\t Time from start execution (ms): " + task.getTimeFromStartExecution() +
                            "\n\t Summary time of calculation operations (ms): " + calcTime +
                            "\n\t Summary time of IO operations (ms): " + ioTime +
                            "\n\t Summary time of waiting (ms): " + waitingTime,
                    task.getDecoration());
        }
        LongSummaryStatistics execTimeStat = this.tasks.stream()
                .mapToLong(Task::getTotalExecutionTime)
                .summaryStatistics();
        LongSummaryStatistics fromExecStat = this.tasks.stream()
                .mapToLong(Task::getTimeFromStartExecution)
                .summaryStatistics();
        LongSummaryStatistics fromStartStat = this.tasks.stream()
                .mapToLong(Task::getTimeFromStartSystem)
                .summaryStatistics();
        LongSummaryStatistics waitTimeStat = this.tasks.stream()
                .mapToLong(Task::getWaitingTime)
                .summaryStatistics();
        RichTextConfig statConfig = RichTextConfig.builder()
                .decoration(Decoration.UNDERLINE)
                .build();
        RichConsole.print("Statistic:\n\t", statConfig);
        printStat("total execution time", execTimeStat, statConfig);
        printStat("time from start execution", fromExecStat, statConfig);
        printStat("time from start system", fromStartStat, statConfig);
        printStat("waiting time", waitTimeStat, statConfig);
    }

    private void printStat(String statName, LongSummaryStatistics stat, RichTextConfig config) {
        RichConsole.print(config,
                "Average %s (ms): %.2f".formatted(statName, stat.getAverage()),
                "Max %s (ms): %d".formatted(statName, stat.getMax()),
                "Min %s (ms): %d".formatted(statName, stat.getMin()));
    }

    private long processTask(Task task) {
        long timeOfTask = System.currentTimeMillis();
        if (task.getStatus() == Task.Status.ENDED || task.getStatus() == Task.Status.IO_OPERATION)
            return 0L;
        task.setStatus(Task.Status.PROCESSING);
        var operation = task.getCurrentOperation();
        while (true) {
            if (operation.getType() == Operation.Type.IO) {
                task.setStatus(Task.Status.IO_OPERATION);
                ioProcessor.add(task);
                return System.currentTimeMillis() - timeOfTask;
            } else {
                operation.proceedFully();
                var optionalOperation = task.nextOperation();
                if (optionalOperation.isPresent()) {
                    operation = optionalOperation.get();
                } else {
                    // all tasks processed
                    task.setStatus(Task.Status.ENDED);
                    return System.currentTimeMillis() - timeOfTask;
                }
            }
        }
    }

}
