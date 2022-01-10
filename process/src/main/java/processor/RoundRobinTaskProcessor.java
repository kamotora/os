package processor;

import lombok.Builder;
import lombok.Singular;
import output.RichConsole;
import task.DurationWrapper;
import task.Operation;
import task.Task;

import java.util.List;

public class RoundRobinTaskProcessor extends QuantizedProcessor<Task> {

    // Неизрасходованное время прошлой операции
    private long additionalTime = 0L;

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
            var workingTime = operation.proceed(quantum.getMillis() + additionalTime);
            additionalTime += quantum.getMillis() - workingTime;
//            RichConsole.print("Operation '%s' with type '%s' of task %s was worked %d mills"
//                    .formatted(operation.getName(), operation.getType(), task.getName(), workingTime), task.getDecoration());
            if (operation.isEnded()) {
                RichConsole.print("Operation '%s' with type '%s' of task %s was ended"
                        .formatted(operation.getName(), operation.getType(), task.getName()), task.getDecoration());
                var nextOperation = task.nextOperation();
                if (nextOperation.isEmpty()) {
                    task.endTask(System.currentTimeMillis());
                }
            }
        }
        return System.currentTimeMillis() - startTime;

    }
}
