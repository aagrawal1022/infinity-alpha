import pandas as pd

def calculate_returns(prices: list):
    df = pd.DataFrame(prices)
    df["return"] = df["Close"].pct_change()
    return df[["Date", "return"]].dropna().to_dict(orient="records")
