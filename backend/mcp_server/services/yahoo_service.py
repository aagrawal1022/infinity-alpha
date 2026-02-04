"""Simple wrapper around yfinance download with column-flattening.

Provides a `download` function matching the `yfinance.download` signature
used in the project but ensures returned DataFrame has a flat column index
and a `Date` column when possible.
"""
from typing import Any
import pandas as pd
import yfinance as yf


def download(symbol: str, start: str, end: str, progress: bool = False, **kwargs: Any) -> pd.DataFrame:
    """Download historical data for `symbol` and normalize the DataFrame.

    Returns the DataFrame as returned by `yfinance.download`, but with MultiIndex
    columns dropped (if present) and a reset index so callers can assume a
    `Date` column exists.
    """
    df = yf.download(symbol, start=start, end=end, progress=progress, **kwargs)

    if df is None:
        return pd.DataFrame()

    if df.empty:
        return df

    # ensure Date is a column (reset_index) and flatten MultiIndex columns
    df = df.reset_index()
    if isinstance(df.columns, pd.MultiIndex):
        df.columns = df.columns.droplevel(-1)

    return df
