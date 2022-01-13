package ru.kamotora.memory.printer;

import ru.kamotora.memory.PageInfo;
import ru.kamotora.memory.PhysicalPage;
import ru.kamotora.output.Background;
import ru.kamotora.output.Color;
import ru.kamotora.output.RichConsole;
import ru.kamotora.output.RichTextConfig;

import java.util.List;

public class PictureMemoryPrinter {
    public void print(final List<PhysicalPage> virtualMemory) {
        RichConsole.print(RichTextConfig.metaMessageStyle(), "Memory usage:");
        for (var page : virtualMemory) {
            RichTextConfig rtc = RichTextConfig.builder()
                    .color(Color.BLACK)
                    .background(getBackgroundForPage(page))
                    .newLine(false)
                    .build();
            RichConsole.print(rtc, " %s |", page.address());
        }
        RichConsole.newLine();
    }

    private Background getBackgroundForPage(PhysicalPage page){
        return page.isUsed() ? Background.RED : Background.GREEN;
    }
}
