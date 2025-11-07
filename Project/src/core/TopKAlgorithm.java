package core;

import model.UncertainDatabase;

public interface TopKAlgorithm {
    AlgorithmResult run(UncertainDatabase db, int K, int maxLen);
}
