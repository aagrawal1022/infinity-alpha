import pandas as pd
import numpy as np
from services.yahoo_service import download as yf_download

def dip_investment_cagr(
    symbol: str,
    start: str,
    end: str,
    dip_percent: float = 1.0,
    investment_amount: float = 10000.0
):
    df = yf_download(symbol, start=start, end=end, progress=False)

    if df.empty:
        return {"error": "No data found"}

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
        "absolute_percent": float(round(((final_value / total_invested) - 1) * 100, 2)) if total_invested > 0 else None,
        "cagr_percent": float(round(cagr, 2)),
        "investment_count": int(investments)
    }


def dip_investment_rolling_returns(
    symbol: str,
    start: str,
    end: str,
    dip_percent: float = 1.0,
    investment_amount: float = 10000.0,
    window_days: int = 252
):
    """Simulate dip purchases and compute rolling returns of the portfolio value.

    Returns a summary with rolling-return statistics and a time series of rolling returns.
    """
    df = yf_download(symbol, start=start, end=end, progress=False)

    if df.empty:
        return {"error": "No data found"}

    df["pct_change"] = df["Close"].pct_change() * 100

    # Track cumulative units purchased at each date
    df.loc[:, "total_units"] = 0.0
    total_units = 0.0
    total_invested = 0.0
    investments = 0

    for i in range(1, len(df)):
        if df["pct_change"].iloc[i] <= -dip_percent:
            price = float(df["Close"].iloc[i])
            units = investment_amount / price

            total_units += units
            total_invested += investment_amount
            investments += 1

        df.at[i, "total_units"] = total_units

    # Compute portfolio value series (ensure numeric types)
    df["portfolio_value"] = df["total_units"].astype(float) * df["Close"].astype(float)

    # Compute rolling returns over the specified window (period-shifted percent change)
    df["rolling_return"] = (df["portfolio_value"] / df["portfolio_value"].shift(window_days) - 1) * 100
    df["rolling_return"].replace([np.inf, -np.inf], np.nan, inplace=True)

    rolling_series = df.dropna(subset=["rolling_return"]).loc[:, ["Date", "rolling_return"]].copy()

    if rolling_series.empty:
        rolling_stats = {
            "mean": None,
            "std": None,
            "median": None,
            "latest": None,
            "count": 0
        }
    else:
        rolling_stats = {
            "mean": float(round(rolling_series["rolling_return"].mean(), 4)),
            "std": float(round(rolling_series["rolling_return"].std(), 4)),
            "median": float(round(rolling_series["rolling_return"].median(), 4)),
            "latest": float(round(rolling_series["rolling_return"].iloc[-1], 4)),
            "count": int(len(rolling_series))
        }

    final_price = float(df.iloc[-1]["Close"])
    final_value = float(df.iloc[-1]["total_units"] * final_price)

    # Convert rolling series to simple list for JSON-friendly output
    rolling_list = [
        {"date": row["Date"].strftime("%Y-%m-%d"), "rolling_return_percent": float(round(row["rolling_return"], 4))}
        for _, row in rolling_series.iterrows()
    ]

    return {
        "symbol": symbol,
        "period": f"{start} to {end}",
        "dip_percent": dip_percent,
        "investment_amount": investment_amount,
        "window_days": int(window_days),
        "total_invested": float(round(total_invested, 2)),
        "total_units": float(round(total_units, 4)),
        "final_value": float(round(final_value, 2)),
        "absolute_percent": float(round(((final_value / total_invested) - 1) * 100, 2)) if total_invested > 0 else None,
        "investment_count": int(investments),
        "rolling_stats": rolling_stats,
        "rolling_series": rolling_list
    }


def _xnpv(rate: float, cashflows: list):
    return sum([amt / ((1 + rate) ** ( (dt - cashflows[0][0]).days / 365.0 )) for dt, amt in cashflows])


