package ru.kamotora.util;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true, prefix = "")
public class IntRange {
    int origin;
    int bound;

    public int getRandom() {
        return origin + (int) (Math.random() * (bound - origin));
    }
}