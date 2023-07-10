#!/bin/bash

WORKDIR=$(pwd)
cd $WORKDIR

# Initialize arguments
DATES=""
SUPERDIR=""
RUNNUMS=""

# Loop over all arguments
for ARGUMENT in "$@"
do
	KEY=$(echo $ARGUMENT | cut -f1 -d=)
	VALUE=$(echo $ARGUMENT | cut -f2 -d=)

	case "$KEY" in
		DATES)	DATES=($VALUE) ;;
		SUPERDIR)	SUPERDIR=$VALUE ;;
		RUNNUMS)	RUNNUMS=($VALUE) ;;
	esac
done

# Print out the arguments
echo "DATES = ${DATES[@]}"
echo "SUPERDIR = $SUPERDIR"
echo "RUNNUMS = ${RUNNUMS[@]}"
echo ""

# Iterate over run numbers
for ORIGINAL_RUN in ${RUNNUMS[@]}; do
	RUN=$ORIGINAL_RUN
	STRIPPED_RUN=$(echo $RUN | sed 's/^0*//')
	echo "Original run number: $RUN"
	echo "Stripped run number: $STRIPPED_RUN"

	# Flag to check if we've found the data
	DATA_FOUND=false

	# Loop over all provided dates
	for DATE in ${DATES[@]}; do
		if [ -d "$SUPERDIR/$RUN/$DATE" ] 
		then
			echo "Directory $SUPERDIR/$RUN/$DATE exists."
			echo "Compare GAIN for $RUN"
			./compareRunCCDB.sh RUN_NUM=$RUN PARAMETER="gain" FILE_PATH="$SUPERDIR/$RUN/$DATE/npePMT$STRIPPED_RUN.dat"
			echo "Compare TIME for $RUN"
			./compareRunCCDB.sh RUN_NUM=$RUN PARAMETER="time" FILE_PATH="$SUPERDIR/$RUN/$DATE/timePMT$STRIPPED_RUN.dat"
			echo "Completed comparison for $RUN" 

			# Move the files
			mv *$STRIPPED_RUN* $SUPERDIR/$RUN/$DATE/.
			echo "Moved files to $SUPERDIR/$RUN/$DATE/"
			ls $SUPERDIR/$RUN/$DATE/

			DATA_FOUND=true
			break
		fi
	done

	if ! $DATA_FOUND ; then
		echo "ERROR: Data for run $RUN was not found on any provided date."
	fi

	echo ""
done
