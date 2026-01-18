import yfinance as yf
from fastmcp import FastMCP

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
