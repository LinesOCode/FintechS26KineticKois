import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compute simple features from historical bars.
 */
public class FeatureExtractor {

    public static class Features {
        public double smaShort;   // 5-day SMA
        public double smaLong;    // 20-day SMA
        public double volatility; // stddev of daily returns (20-day)
        public double momentum;   // close - close_n (5)
        public double lastClose;
        public double avgReturn;  // mean of daily returns over lookback
    }

    public static Features compute(List<Fetcher.Bar> bars) {
        // bars must be ordered oldest -> newest (Yahoo returns oldest first)
        Features f = new Features();
        int n = bars.size();
        if (n == 0) return f;
        f.lastClose = bars.get(n - 1).close;
        // compute returns
        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < n; i++) {
            double prev = bars.get(i - 1).close;
            double cur = bars.get(i).close;
            returns.add((cur - prev) / prev);
        }

        int shortN = Math.min(5, n);
        int longN = Math.min(20, n);

        double smaShort = averageClose(bars, n - shortN, n);
        double smaLong = averageClose(bars, n - longN, n);

        f.smaShort = smaShort;
        f.smaLong = smaLong;

        // volatility: stddev of last longN-1 returns
        List<Double> volWindow = returns.stream().skip(Math.max(0, returns.size() - (longN - 1))).collect(Collectors.toList());
        f.volatility = stddev(volWindow);

        // momentum: close - close_5
        if (n >= 6) {
            double close5 = bars.get(n - 1 - 5).close;
            f.momentum = f.lastClose - close5;
        } else {
            f.momentum = f.lastClose - bars.get(0).close;
        }

        // avg return (last longN-1 returns)
        List<Double> retWindow = returns.stream().skip(Math.max(0, returns.size() - (longN - 1))).collect(Collectors.toList());
        f.avgReturn = retWindow.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        return f;
    }

    private static double averageClose(List<Fetcher.Bar> bars, int fromInclusive, int toExclusive) {
        if (fromInclusive < 0) fromInclusive = 0;
        if (toExclusive > bars.size()) toExclusive = bars.size();
        if (fromInclusive >= toExclusive) return bars.get(bars.size() - 1).close;
        double sum = 0;
        int cnt = 0;
        for (int i = fromInclusive; i < toExclusive; i++) {
            sum += bars.get(i).close;
            cnt++;
        }
        return sum / Math.max(1, cnt);
    }

    private static double stddev(List<Double> arr) {
        if (arr.isEmpty()) return 0.0;
        double mean = arr.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double s = 0;
        for (double v : arr) s += (v - mean) * (v - mean);
        return Math.sqrt(s / arr.size());
    }
}
