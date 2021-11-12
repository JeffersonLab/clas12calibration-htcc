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
		FILE_PATH)	FILE_PATH=${VALUE} ;;
		SKIM_NUM)	SKIM_NUM=${VALUE} ;;
	esac
done

#2 command line arguments
#RUN_NUM = number of run
#FILE_PATH = file path of skim6 run hipo
#USE SKIM6 FOR HTCC
echo "RUN_NUM = $RUN_NUM"
echo "FILE_PATH = $FILE_PATH"
echo ""

echo "CREATING DIR FOR CALIBRATION RESULTS..."
# -p flag checks if dir already exists
mkdir -p $PWD/CalibRes
mkdir -p $PWD/CalibRes/$RUN_NUM
mkdir -p $PWD/CalibRes/$RUN_NUM/$TODAY
echo "DIRECTORIES CREATED"
echo ""

echo "RUNNING GROOVY SCRIPT..."
echo "FILE TO USE = $FILE_PATH "
$COATJAVA/bin/run-groovy htccCalib.groovy $FILE_PATH
echo "GROOVY SCRIPT DONE"
echo ""

echo "MOVING RESULTS TO APPROPRIATE DIR..."
# get run number without the leading zeroes
mv $PWD/*.png $PWD/CalibRes/$RUN_NUM/$TODAY
mv $PWD/*.dat $PWD/CalibRes/$RUN_NUM/$TODAY
echo "FILES MOVED TO $PWD/CalibRes/$RUN_NUM/$TODAY"
echo ""

ls -R $PWD/CalibRes/$RUN_NUM/$TODAY
echo ""

echo "PROGRAM FINISHED."
