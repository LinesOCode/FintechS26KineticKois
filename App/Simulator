"""
Streamlit Investing Simulation (beginner-friendly)

Saves: app.py

Install dependencies:
  pip install -r requirements.txt

Run:
  streamlit run app.py

This app:
 - Lets user choose a starting amount (15k/10k/5k)
 - Fetches data via yfinance (Yahoo Finance)
 - Scores stocks using simple metrics (momentum, volatility, valuation, dividend yield)
 - Picks top stocks and simulates equal-weight buy-and-hold portfolio
 - Shows interactive Plotly charts and step-by-step explanations
"""

import streamlit as st
import yfinance as yf
import pandas as pd
import numpy as np
import plotly.express as px
import plotly.graph_objects as go
from datetime import datetime, timedelta

st.set_page_config(page_title="Beginner Investing Simulator", layout="wide")

# --- Sidebar: user choices ---
st.sidebar.title("Investing Simulator · Settings")

starting_amount = st.sidebar.radio(
    "Choose starting amount",
    options=[15000, 10000, 5000],
    format_func=lambda x: f"${x:,}"
)

risk_profile = st.sidebar.selectbox(
    "Risk profile",
    options=["Conservative", "Moderate", "Aggressive"]
)

num_picks = st.sidebar.slider("Number of stocks to pick for portfolio", 3, 10, 5)
lookback_months = st.sidebar.selectbox("Lookback period to evaluate momentum", [6, 12], index=0)
start_backtest = st.sidebar.date_input(
    "Backtest start date",
    value=datetime.today() - timedelta(days=365),
    max_value=datetime.today() - timedelta(days=30)
)

st.sidebar.markdown("---")
st.sidebar.write("This simulation uses Yahoo Finance (via yfinance) to fetch data. Data is for educational purposes only — not financial advice.")

# --- Explanation & beginner-friendly steps ---
st.title("Beginner-Friendly Investing Simulator")
st.markdown(
    """
This app walks you through a simple, explainable approach to pick stocks and simulate investing:
1. We fetch recent price and fundamental data from Yahoo Finance.
2. We compute easy-to-understand metrics:
   - Momentum: recent returns (stocks with positive momentum often continue).
   - Volatility: how much price swings (lower for conservative profiles).
   - Valuation (P/E): lower can signal cheaper relative price (but context matters).
   - Dividend yield: income for conservative investors.
3. We score stocks using a weighted combination of these metrics (weights change by risk profile).
4. The top-scoring stocks are chosen for an equal-weight portfolio. We'll simulate buy-and-hold from your chosen start date and compare to the market ETF (SPY).
5. Interactive charts let you explore results and learn how choices affect outcomes.
"""
)

# --- Ticker set (curated, cross-sector) ---
DEFAULT_TICKERS = [
    "AAPL","MSFT","AMZN","GOOGL","META","NVDA","TSLA","BRK-B","JNJ","V",
    "JPM","PG","MA","UNH","HD","BAC","XOM","CVX","KO","PFE",
    "DIS","NKE","MCD","SBUX","T","VZ","IBM","ORCL","AMD","INTC"
]

st.markdown("### 1) Which stocks we're evaluating")
st.write("We're evaluating a curated list of common, widely-known stocks across sectors. You can edit the list below.")

tickers_input = st.text_area("Tickers (comma-separated)", value=",".join(DEFAULT_TICKERS), height=80)
tickers = [t.strip().upper() for t in tickers_input.split(",") if t.strip()]

# --- Fetch data from yfinance ---
@st.cache_data(ttl=3600)
def fetch_data(tickers, start_date, end_date):
    # Download daily adjusted close
    data = yf.download(tickers, start=start_date, end=end_date, progress=False, group_by='ticker', threads=True)
    # For fundamentals (info), query per ticker (yfinance only gives limited info via bulk)
    infos = {}
    for t in tickers:
        try:
            tick = yf.Ticker(t)
            infos[t] = tick.info
        except Exception:
            infos[t] = {}
    return data, infos

end_date = datetime.today().date()
start_date = start_backtest
data_load_state = st.info("Fetching data from Yahoo Finance...")
price_data, infos = fetch_data(tickers, start_date - timedelta(days=30), end_date + timedelta(days=1))
data_load_state.empty()

# --- Compute metrics ---
st.markdown("### 2) Compute simple metrics (easy to understand)")
# We will compute:
# - 6-month or 12-month return (depending on lookback)
# - annualized volatility
# - trailing P/E
# - dividend yield
metrics = []
for t in tickers:
    try:
        if t in price_data.columns.levels[0]:
            adj_close = price_data[t]['Adj Close'].dropna()
        else:
            # when only single ticker, yfinance returns series
            adj_close = price_data['Adj Close'].dropna() if 'Adj Close' in price_data else pd.Series()
        if adj_close.empty:
            continue
        # select lookback window
        lookback_days = int(lookback_months * 30)
        recent = adj_close.tail(lookback_days)
        # if not enough data, extend to available
        if recent.empty:
            continue
        ret = (recent.iloc[-1] / recent.iloc[0]) - 1
        daily_returns = adj_close.pct_change().dropna()
        vol = daily_returns.std() * np.sqrt(252)  # annualized volatility
        info = infos.get(t, {})
        pe = info.get('trailingPE', np.nan)
        div_yield = info.get('dividendYield', 0.0)  # already in decimal form sometimes
        # Normalize dividendYield (some tickers may have None)
        if div_yield is None:
            div_yield = 0.0
        metrics.append({
            "ticker": t,
            "momentum": ret,
            "volatility": vol,
            "pe": pe,
            "dividend_yield": div_yield
        })
    except Exception as e:
        # skip ticker if any error
        print(f"Error processing {t}: {e}")
