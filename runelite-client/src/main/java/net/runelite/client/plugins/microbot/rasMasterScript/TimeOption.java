package net.runelite.client.plugins.microbot.rasMasterScript;

enum TimerOptions {
    MIN_5("5min"),
    MIN_30_45("30-45mins");

    private final String label;

    TimerOptions(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}