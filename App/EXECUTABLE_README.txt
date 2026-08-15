# Fintech S26 Kinetic Kois - Executable Guide

## What Was Created

I've compiled your Java application into an executable package:

### Files Created:
1. **KineticKois.jar** - The compiled executable Java application
2. **RunApp.bat** - Quick launcher batch file (just double-click to run)
3. **MANIFEST.MF** - Manifest file that specifies the main entry point

### How to Run

**Option 1: Using the Batch File (Easiest)**
- Double-click `RunApp.bat` in the App folder
- The application will run and display the trading feature analysis

**Option 2: Using Command Line**
```bash
java -jar KineticKois.jar
```

**Option 3: Direct JAR Execution**
- Double-click `KineticKois.jar` (if Java is set up correctly)

## What the Application Does

The application:
- Reads market data (stock price bars)
- Computes technical trading features:
  - Short-term Simple Moving Average (5-day)
  - Long-term Simple Moving Average (20-day)
  - Volatility (standard deviation of returns)
  - Momentum (5-day price change)
  - Average return

## Requirements

- **Java Runtime Environment (JRE) 8 or higher** - [Download from java.com](https://www.java.com/download/)

## Java Classes Included

- **Main.java** - Entry point with demonstration data
- **FeatureExtractor.java** - Core trading feature computation engine
- **Fetcher.java** - Data structure for market bars

## Next Steps

To use real market data instead of sample data:
1. Modify the `createSampleBars()` method in Main.java to fetch real data from Yahoo Finance or another API
2. Recompile with: `javac *.java`
3. Recreate the jar with: `jar cvfm KineticKois.jar MANIFEST.MF *.class`

---
**Application compiled and packaged successfully!**
