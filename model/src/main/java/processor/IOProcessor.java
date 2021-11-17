package processor;

import output.RichConsole;
import task.Operation;
import task.Task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Кол-во задач в единицу времени,
 * время задачи в системе (от начала работы задачи, от начала работы всей системы)
 */
//todo переделать на имитацию задержки (без Thread.sleep() и System.currentTimeMillis())
public class IOProcessor {
    // Общее время операций ввода вывода
    private final AtomicLong ioOperationsTimer = new AtomicLong(0L);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CompletableFuture<Operation> add(Task task) {
        RichConsole.print("'%s' interrupted for IO operation".formatted(task.getName()), task.getDecoration());
        WaitingTask waitingTask = new WaitingTask(task, System.currentTimeMillis());
        return CompletableFuture
                .supplyAsync(() -> start(waitingTask), executor);
    }

    private Operation start(WaitingTask waitingTask) {
        if (waitingTask.task.getStatus() != Task.Status.IO_OPERATION)
            System.err.println("ERROR! io queue contains not io operation!!!");
        var task = waitingTask.task;
        if (task.isDone() || task.getCurrentOperation().getType() != Operation.Type.IO) {
            System.err.printf("Into IOProcessor was added NOT IO operation %s of task %s%n", task.getCurrentOperation(), task.getName());
        }
        var operation = task.getCurrentOperation();
        operation.setWaitingTime(System.currentTimeMillis() - waitingTask.startTime);
        operation.proceedFully();
        var endTime = System.currentTimeMillis();
        RichConsole.print("Operation '%s' with type %s of task %s was executed"
                .formatted(operation.getName(), operation.getType(), task.getName()), task.getDecoration());
        ioOperationsTimer.getAndAdd(operation.getExecutionTime());
        if (task.nextOperation().isPresent())
            task.setStatus(Task.Status.WAITING);
        else
            task.endTask(endTime);
        return operation;
    }

    public long getTime() {
        return ioOperationsTimer.get();
    }

    public static record WaitingTask(Task task, long startTime) {
    }

}
