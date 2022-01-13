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
@ToString
public class PageInfo {
    boolean isReadOnly;
    boolean isChanged;
    boolean wasSwapped;
    int physicalAddress;
    int virtualAddress;
    ProcessTable owner;

    boolean isAllocated() {
        return physicalAddress > -1;
    }
}
