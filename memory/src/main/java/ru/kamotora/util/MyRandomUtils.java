package ru.kamotora.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomUtils;

@UtilityClass
public class MyRandomUtils {
    public int nextInt(int max) {
        return RandomUtils.nextInt(0, max + 1);
    }
}
