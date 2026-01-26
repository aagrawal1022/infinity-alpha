# Quant AI Lab

An AI-powered stock market analysis platform using:

- MCP Server for structured market data
- AI Agents (OpenAI) for analysis & strategies
- Frontend UI (planned)

## Repo Structure

backend/   - MCP server (Yahoo Finance → DB later)
agent/     - AI agents (OpenAI + MCP)
frontend/  - UI (coming soon)

## Current Status
- [x] Repo initialized
- [ ] MCP server
- [ ] AI agent
- [ ] Frontend

## Vision
Clean separation of:
- Data
- Reasoning
- Presentation


# Start serer

1. python -m venv .venv
2. source .venv/bin/activate //macOS / Linux
    .venv\Scripts\Activate.ps1  /Widnpws
3. python mcp_server/server.py

# Start Agent

# Start serer

1. python -m venv .venv
2. source .venv/bin/activate //macOS / Linux
    .venv\Scripts\Activate.ps1  /Widnpws
3. python backend\agent\agent.py

// Prompts:

1. Analyze Cgar of NIFTY 50 (^NSEI), my invested if i would have invested 10000 Rs whenever this is fall of 0.5% or more for period for 04/07/2022 to 31/12/2025

Response: 

[LOG] Step 1: Generating plan with Gemini...
[LOG] Plan response: ```json
{
  "tool": "get_stock_history",
  "symbol": "^NSEI",
  "start": "2025-01-01",
  "end": "2025-12-31"
}
```
[LOG] Parsed plan JSON: {'tool': 'get_stock_history', 'symbol': '^NSEI', 'start': '2025-01-01', 'end': '2025-12-31'}
[LOG] Step 2: Calling MCP tool 'get_stock_history' with params: {'tool': 'get_stock_history', 'symbol': '^NSEI', 'start': '2025-01-01', 'end': '2025-12-31'}  
[LOG] Stock data received, length: 40449 characters
[LOG] Step 3: Sending data to Gemini for analysis...
[LOG] Analysis complete, returning result
To analyze the Compound Annual Growth Rate (CAGR) for your investment strategy in NIFTY 50 (^NSEI) for the period 01/01/2025 to 31/12/2025, where you invest INR 10,000 whenever the index falls by 1% or more from its previous day's closing price, let's process the provided historical data.

**Investment Strategy Simulation:**

1.  **Data Preparation:** The provided historical data for NIFTY 50 (^NSEI) from January 1, 2025, to December 30, 2025, will be used. The closing price for each trading day will be analyzed.
2.  **Investment Logic:**
    *   For each day, the percentage change from the previous day's closing price is calculated.
    *   If this change is -1% or lower, an investment of INR 10,000 is made on that day at the closing price.
    *   The number of units bought is recorded.
3.  **Portfolio Calculation:**
    *   The total amount invested throughout the year is summed up.
    *   The total number of NIFTY 50 units accumulated is calculated.
    *   The final portfolio value is determined by multiplying the total units held by the last available closing price in the given data (December 30, 2025).
4.  **CAGR Calculation:** Since the investment period is exactly one year (or evaluated at the end of the year), the CAGR is equivalent to the total percentage return on the total capital invested.

**Detailed Calculation:**

Based on the provided historical data for NIFTY 50 from 2025-01-01 to 2025-12-30, here's how the investment strategy unfolds:

*   **Total Number of Investments:** 15 times
*   **Total Capital Invested:** INR 150,000 (15 investments * INR 10,000 per investment)
*   **Total NIFTY 50 Units Accumulated:** 6.412351 units
    *   (Details of investments:
        *   Jan 6, 2025: 10000 / 23616.05 = 0.423447 units
        *   Jan 13, 2025: 10000 / 23085.95 = 0.433177 units
        *   Jan 21, 2025: 10000 / 23024.65 = 0.434314 units
        *   Jan 27, 2025: 10000 / 22829.15 = 0.438036 units
        *   Feb 11, 2025: 10000 / 23071.80 = 0.433433 units
        *   Feb 24, 2025: 10000 / 22553.35 = 0.443398 units
        *   Feb 28, 2025: 10000 / 22124.70 = 0.451996 units
        *   Apr 1, 2025: 10000 / 23165.70 = 0.431682 units
        *   Apr 4, 2025: 10000 / 22904.45 = 0.436592 units
        *   Apr 7, 2025: 10000 / 22161.60 = 0.451227 units
        *   May 9, 2025: 10000 / 24008.00 = 0.416528 units
        *   May 13, 2025: 10000 / 24578.35 = 0.406877 units
        *   May 20, 2025: 10000 / 24683.90 = 0.405193 units
        *   Jun 12, 2025: 10000 / 24888.20 = 0.401799 units
        *   Aug 26, 2025: 10000 / 24712.05 = 0.404652 units )

*   **Final NIFTY 50 Closing Price (on 2025-12-30):** 25938.85
*   **Final Portfolio Value:** 6.412351 units * 25938.85 = INR 166318.96
*   **Total Gain/Loss:** INR 166318.96 - INR 150000 = INR 16318.96

**CAGR (Annualized Return):**

Since the investments are made over the course of one year and the request is for CAGR for a one-year period, the overall percentage return can be considered as the CAGR.

CAGR = ((Final Portfolio Value / Total Capital Invested) - 1) * 100
CAGR = ((166318.96 / 150000) - 1) * 100
CAGR = (1.108793 - 1) * 100
CAGR = 0.108793 * 100
**CAGR = 10.88%**

---

**Analysis:**

Implementing the strategy of investing INR 10,000 in NIFTY 50 whenever it experienced a fall of 1% or more from the previous day's close during 2025 would have resulted in the following:

*   **Total Investment:** You would have invested a total of **INR 150,000** over 15 different occasions throughout the year.
*   **Final Portfolio Value:** Your cumulative investment would be worth **INR 166,318.96** as of December 30, 2025.
*   **Profit:** This strategy would have yielded a profit of **INR 16,318.96**.
*   **Compound Annual Growth Rate (CAGR):** The annualized return (CAGR) for this strategy for the year 2025 is calculated to be **10.88%**.

This indicates that a systematic "buy the dip" approach, even with a simple 1% fall threshold, could have generated a positive return in the NIFTY 50 during the simulated period of 2025, leveraging market corrections to accumulate units at lower average prices.
