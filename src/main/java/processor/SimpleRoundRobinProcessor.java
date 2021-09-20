package processor;

import exception.IOInterruptException;
import lombok.Builder;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import output.Decoration;
import output.RichConsole;
import output.RichTextConfig;
import task.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

@SuperBuilder
public class SimpleRoundRobinProcessor implements ITaskProcessor<Task> {

    @Singular
    protected List<Task> tasks;

    @Builder.Default
    private final IOProcessor ioProcessor = new IOProcessor();

    @Override
    public void processTasks() {
        List<Task> processingTasks = new ArrayList<>(tasks);
        // processing
        while (!processingTasks.isEmpty()) {
            for (var iter = processingTasks.iterator(); iter.hasNext(); ) {
                Task currentTask = iter.next();
                try {
                    RichConsole.print("%s start execution".formatted(currentTask.getName()), currentTask.getDecoration());
                    currentTask.proceed();
                    iter.remove();
                } catch (IOInterruptException e) {
                    if (!ioProcessor.contains(currentTask)) {
                        RichConsole.print("%s was interrupted. Cause %s".formatted(currentTask.getName(), currentTask.getCurrentOperation().getInterruptionDescription()), currentTask.getDecoration());
                        ioProcessor.add(e.getTask());
                    }
                }
            }
        }
        // output stats
        RichConsole.print("Tasks performed:\n", null);
        for (Task roundRobinTask : this.tasks) {
            RichConsole.print(roundRobinTask.getName() +
                            ":\n\t Burst time (ms): " + roundRobinTask.getTotalBurstTime() +
                            "\n\t Waiting time (ms): " + roundRobinTask.getTotalWaitingTime() +
                            "\n\t Turn around time (ms): " + roundRobinTask.getTotalTime(),
                    roundRobinTask.getDecoration());
        }
        LongSummaryStatistics waitingTimeStat = this.tasks.stream()
                .mapToLong(Task::getTotalWaitingTime)
                .summaryStatistics();
        LongSummaryStatistics turnAroundStat = this.tasks.stream()
                .mapToLong(Task::getTotalTime)
                .summaryStatistics();
        RichConsole.print("Statistic:\n\t Average waiting time (ms): " + Math.round(waitingTimeStat.getAverage()) +
                        "\n\t Max waiting time (ms): " + Math.round(waitingTimeStat.getMax()) +
                        "\n\t Min waiting time (ms): " + Math.round(waitingTimeStat.getMin()) +
                        "\n\t Average turn around time (ms): " + Math.round(turnAroundStat.getAverage()) +
                        "\n\t Max turn around time (ms): " + Math.round(turnAroundStat.getMax()) +
                        "\n\t Min turn around time (ms): " + Math.round(turnAroundStat.getMin()),
                RichTextConfig.builder()
                        .decoration(Decoration.UNDERLINE)
                        .build());

    }

}
