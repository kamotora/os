package exception;

import lombok.Getter;
import task.Task;

@Getter
public class IOInterruptException extends RuntimeException {
    private final Task task;

    public IOInterruptException(Task task) {
        this.task = task;
    }
}
