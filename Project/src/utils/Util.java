package utils;

import java.util.*;

public final class Util {
    private Util() {}

    public static List<List<String>> combinations(List<String> items, int r) {
        List<List<String>> res = new ArrayList<>();
        combineRec(items, r, 0, new ArrayList<>(), res);
        return res;
    }

    private static void combineRec(List<String> items, int r, int start, List<String> curr, List<List<String>> res) {
        if (curr.size() == r) { res.add(new ArrayList<>(curr)); return; }
        for (int i = start; i <= items.size() - (r - curr.size()); i++) {
            curr.add(items.get(i));
            combineRec(items, r, i + 1, curr, res);
            curr.remove(curr.size()-1);
        }
    }

    public static String join(Collection<String> col) {
        StringBuilder sb = new StringBuilder();
        boolean first=true;
        for (String s : col) {
            if (!first) sb.append(",");
            sb.append(s);
            first=false;
        }
        return sb.toString();
    }
}
