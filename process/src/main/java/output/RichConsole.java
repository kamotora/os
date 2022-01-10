package output;

import java.util.Optional;

public class RichConsole {

    private final static String RESET_ANSI = "\u001b[0m";
    private static final String EMPTY_STRING = "";

    public static void newLine() {
        System.out.println();
    }

    public static void print(String message, RichTextConfig config) {
        StringBuilder target = new StringBuilder();
        Optional.ofNullable(config)
                .ifPresent(self ->
                        target
                                // color
                                .append(Optional.ofNullable(self.getColor())
                                        .map(Color::getEscapeCode)
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

    public static void print(RichTextConfig config, String... messages) {
        print("\n" + String.join("\n", messages), config);
    }
}
