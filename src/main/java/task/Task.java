package task;

import exception.IOInterruptException;
import lombok.*;
import lombok.experimental.SuperBuilder;
import output.RichConsole;
import output.RichTextConfig;

import java.util.List;
import java.util.Optional;

@SuperBuilder
@Getter
@NoArgsConstructor
@ToString
public class Task {
    String name;
    @Singular(value = "operation")
    List<Operation> operations;
    @Builder.Default
    int curOpIndex = 0;
    RichTextConfig decoration;

    public boolean isDone() {
        // all parts were performed
        return curOpIndex >= operations.size();
    }

    private boolean proceed(long time) {
        // operation already ended
        if (isDone())
            return true;
        if ((getCurrentOperation().remainedBurstTime -= time) <= 0) {
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


    public long getTotalBurstTime() {
        return operations.stream().reduce(0L, (sum, oper) -> sum + oper.getBurstTime(), Long::sum);
    }


    public long getTotalWaitingTime() {
        return operations.stream().reduce(0L, (sum, oper) -> sum + oper.getWaitingTime(), Long::sum);
    }


    public long getTotalTime() {
        return operations.stream().reduce(0L, (sum, oper) -> sum + oper.getTotalTime(), Long::sum);
    }
}
