import pandas as pd
import yfinance as yf
import datetime

def dip_investment_cagr(
    symbol: str,
    start: str,
    end: str,
    dip_percent: float = 1.0,
    investment_amount: float = 10000.0
):
    df = yf.download(symbol, start=start, end=end, progress=False)

    if df.empty:
        return {"error": "No data found"}

    df = df.reset_index()
    df["pct_change"] = df["Close"].pct_change() * 100

    total_invested = 0.0
    total_units = 0.0
    investments = 0

    for i in range(1, len(df)):
        if df["pct_change"].iloc[i] <= -dip_percent:
            price = float(df["Close"].iloc[i])
            units = investment_amount / price

            total_units += units
            total_invested += investment_amount
            investments += 1

    final_price = float(df.iloc[-1]["Close"])
    final_value = float(total_units) * final_price

    years = (pd.to_datetime(end) - pd.to_datetime(start)).days / 365.25
    cagr = ((final_value / total_invested) ** (1 / years) - 1) * 100 if total_invested > 0 else 0

    return {
        "symbol": symbol,
        "period": f"{start} to {end}",
        "dip_percent": dip_percent,
        "investment_amount": investment_amount,
        "total_invested": float(round(total_invested, 2)),
        "total_units": float(round(total_units, 4)),
        "final_value": float(round(final_value, 2)),
        "cagr_percent": float(round(cagr, 2)),
        "investment_count": int(investments)
    }
