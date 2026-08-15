 /*
  InvestingSimulator.java
  Beginner-friendly investing simulator with live Yahoo Finance lookups and simple visuals.
 
  Requirements:
   - Java 11 or newer
   - No external libraries required
 
  How to compile and run:
   javac InvestingSimulator.java
   java InvestingSimulator
 
  The program opens a window. Choose a starting amount, click "Fetch recommendations",
  review the step-by-step explanation, then "Buy recommended" and "Run simulation".
 */
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.net.*;
 import java.net.http.*;
 import java.time.*;
 import java.util.*;
 import java.util.List;
 import java.util.regex.*;
 import java.io.*;
 import java.util.concurrent.*;
 
 // Single-file Swing application
 public class InvestingSimulator {
     private static final String[] WATCHLIST = {
         "AAPL","MSFT","AMZN","GOOGL","NVDA","TSLA","META","JNJ","JPM","V"
     };
 
     private JFrame frame;
     private JTextArea log;
     private ChartPanel chartPanel;
     private JComboBox<String> amountBox;
     private JButton fetchBtn, buyBtn, simulateBtn, stepBtn, animateBtn;
     private JTable table;
     private Portfolio portfolio;
     private List<StockInfo> recommendedGood = new ArrayList<>();
     private List<StockInfo> recommendedBad = new ArrayList<>();
     private Map<String, Double[]> simulatedPaths = new HashMap<>();
     private volatile boolean animating = false;
 
     public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> new InvestingSimulator().createAndShowGUI());
     }
 
     private void createAndShowGUI() {
         frame = new JFrame("Beginner Investing Simulator");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(1000, 700);
         frame.setLayout(new BorderLayout());
 
         // Top control panel
         JPanel top = new JPanel();
         top.setLayout(new FlowLayout(FlowLayout.LEFT));
         top.add(new JLabel("Starting amount:"));
         amountBox = new JComboBox<>(new String[] {"$15,000", "$10,000", "$5,000"});
         top.add(amountBox);
         fetchBtn = new JButton("Fetch recommendations (Yahoo Finance)");
         top.add(fetchBtn);
         buyBtn = new JButton("Buy recommended");
         buyBtn.setEnabled(false);
         top.add(buyBtn);
         simulateBtn = new JButton("Run simulation (30 days)");
         simulateBtn.setEnabled(false);
         top.add(simulateBtn);
         stepBtn = new JButton("Step 1 day");
         stepBtn.setEnabled(false);
         top.add(stepBtn);
         animateBtn = new JButton("Animate");
         animateBtn.setEnabled(false);
         top.add(animateBtn);
 
         frame.add(top, BorderLayout.NORTH);
 
         // Left: explanation/log
         log = new JTextArea();
         log.setEditable(false);
         log.setLineWrap(true);
         JScrollPane logPane = new JScrollPane(log);
         logPane.setPreferredSize(new Dimension(360, 0));
         frame.add(logPane, BorderLayout.WEST);
 
         // Center: chart panel
         chartPanel = new ChartPanel();
         frame.add(chartPanel, BorderLayout.CENTER);
 
         // Bottom: stocks table
         String[] cols = {"Symbol","Price","% Change (recent)","Why chosen"};
         Object[][] data = new Object[WATCHLIST.length][4];
         table = new JTable(data, cols);
         JScrollPane tablePane = new JScrollPane(table);
         tablePane.setPreferredSize(new Dimension(0, 160));
         frame.add(tablePane, BorderLayout.SOUTH);
 
         // Button actions
         fetchBtn.addActionListener(e -> fetchRecommendations());
         buyBtn.addActionListener(e -> buyRecommended());
         simulateBtn.addActionListener(e -> runSimulation(30));
         stepBtn.addActionListener(e -> stepSimulation(1));
         animateBtn.addActionListener(e -> toggleAnimate());
 
         frame.setVisible(true);
 
         writeLog("Welcome! This simulator will fetch recent price moves from Yahoo Finance for a small watchlist, recommend top movers, and simulate a simple future price path. Start by choosing a starting amount and clicking 'Fetch recommendations'.");
     }
 
     private void writeLog(String s) {
         SwingUtilities.invokeLater(() -> {
             log.append(s + "\n\n");
             log.setCaretPosition(log.getDocument().getLength());
         });
     }
 
     // Fetch quotes from Yahoo Finance public quote endpoint and classify
     private void fetchRecommendations() {
         fetchBtn.setEnabled(false);
         writeLog("Step 1 — fetching recent data from Yahoo Finance for a small, curated watchlist...");
         CompletableFuture.runAsync(() -> {
             try {
                 List<StockInfo> infos = fetchQuotes(WATCHLIST);
                 // sort by percent change descending
                 infos.sort(Comparator.comparingDouble(StockInfo::getPercentChange).reversed());
                 recommendedGood.clear();
                 recommendedBad.clear();
                 int topN = Math.min(3, infos.size());
                 for (int i = 0; i < topN; i++) recommendedGood.add(infos.get(i));
                 for (int i = 0; i < topN; i++) recommendedBad.add(infos.get(infos.size()-1-i));
                 updateTable(infos);
                 writeLog("Fetched " + infos.size() + " tickers. Top " + topN + " treated as 'good' (recent positive movers), bottom " + topN + " as 'bad' (recent negative movers).");
                 explainRecommendations();
                 SwingUtilities.invokeLater(() -> {
                     buyBtn.setEnabled(true);
                     simulateBtn.setEnabled(false);
                     stepBtn.setEnabled(false);
                     animateBtn.setEnabled(false);
                 });
             } catch (Exception ex) {
                 writeLog("Error fetching data: " + ex.getMessage());
                 ex.printStackTrace();
             } finally {
                 SwingUtilities.invokeLater(() -> fetchBtn.setEnabled(true));
             }
         });
     }
 
     private void explainRecommendations() {
         writeLog("Step 2 — explanation (beginner-friendly):");
         writeLog("We look at the most recent percent move for each stock. Stocks that have been rising recently may have momentum (short-term strength). " +
                 "We call the top few 'good' for this demo (momentum-based pick). Stocks that have fallen recently are marked 'bad' — watch for value or further weakness.");
         writeLog("Selection summary:");
         writeLog("Good picks:");
         for (StockInfo s : recommendedGood) {
             writeLog("- " + s.symbol + ": price $" + String.format("%.2f", s.price) +
                     ", recent change " + String.format("%.2f", s.percentChange) + "% — chosen because it is among the top recent movers.");
         }
         writeLog("Bad picks:");
         for (StockInfo s : recommendedBad) {
             writeLog("- " + s.symbol + ": price $" + String.format("%.2f", s.price) +
                     ", recent change " + String.format("%.2f", s.percentChange) + "% — chosen because it is among the recent laggards.");
         }
         writeLog("Beginner tip: this is one simple filter (momentum). A safer real-world approach would include diversification, long-term fundamentals, and position sizing rules.");
     }
 
     private void updateTable(List<StockInfo> infos) {
         SwingUtilities.invokeLater(() -> {
             Object[][] data = new Object[infos.size()][4];
             for (int i = 0; i < infos.size(); i++) {
                 StockInfo s = infos.get(i);
                 String reason = recommendedGood.contains(s) ? "Recommended (good recent move)"
                               : recommendedBad.contains(s) ? "Avoid (recent weak move)"
                               : "Neutral";
                 data[i][0] = s.symbol;
                 data[i][1] = "$" + String.format("%.2f", s.price);
                 data[i][2] = String.format("%.2f%%", s.percentChange);
                 data[i][3] = reason;
             }
             table.setModel(new javax.swing.table.DefaultTableModel(data, new String[] {"Symbol","Price","% Change (recent)","Why chosen"}));
         });
     }
 
     // Simple portfolio purchase: allocate equally among recommendedGood
     private void buyRecommended() {
         if (recommendedGood.isEmpty()) {
             writeLog("No recommendations available. Click 'Fetch recommendations' first.");
             return;
         }
         double starting = parseAmount((String)amountBox.getSelectedItem());
         portfolio = new Portfolio(starting);
         writeLog("Step 3 — building a beginner portfolio using equal allocation across recommended picks.");
         int buyCount = recommendedGood.size();
         double perStock = starting / buyCount;
         for (StockInfo s : recommendedGood) {
             int shares = (int)(perStock / s.price);
             if (shares <= 0) {
                 writeLog("Not enough funds to buy a share of " + s.symbol + " at $" + String.format("%.2f", s.price) + ". Skipping.");
             } else {
                 portfolio.buy(s.symbol, s.price, shares);
                 writeLog("Bought " + shares + " shares of " + s.symbol + " at $" + String.format("%.2f", s.price) + " (" + String.format("%.2f", shares * s.price) + ").");
             }
         }
         writeLog("Portfolio cash remaining: $" + String.format("%.2f", portfolio.cash));
         writeLog("Beginner tip: equal-dollar allocation is a simple starting rule. You can study position sizing and rebalancing later.");
         chartPanel.clear();
         chartPanel.setPortfolioHoldings(portfolio.holdings);
         simulateBtn.setEnabled(true);
         stepBtn.setEnabled(true);
         animateBtn.setEnabled(true);
     }
 
     private double parseAmount(String label) {
         return label.contains("15") ? 15000.0 : label.contains("10") ? 10000.0 : 5000.0;
     }
 
     // Run a multi-day simulation and display paths
     private void runSimulation(int days) {
         if (portfolio == null) {
             writeLog("Please buy recommended stocks first.");
             return;
         }
         writeLog("Step 4 — running a " + days + "-day simulation using a simple stochastic model.");
         simulatedPaths.clear();
         // For each holding, simulate a path using today's percent change magnitude as a proxy for daily volatility
         for (Map.Entry<String, Holding> e : portfolio.holdings.entrySet()) {
             String symbol = e.getKey();
             double startPrice = e.getValue().price;
             // find StockInfo to get recent percent change
             Optional<StockInfo> info = recommendedGood.stream().filter(s -> s.symbol.equals(symbol)).findFirst();
             double recentPct = info.map(StockInfo::getPercentChange).orElse(0.0);
             Double[] path = simulatePath(startPrice, days, recentPct);
             simulatedPaths.put(symbol, path);
         }
         chartPanel.setPaths(simulatedPaths);
         chartPanel.repaint();
         writeLog("Simulation created. Use 'Step 1 day' to move forward one day or 'Animate' to play automatically.");
     }
 
     private void stepSimulation(int days) {
         chartPanel.step(days);
         double total = chartPanel.currentPortfolioValue(portfolio);
         writeLog("Step forward " + days + " day(s). Portfolio simulated value: $" + String.format("%.2f", total));
     }
 
     private void toggleAnimate() {
         if (animating) {
             animating = false;
             animateBtn.setText("Animate");
         } else {
             animating = true;
             animateBtn.setText("Stop");
             CompletableFuture.runAsync(() -> {
                 while (animating && chartPanel.hasMoreSteps()) {
                     try {
                         SwingUtilities.invokeLater(() -> {
                             chartPanel.step(1);
                             double total = chartPanel.currentPortfolioValue(portfolio);
                             writeLog("Animated day: portfolio simulated value $" + String.format("%.2f", total));
                         });
                         Thread.sleep(600);
                     } catch (InterruptedException ex) {
                         Thread.currentThread().interrupt();
                     }
                 }
                 animating = false;
                 SwingUtilities.invokeLater(() -> animateBtn.setText("Animate"));
             });
         }
     }
 
     // Simulate a price path (simple model): use recent percent change magnitude as a volatility proxy
     private Double[] simulatePath(double startPrice, int days, double recentPercent) {
         Double[] path = new Double[days+1];
         path[0] = startPrice;
         double volProxy = Math.max(0.005, Math.abs(recentPercent) / 100.0); // e.g., 1% -> 0.01
         Random rnd = new Random();
         for (int d = 1; d <= days; d++) {
             // daily return ~ Normal(0, volProxy) with a tiny upward drift
             double dailyReturn = (rnd.nextGaussian() * volProxy) + (volProxy * 0.02);
             double next = Math.max(0.01, path[d-1] * (1 + dailyReturn));
             path[d] = next;
         }
         return path;
     }
 
     // Fetch multiple symbols' quotes using Yahoo Finance query endpoint.
     private List<StockInfo> fetchQuotes(String[] symbols) throws Exception {
         String joined = String.join(",", symbols);
         String url = "https://query1.finance.yahoo.com/v7/finance/quote?symbols=" + URLEncoder.encode(joined, "UTF-8");
         HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
         HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
         HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
         if (resp.statusCode() != 200) {
             throw new IOException("Yahoo request returned status " + resp.statusCode());
         }
         String body = resp.body();
         List<StockInfo> infos = new ArrayList<>();
         // Very small, robust parsing using regex for each symbol block
         for (String sym : symbols) {
             Pattern p = Pattern.compile("\\{[^}]*\"symbol\"\\s*:\\s*\"" + Pattern.quote(sym) + "\".*?\\}", Pattern.DOTALL);
             Matcher m = p.matcher(body);
             if (m.find()) {
                 String block = m.group();
                 double price = extractDouble(block, "\"regularMarketPrice\"\\s*:\\s*([0-9.+-Eed]+)");
                 double pct = extractDouble(block, "\"regularMarketChangePercent\"\\s*:\\s*([0-9.+-Eed]+)");
                 // Fallbacks if not present
                 if (Double.isNaN(price)) price = extractDouble(block, "\"regularMarketPreviousClose\"\\s*:\\s*([0-9.+-Eed]+)");
                 if (Double.isNaN(pct)) pct = 0.0;
                 if (!Double.isNaN(price)) {
                     infos.add(new StockInfo(sym, price, pct));
                 } else {
                     writeLog("Warning: couldn't parse price for " + sym + ". Skipping.");
                 }
             } else {
                 writeLog("Warning: no data block for " + sym + " in response.");
             }
         }
         return infos;
     }
 
     // Helper to extract first matched double; returns NaN if missing
     private double extractDouble(String text, String regex) {
         Pattern p = Pattern.compile(regex);
         Matcher m = p.matcher(text);
         if (m.find()) {
             try {
                 return Double.parseDouble(m.group(1));
             } catch (NumberFormatException ex) {
                 return Double.NaN;
             }
         }
         return Double.NaN;
     }
 
     // Simple data classes
     static class StockInfo {
         String symbol;
         double price;
         double percentChange;
         StockInfo(String s, double p, double pct) { symbol=s; price=p; percentChange=pct; }
         double getPercentChange() { return percentChange; }
     }
 
     static class Portfolio {
         double cash;
         Map<String, Holding> holdings = new LinkedHashMap<>();
         Portfolio(double starting) { cash = starting; }
         void buy(String symbol, double price, int shares) {
             double cost = price * shares;
             if (cost > cash) return;
             cash -= cost;
             holdings.put(symbol, new Holding(symbol, price, shares));
         }
     }
 
     static class Holding {
         String symbol;
         double price; // purchase price
         int shares;
         Holding(String s, double p, int q) { symbol=s; price=p; shares=q; }
     }
 
     // Panel for simple line chart
     class ChartPanel extends JPanel {
         Map<String, Double[]> paths = new LinkedHashMap<>();
         int currentStep = 0;
         Map<String, Color> colors = new HashMap<>();
         Map<String, Holding> holdings = new LinkedHashMap<>();
 
         ChartPanel() {
             setBackground(Color.WHITE);
         }
 
         void setPortfolioHoldings(Map<String, Holding> h) {
             holdings.clear();
             holdings.putAll(h);
         }
 
         void setPaths(Map<String, Double[]> p) {
             paths.clear();
             paths.putAll(p);
             currentStep = 0;
             colors.clear();
             Random r = new Random();
             for (String s : paths.keySet()) {
                 colors.put(s, new Color(50 + r.nextInt(180), 50 + r.nextInt(180), 50 + r.nextInt(180)));
             }
             repaint();
         }
 
         void clear() {
             paths.clear();
             colors.clear();
             currentStep = 0;
             repaint();
         }
 
         void step(int days) {
             if (paths.isEmpty()) return;
             currentStep = Math.min(maxPathLength()-1, currentStep + days);
             repaint();
         }
 
         boolean hasMoreSteps() {
             return currentStep < (maxPathLength()-1);
         }
 
         int maxPathLength() {
             int m = 0;
             for (Double[] arr : paths.values()) if (arr.length > m) m = arr.length;
             return m;
         }
 
         double currentPortfolioValue(Portfolio p) {
             double total = p.cash;
             for (Map.Entry<String, Holding> e : p.holdings.entrySet()) {
                 Double[] path = paths.get(e.getKey());
                 if (path != null && path.length > currentStep) total += path[currentStep] * e.getValue().shares;
                 else total += e.getValue().price * e.getValue().shares;
             }
             return total;
         }
 
         @Override
         protected void paintComponent(Graphics g0) {
             super.paintComponent(g0);
             Graphics2D g = (Graphics2D)g0;
             g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             int w = getWidth(), h = getHeight();
             // axes
             g.setColor(Color.LIGHT_GRAY);
             g.fillRect(0,0,w,h);
             g.setColor(Color.WHITE);
             g.fillRect(40,20,w-60,h-60);
             g.setColor(Color.DARK_GRAY);
             g.drawRect(40,20,w-60,h-60);
             if (paths.isEmpty()) {
                 g.setColor(Color.BLACK);
                 g.drawString("No simulation yet. Buy recommended and Run simulation to see the chart.", 60, 60);
                 return;
             }
             int left = 40, top = 20, right = w-20, bottom = h-40;
             // find min/max up to currentStep
             double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
             for (Double[] arr : paths.values()) {
                 int upto = Math.min(currentStep, arr.length-1);
                 for (int i = 0; i <= upto; i++) {
                     min = Math.min(min, arr[i]);
                     max = Math.max(max, arr[i]);
                 }
             }
             if (min <= 0 || Double.isInfinite(min)) min = 0.01;
             if (max <= min) max = min * 1.1;
             // grid lines & labels
             g.setColor(Color.LIGHT_GRAY);
             for (int i = 0; i <= 4; i++) {
                 int yy = top + i*(bottom-top)/4;
                 g.drawLine(left, yy, right, yy);
                 double val = max - (max-min)*(i/4.0);
                 g.setColor(Color.DARK_GRAY);
                 g.drawString(String.format("%.2f", val), 6, yy+4);
                 g.setColor(Color.LIGHT_GRAY);
             }
             int steps = maxPathLength();
             int usedSteps = Math.max(1, Math.min(currentStep+1, steps));
             // draw each path
             for (Map.Entry<String, Double[]> e : paths.entrySet()) {
                 String sym = e.getKey();
                 Double[] arr = e.getValue();
                 g.setColor(colors.getOrDefault(sym, Color.BLUE));
                 int len = Math.min(arr.length, currentStep+1);
                 int prevX = -1, prevY = -1;
                 for (int i = 0; i < len; i++) {
                     int x = left + (i * (right-left)) / (steps-1);
                     double v = arr[i];
                     int y = top + (int)((max - v) / (max - min) * (bottom - top));
                     if (prevX != -1) {
                         g.setStroke(new BasicStroke(2f));
                         g.drawLine(prevX, prevY, x, y);
                     }
                     prevX = x; prevY = y;
                 }
                 // label last point
                 if (len > 0) {
                     int x = left + ((len-1) * (right-left)) / (steps-1);
                     double v = arr[len-1];
                     int y = top + (int)((max - v) / (max - min) * (bottom - top));
                     g.fillOval(x-3, y-3, 6, 6);
                     g.drawString(sym + " $" + String.format("%.2f", v), x+6, y-6);
                 }
             }
             // current day label
             g.setColor(Color.BLACK);
             g.drawString("Day " + currentStep + " of " + (steps-1), left + 6, bottom + 20);
         }
     }
 }