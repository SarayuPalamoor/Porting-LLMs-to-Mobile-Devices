#!/data/data/com.termux/files/usr/bin/bash

WATCH_FILE="$HOME/storage/downloads/prompt.txt"
SCRIPT_PATH="$HOME/calendar_project/llm_to_calendar.py"
HASH_FILE="$HOME/calendar_project/.last_prompt_hash"  # Save last hash here

echo "🔁 Watching for changes in $WATCH_FILE"

# Load the last hash from file if it exists
if [ -f "$HASH_FILE" ]; then
    LAST_HASH=$(cat "$HASH_FILE")
else
    LAST_HASH=""
fi

FILE_EXISTS=""

while true; do
    if [ -f "$WATCH_FILE" ]; then
        if [ "$FILE_EXISTS" != "yes" ]; then
            echo "✅ Found: $WATCH_FILE"
            FILE_EXISTS="yes"
        fi

        CURRENT_HASH=$(md5sum "$WATCH_FILE" | cut -d ' ' -f1)
        if [ "$CURRENT_HASH" != "$LAST_HASH" ]; then
            if [ -s "$WATCH_FILE" ]; then
                echo "📥 Detected new prompt. Running llm_to_calendar.py..."
                python "$SCRIPT_PATH" "$WATCH_FILE"
                echo "$CURRENT_HASH" > "$HASH_FILE"   # Save hash to disk
                LAST_HASH="$CURRENT_HASH"
            fi
        fi
    else
        if [ "$FILE_EXISTS" != "no" ]; then
            echo "⚠️ File not found: $WATCH_FILE"
            FILE_EXISTS="no"
        fi
    fi
    sleep 2
done
