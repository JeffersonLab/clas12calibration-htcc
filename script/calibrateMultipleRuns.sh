#!/bin/bash

echo 'Setting up environment'
source /group/clas12/packages/setup.sh
module load clas12
module load workflow
echo 'Checking environment'
module list

WORKDIR=$(pwd)
echo "WORK DIR = $WORKDIR"

#for and do loop to pass in keyword command line inputs
#order of arguments do not matter if you use the key
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

echo ls $FILE_PATH
ls $FILE_PATH

COUNT=0
for f in $FILE_PATH/htcc*
do
	fName="$(basename $f)"
	fNoExt="$(basename -s .hipo $fName)"
	fNum=${fNoExt//[!0-9]/}
	echo "RUNNING COMMAND: bash $WORKDIR/htccCalib.sh RUN_NUM=$fNum FILE_PATH=$f"
	bash $WORKDIR/htccCalib.sh RUN_NUM=$fNum FILE_PATH=$f
	(( COUNT++ ))
done
echo "FILES ANALYZED: $COUNT"
