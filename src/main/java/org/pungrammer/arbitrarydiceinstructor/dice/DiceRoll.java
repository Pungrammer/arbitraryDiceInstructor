package org.pungrammer.arbitrarydiceinstructor.dice;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiceRoll {
    private final Die die;
    private final int numberOfDice;

    @Override
    public String toString() {
        return String.format("%d%s", numberOfDice, die.toString());
    }
}
