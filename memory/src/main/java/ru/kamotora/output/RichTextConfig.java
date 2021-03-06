package ru.kamotora.output;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RichTextConfig {
    private final MyColor color;
    private final Decoration decoration;
    private final Background background;
    @Builder.Default
    private final boolean newLine = true;

    public static RichTextConfig metaMessageStyle() {
        return RichTextConfig.builder()
                .decoration(Decoration.UNDERLINE)
                .build();
    }
}
