from google import genai
from fastmcp import Client
from config import GEMINI_API_KEY, MODEL, MCP_SERVER_URL
from prompts import SYSTEM_PROMPT
import json
import asyncio


model = genai.Client(api_key=GEMINI_API_KEY)
mcp = Client(MCP_SERVER_URL)


async def run_agent(user_query: str):
    print(f"\n[LOG] Starting agent with query: {user_query}")
    
    # Step 1: Ask Gemini what data is needed
    print("[LOG] Step 1: Generating plan with Gemini...")
    planning_prompt = f"""
{SYSTEM_PROMPT}

User query:
{user_query}

If stock data is required, respond ONLY in JSON like:
{{
  "tool": "get_stock_history",
  "symbol": "SYMBOL_HERE",
  "start": "YYYY-MM-DD",
  "end": "YYYY-MM-DD"
}}

Otherwise respond with:
{{ "tool": null }}
"""

    plan = model.models.generate_content(model=MODEL,contents=planning_prompt).text.strip()
    print(f"[LOG] Plan response: {plan}")

    try:
        # Remove markdown code blocks if present
        plan_text = plan
        if plan_text.startswith("```"):
            plan_text = plan_text.split("```")[1].strip()
            if plan_text.startswith("json"):
                plan_text = plan_text[4:].strip()
        
        plan_json = json.loads(plan_text)
        print(f"[LOG] Parsed plan JSON: {plan_json}")
    except Exception as e:
        print(f"[LOG] Failed to parse JSON, returning raw response: {e}")
        return plan  # Gemini answered directly

    # Step 2: Call MCP tool if needed
    if plan_json.get("tool") == "get_stock_history":
        print(f"[LOG] Step 2: Calling MCP tool 'get_stock_history' with params: {plan_json}")
        data = await mcp.call_tool(
            "get_stock_history",
            {
                "symbol": plan_json["symbol"],
                "start": plan_json["start"],
                "end": plan_json["end"],
            }
        )
        print(f"[LOG] Stock data received, length: {len(str(data))} characters")

        # Step 3: Send data back to Gemini for analysis
        print("[LOG] Step 3: Sending data to Gemini for analysis...")
        analysis_prompt = f"""
{SYSTEM_PROMPT}

User question:
{user_query}

Here is the historical stock data:
{data}

Provide analysis.
"""
        result = model.models.generate_content(model=MODEL, contents=analysis_prompt).text
        print("[LOG] Analysis complete, returning result")
        return result

    # Step 4: No tool needed
    print("[LOG] Step 4: No tool needed, returning plan as-is")
    return plan


if __name__ == "__main__":
    print("[LOG] Starting agent loop...")
    async def main():
        async with mcp:
            while True:
                q = input("\nAsk stock question (or 'exit'): ")
                if q.lower() == "exit":
                    print("[LOG] Exiting agent")
                    break
                print("[LOG] Processing user input...")
                print(await run_agent(q))
    
    asyncio.run(main())
