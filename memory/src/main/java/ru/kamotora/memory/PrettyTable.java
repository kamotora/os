package ru.kamotora.memory;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class PrettyTable {
    private final List<String> headers = new ArrayList<>();
    private final List<List<String>> data = new ArrayList<>();

    public PrettyTable(String... headers) {
        this.headers.addAll(Arrays.asList(headers));
    }

    public void addRow(String... row) {
        data.add(Arrays.asList(row));
    }

    public void addRow(Object... row) {
        data.add(Stream.of(row)
                .map(Object::toString)
                .collect(Collectors.toList()));
    }

    private int getMaxSize(int column) {
        int maxSize = headers.get(column).length();
        for (List<String> row : data) {
            if (row.get(column).length() > maxSize)
                maxSize = row.get(column).length();
        }
        return maxSize;
    }

    private String formatRow(List<String> row) {
        StringBuilder result = new StringBuilder();
        result.append("|");
        for (int i = 0; i < row.size(); i++) {
            result.append(StringUtils.center(row.get(i), getMaxSize(i) + 2));
            result.append("|");
        }
        result.append("\n");
        return result.toString();
    }

    private String formatRule() {
        StringBuilder result = new StringBuilder();
        result.append("+");
        for (int i = 0; i < headers.size(); i++) {
            result.append("-".repeat(Math.max(0, getMaxSize(i) + 2)));
            result.append("+");
        }
        result.append("\n");
        return result.toString();
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(formatRule());
        result.append(formatRow(headers));
        result.append(formatRule());
        for (List<String> row : data) {
            result.append(formatRow(row));
        }
        result.append(formatRule());
        result.append('\n');
        return result.toString();
    }

}
