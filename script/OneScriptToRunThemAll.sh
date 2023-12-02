#!/bin/bash

for ARGUMENT in "$@"
do
	KEY=$(echo $ARGUMENT | cut -f1 -d=)
	VALUE=$(echo $ARGUMENT | cut -f2 -d=)

	case "$KEY" in
		FILE_PATH)	FILE_PATH=${VALUE} ;;
	esac
done

#1 command line arguments
#FILE_PATH = file path of HTCC skim of run hipo file
echo "FILE_PATH = $FILE_PATH"
echo ""

# Determine the date
TODAY=$(date +%d-%b-%Y)
echo "TODAY'S DATE = $TODAY"

# Set SUPERDIR
SUPERDIR="$PWD/CalibRes"
echo $SUPERDIR

# Determine RUNNUMS
declare -a RUNNUMS
for f in $FILE_PATH/htcc*; do
    fName="$(basename $f)"
    fNoExt="$(basename -s .hipo $fName)"
    fNum=${fNoExt//[!0-9]/}
    RUNNUMS+=($fNum)
done

#RUNNUMS_STR=$(IFS=,; echo "${RUNNUMS[*]}")
RUNNUMS_STR="${RUNNUMS[*]}"

echo "RUNNUMS = $RUNNUMS_STR"

echo "RUNNING SCRIPTS"

# Step 1
echo "STEP 1"
./calibrateMultipleRuns.sh FILE_PATH=$FILE_PATH

# Step 2
echo "STEP 2"
# Step 2
./compareMultipleCCDB.sh DATES=$TODAY SUPERDIR=$SUPERDIR RUNNUMS=$RUNNUMS_STR

# STEP 3
echo "STEP 3"
./createImages.sh DATES=$TODAY SUPERDIR=$SUPERDIR RUNNUMS=$RUNNUMS_STR

#STEP 4
echo "STEP 4"
python3 generateComparisonPlots.py --run_nums $RUNNUMS_STR --dates $TODAY --top_dir $SUPERDIR

