package task;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

@UtilityClass
public class OperationFactory {

    private static final SplittableRandom splittableRandom = new SplittableRandom();

    public static Operation defaultOperation(DurationWrapper time) {
        return Operation.builder()
                .type(Operation.Type.CALCULATION)
                .name("Some calculations...")
                .time(time)
                .interruptionDescription("Waiting for a wonder..")
                .operationDescription("I am a chunk of bigger task, I take " + time.toString() + "...")
                .build();
    }

    public static Operation networkOperation(DurationWrapper time) {
        return Operation.builder()
                .type(Operation.Type.IO)
                .name("Network operation")
                .time(time)
                .interruptionDescription("Waiting for a remote host...")
                .build();
    }

    public static Operation calculationOperation(DurationWrapper time) {
        return Operation.builder()
                .type(Operation.Type.CALCULATION)
                .name("Calculation operation")
                .time(time)
                .interruptionDescription("Requesting for extra data...")
                .build();
    }

    public static Operation guiOperation(DurationWrapper time) {
        return Operation.builder()
                .type(Operation.Type.IO)
                .name("GUI operation")
                .time(time)
                .interruptionDescription("Waiting for an user input...")
                .build();
    }

    public static Operation calculationOperation() {
        return calculationOperation(100, 10000);
    }

    public static Operation ioOperation() {
        return ioOperation(100, 1000);
    }

    public static Operation calculationOperation(long start, long end) {
        return switch (RandomUtils.nextInt(0, 1)) {
            case 0 -> calculationOperation(DurationWrapper.millis(RandomUtils.nextLong(start, end)));
            default -> defaultOperation(DurationWrapper.millis(RandomUtils.nextLong(start, end)));
        };
    }


    public static Operation ioOperation(long start, long end) {
        return switch (RandomUtils.nextInt(0, 1)) {
            case 0 -> networkOperation(DurationWrapper.millis(RandomUtils.nextLong(start, end)));
            default -> guiOperation(DurationWrapper.millis(RandomUtils.nextLong(start, end)));
        };
    }

    public static List<Operation> randomOperations(int maxOperationsCount, int percentOfIoOperations) {
        return fixedCountRandomOperations(RandomUtils.nextInt(maxOperationsCount / 2, maxOperationsCount), percentOfIoOperations);
    }

    public static List<Operation> fixedCountRandomOperations(int operationsCount, int percentOfIoOperations) {
        return Stream.generate(() -> probabilitySupplier(percentOfIoOperations, OperationFactory::ioOperation, OperationFactory::calculationOperation))
                .limit(operationsCount)
                .toList();
    }

    //Вероятность не точная, т.к. рандом
    private  <T> T probabilitySupplier(int probability, Supplier<T> good, Supplier<T> bad) {
        return splittableRandom.nextInt(1, 101) <= probability ? good.get() : bad.get();
    }
}
