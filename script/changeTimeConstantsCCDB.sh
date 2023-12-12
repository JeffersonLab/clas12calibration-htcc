#!/bin/bash

# Check for minimum number of arguments
if [ "$#" -lt 3 ]; then
    echo "Usage: $0 timeShift minRuns maxRuns"
    exit 1
fi

timeShift=$1
MIN_RUNS=($2)  # Expecting a space-separated list
MAX_RUNS=($3)  # Expecting a space-separated list

echo 'Setting up environment'
source /group/clas12/packages/setup.sh
module load clas12
module load workflow
echo 'Checking environment'
module list

#MIN_RUNS=(6608 6633 6634 6662 6663 6687 6688 6742 6743)
#MIN_RUNS=(6156 6164 6216 6217 6223 6225 6226 6235 6252 6253 6255 6256 6267 6285 6303 6304 6321 6322 6330 6331 6349 6350 6351 6361 6362 6368 6369 6379 6380 6386 6387 6420 6431 6432 6449 6450 6456 6457 6471 6472 6515 6517 6546 6549 6550 6563 6564 6573 6574 6576 6577 6595 6596 6599 6600)
#MAX_RUNS=(6632 6633 6661 6662 6686 6687 6741 6742 6783)
#MAX_RUNS=(6163 6215 6216 6222 6224 6225 6234 6251 6252 6254 6255 6266 6284 6302 6303 6320 6321 6329 6330 6348 6349 6350 6360 6361 6367 6368 6378 6379 6385 6386 6419 6430 6431 6448 6449 6455 6456 6470 6471 6514 6516 6545 6548 6549 6562 6563 6572 6573 6575 6576 6594 6595 6598 6599 6603)

#-----RUN RANGES FOR A SPECIFIC PERIOD-----#
#these specify a run ranges in which you wish to change a time constants. Range is (min[i], max[i])
#MIN_RUNS=(5674 5675 5886 5893 5894)
#MAX_RUNS=(5674 5885 5892 5893 6000)
#MIN_RUNS=(11093 11244 11245 11247 11250)
#MAX_RUNS=(11243 11244 11246 11249 11283)

NUMRUNS=${#MIN_RUNS[@]}
echo "number of runs is $NUMRUNS"

#for i in {0..5}
for ((i=0; i<NUMRUNS; i++))
do
	echo "min = ${MIN_RUNS[$i]}; max = ${MAX_RUNS[$i]}"
	ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 dump /calibration/htcc/time -r ${MIN_RUNS[$i]} > ccdb_${MIN_RUNS[$i]}.dat
	#python3 changeTimeConstantsCCDB.py ${MIN_RUNS[$i]} ccdb_${MIN_RUNS[$i]}.dat
	python3 changeTimeConstantsCCDB.py ${MIN_RUNS[$i]} ccdb_${MIN_RUNS[$i]}.dat $timeShift
	echo "submitting to ccdb"
	echo "ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add /calibration/htcc/time -r ${MIN_RUNS[$i]}-${MAX_RUNS[$i]} run${MIN_RUNS[$i]}_time.dat"
	ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add /calibration/htcc/time -r ${MIN_RUNS[$i]}-${MAX_RUNS[$i]} run${MIN_RUNS[$i]}_time.dat
	echo "Submitted new time constants using ${MIN_RUNS[$i]} for run range ${MIN_RUNS[$i]}-${MAX_RUNS[$i]}"
	echo
done

echo "PROGRAM DONE"