def _xirr(cashflows: list, guess: float = 0.1, tol: float = 1e-6, maxiter: int = 100):
    if not cashflows:
        return None

    # ensure sorted by date
    cashflows = sorted(cashflows, key=lambda x: x[0])
    rate = guess
    for _ in range(maxiter):
        f = _xnpv(rate, cashflows)
        # numerical derivative
        eps = 1e-6
        f1 = _xnpv(rate + eps, cashflows)
        deriv = (f1 - f) / eps
        if deriv == 0:
            break
        new_rate = rate - f / deriv
        if abs(new_rate - rate) < tol:
            return new_rate
        rate = new_rate
    # fallback: try a simple scan to bracket a root and bisection
    low, high = -0.9999999, 10
    f_low, f_high = _xnpv(low, cashflows), _xnpv(high, cashflows)
    if f_low * f_high > 0:
        return None
    for _ in range(200):
        mid = (low + high) / 2
        f_mid = _xnpv(mid, cashflows)
        if abs(f_mid) < tol:
            return mid
        if f_low * f_mid < 0:
            high = mid
            f_high = f_mid
        else:
            low = mid
            f_low = f_mid
    return None


def dip_investment_xirr(
    symbol: str,
    start: str,
    end: str,
    dip_percent: float = 1.0,
    investment_amount: float = 10000.0,
    annualize_periods: int = 252
):
    """Simulate dip purchases and compute XIRR and periodic IRR (annualized).

    Returns XIRR (date-weighted) and an IRR computed on a periodic series
    constructed from the trading-day cashflow sequence and annualized.
    """
    df = yf_download(symbol, start=start, end=end, progress=False)

    if df.empty:
        return {"error": "No data found"}

    df["pct_change"] = df["Close"].pct_change() * 100

    total_units = 0.0
    total_invested = 0.0
    investments = 0

    # collect cashflows by date (date, amount)
    cashflow_map = {}

    for i in range(1, len(df)):
        if df["pct_change"].iloc[i] <= -dip_percent:
            price = float(df["Close"].iloc[i])
            units = investment_amount / price
            total_units += units
            total_invested += investment_amount
            investments += 1
            dt = df["Date"].iloc[i].to_pydatetime().date()
            cashflow_map.setdefault(dt, 0.0)
            cashflow_map[dt] -= float(round(investment_amount, 2))

    final_date = df["Date"].iloc[-1].to_pydatetime().date()
    final_price = float(df["Close"].iloc[-1])
    final_value = total_units * final_price
    # add final positive cashflow
    cashflow_map.setdefault(final_date, 0.0)
    cashflow_map[final_date] += float(round(final_value, 2))

    # build sorted cashflow list for XIRR
    cashflows = [(pd.to_datetime(d).to_pydatetime(), amt) for d, amt in sorted(cashflow_map.items())]

    xirr_rate = _xirr(cashflows)
    xirr_percent = float(round(xirr_rate * 100, 4)) if xirr_rate is not None else None

    # Build periodic cashflow series aligned to trading rows for np.irr
    # Map df dates to amounts (use date objects)
    cf_by_date = {d: amt for d, amt in cashflow_map.items()}
    periodic_cf = [cf_by_date.get(df["Date"].iloc[i].to_pydatetime().date(), 0.0) for i in range(len(df))]

    periodic_rate = None
    try:
        irr_per_period = np.irr(periodic_cf)
        if irr_per_period is not None and not np.isnan(irr_per_period):
            periodic_rate = (1 + irr_per_period) ** annualize_periods - 1
    except Exception:
        periodic_rate = None

    periodic_irr_percent = float(round(periodic_rate * 100, 4)) if periodic_rate is not None else None

    return {
        "symbol": symbol,
        "period": f"{start} to {end}",
        "dip_percent": dip_percent,
        "investment_amount": investment_amount,
        "total_invested": float(round(total_invested, 2)),
        "total_units": float(round(total_units, 4)),
        "final_value": float(round(final_value, 2)),
        "absolute_percent": float(round(((final_value / total_invested) - 1) * 100, 2)) if total_invested > 0 else None,
        "investment_count": int(investments),
        "xirr_percent": xirr_percent,
        "periodic_irr_annualized_percent": periodic_irr_percent
    }
