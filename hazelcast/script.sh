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
echo "🚀 Starting Hazelcast cluster and running Query 1..."

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

# Run client (Query 1)
echo "💻 Running Query 1..."
cd "$CLIENT_DIR"
./query1.sh -Daddresses='10.5.14.249:5701;10.5.14.249:5702' -DinPath=/Users/agostinasquillari/Documents/ITBA/4to_1C/POD/tp2/hazelcast_pod -DoutPath=/Users/agostinasquillari/Documents/ITBA/4to_1C/POD/tp2/hazelcast_pod

echo "✅ Query 1 finished!"