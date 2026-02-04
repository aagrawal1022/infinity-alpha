from tools.yahoo import mcp

if __name__ == "__main__":
    # Explicitly enable HTTP transport
    mcp.run(transport="streamable-http")
