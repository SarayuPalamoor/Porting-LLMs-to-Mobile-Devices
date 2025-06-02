#!/data/data/com.termux/files/usr/bin/bash

echo "🚀 Starting LLM + Watcher"

cd ~/llama.cpp/build/bin
nohup ./llama-server -m ~/llama.cpp/models/orca_mini_v9_6_3B-Instruct.Q5_K_M.gguf --port 8081 > /dev/null 2>&1 &

cd ~/calendar_project
nohup bash watch_prompt.sh > /dev/null 2>&1 &
