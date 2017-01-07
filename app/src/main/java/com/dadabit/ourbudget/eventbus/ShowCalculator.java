package com.dadabit.ourbudget.eventbus;

/**
 * EventBus define an event
 */

public class ShowCalculator {
    private final boolean isVisible;

    public ShowCalculator(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public boolean isVisible() {
        return isVisible;
    }
}
