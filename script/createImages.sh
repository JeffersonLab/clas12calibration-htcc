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

for RUN in ${RUNNUMS[@]}; do
	# Strip leading zeros for some operations
	STRIPPED_RUN=$(echo $RUN | sed 's/^0*//')
	
	echo "Original run number: $RUN"
	echo "Stripped run number: $STRIPPED_RUN"

	GAINPPLOT="nphePMT_ZOOM_$STRIPPED_RUN.png"  
	TIMEPLOT="timePMT$STRIPPED_RUN.png"
	INFO_NPHE_PLOT="infoplots_NPHE$STRIPPED_RUN.png"
	INFO_TIME_PLOT="infoplots_Time$STRIPPED_RUN.png"

	# Flag to check if we've found the data
	DATA_FOUND=false

	# Loop over all provided dates
	for DATE in ${DATES[@]}; do
		DATAPATH="$SUPERDIR/$RUN/$DATE"
		if [ -d $DATAPATH ]
		then
			echo "$DATAPATH EXISTS!"
			cd $DATAPATH
			convert $GAINPPLOT $TIMEPLOT -append "Combo_GainTime_$RUN.png"
			convert $INFO_NPHE_PLOT $INFO_TIME_PLOT +append "Combo_Info_$RUN.png"
			echo "FILES IN $SUPERDIR/$RUN/$DATE/"
			ls $SUPERDIR/$RUN/$DATE/
			DATA_FOUND=true
			break
		fi
	done
	
	if ! $DATA_FOUND ; then
		echo "ERROR: DIRECTORY FOR RUN $RUN DOES NOT EXIST ON ANY PROVIDED DATE!"
	fi
done 
