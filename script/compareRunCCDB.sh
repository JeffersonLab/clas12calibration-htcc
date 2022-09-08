#!/bin/bash
echo 'Setting up environment'
source /group/clas12/packages/setup.sh
module load clas12
module load workflow
echo 'Checking environment'
module list
echo ""

#for and do loop to pass in keyword command line inputs
#order of arguments do not matter if you use the key
for ARGUMENT in "$@"
do
	KEY=$(echo $ARGUMENT | cut -f1 -d=)
	VALUE=$(echo $ARGUMENT | cut -f2 -d=)

	case "$KEY" in
		RUN_NUM)	RUN_NUM=${VALUE} ;;
		FILE_PATH)	FILE_PATH=${VALUE} ;;
		PARAMETER)	PARAMETER=${VALUE} ;;
	esac
done

#3 command line arguments
#RUN_NUM = number of run
#FILE_PATH = file path of HTCC skim of run hipo file
#PARAMETER = gain or time
echo "RUN_NUM = $RUN_NUM"
echo "FILE_PATH = $FILE_PATH"
echo "PARAMETER = $PARAMETER"
echo ""

echo "Accessing ccdb for run $RUN_NUM"
CCDB_OUT="ccdb_"$PARAMETER"_run"$RUN_NUM".dat"
if [[ -f "$CCDB_OUT" ]]; then
    rm $CCDB_OUT
fi
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 dump /calibration/htcc/$PARAMETER -r $RUN_NUM > $CCDB_OUT
cat $CCDB_OUT

if [[ -f "$FILE_PATH" ]]; then
    echo "file to compare to ccdb data"
    cat $FILE_PATH
fi

echo ""
echo "RUNNING PYTHON SCRIPT"

python3 compareRunCCDB.py $RUN_NUM $PARAMETER $CCDB_OUT $FILE_PATH 
