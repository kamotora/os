package task;

import lombok.*;

@Getter
@ToString
public class Operation {
    private final String name;
    private final String operationDescription;
    private final String interruptionDescription;
    private final DurationWrapper time;
    private final Type type;
    long executionTime;
    @Setter
    long waitingTime;
    @Setter
    long remainedTime;

    @Builder
    private Operation(Type type, String name, String operationDescription, String interruptionDescription, DurationWrapper time) {
        this.type = type;
        this.name = name;
        this.operationDescription = operationDescription;
        this.interruptionDescription = interruptionDescription;
        this.time = time;
        this.executionTime = time.getMillis();
        this.remainedTime = executionTime;
    }

    public long getTotalTime() {
        return executionTime + waitingTime;
    }

    public enum Type {
        CALCULATION, IO
    }

    /**
     * Выполнить операцию полностью
     *
     * @return время выполнения операции
     */
    @SneakyThrows
    public long proceedFully() {
        Thread.sleep(remainedTime);
        remainedTime = 0;
        return getTotalTime();
    }
}
