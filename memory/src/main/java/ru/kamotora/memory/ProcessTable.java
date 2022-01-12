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
public class ProcessTable {
    String label;
    List<Page.Info> pagesInfo;
}
