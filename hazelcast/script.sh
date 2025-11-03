#!/bin/bash
set -e  # Stop on first error

echo "🚀 Cleaning and packaging project..."
mvn clean
mvn package -DskipTests

# Array of modules
MODULES=("api" "server" "client")

for MODULE in "${MODULES[@]}"; do
  echo "📦 Processing module: $MODULE"

  cd "$MODULE/target" || { echo "❌ Target folder not found for $MODULE"; exit 1; }

  # Detect the tar.gz automatically
  TAR_FILE=$(ls tpe2-g5-${MODULE}-*-bin.tar.gz 2>/dev/null | head -n 1)

  if [ -z "$TAR_FILE" ]; then
    echo "⚠️  No tar.gz found for $MODULE"
    cd ../../
    continue
  fi

  echo "🗜️  Extracting $TAR_FILE..."
  tar -xzf "$TAR_FILE"

  # Find the extracted directory name dynamically
  EXTRACTED_DIR=$(tar -tzf "$TAR_FILE" | head -1 | cut -f1 -d"/")

  cd "$EXTRACTED_DIR" || { echo "❌ Extracted dir not found for $MODULE"; exit 1; }

  echo "🔑 Setting execute permissions for .sh files..."
  chmod u+x *.sh

  echo "✅ Done with $MODULE"
  cd ../../../
done

echo "🎉 All modules built and unpacked successfully!"

# Ask which query to run
echo ""
echo "Which query do you want to run?"
echo "1) Query 1 - Total trips by pickup and dropoff zone"
echo "2) Query 2 - Longest trip within NYC by pickup zone"
echo "3) Query 3 - Average fare by borough and company"
echo "5) Query 5 - Total YTD miles by company"
read -p "Enter your choice (1, 2, 3, or 5): " QUERY_CHOICE

if [[ "$QUERY_CHOICE" != "1" && "$QUERY_CHOICE" != "2" && "$QUERY_CHOICE" != "3" && "$QUERY_CHOICE" != "5" ]]; then
  echo "❌ Invalid choice. Please run the script again and select 1, 2, 3, or 5."
  exit 1
fi

echo "🚀 Starting Hazelcast cluster and running Query $QUERY_CHOICE..."

# Paths to server and client
SERVER_DIR="server/target/$(ls server/target | grep tpe2-g5-server- | head -n 1)"
CLIENT_DIR="client/target/$(ls client/target | grep tpe2-g5-client- | head -n 1)"

# Run two server nodes in separate terminals
echo "🟢 Launching Node 1..."
osascript -e "tell application \"Terminal\" to do script \"cd $(pwd)/$SERVER_DIR && ./run-server.sh\""

sleep 2

echo "🟢 Launching Node 2..."
osascript -e "tell application \"Terminal\" to do script \"cd $(pwd)/$SERVER_DIR && ./run-server.sh\""

# Give servers time to start
echo "⏳ Waiting for cluster to initialize..."
sleep 10

# Run client based on selection
cd "$CLIENT_DIR"
MYPATH="/Users/jperalb/Documents/ITBA/POD/hazelcast_pod"
ADDRESS="127.0.0.1:5701;127.0.0.1:5702"

if [ "$QUERY_CHOICE" == "1" ]; then
  echo "💻 Running Query 1..."
  ./query1.sh -Daddresses=$ADDRESS -DinPath=$MYPATH -DoutPath=$MYPATH
  echo "✅ Query 1 finished!"
  echo "📄 Results written to: $MYPATH/query1.csv"
  echo "⏱️  Time log written to: $MYPATH/time1.txt"
elif [ "$QUERY_CHOICE" == "2" ]; then
  echo "💻 Running Query 2..."
  ./query2.sh -Daddresses=$ADDRESS -DinPath=$MYPATH -DoutPath=$MYPATH
  echo "✅ Query 2 finished!"
  echo "📄 Results written to: $MYPATH/query2.csv"
  echo "⏱️  Time log written to: $MYPATH/time2.txt"
elif [ "$QUERY_CHOICE" == "3" ]; then
  echo "💻 Running Query 3..."
  ./query3.sh -Daddresses=$ADDRESS -DinPath=$MYPATH -DoutPath=$MYPATH
  echo "✅ Query 3 finished!"
  echo "📄 Results written to: $MYPATH/query3.csv"
  echo "⏱️  Time log written to: $MYPATH/time3.txt"
else
  echo "💻 Running Query 5..."
  ./query5.sh -Daddresses=$ADDRESS -DinPath=$MYPATH -DoutPath=$MYPATH
  echo "✅ Query 5 finished!"
  echo "📄 Results written to: $MYPATH/query5.csv"
  echo "⏱️  Time log written to: $MYPATH/time5.txt"
fi
