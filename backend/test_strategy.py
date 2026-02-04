#!/usr/bin/env python3
import sys
import json

sys.path.append(r"e:\quant-ai-lab")

from backend.mcp_server.tools.strategy import (
    dip_investment_cagr,
    dip_investment_rolling_returns,
    dip_investment_xirr,
)


def run(symbol="^NSEI", start="2023-01-01", end="2025-12-31"):
    results = {}

    try:
        results['cagr'] = dip_investment_cagr(symbol, start, end, dip_percent=1.0, investment_amount=10000)
    except Exception as e:
        results['cagr_error'] = str(e)

    # try:
    #     results['rolling'] = dip_investment_rolling_returns(symbol, start, end, dip_percent=1.0, investment_amount=10000, window_days=252)
    # except Exception as e:
    #     results['rolling_error'] = str(e)

    try:
        results['xirr'] = dip_investment_xirr(symbol, start, end, dip_percent=1.0, investment_amount=10000, annualize_periods=252)
    except Exception as e:
        results['xirr_error'] = str(e)

    print(json.dumps(results, default=str, indent=2))


if __name__ == '__main__':
    run()
