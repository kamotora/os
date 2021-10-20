package processor;

import lombok.Builder;
import lombok.Singular;
import output.Decoration;
import output.RichConsole;
import output.RichTextConfig;
import task.DurationWrapper;
import task.Operation;
import task.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

public class RoundRobinTaskProcessor extends QuantizedProcessor<Task> {

    @Builder
    public RoundRobinTaskProcessor(@Singular List<Task> tasks, DurationWrapper quantum) {
        super(tasks, quantum);
    }

    @Override
    protected void processTasks(List<Task> processingTasks) {
        // processing
        while (!processingTasks.isEmpty()) {
            var iterator = processingTasks.iterator();
            while (iterator.hasNext()) {
                var task = iterator.next();
                if (task.getStatus() == Task.Status.CREATED) {
                    task.setStart(System.currentTimeMillis());
                    RichConsole.print("%s start execution".formatted(task.getName()), task.getDecoration());
                }
                timer += processTask(task);
                if (task.isDone()) {
                    iterator.remove();
                }
            }
        }
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
        if (operation.getType() == Operation.Type.IO) {
            task.setStatus(Task.Status.IO_OPERATION);
            ioProcessor.add(task);
        } else {
            var workingTime = operation.proceed(quantum);
            RichConsole.print("Operation '%s' with type '%s' of task %s was worked %d mills"
                    .formatted(operation.getName(), operation.getType(), task.getName(), workingTime), task.getDecoration());
            if (operation.isEnded()) {
                var nextOperation = task.nextOperation();
                if (nextOperation.isEmpty())
                    task.setStatus(Task.Status.ENDED);
            }
        }
        return System.currentTimeMillis() - startTime;

    }
}
