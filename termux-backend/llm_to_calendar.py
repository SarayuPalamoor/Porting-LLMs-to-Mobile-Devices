import uuid
import requests
import re
from datetime import datetime
import pytz
import sys
import os
import subprocess
import json
import time

# === CONFIG ===
DOWNLOADS = os.path.expanduser("~/storage/downloads/")
PROMPT_FILE = os.path.join(DOWNLOADS, "prompt.txt")
RESPONSE_FILE = os.path.join(DOWNLOADS, "response.txt")
ICS_FILE = os.path.join(DOWNLOADS, "event.ics")

# === Step 1: Read prompt ===
if not os.path.exists(PROMPT_FILE):
    print("❌ prompt.txt not found in Downloads.")
    sys.exit(1)

with open(PROMPT_FILE, "r") as f:
    prompt = f.read().strip()

# === Step 2: Send prompt to LLM ===
try:
    response = requests.post(
        "http://localhost:8081/v1/completions",
        headers={"Content-Type": "application/json"},
        json={"prompt": prompt, "max_tokens": 150}
    )
    response.raise_for_status()
    llm_text = response.json()["choices"][0]["text"].strip()
except Exception as e:
    print(f"❌ Failed to get response from LLM: {e}")
    sys.exit(1)

# ✅ Print LLM raw output
print("🧠 LLM Output:\n" + llm_text + "\n")

# Save to response.txt
with open(RESPONSE_FILE, "w") as f:
    f.write(llm_text)

# === Step 3: Extract and parse JSON from LLM response ===
try:
    match = re.search(r'{.*}', llm_text, re.DOTALL)
    if not match:
        raise ValueError("No JSON object found in LLM response.")
    event = json.loads(match.group(0))
except Exception as e:
    print(f"❌ Failed to parse JSON: {e}")
    sys.exit(1)

# === Step 4: Extract fields ===
title = event.get("summary", "LLM Event")
location = event.get("location", "Unknown Location")
description = event.get("description", "Created by LLM")

tz = pytz.timezone("Asia/Kolkata")
try:
    start_dt = datetime.fromisoformat(event["start"]["dateTime"].replace("Z", "+00:00")).astimezone(tz)
    end_dt = datetime.fromisoformat(event["end"]["dateTime"].replace("Z", "+00:00")).astimezone(tz)
except Exception as e:
    print(f"❌ Invalid datetime: {e}")
    sys.exit(1)

start_ts = int(start_dt.timestamp() * 1000)
end_ts = int(end_dt.timestamp() * 1000)

# === Step 5: Write ICS file ===
uid = f"{uuid.uuid4()}@llm"
ics_content = f"""BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//LLM Scheduler//EN
BEGIN:VEVENT
UID:{uid}
DTSTART:{start_dt.strftime("%Y%m%dT%H%M%S")}
DTEND:{end_dt.strftime("%Y%m%dT%H%M%S")}
SUMMARY:{title}
LOCATION:{location}
DESCRIPTION:{description}
DTSTAMP:{datetime.now(tz).strftime("%Y%m%dT%H%M%S")}
END:VEVENT
END:VCALENDAR
"""
with open(ICS_FILE, "w") as f:
    f.write(ics_content)

# === Step 6: Show toast ===
subprocess.run(["termux-toast", "📅 Event saved. Calendar will open shortly..."])

# === Step 7: Show notification ===
subprocess.run([
    "termux-notification",
    "--title", f"Event Created: {title}",
    "--content", f"{start_dt.strftime('%Y-%m-%d %H:%M')} at {location}",
    "--priority", "high"
])

# === Step 8: Wait and launch intent ===
time.sleep(5)

intent = [
    "am", "start",
    "-a", "android.intent.action.INSERT",
    "-t", "vnd.android.cursor.item/event",
    "--el", "beginTime", str(start_ts),
    "--el", "endTime", str(end_ts),
    "--ez", "allDay", "false",
    "--es", "title", title,
    "--es", "description", description,
    "--es", "eventLocation", location,
    "--es", "eventTimezone", "Asia/Kolkata"
]

try:
    subprocess.run(intent, check=True)
except subprocess.CalledProcessError as e:
    print("⚠️ Intent failed. Try opening the calendar manually.")
    print("📄 You can still import:", ICS_FILE)
