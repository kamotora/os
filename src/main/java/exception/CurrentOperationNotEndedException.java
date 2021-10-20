package exception;

import task.Task;

public class CurrentOperationNotEndedException extends RuntimeException {
    private final Task task;

    public CurrentOperationNotEndedException(Task task) {
        super("Operation not allowed, because current operation %s of task %s not ended"
                .formatted(task.getCurrentOperation().getName(), task.getName()));
        this.task = task;
    }
}
