package processor;

import lombok.Singular;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import output.*;
import task.Task;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
public interface ITaskProcessor<T extends Task> {

    void processTasks();

    default void processTasksTraceable() {
        RichTextConfig richTextConfig = RichTextConfig.builder()
                .background(Background.BLACK)
                .color(Color.WHITE)
                .decoration(Decoration.BOLD)
                .build();
        String processorName = getClass().getSimpleName();
        RichConsole.print(richTextConfig, "%s has started task processing...", processorName);
        processTasks();
        RichConsole.print(richTextConfig, "%s has finished task processing...", processorName);
    }
}
