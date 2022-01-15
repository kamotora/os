package ru.kamotora.output;

import ru.kamotora.memory.PhysicalPage;
import ru.kamotora.memory.PrettyTable;

import java.util.List;
import java.util.Optional;

public class ConsolePrinter extends AbstractPrinter {

    private final static String RESET_ANSI = "\u001b[0m";
    private static final String EMPTY_STRING = "";

    @Override
    public void newLine() {
        System.out.println();
    }

    @Override
    public void print(String message, RichTextConfig config) {
        StringBuilder target = new StringBuilder();
        Optional.ofNullable(config)
                .ifPresent(self ->
                        target
                                // color
                                .append(Optional.ofNullable(self.getColor())
                                        .map(MyColor::getEscapeCode)
                                        .orElse(EMPTY_STRING))
                                // decoration
                                .append(Optional.ofNullable(self.getDecoration())
                                        .map(Decoration::getEscapeCode)
                                        .orElse(EMPTY_STRING))
                                // background
                                .append(Optional.ofNullable(self.getBackground())
                                        .map(Background::getEscapeCode)
                                        .orElse(EMPTY_STRING))
                );
        target.append(message)
                .append(RESET_ANSI);
        if (Optional.ofNullable(config)
                .map(RichTextConfig::isNewLine).orElse(true)) {
            target.append('\n');
        }
        System.out.print(target);
    }

    public void print(final List<PhysicalPage> virtualMemory) {
        print(RichTextConfig.metaMessageStyle(), "Memory usage:");
        for (var page : virtualMemory) {
            RichTextConfig rtc = RichTextConfig.builder()
                    .color(MyColor.BLACK)
                    .background(getBackgroundForPage(page))
                    .newLine(false)
                    .build();
            print(rtc, " %s |", page.address());
        }
        newLine();
    }

    @Override
    public void print(PrettyTable prettyTable) {
        print(prettyTable.toString(), (RichTextConfig) null);
    }
}
