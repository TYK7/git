package com.example.coloranalyzer.service;

import java.util.Objects;

public class AnalyzedColor implements Comparable<AnalyzedColor> {
    private final String hexColor;
    private final String category;
    private final long frequency;

    public AnalyzedColor(String hexColor, String category, long frequency) {
        this.hexColor = hexColor;
        this.category = category;
        this.frequency = frequency;
    }

    public String getHexColor() {
        return hexColor;
    }

    public String getCategory() {
        return category;
    }

    public long getFrequency() {
        return frequency;
    }

    @Override
    public int compareTo(AnalyzedColor other) {
        // Sort by frequency descending
        return Long.compare(other.frequency, this.frequency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalyzedColor that = (AnalyzedColor) o;
        return frequency == that.frequency &&
               Objects.equals(hexColor, that.hexColor) &&
               Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hexColor, category, frequency);
    }

    @Override
    public String toString() {
        return "AnalyzedColor{" +
               "hexColor='" + hexColor + '\'' +
               ", category='" + category + '\'' +
               ", frequency=" + frequency +
               '}';
    }
}
