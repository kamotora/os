package processor;

import output.*;
import task.Task;

import java.util.List;

public abstract class AbstractTaskProcessor<T extends Task> {
    protected final List<T> tasks;

    protected AbstractTaskProcessor(List<T> tasks) {
        this.tasks = tasks;
    }

    abstract void processTasks();

    protected void processTasksTraceable() {
        RichTextConfig richTextConfig = RichTextConfig.builder()
                .background(Background.BLACK)
                .color(Color.WHITE)
                .decoration(Decoration.BOLD)
                .build();
        String processorName = getClass().getSimpleName();
        printTasksBeforeStart();
        RichConsole.print(richTextConfig, "%s has started task processing...".formatted(processorName));
        processTasks();
        RichConsole.print(richTextConfig, "%s has finished task processing...".formatted(processorName));
    }

    protected void printTasksBeforeStart() {
        tasks.forEach(task -> {
            RichConsole.print("'%s' info".formatted(task.getName()), task.getDecoration());
            task.getOperations().forEach(operation -> {
                RichConsole.print("\tOperation '%s': type: %s, time: %s".formatted(operation.getName(), operation.getType(), operation.getTime().toString()), task.getDecoration());
            });
        });
    }
}
