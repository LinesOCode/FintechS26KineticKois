import java.util.ArrayList;
import java.util.List;

/**
 * Main entry point for the Fintech Kinetic Kois trading application.
 * Demonstrates usage of FeatureExtractor to compute trading features.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Fintech S26 Kinetic Kois Trading Application ===\n");
        
        // Create sample bars data
        List<Fetcher.Bar> bars = createSampleBars();
        
        // Compute features
        FeatureExtractor.Features features = FeatureExtractor.compute(bars);
        
        // Display results
        System.out.println("Trading Feature Analysis:");
        System.out.println("--------------------------");
        System.out.printf("Last Close Price: $%.2f\n", features.lastClose);
        System.out.printf("Short SMA (5-day): $%.2f\n", features.smaShort);
        System.out.printf("Long SMA (20-day): $%.2f\n", features.smaLong);
        System.out.printf("Volatility (σ): %.4f\n", features.volatility);
        System.out.printf("Momentum (5-day): $%.2f\n", features.momentum);
        System.out.printf("Average Return: %.4f\n", features.avgReturn);
        System.out.println("--------------------------");
        System.out.println("Application completed successfully!");
    }
    
    /**
     * Creates sample bar data for demonstration.
     * In production, this would fetch real market data via Yahoo Finance or similar API.
     */
    private static List<Fetcher.Bar> createSampleBars() {
        List<Fetcher.Bar> bars = new ArrayList<>();
        double[] closes = {
            100.0, 101.5, 99.8, 102.3, 101.2, 103.0, 104.5, 103.2,
            105.1, 106.0, 104.8, 107.2, 108.5, 107.8, 109.0, 110.5,
            111.2, 110.8, 112.3, 113.5, 114.0
        };
        
        for (double close : closes) {
            bars.add(new Fetcher.Bar(close));
        }
        
        return bars;
    }
}
