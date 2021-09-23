package task;

import output.Color;
import output.RichTextConfig;

import java.util.List;
import java.util.UUID;

public class TaskFactory {
    public static Task fixedTask(String name, Color color, List<Operation> operations) {
        return Task.builder()
                .name(name)
                .decoration(RichTextConfig.builder().color(color).build())
                .operations(operations)
                .build();
    }

    public static Task randomTask(String name, Color color, int maxOperations, int percentOfIoOperations) {
        return Task.builder()
                .operations(OperationFactory.randomOperations(maxOperations, percentOfIoOperations))
                .name(name)
                .decoration(RichTextConfig.builder()
                        .color(color)
                        // add decorations here ...
                        .build())
                .build();
    }

    public static Task randomTask(int maxOperations, int percentOfIoOperations) {
        return randomTask("Task " + UUID.randomUUID(), Color.randomColor(), maxOperations, percentOfIoOperations);
    }
}
