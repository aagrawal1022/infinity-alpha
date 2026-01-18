from fastmcp import Client

def test_server_connection():
    client = Client("http://127.0.0.1:8000")

    # This should not throw
    tools = client.list_tools()
    print("✅ Connected to MCP Server")
    print("Available tools:", tools)


if __name__ == "__main__":
    test_server_connection()
