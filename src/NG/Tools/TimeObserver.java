package NG.Tools;

import NG.DataStructures.Generic.PairList;

/**
 * @author Geert van Ieperen created on 30-9-2019.
 */
public interface TimeObserver {
    void startNewLoop();

    void startTiming(String identifier);

    void endTiming(String identifier);

    PairList<String, Float> results();

    String resultsTable();

    class EmptyObserver implements TimeObserver {
        @Override
        public void startNewLoop() {
        }

        @Override
        public void startTiming(String identifier) {
        }

        @Override
        public void endTiming(String identifier) {
        }

        @Override
        public PairList<String, Float> results() {
            return new PairList<>();
        }

        @Override
        public String resultsTable() {
            return "No time observer installed";
        }
    }
}
