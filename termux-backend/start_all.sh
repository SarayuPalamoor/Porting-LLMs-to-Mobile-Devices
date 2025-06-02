#!/data/data/com.termux/files/usr/bin/bash

echo "🚀 Starting LLM Server..."
cd ~/llama.cpp/build/bin || exit
./llama-server -m ~/llama.cpp/models/orca_mini_v9_6_3B-Instruct.Q5_K_M.gguf --port 8081 &
LLM_PID=$!

sleep 3  # Give LLM time to start

echo "👀 Starting Prompt Watcher..."
cd ~/calendar_project || exit
./watch_prompt.sh &

wait $LLM_PID
