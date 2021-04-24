package org.pungrammer.arbitrarydiceinstructor.dice;

import lombok.Data;

import java.util.List;

@Data
public class Die {
    private final int sides;

    @Override
    public String toString() {
        return String.format("d%d", sides);
    }
}
