package org.pungrammer.arbitrarydiceinstructor.config;

import java.util.List;
import java.util.stream.Collectors;

public class Config {

    private List<Integer> availableDice;
    private int maxRollCount;

    public Config() {
        availableDice = List.of(4, 6, 8, 10, 12, 20, 100);
        maxRollCount = 3;
    }

    public List<Integer> getAvailableDice() {
        return availableDice;
    }

    public void setAvailableDice(final List<Integer> dice) {
        availableDice = dice.stream().distinct().sorted(Integer::compareTo).collect(Collectors.toList());
    }

    public int getMaxRollCount() {
        return maxRollCount;
    }

    public void setMaxRollCount(final int maxRollCount) {
        this.maxRollCount = maxRollCount;
    }
}
