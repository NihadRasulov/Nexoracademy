#!/bin/bash

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "========================================"
echo "    Nexora Chatbot API - Starting..."
echo "========================================"

# Start python app in background
cd "$SCRIPT_DIR/app" && python3 main.py &
APP_PID=$!

# Start ngrok in background
ngrok http 8000 --log=stdout > /dev/null 2>&1 &
NGROK_PID=$!

# Function to clean up background processes on exit
cleanup() {
    echo ""
    echo "Shutting down..."
    kill $APP_PID $NGROK_PID 2>/dev/null
    echo "Done."
    exit 0
}

# Trap CTRL+C (SIGINT) and SIGTERM to ensure clean shutdown
trap cleanup SIGINT SIGTERM

echo ""
echo "Server and ngrok starting in background..."
echo ""
echo "    Server:  http://localhost:8000"
echo "    ngrok:   http://127.0.0.1:4040"
echo ""
echo "Press ENTER to stop all services..."
read -r

# Run cleanup if user hits ENTER
cleanup
