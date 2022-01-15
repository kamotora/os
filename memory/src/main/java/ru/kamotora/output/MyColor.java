package ru.kamotora.output;

import java.awt.*;

public enum MyColor implements AnsiEscapeCode, RgbColor {
    BLACK("\u001b[30m", Color.BLACK);
    private final String ansi;
    private final Color color;

    MyColor(String ansi, Color color) {
        this.ansi = ansi;
        this.color = color;
    }

    @Override
    public String getEscapeCode() {
        return ansi;
    }


    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public String getRgbAsHex() {
        return Integer.toHexString(color.getRGB());
    }
}
