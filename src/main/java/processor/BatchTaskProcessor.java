package processor;

import lombok.Builder;
import lombok.Singular;
import output.RichConsole;
import task.Operation;
import task.Task;

import java.util.List;

public class BatchTaskProcessor extends AbstractTaskProcessor<Task> {

    @Builder
    public BatchTaskProcessor(@Singular List<Task> tasks) {
        super(tasks);
    }

    @Override
    public void processTasks(List<Task> processingTasks) {
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
