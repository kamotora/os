package ru.kamotora.memory;


import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Page {
    int address;
    boolean isOccupied;
    int addingTime;

    @Builder
    @Getter
    @Setter
    @EqualsAndHashCode
    @Accessors(fluent = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Info {
        boolean isReadOnly;
        boolean isChanged;
        boolean isShown;
        int address;
    }
}

