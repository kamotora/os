package processor;

import output.RichConsole;
import task.Operation;
import task.Task;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class IOProcessor implements ITaskProcessor<Task> {
    protected final Queue<WaitingTask> ioQueue = new ConcurrentLinkedQueue<>();
    protected final AtomicInteger timer = new AtomicInteger();

    public IOProcessor() {
        processTasks();
    }

    public void add(Task task) {
        ioQueue.add(new WaitingTask(task, timer.get()));
    }

    public boolean contains(Task otherTask) {
        return ioQueue.stream().anyMatch(t -> t.task.equals(otherTask));
    }

    @SuppressWarnings("BusyWait")
    private void start() {
        while (true) {
            var waitingTask = ioQueue.peek();
            if (waitingTask == null) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }
            var task = waitingTask.task;
            if (task.isDone() || task.getCurrentOperation().getType() != Operation.Type.IO) {
                System.err.printf("Into IOProcessor was added NOT IO operation %s of task %s%n", task.getCurrentOperation(), task.getName());
            }
            var operation = task.getCurrentOperation();
            operation.setWaitingTime(timer.get() - waitingTask.startTime);
            operation.proceedFully();
            RichConsole.print("IO Operation %s of task %s was executed".formatted(operation.getName(), task.getName()), task.getDecoration() );
            timer.getAndAdd((int) operation.getBurstTime());
            task.nextOperation();
            ioQueue.poll();
        }
    }

    @Override
    public void processTasks() {

        Executors.newSingleThreadExecutor()
                .submit(this::start);

    }

    public static record WaitingTask(Task task, int startTime) {
    }

}
