package processor;

import lombok.Builder;
import task.DurationWrapper;
import task.Task;

import java.util.List;

public abstract class QuantizedProcessor<T extends Task> extends AbstractTaskProcessor<T> {

    public static DurationWrapper DEFAULT_QUANTUM = DurationWrapper.millis(150);
    @Builder.Default
    protected DurationWrapper quantum = DEFAULT_QUANTUM;
    protected long processingTime;

    protected QuantizedProcessor(List<T> tasks, DurationWrapper tick) {
        super(tasks);
        if (tick != null)
            this.quantum = tick;
    }
}
