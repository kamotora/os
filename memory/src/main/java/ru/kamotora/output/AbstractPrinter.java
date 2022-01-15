package ru.kamotora.output;

import ru.kamotora.memory.PhysicalPage;
import ru.kamotora.memory.PrettyTable;

import java.util.List;

public abstract class AbstractPrinter {
    public abstract void newLine();

    abstract void print(String message, RichTextConfig config);

    public void print(RichTextConfig config, String message, Object... substitutions){
        print(String.format(message, substitutions), config);
    }

    public void print(String message, Object... substitutions) {
        print(RichTextConfig.metaMessageStyle(), message, substitutions);
    }

    public abstract void print(final List<PhysicalPage> virtualMemory);

    public abstract void print(PrettyTable prettyTable);

    public void save() {
        // do nothing
    }

    protected Background getBackgroundForPage(PhysicalPage page) {
        return page.isUsed() ? Background.RED : Background.GREEN;
    }
}
