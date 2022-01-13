package ru.kamotora.memory;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Process {
    String label;
    int lifetime;
    List<VirtualPage> virtualPages;

    @Builder
    @Getter
    @Setter
    @EqualsAndHashCode
    @Accessors(fluent = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class VirtualPage {
        int address;
        boolean isReadOnly;
        boolean isChanged;
    }
}
