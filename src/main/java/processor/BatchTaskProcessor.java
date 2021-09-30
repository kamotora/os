package processor;

import lombok.Builder;
import lombok.Singular;
import output.Decoration;
import output.RichConsole;
import output.RichTextConfig;
import task.Operation;
import task.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

public class BatchTaskProcessor extends AbstractTaskProcessor<Task> {
    public static long startOfSystemTime;

    private final IOProcessor ioProcessor = new IOProcessor();

    @Builder
    public BatchTaskProcessor(@Singular List<Task> tasks) {
        super(tasks);
    }

    @Override
    public void processTasks() {
        startOfSystemTime = System.currentTimeMillis();
        ArrayList<Task> processingTasks = new ArrayList<>(this.tasks);
        // Общее время на обработку операций НЕ ввода/вывода
        long batchTimer = 0L;
        // processing
        while (!processingTasks.isEmpty()) {
            var iterator = processingTasks.iterator();
            while (iterator.hasNext()) {
                var task = iterator.next();
                if (task.getStatus() == Task.Status.CREATED) {
                    task.setStart(System.currentTimeMillis());
                    RichConsole.print("%s start execution".formatted(task.getName()), task.getDecoration());
                }
                batchTimer += processTask(task);
                if (task.isDone()) {
                    iterator.remove();
                }
            }
        }
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
                            "\n\t Время от начала старта системы до завершения задачи (ms): " + task.getTimeFromStartSystem() +
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
                .mapToLong(Task::getTimeFromStartSystem)
                .summaryStatistics();
        LongSummaryStatistics waitTimeStat = this.tasks.stream()
                .mapToLong(Task::getWaitingTime)
                .summaryStatistics();
        RichTextConfig statConfig = RichTextConfig.builder()
                .decoration(Decoration.UNDERLINE)
                .build();
        RichConsole.print("Статистика:\n\t", statConfig);
        RichConsole.print("Время, затраченное на диспетчеризацию (ms): %d".formatted(batchTimer - calcOperationsTime), statConfig);
        RichConsole.print("Время от старта системы до завершения всех задач (ms): %d".formatted(totalTime), statConfig);
        RichConsole.print("Общее время вычислительных операций (ms): %d".formatted(calcOperationsTime), statConfig);
        RichConsole.print("Общее время IO операций (ms): %d".formatted(ioProcessor.getTime()), statConfig);
        printStat("Рабочее время задачи", execTimeStat, statConfig);
        printStat("Время от начала старта системы до завершения задачи", fromExecStat, statConfig);
        printStat("Время от начала старта задачи до завершения задачи", fromStartStat, statConfig);
        printStat("Общее время ожидания задачи", waitTimeStat, statConfig);
    }

    private void printStat(String statName, LongSummaryStatistics stat, RichTextConfig config) {
        RichConsole.print(config,
                "Average %s (ms): %.2f".formatted(statName, stat.getAverage()),
                "Max %s (ms): %d".formatted(statName, stat.getMax()),
                "Min %s (ms): %d".formatted(statName, stat.getMin()),
                "Sum %s (ms): %d".formatted(statName, stat.getSum()));
    }

    private long processTask(Task task) {
        long startTime = System.currentTimeMillis();
        if (task.getStatus() == Task.Status.ENDED || task.getStatus() == Task.Status.IO_OPERATION)
            return 0L;
        task.setStatus(Task.Status.PROCESSING);
        var operation = task.getCurrentOperation();
        while (true) {
            if (operation.getType() == Operation.Type.IO) {
                task.setStatus(Task.Status.IO_OPERATION);
                ioProcessor.add(task);
                return System.currentTimeMillis() - startTime;
            } else {
                operation.proceedFully();
                RichConsole.print("Operation '%s' with type '%s' of task %s was executed"
                        .formatted(operation.getName(), operation.getType(), task.getName()), task.getDecoration());
                var optionalOperation = task.nextOperation();
                if (optionalOperation.isPresent()) {
                    operation = optionalOperation.get();
                } else {
                    // all tasks processed
                    long endTime = System.currentTimeMillis();
                    task.endTask(endTime);
                    return endTime - startTime;
                }
            }
        }
    }

}
