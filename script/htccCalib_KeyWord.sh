#!/bin/bash

echo "CURRENT DIR = $PWD"
TODAY=$(date +%d-%b-%Y)
echo "TODAY'S DATE = $TODAY"
echo ""

echo "SOURCING ENVIRONMENT..."
bash $PWD"/HTCC_CalibEnviro.sh"

#for and do loop to pass in keyword command line inputs
#order of arguments do not matter if you use the key
for ARGUMENT in "$@"
do
	KEY=$(echo $ARGUMENT | cut -f1 -d=)
	VALUE=$(echo $ARGUMENT | cut -f2 -d=)

	case "$KEY" in
		RUN_NUM)	RUN_NUM=${VALUE} ;;
		RUN_DIR)	RUN_DIR=${VALUE} ;;
		SKIM_NUM)	SKIM_NUM=${VALUE} ;;
	esac
done

#3 command line arguments
#RUN_NUM = number of run
#SKIM_NUM = skim of run, check dir name for skim
#RUN_DIR = dir of run
echo "RUN_NUM = $RUN_NUM"
echo "SKIM_NUM = $SKIM_NUM"
echo "RUN_DIR = $RUN_DIR"
HIPO=$SKIM_NUM"_"$RUN_NUM
echo "HIPO FILE = $HIPO"
echo ""

echo "CREATING DIR FOR CALIBRATION RESULTS..."
# -p flag checks if dir already exists
mkdir -p $PWD/CalibRes
mkdir -p $PWD/CalibRes/$RUN_NUM
mkdir -p $PWD/CalibRes/$RUN_NUM/$TODAY
echo "DIRECTORIES CREATED"
echo ""

echo "RUNNING GROOVY SCRIPT..."
echo "FILE TO USE = $RUN_DIR$SKIM_NUM"/"$HIPO "
#$COATJAVA/bin/run-groovy htcc_Calib_Chnaged.groovy $RUN_DIR$SKIM_NUM"/"$HIPO
echo "GROOVY SCRIPT DONE"
echo ""

echo "MOVING RESULTS TO APPROPRIATE DIR..."
# get run number without the leading zeroes
RUN_NUM_No0=$(echo $RUN_NUM | sed 's/^0*//')
#echo "$RUN_NUM_No0"
mv $PWD/*$RUN_NUM_No0.png $PWD/CalibRes/$RUN_NUM/$TODAY
mv $PWD/*$RUN_NUM_No0.dat $PWD/CalibRes/$RUN_NUM/$TODAY
echo "FILES MOVED TO $PWD/CalibRes/$RUN_NUM/$TODAY"
echo ""

ls -R $PWD/CalibRes/$RUN_NUM/$TODAY
echo ""

echo "PROGRAM FINISHED."
