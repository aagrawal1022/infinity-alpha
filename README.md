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


// Prompts:

1. Analyze Cgar of NIFTY 50 (^NSEI), my invested if i would have invested 10000 Rs whenever this is fall of 1% or more for period for jan 25 to dec 25