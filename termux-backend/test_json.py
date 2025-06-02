import json

FILE = "/data/data/com.termux/files/home/storage/downloads/calendar_response.txt"

with open(FILE, "r") as f:
    data = json.load(f)

print("Title:", data.get("title"))
print("Start time:", data.get("start_time"))
print("End time:", data.get("end_time"))
