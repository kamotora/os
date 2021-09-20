package task;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Operation {
    private final String name;
    private final String operationDescription;
    private final String interruptionDescription;
    private final DurationWrapper time;
    private final Type type;
    long burstTime;
    //    long turnAroundTime;
    @Setter
    long waitingTime;
    @Setter
    long remainedBurstTime;

    @Builder
    private Operation(Type type, String name, String operationDescription, String interruptionDescription, DurationWrapper time) {
        this.type = type;
        this.name = name;
        this.operationDescription = operationDescription;
        this.interruptionDescription = interruptionDescription;
        this.time = time;
        this.burstTime = time.getMillis();
        this.remainedBurstTime = burstTime;
    }

    public long getTotalTime() {
        return burstTime + waitingTime;
    }

    public static enum Type {
        CALCULATION, IO
    }

    /**
     * Выполнить операцию полностью
     *
     * @return время выполнения операции
     */
    public long proceedFully() {
        remainedBurstTime = 0;
        return getTotalTime();
    }
}
