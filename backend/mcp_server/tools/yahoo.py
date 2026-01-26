import yfinance as yf
from fastmcp import FastMCP
from tools.strategy import dip_investment_cagr as calculate_cagr

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