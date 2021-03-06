package ru.kamotora.output;

import java.awt.Color;

public enum Background implements AnsiEscapeCode, RgbColor {
    RED("\u001b[41m", Color.RED),
    GREEN("\u001b[42m", Color.GREEN);

    private final String ansi;
    private final Color color;

    Background(String ansi, Color color) {
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
        return Integer.toHexString(color.getRGB()).substring(2); // without alpha
    }
}
