package task;

import output.Color;
import output.RichTextConfig;

public class TaskFactory {
    public static Task randomTask(Color color, int maxOperations, int percentOfIoOperations) {
        return Task.builder()
                .operations(OperationFactory.randomOperations(maxOperations, percentOfIoOperations))
                .name("Random task")
                .decoration(RichTextConfig.builder()
                        .color(color)
                        // add decorations here ...
                        .build())
                .build();
    }
}
