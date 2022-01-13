package ru.kamotora.memory;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.kamotora.util.IntRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@SuperBuilder
@Getter
@Accessors(fluent = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public abstract class VirtualMemoryManager implements Consumer<List<Process>> {

    List<String> labels = Arrays.asList("Browser Page", "Game", "Background Task");

    @Builder.Default
    int lifetime = 30;
    @Builder.Default
    int memorySize = 64 * 1024;
    @Builder.Default
    int pageSize = 4 * 1024;
    @Builder.Default
    int processesLimit = 7;
    @Builder.Default
    float processCreationProbability = .35f;
    @Builder.Default
    float readOnlyPageProbability = .3f;
    /**
     * Если свободно больше данного значения, чем процессу нужно, выделяем процессу всю память
     */
    @Builder.Default
    float memoryOccupiedThreshold = .6f;
    @Builder.Default
    int minProcessPages = 3;
    @Builder.Default
    int minProcessLifetime = 5;

    int pagesCount() {
        return memorySize / pageSize;
    }

    int randomPagesCount() {
        return new IntRange(minProcessPages, pagesCount()).getRandom();
    }

    int randomLifetime() {
        return new IntRange(minProcessLifetime, lifetime).getRandom();
    }

    public final void manage() {
        accept(new ArrayList<>());
    }
}
