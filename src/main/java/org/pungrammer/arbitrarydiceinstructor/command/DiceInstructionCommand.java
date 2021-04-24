package org.pungrammer.arbitrarydiceinstructor.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pungrammer.arbitrarydiceinstructor.config.Config;
import org.pungrammer.arbitrarydiceinstructor.dice.DiceRoll;
import org.pungrammer.arbitrarydiceinstructor.dice.Die;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiceInstructionCommand extends Command {

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode
    private abstract static class Option {
        // Returns a score.
        // Positive numbers mean this is better than other.
        // 0 means they are equal.
        // Negative numbers mean this is worse the other.
        protected abstract int isBetterThan(Option other);

        protected abstract String getPrintableOption();
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    private static class RollMultiple extends Option {

        private DiceRoll roll;

        @Override
        protected int isBetterThan(Option other) {
            if (!(other instanceof RollMultiple)) {
                throw new IllegalArgumentException("Wrong type");
            }

            RollMultiple castedOther = ((RollMultiple) other);
            if (this.equals(castedOther)) {
                return 0;
            }

            int score = 0;
            if (this.roll.getNumberOfDice() > castedOther.roll.getNumberOfDice()) {
                score--;
            } else {
                score++;
            }
            if (this.roll.getDie().getSides() > castedOther.roll.getDie().getSides()) {
                score--;
            } else {
                score++;
            }
            return score;
        }

        @Override
        protected String getPrintableOption() {
            return String.format("Roll %s", roll.toString());
        }
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    private static class Discard extends Option {

        private Die die;
        private int discardAbove;

        @Override
        protected int isBetterThan(Option other) {
            if (!(other instanceof Discard)) {
                throw new IllegalArgumentException("Wrong type");
            }

            Discard castedOther = ((Discard) other);
            if (this.equals(castedOther)) {
                return 0;
            }

            int score = 0;
            if (this.discardAbove > castedOther.discardAbove) {
                score--;
            } else {
                score++;
            }
            if (this.die.getSides() > castedOther.die.getSides()) {
                score--;
            } else {
                score++;
            }
            return score;
        }

        @Override
        protected String getPrintableOption() {
            return String.format("Roll 1%s and discard any results above %d",
                    die.toString(), discardAbove);
        }
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    private static class MultiplyDivideDiscard extends Option {
        private int divideBy;
        private int discardAbove;
        private List<DiceRoll> requiredRolls;

        @Override
        protected int isBetterThan(Option other) {
            if (!(other instanceof MultiplyDivideDiscard)) {
                throw new IllegalArgumentException("Wrong type");
            }

            MultiplyDivideDiscard castedOther = ((MultiplyDivideDiscard) other);
            if (this.equals(castedOther)) {
                return 0;
            }

            return ((int) (this.getScore() - castedOther.getScore()));
        }

        private double getScore() {
            int rollCount = 0;
            int simDieSides = 1;
            for (DiceRoll roll : this.requiredRolls) {
                rollCount += roll.getNumberOfDice();
                simDieSides *= roll.getNumberOfDice() * roll.getDie().getSides();
            }

            double discardRatio = ((double) simDieSides) / (simDieSides - this.discardAbove);

            return simDieSides * rollCount * discardRatio;
        }

        public String getExample() {
            return "Example: You wanted to get a d7.\n" +
                    "Your instructions said to roll 1d6 and 1d12.\n" +
                    "The solution slots are 10 wide and you need to discard all results above 70.\n" +
                    "Roll the dice. Assume the d6 shows 2 and the d12 shows 7.\n" +
                    "2 * 12 + 7 = 31\n" +
                    "Since the solution slots are 10 wide, this result falls into slot number 4. This is your result.";
        }

        @Override
        protected String getPrintableOption() {
            StringBuilder diceString = new StringBuilder();
            StringBuilder calcInstruction = new StringBuilder();
            int simDieSides = 1;
            for (int i = 0; i < requiredRolls.size(); i++) {
                if (i == 0) {
                    // First roll does not need joining
                    diceString.append(requiredRolls.get(i).toString());
                    // First roll does not need the leading +
                    calcInstruction
                            .append("roll")
                            .append(i + 1)
                            .append(" * ")
                            .append(requiredRolls.get(i + 1).getDie().getSides());
                } else if (i == requiredRolls.size() - 1) {
                    // Join last one with "and"
                    diceString.append(String.format(" and %s", requiredRolls.get(i).toString()));
                    // Last roll should not be multiplied
                    calcInstruction.append(" + roll").append(i + 1);
                } else {
                    diceString.append(String.format(", %s", requiredRolls.get(i).toString()));
                    calcInstruction
                            .append(" + roll")
                            .append(i + 1)
                            .append(" * ")
                            .append(requiredRolls.get(i + 1).getDie().getSides());
                }
                simDieSides *= requiredRolls.get(i).getNumberOfDice() * requiredRolls.get(i).getDie().getSides();
            }

            // first result * second die sides + second result * third die sides ...

            double discardProbability = 100 - (((double) discardAbove) / ((double) simDieSides) * 100);
            return String.format("" +
                            "1) Roll %s.\n" +
                            "2) Then replace your roll results in this calculation. Order is the same as above.\n" +
                            "%s\n" +
                            "3) Discard any result above %d (probability to discard: %f%%).\n" +
                            "4) If not discarded, divide the result by %d and round up to the next integer.",
                    diceString.toString(), calcInstruction.toString(),
                    discardAbove, discardProbability,
                    divideBy);
        }
    }

    protected DiceInstructionCommand(final Config config) {
        super(config);
    }

    @Override
    protected String getCommandWord() {
        return "calc";
    }

    @Override
    protected void executeCommand(final String commandArgs) {
        Integer wantedDie = Integer.valueOf(commandArgs);

        if (wantedDie < 2) {
            throw new IllegalArgumentException("The desired die is too small. Die must be a d2 or bigger.");
        }

        List<Option> options = new ArrayList<>();
        for (Integer commonDie : config.getAvailableDice()) {
            if (wantedDie % commonDie == 0) {
                options.add(new RollMultiple(new DiceRoll(new Die(commonDie), wantedDie / commonDie)));
            }

            if (wantedDie < commonDie) {
                options.add(new Discard(new Die(commonDie), wantedDie));
            }
        }

        // Start with the smallest die and expand it. If it does not result in anything try lager dies.
        // If there is still no result, add another die and repeat.
        options.addAll(expandDie(wantedDie, config.getMaxRollCount()));

        if (options.isEmpty()) {
            System.out.printf("Unable to find any solutions for your d%d%n", wantedDie);
            return;
        }

        RollMultiple bestRollMultiple = null;
        Discard bestDiscard = null;
        MultiplyDivideDiscard bestMDD = null;

        for (Option option : options) {
            if (option instanceof RollMultiple) {
                bestRollMultiple = findBestOption(bestRollMultiple, ((RollMultiple) option));
            }
            if (option instanceof Discard) {
                bestDiscard = findBestOption(bestDiscard, ((Discard) option));
            }
            if (option instanceof MultiplyDivideDiscard) {
                bestMDD = findBestOption(bestMDD, ((MultiplyDivideDiscard) option));
            }
        }

        StringBuilder sb = new StringBuilder();
        if (bestRollMultiple != null) {
            sb.append(bestRollMultiple.getPrintableOption());
            sb.append("\n-------------------------------------------\n");
        }
        if (bestDiscard != null) {
            sb.append(bestDiscard.getPrintableOption());
            sb.append("\n-------------------------------------------\n");
        }
        if (bestMDD != null) {
            sb.append(bestMDD.getPrintableOption());
            sb.append("\n-------------------------------------------\n");
        }

        System.out.println(sb.toString());
    }

    private <T extends Option> T findBestOption(T champ, T contestant) {
        if (champ == null) {
            champ = contestant;
        } else if (contestant.isBetterThan(champ) > 1) {
            champ = contestant;
        }
        return champ;
    }

    private List<MultiplyDivideDiscard> expandDie(final int wantedDie, final int maxDieCount) {
        List<MultiplyDivideDiscard> results = new ArrayList<>();

        // Highest number any digit is allowed to reach
        int oneDigitLimit = config.getAvailableDice().size() - 1;

        // -1 indicates we do not use this die yet
        int[] dieCounters = new int[maxDieCount];
        Arrays.fill(dieCounters, -1);
        dieCounters[maxDieCount - 1] = 0; // We need to start counting at the last element

        while (!Thread.currentThread().isInterrupted()) {
            int simulatedDie = 0; // Our current compound die
            for (int dieCounter : dieCounters) {
                // Exclude any dies we don't use yet
                if (dieCounter < 0) {
                    continue;
                }
                // If this is the first die, assign it otherwise multiply with previous simulated die
                if (simulatedDie == 0) {
                    simulatedDie = config.getAvailableDice().get(dieCounter);
                } else {
                    simulatedDie = simulatedDie * config.getAvailableDice().get(dieCounter);
                }
            }

            if (simulatedDie > wantedDie) {
                MultiplyDivideDiscard result = expand(wantedDie, dieCounters, simulatedDie);
                if (result != null) {
                    results.add(result);
                }
            }

            // increase lowest digit
            dieCounters[dieCounters.length - 1]++;

            // check if we need to increase the next digit and reset the lower ones
            for (int i = dieCounters.length - 1; i >= 0; i--) {
                if (dieCounters[i] > oneDigitLimit) {
                    // if the highest digit is over the limit, we tried all combinations and reached the end.
                    if (i == 0) {
                        return results;
                    }
                    dieCounters[i] = 0;
                    dieCounters[i - 1] = dieCounters[i - 1] + 1;
                }
            }
        }
        return results;
    }

    private MultiplyDivideDiscard expand(int wantedDie, int[] dieIndizes, int simulatedDie) {
        // Our current die is large enough to fit at least one time the wantedDie range.
        // Find out by how much the resulting die needs to be divided (if at all):
        int largestMultiple = 0;
        int multiplications = 0;
        while (largestMultiple < simulatedDie) {
            multiplications++;
            largestMultiple = wantedDie * multiplications;
        }
        largestMultiple -= wantedDie;
        multiplications--;

        // Find out which results need to be discarded
        int discardAbove = simulatedDie - (simulatedDie - largestMultiple);

        // first die result * (first die sides + second die sides) + second die result + ...

        // Exclude combinations where the discard is lager than 1/3 of the die
        // (would mean only one in 3 tries would produce a non-discard result)
        if (simulatedDie / 3 > discardAbove) {
            return null;
        }
        // Exclude combinations where we need to divide by more than 10
        if (multiplications > 10) {
            return null;
        }
        // Exclude combinations which only have one die, as they are already found by another Option.
        if (dieIndizes[dieIndizes.length - 2] < 0) {
            return null;
        }

        // Find all dies needed for this option
        Map<Die, Integer> usedDies = new HashMap<>();
        for (int dieIndex : dieIndizes) {
            if (dieIndex >= 0) {
                Die die = new Die(config.getAvailableDice().get(dieIndex));
                Integer counter = usedDies.computeIfAbsent(die, ignored -> 0);
                counter++;
                usedDies.put(die, counter);
            }
        }
        List<DiceRoll> diceRolls = new ArrayList<>();
        usedDies.forEach((die, count) -> diceRolls.add(new DiceRoll(die, count)));
        return new MultiplyDivideDiscard(multiplications, discardAbove, diceRolls);
    }
}
