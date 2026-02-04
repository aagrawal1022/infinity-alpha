import yfinance as yf
from fastmcp import FastMCP
from tools.strategy import (
    dip_investment_cagr as calculate_cagr,
    dip_investment_rolling_returns as calculate_rolling_returns,
    dip_investment_xirr as calculate_xirr,
)

mcp = FastMCP()

@mcp.tool()
def get_stock_history(symbol: str, start: str, end: str):
    """
    Fetch historical OHLC data using Yahoo Finance.
    Example:
      RELIANCE.NS
      ^NSEI
    """
    df = yf.download(symbol, start=start, end=end, progress=False)

    if df.empty:
        return []

    return df.reset_index().to_dict(orient="records")

@mcp.tool()
def dip_investment_cagr(
    symbol: str,
    start: str,
    end: str,
    dip_percent: float = 1.0,
    investment_amount: float = 10000.0
):
    """
    Calculate CAGR for a dip investment strategy.
    Invest a fixed amount whenever the stock dips by a certain percentage.
    """
    result = calculate_cagr(
        symbol=symbol,
        start=start,
        end=end,
        dip_percent=dip_percent,
        investment_amount=investment_amount
    )
    return result


@mcp.tool()
def dip_investment_rolling_returns(
    symbol: str,
    start: str,
    end: str,
    dip_percent: float = 1.0,
    investment_amount: float = 10000.0,
    window_days: int = 252,
):
    """MCP tool wrapper for `dip_investment_rolling_returns` in tools.strategy."""
    result = calculate_rolling_returns(
        symbol=symbol,
        start=start,
        end=end,
        dip_percent=dip_percent,
        investment_amount=investment_amount,
        window_days=window_days,
    )
    return result


@mcp.tool()
def dip_investment_xirr(
    symbol: str,
    start: str,
    end: str,
    dip_percent: float = 1.0,
    investment_amount: float = 10000.0,
    annualize_periods: int = 252,
):
    """MCP tool wrapper for `dip_investment_xirr` in tools.strategy."""
    result = calculate_xirr(
        symbol=symbol,
        start=start,
        end=end,
        dip_percent=dip_percent,
        investment_amount=investment_amount,
        annualize_periods=annualize_periods,
    )
    return result