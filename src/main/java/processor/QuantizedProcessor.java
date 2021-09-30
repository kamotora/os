package processor;

import task.DurationWrapper;
import task.Task;

import java.util.List;

public abstract class QuantizedProcessor<T extends Task> extends AbstractTaskProcessor<T> {
    protected DurationWrapper quantum;
    protected long processingTime;

    protected QuantizedProcessor(List<T> tasks) {
        super(tasks);
    }
}
