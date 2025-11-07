package core;

import java.util.Set;
import java.util.LinkedHashSet;

public class ItemsetResult {
    public final Set<String> items;
    public final double expectedSupport;
    public final double expectedUtility;

    public ItemsetResult(Set<String> items, double expectedSupport, double expectedUtility) {
        this.items = new LinkedHashSet<>(items);
        this.expectedSupport = expectedSupport;
        this.expectedUtility = expectedUtility;
    }

    @Override
    public String toString() {
        return String.format("%s | ES=%.5f | EU=%.5f", items.toString(), expectedSupport, expectedUtility);
    }
}
