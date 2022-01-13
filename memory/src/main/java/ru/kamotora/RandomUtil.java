package ru.kamotora;

import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Supplier;

@UtilityClass
public class RandomUtil {

    private static final SplittableRandom splittableRandom = new SplittableRandom();


    public int nextInt(int maxExclusive) {
        return RandomUtils.nextInt(0, maxExclusive);
    }

    //Вероятность не точная, т.к. рандом
    private <T> T probabilitySupplier(int probability, Supplier<T> good, Supplier<T> bad) {
        return splittableRandom.nextInt(1, 101) <= probability ? good.get() : bad.get();
    }

    public boolean roll() {
        return roll(0.5);
    }

    public boolean roll(double probability) {
        return Math.random() < probability;
    }

    public <T> T choiceElement(List<T> options) {
        return options.get((int) (Math.random() * options.size()));
    }

    /**
     * Вернуть случайный элемент из массива и его индекс
     */
    public <T> Pair<Integer, T> choice(List<T> options) {
        int index = nextInt(options.size());
        return Pair.of(index, options.get(index));
    }

    public <T> T choice(T[] options) {
        return options[(int) (Math.random() * options.length)];
    }
}
