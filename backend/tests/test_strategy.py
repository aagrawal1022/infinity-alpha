import pandas as pd
import numpy as np
import pytest
import datetime

from backend.mcp_server import tools
from backend.mcp_server.tools import strategy


def _mock_df():
    # Create three dates across ~3 years
    dates = pd.to_datetime(["2023-01-01", "2024-01-01", "2025-12-31"]) 
    # Prices: start 100, drop to 90 (10% drop -> dip), finish 120
    close = [100.0, 90.0, 120.0]
    data = pd.DataFrame({
        "Close": close,
        "Open": close,
        "High": close,
        "Low": close,
        "Volume": [1000, 1000, 1000],
    }, index=dates)
    data.index.name = 'Date'
    return data


def test_dip_investment_cagr_monkeypatched(monkeypatch):
    df = _mock_df()

    def fake_download(symbol, start, end, progress=False):
        return df

    monkeypatch.setattr(strategy, 'yf', strategy.yf)
    monkeypatch.setattr(strategy.yf, 'download', fake_download)

    res = strategy.dip_investment_cagr("ANY", "2023-01-01", "2025-12-31", dip_percent=5.0, investment_amount=1000)

    assert res["total_invested"] == 1000.0
    assert res["investment_count"] == 1
    # final value = units * final_price = (1000/90)*120 = 1333.333...
    assert pytest.approx(res["final_value"], rel=1e-3) == (1000.0 / 90.0) * 120.0


def test_dip_investment_rolling_returns_monkeypatched(monkeypatch):
    df = _mock_df()

    def fake_download(symbol, start, end, progress=False):
        return df

    monkeypatch.setattr(strategy, 'yf', strategy.yf)
    monkeypatch.setattr(strategy.yf, 'download', fake_download)

    # use window_days=1 so rolling return between consecutive rows is computed
    res = strategy.dip_investment_rolling_returns("ANY", "2023-01-01", "2025-12-31", dip_percent=5.0, investment_amount=1000, window_days=1)

    assert res["investment_count"] == 1
    # Rolling series should contain an entry for the last date with ~33.333% return
    last = res["rolling_series"][-1]
    assert last["date"] == "2025-12-31"
    assert pytest.approx(last["rolling_return_percent"], rel=1e-3) == ((120.0 / 90.0) - 1) * 100


def test_dip_investment_xirr_monkeypatched(monkeypatch):
    df = _mock_df()

    def fake_download(symbol, start, end, progress=False):
        return df

    monkeypatch.setattr(strategy, 'yf', strategy.yf)
    monkeypatch.setattr(strategy.yf, 'download', fake_download)

    res = strategy.dip_investment_xirr("ANY", "2023-01-01", "2025-12-31", dip_percent=5.0, investment_amount=1000, annualize_periods=365)

    assert res["investment_count"] == 1
    assert res["total_invested"] == 1000.0
    assert res["final_value"] == pytest.approx((1000.0 / 90.0) * 120.0, rel=1e-6)
    # XIRR should be a positive annualized number ~15% (approx)
    assert res["xirr_percent"] is not None
    assert 5.0 < res["xirr_percent"] < 30.0
