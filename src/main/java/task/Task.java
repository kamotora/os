package task;

import exception.CurrentOperationNotEndedException;
import exception.IOInterruptException;
import lombok.*;
import output.RichConsole;
import output.RichTextConfig;
import processor.BatchTaskProcessor;

import java.util.List;
import java.util.Optional;

@Getter
@ToString
@EqualsAndHashCode
public class Task {
    // timestamp начала обработки
    @Setter
    private long start = 0L;
    // timestamp окончания обработки
    @Setter
    private long end = 0L;

    private Status status = Status.CREATED;
    int curOpIndex = 0;
    private final String name;
    private final List<Operation> operations;
    private final RichTextConfig decoration;

    @Builder
    public Task(String name, List<Operation> operations, RichTextConfig decoration) {
        this.name = name;
        this.operations = operations;
        this.decoration = decoration;
    }

    public boolean isDone() {
        // all parts were performed
        return status == Status.ENDED;
    }

    private boolean proceed(long time) {
        // operation already ended
        if (isDone())
            return true;
        if ((getCurrentOperation().remainedTime -= time) <= 0) {
            // operation is done
            curOpIndex++;
        }
        return isDone();
    }

    public void proceed() {
        while (!isDone()) {
            var operation = getCurrentOperation();
            if (operation.getType() == Operation.Type.IO)
                throw new IOInterruptException(this);
            operation.proceedFully();
            RichConsole.print("Operation %s of task %s was executed".formatted(operation.getName(),
                    this.getName()), this.getDecoration());
            curOpIndex++;
        }
    }

    public Operation getCurrentOperation() {
        return operations.get(curOpIndex);
    }

    public Optional<Operation> nextOperation() {
        if(!getCurrentOperation().isEnded())
            throw new CurrentOperationNotEndedException(this);
        curOpIndex++;
        try {
            return Optional.of(getCurrentOperation());
        } catch (IndexOutOfBoundsException e) {
            curOpIndex--;
            return Optional.empty();
        }
    }

    public Optional<Operation> prevOperation() {
        return curOpIndex - 1 >= 0 ? Optional.of(operations.get(curOpIndex - 1)) : Optional.empty();
    }


    public long getTotalExecutionTime() {
        return operations.stream().reduce(0L, (sum, oper) -> sum + oper.getExecutionTime(), Long::sum);
    }

    public long getTimeFromStartExecution() {
        checkIfDone();
        return end - start;
    }

    public long getWaitingTime() {
        return operations.stream()
                .filter(oper -> oper.getType() == Operation.Type.IO)
                .mapToLong(Operation::getWaitingTime)
                .sum();
    }

    private void checkIfDone() {
        if (!isDone()) {
            throw new RuntimeException("Operation is not done");
        }
        if (end == 0L) {
            throw new RuntimeException("End ioOperationsTime is zero");
        }
    }

    public void endTask(long endTime) {
        if(getEnd() == 0L && !isDone()){
            setEnd(endTime);
            setStatus(Status.ENDED);
        }
        else
            System.err.printf("Error to end task '%s'. Task already ended%n", getName());

        RichConsole.print("'%s' ended".formatted(this.getName()), this.getDecoration());
    }

    @Synchronized
    public void setStatus(Status status) {
        this.status = status;
    }


    public enum Status {
        CREATED, PROCESSING, IO_OPERATION, WAITING, ENDED
    }
}
