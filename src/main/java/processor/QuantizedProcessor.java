package processor;

import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import task.DurationWrapper;
import task.Task;

@SuperBuilder
@Accessors(fluent = true)
public abstract class QuantizedProcessor<T extends Task> implements ITaskProcessor<T> {
    protected DurationWrapper quantum;
    protected long processingTime;
}
