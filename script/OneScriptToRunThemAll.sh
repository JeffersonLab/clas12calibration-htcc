#!/bin/bash

# Function to display usage instructions
usage() {
    echo "Usage: $0 FILE_PATH=/path/to/htcc/skims"
    echo "Example: $0 FILE_PATH=/data/htcc_skims"
    exit 1
}

# Parse command line arguments
for ARGUMENT in "$@"
do
    KEY=$(echo "$ARGUMENT" | cut -f1 -d=)
    VALUE=$(echo "$ARGUMENT" | cut -f2- -d=)

    case "$KEY" in
        FILE_PATH) FILE_PATH=${VALUE} ;;
        *)
            echo "Unknown argument: $KEY"
            usage
            ;;
    esac
done

# Check if FILE_PATH is set
if [ -z "$FILE_PATH" ]; then
    echo "Error: FILE_PATH is not set."
    usage
fi

# Confirm that FILE_PATH exists and is a directory
if [ ! -d "$FILE_PATH" ]; then
    echo "Error: FILE_PATH '$FILE_PATH' does not exist or is not a directory."
    exit 1
fi

#1 command line arguments
# FILE_PATH = file path of HTCC skim of run hipo files
echo "FILE_PATH = $FILE_PATH"
echo ""

# Determine the date
TODAY=$(date +%d-%b-%Y)
echo "TODAY'S DATE = $TODAY"

# Set SUPERDIR
SUPERDIR="$PWD/CalibRes"
echo "SUPERDIR = $SUPERDIR"

# Determine RUNNUMS
declare -a RUNNUMS
for f in "$FILE_PATH"/htcc*; do
    if [ -f "$f" ]; then
        fName="$(basename "$f")"
        fNoExt="$(basename -s .hipo "$fName")"
        fNum=${fNoExt//[!0-9]/}
        RUNNUMS+=("$fNum")
    fi
done

if [ ${#RUNNUMS[@]} -eq 0 ]; then
    echo "Error: No HTCC skim files found in $FILE_PATH."
    exit 1
fi

RUNNUMS_STR="${RUNNUMS[*]}"

echo "RUNNUMS = $RUNNUMS_STR"

echo "RUNNING SCRIPTS"

# Step 1
echo "STEP 1"
./calibrateMultipleRuns.sh FILE_PATH="$FILE_PATH"

# Step 2
echo "STEP 2"
./compareMultipleCCDB.sh DATES="$TODAY" SUPERDIR="$SUPERDIR" RUNNUMS="$RUNNUMS_STR"

# STEP 3
echo "STEP 3"
./createImages.sh DATES="$TODAY" SUPERDIR="$SUPERDIR" RUNNUMS="$RUNNUMS_STR"

# STEP 4
echo "STEP 4"
python3 generateComparisonPlots.py --run_nums "$RUNNUMS_STR" --dates "$TODAY" --top_dir "$SUPERDIR"
