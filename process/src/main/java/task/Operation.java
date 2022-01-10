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
    private final Task parentTask;
    long executionTime;
    @Setter
    long waitingTime;
    @Setter
    long remainedTime;

    @Builder(toBuilder = true)
    private Operation(Type type, String name, String operationDescription, String interruptionDescription, DurationWrapper time, Task parentTask) {
        this.type = type;
        this.name = name;
        this.operationDescription = operationDescription;
        this.interruptionDescription = interruptionDescription;
        this.time = time;
        this.executionTime = time.getMillis();
        this.remainedTime = executionTime;
        this.parentTask = parentTask;
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
        return executionTime;
    }

    /**
     * Выполнять операцию заданное кол-во времени
     *
     * @param quantum квант времени (в мс)
     * @return время выполнения операции (минимум от {@code quantum} и  {@code remainedTime}) в мс
     */
    @SneakyThrows
    public long proceed(long quantum) {
        long workingTime = Math.min(quantum, remainedTime);
        Thread.sleep(workingTime);
        remainedTime -= workingTime;
        return workingTime;
    }

    public boolean isEnded() {
        return remainedTime <= 0;
    }

    @Override
    public Operation clone() {
        return this.toBuilder()
                .build();
    }
}
