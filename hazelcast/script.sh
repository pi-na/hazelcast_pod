#!/bin/bash
set -e  # Stop on first error

# === Check or create MYPATH variable ===
if [ -z "$MYPATH" ]; then
  echo "⚠️  MYPATH not found in environment variables."

  ENV_FILE=".env"
  PARENT_PATH="$(dirname "$(pwd)")"

  echo "📝 Creating $ENV_FILE with MYPATH=$PARENT_PATH"
  echo "MYPATH=$PARENT_PATH" > "$ENV_FILE"

  export MYPATH="$PARENT_PATH"
else
  echo "✅ MYPATH detected: $MYPATH"
fi

# === Detect and ask to kill old nodes ===
echo "🔍 Checking for old Hazelcast or Java nodes..."

# Detect by process name (java + hazelcast or project references)
OLD_PROCESSES=$(ps aux | grep -E "java.*(hazelcast|tpe2-g5|run-server|run-client)" | grep -v grep | awk '{print $2}')

# Detect by ports 5701 or 5702 (Hazelcast defaults)
PORT_PROCESSES=$(lsof -ti:5701,5702 2>/dev/null || true)

# Combine unique list
ALL_PROCESSES=$(echo -e "$OLD_PROCESSES\n$PORT_PROCESSES" | sort -u | grep -v '^$' || true)

if [ -n "$ALL_PROCESSES" ]; then
  COUNT=$(echo "$ALL_PROCESSES" | wc -l | tr -d ' ')
  echo "⚠️  Detected $COUNT running Hazelcast/Java processes:"

  # Detect platform
  OS_TYPE=$(uname -s)

  if [[ "$OS_TYPE" == "Darwin" ]]; then
    # macOS ps syntax
    for PID in $ALL_PROCESSES; do
      PROC_LINE=$(ps -p "$PID" -o pid=,comm= | awk '{$1=$1;print}')
      echo "  • $PROC_LINE"
    done
  else
    # Linux ps syntax
    ps -p $ALL_PROCESSES -o pid,comm,args --no-headers | awk '{print "  • "$0}'
  fi

  echo ""
  echo -n "Do you want to kill them? (n/Y): "
  read -n1 KILL_CHOICE
  echo ""
  if [[ "$KILL_CHOICE" =~ ^[yY]$ ]]; then
    echo "💀 Killing $COUNT old processes..."
    kill -9 $ALL_PROCESSES || true
    echo "✅ All old processes killed."
  else
    echo "⏭️  Keeping old processes alive."
  fi
else
  echo "🧹 No old Hazelcast or Java nodes found."
fi

# === Ask if user wants to compile ===
echo -n "Do you want to compile the project? (y/N): "
read -n1 COMPILE_CHOICE
echo ""  # line break
COMPILE_CHOICE=${COMPILE_CHOICE:-n}

if [[ "$COMPILE_CHOICE" =~ ^[yY]$ ]]; then
  echo "🚀 Cleaning and packaging project..."
  mvn clean
  mvn package -DskipTests
else
  echo "⏭️  Skipping compilation."
fi

# === Process modules ===
MODULES=("api" "server" "client")

for MODULE in "${MODULES[@]}"; do
  echo "📦 Processing module: $MODULE"

  cd "$MODULE/target" || { echo "❌ Target folder not found for $MODULE"; exit 1; }

  TAR_FILE=$(ls tpe2-g5-${MODULE}-*-bin.tar.gz 2>/dev/null | head -n 1)
  if [ -z "$TAR_FILE" ]; then
    echo "⚠️  No tar.gz found for $MODULE"
    cd ../../
    continue
  fi

  echo "🗜️  Extracting $TAR_FILE..."
  tar -xzf "$TAR_FILE"
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
echo "4) Query 4 - Longest wait time by pickup zone"
echo "5) Query 5 - Total YTD miles by company"
read -p "Enter your choice (1, 2, 3, 4, or 5): " QUERY_CHOICE

if [[ "$QUERY_CHOICE" != "1" && "$QUERY_CHOICE" != "2" && "$QUERY_CHOICE" != "3" && "$QUERY_CHOICE" != "4" && "$QUERY_CHOICE" != "5" ]]; then
  echo "❌ Invalid choice. Please run the script again and select 1, 2, 3, 4, or 5."
  exit 1
fi

echo "🚀 Starting Hazelcast cluster and running Query $QUERY_CHOICE..."

# Paths to server and client
SERVER_DIR="server/target/$(ls server/target | grep tpe2-g5-server- | head -n 1)"
CLIENT_DIR="client/target/$(ls client/target | grep tpe2-g5-client- | head -n 1)"
ADDRESS="127.0.0.1:5701"

# Launch servers
echo "🟢 Launching Node 1..."
osascript -e "tell application \"Terminal\" to do script \"cd $(pwd)/$SERVER_DIR && ./run-server.sh\""

echo "⏳ Waiting for cluster to initialize..."
sleep 5

# Run client based on selection
cd "$CLIENT_DIR"
MYPATH="/Users/agostinasquillari/Documents/ITBA/4to_1C/POD/tp2/hazelcast_pod"
ADDRESS="127.0.0.1:5701;127.0.0.1:5702"

if [ "$QUERY_CHOICE" == "1" ]; then
  echo "💻 Running Query 1..."
  ./query1.sh -Daddresses=$ADDRESS -DinPath=$MYPATH -DoutPath=$MYPATH
  echo "📄 Results: $MYPATH/query1.csv"
  echo "⏱️  Time: $MYPATH/time1.txt"
elif [ "$QUERY_CHOICE" == "2" ]; then
  echo "💻 Running Query 2..."
  ./query2.sh -Daddresses=$ADDRESS -DinPath=$MYPATH -DoutPath=$MYPATH
  echo "📄 Results: $MYPATH/query2.csv"
  echo "⏱️  Time: $MYPATH/time2.txt"
elif [ "$QUERY_CHOICE" == "3" ]; then
  echo "💻 Running Query 3..."
  ./query3.sh -Daddresses=$ADDRESS -DinPath=$MYPATH -DoutPath=$MYPATH
  echo "📄 Results: $MYPATH/query3.csv"
  echo "⏱️  Time: $MYPATH/time3.txt"
elif [ "$QUERY_CHOICE" == "4" ]; then
  echo "💻 Running Query 4..."
  read -p "Enter borough name (e.g., Manhattan, Brooklyn, Queens): " BOROUGH
  ./query4.sh -Daddresses=$ADDRESS -DinPath=$MYPATH -DoutPath=$MYPATH -Dborough="$BOROUGH"
  echo "📄 Results written to: $MYPATH/query4.csv"
  echo "⏱️  Time log written to: $MYPATH/time4.txt"
else
  echo "💻 Running Query 5..."
  ./query5.sh -Daddresses=$ADDRESS -DinPath=$MYPATH -DoutPath=$MYPATH
  echo "📄 Results: $MYPATH/query5.csv"
  echo "⏱️  Time: $MYPATH/time5.txt"
fi

