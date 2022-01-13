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
public class PhysicalPage {
    int address;
    /**
     * Занято
     */
    boolean isUsed;
    int lastAllocationTime;
}

