package task;

import exception.IOInterruptException;
import lombok.*;
import lombok.experimental.SuperBuilder;
import output.RichConsole;
import output.RichTextConfig;
import processor.BatchTaskProcessor;

import java.util.List;
import java.util.Optional;

@SuperBuilder
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Task {
    @Setter
    long start = 0L;
    @Setter
    long end = 0L;
    @Builder.Default
    Status status = Status.CREATED;
    String name;
    @Singular(value = "operation")
    List<Operation> operations;
    @Builder.Default
    int curOpIndex = 0;
    RichTextConfig decoration;

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
        curOpIndex++;
        try {
            return Optional.of(getCurrentOperation());
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    public Optional<Operation> prevOperation() {
        return curOpIndex - 1 >= 0 ? Optional.of(operations.get(curOpIndex - 1)) : Optional.empty();
    }


    public long getTotalExecutionTime() {
        return operations.stream().reduce(0L, (sum, oper) -> sum + oper.getExecutionTime(), Long::sum);
    }


    public long getTimeFromStartSystem() {
        return end - BatchTaskProcessor.startOfSystem;
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
        if(end == 0L){
            throw new RuntimeException("End time is zero");
        }
    }

    @Synchronized
    public void setStatus(Status status) {
        this.status = status;
    }


    public enum Status {
        CREATED, PROCESSING, IO_OPERATION, WAITING, ENDED
    }
}