metrics_df = pd.DataFrame(metrics).set_index('ticker')
st.dataframe(metrics_df.style.format({
    'momentum': '{:.2%}',
    'volatility': '{:.2%}',
    'pe': '{:.1f}',
    'dividend_yield': '{:.2%}'
}))

st.markdown("Why these metrics? Momentum helps find recent winners; volatility measures risk; P/E is a simple valuation proxy; dividend yield provides income for conservative portfolios.")

# --- Scoring function ---
def score_stocks(df, profile):
    # We'll transform metrics to scores 0-1 (higher better)
    # For momentum: higher is better
    # For volatility: lower is better
    # For pe: lower is better (but NaNs will be set to median)
    # For dividend: higher is better
    sc = pd.DataFrame(index=df.index)
    # Handle missing PE
    pe = df['pe'].replace({0: np.nan})
    pe_filled = pe.fillna(pe.median())
    # Min-max normalization helper
    def minmax(s, invert=False):
        if s.max() - s.min() == 0:
            return pd.Series(0.5, index=s.index)
        r = (s - s.min()) / (s.max() - s.min())
        return 1 - r if invert else r
    sc['momentum_s'] = minmax(df['momentum'], invert=False)
    sc['vol_s'] = minmax(df['volatility'], invert=True)  # lower vol -> higher score
    sc['pe_s'] = minmax(pe_filled, invert=True)  # lower PE -> higher
    sc['div_s'] = minmax(df['dividend_yield'], invert=False)
    # weights by profile
    if profile == "Conservative":
        weights = {'momentum_s': 0.2, 'vol_s': 0.4, 'pe_s': 0.2, 'div_s': 0.2}
    elif profile == "Aggressive":
        weights = {'momentum_s': 0.5, 'vol_s': 0.1, 'pe_s': 0.2, 'div_s': 0.2}
    else:  # Moderate
        weights = {'momentum_s': 0.4, 'vol_s': 0.25, 'pe_s': 0.2, 'div_s': 0.15}
    sc['score'] = sum(sc[c] * w for c, w in weights.items())
    return sc

scores = score_stocks(metrics_df, risk_profile)
ranked = scores.sort_values('score', ascending=False)
st.markdown("### 3) Scores and rankings")
st.write("Stocks are scored 0-1 (higher is better). The scoring is transparent — you can see how momentum, volatility, valuation, and dividends contribute.")
st.dataframe(ranked.style.format({'score': '{:.3f}'}))

# --- Pick top and bottom (good vs bad) ---
top = ranked.head(num_picks)
bottom = ranked.tail(num_picks)
st.markdown("#### Top picks (interpreted by simple scoring)")
st.table(top[['score']])
st.markdown("#### Bottom picks (lower-scoring by same rules — candidates to avoid for this strategy)")
st.table(bottom[['score']])

# --- Build portfolio: equal-weight top picks, buy-and-hold ---
st.markdown("### 4) Build portfolio and simulate")
selected = list(top.index)
st.write(f"Selected stocks for portfolio (equal-weight): {', '.join(selected)}")

# Fetch adjusted close for selected and SPY for benchmark
@st.cache_data(ttl=3600)
def get_price_series(tickers, start_date, end_date):
    df = yf.download(tickers, start=start_date, end=end_date, progress=False)['Adj Close']
    return df

price_df = get_price_series(selected + ["SPY"], start_backtest, end_date)
# If only one stock selected, ensure DataFrame shape
if isinstance(price_df, pd.Series):
    price_df = price_df.to_frame()

price_df = price_df.dropna(how='all')  # drop days with no data
price_df = price_df.ffill().dropna(how='all')  # forward fill

# Align index
price_df = price_df.loc[price_df.index >= pd.Timestamp(start_backtest)]

# Compute equal-weight portfolio time series (buy-and-hold)
alloc = {t: 1/len(selected) for t in selected}
alloc_df = price_df[selected].pct_change().fillna(0)
# compute normalized prices (start at 1)
norm = price_df[selected] / price_df[selected].iloc[0]
# portfolio value series (starting_amount)
weights = np.array([alloc[t] for t in selected])
portfolio_norm = (norm * weights).sum(axis=1)
portfolio_values = portfolio_norm * starting_amount

# Benchmark normalised
spy_norm = price_df['SPY'] / price_df['SPY'].iloc[0]
spy_values = spy_norm * starting_amount

