package task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationFactoryTest {

    @Test
    void randomOperationsTest() {
        int countOperations = 100;
        int percentIoOperations = 30;
        var operations = OperationFactory.fixedCountRandomOperations(countOperations, percentIoOperations);
        assertEquals(countOperations, operations.size());
        var countIoOperations = operations.stream()
                .filter(operation -> operation.getType() == Operation.Type.IO)
                .count();
        assertEquals(percentIoOperations, countIoOperations);
    }
}