# Show charts
st.markdown("#### Portfolio vs SPY (benchmark)")
fig = go.Figure()
fig.add_trace(go.Scatter(x=portfolio_values.index, y=portfolio_values.values, name="Portfolio", mode='lines'))
fig.add_trace(go.Scatter(x=spy_values.index, y=spy_values.values, name="SPY (market)", mode='lines'))
fig.update_layout(yaxis_title="Portfolio value (USD)", xaxis_title="Date", hovermode="x unified")
st.plotly_chart(fig, use_container_width=True)

# Allocation pie chart
st.markdown("#### Allocation")
alloc_vals = [starting_amount * alloc[t] for t in selected]
pie = px.pie(names=selected, values=alloc_vals, title="Initial allocation (equal-weight)")
st.plotly_chart(pie, use_container_width=True)

# Individual returns table
st.markdown("#### Individual stock returns since start")
ind_returns = (price_df[selected].iloc[-1] / price_df[selected].iloc[0]) - 1
ind_perf = pd.DataFrame({
    'start_price': price_df[selected].iloc[0],
    'end_price': price_df[selected].iloc[-1],
    'return': ind_returns
}).T if False else pd.DataFrame({
    'start_price': price_df[selected].iloc[0],
    'end_price': price_df[selected].iloc[-1],
    'return': ind_returns
}).T  # ensure DataFrame shape
ind_table = pd.DataFrame({
    'start_price': price_df[selected].iloc[0],
    'end_price': price_df[selected].iloc[-1],
    'return_pct': ind_returns
}).T  # hack to show
# Simpler presentable table:
ind_df_show = pd.DataFrame({
    'start_price': price_df[selected].iloc[0],
    'end_price': price_df[selected].iloc[-1],
    'return': ind_returns
}).T if False else pd.DataFrame({
    'start_price': price_df[selected].iloc[0],
    'end_price': price_df[selected].iloc[-1],
    'return': ind_returns
}).T
# Create readable table:
readable = pd.DataFrame({
    'start_price': price_df[selected].iloc[0],
    'end_price': price_df[selected].iloc[-1],
    'return_pct': ind_returns
}).T  # keep shape consistent for display
# Instead, convert to tidy:
ind_tidy = pd.DataFrame({
    'start_price': price_df[selected].iloc[0],
    'end_price': price_df[selected].iloc[-1],
    'return_pct': ind_returns
}).T  # not ideal; simpler approach:
ind_tidy = pd.DataFrame({
    'start_price': price_df[selected].iloc[0].round(2),
    'end_price': price_df[selected].iloc[-1].round(2),
    'return_pct': ind_returns.round(4)
}).T
# The better display:
ind_display = pd.DataFrame({
    'start_price': price_df[selected].iloc[0].round(2),
    'end_price': price_df[selected].iloc[-1].round(2),
    'return_%': (ind_returns*100).round(2)
}).T  # awkward, swap orientation:
ind_display = pd.DataFrame({
    'start_price': price_df[selected].iloc[0].round(2),
    'end_price': price_df[selected].iloc[-1].round(2),
    'return_%': (ind_returns*100).round(2)
}).T
# Build final nicer table:
ind_final = pd.DataFrame({
    'start_price': price_df[selected].iloc[0].round(2),
    'end_price': price_df[selected].iloc[-1].round(2),
    'return_%': (ind_returns*100).round(2)
})
ind_final = ind_final[['start_price','end_price','return_%']]
st.dataframe(ind_final.style.format({'start_price': '${:,.2f}', 'end_price': '${:,.2f}', 'return_%': '{:.2f}%'}))

# Show time series per stock when clicking/toggling
st.markdown("#### Explore individual stock charts")
chosen_stock = st.selectbox("Show chart for stock:", options=selected)
if chosen_stock:
    fig2 = px.line(price_df[chosen_stock], title=f"{chosen_stock} Adjusted Close")
    fig2.update_layout(yaxis_title="Adj Close (USD)")
    st.plotly_chart(fig2, use_container_width=True)

# --- Step-by-step rationale and tips ---
st.markdown("### 5) Step-by-step explanation (why we did each step)")
st.write("""
- We pick a curated universe to keep the example manageable. In real life you'd start with an index or watchlist.
- Momentum (recent returns) often identifies trending stocks, but momentum can reverse — so diversify.
- Volatility is a measure of risk; conservative investors prefer low volatility.
- P/E is a simple valuation metric — low can be good but may indicate troubles; don't rely on one metric alone.
- Dividend yield provides income and stability for conservative strategies.
- We use equal-weighting to keep allocation simple and avoid over-concentration.
- Backtest here is simple buy-and-hold from the chosen start date. Real investing would consider rebalancing, transaction costs, taxes, and position sizing rules.
""")

st.markdown("### Notes & Next steps")
st.write("""
- This is an educational simulator, not investment advice.
- Data comes from Yahoo Finance via yfinance and can have gaps or delays.
- To improve: expand the universe (e.g., S&P 500), add rebalancing logic, include transaction costs, or use more advanced signals (earnings revisions, fundamentals trends).
""")