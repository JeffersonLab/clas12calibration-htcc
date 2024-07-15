#!/bin/bash

# Default values for time shifts (assuming 0 by default)
SEC1_SHIFT=0.0
SEC2_SHIFT=0.0
SEC3_SHIFT=0.0
SEC4_SHIFT=0.0
SEC5_SHIFT=0.0
SEC6_SHIFT=0.0

# Parse key-value arguments
for ARG in "$@"; do
    KEY=${ARG%%=*}
    VALUE=${ARG#*=}
    case "$KEY" in
        MIN_RUN) MIN_RUN=$VALUE ;;
        MAX_RUN) MAX_RUN=$VALUE ;;
        SEC1_SHIFT) SEC1_SHIFT=$VALUE ;;
        SEC2_SHIFT) SEC2_SHIFT=$VALUE ;;
        SEC3_SHIFT) SEC3_SHIFT=$VALUE ;;
        SEC4_SHIFT) SEC4_SHIFT=$VALUE ;;
        SEC5_SHIFT) SEC5_SHIFT=$VALUE ;;
        SEC6_SHIFT) SEC6_SHIFT=$VALUE ;;
        *) echo "Invalid argument: $KEY"; exit 1 ;;
    esac
done

# Check for mandatory arguments
if [ -z "$MIN_RUN" ] || [ -z "$MAX_RUN" ]; then
    echo "Usage: $0 MIN_RUN=<minRun> MAX_RUN=<maxRun> [SEC1_SHIFT=<shift>] [SEC2_SHIFT=<shift>] [SEC3_SHIFT=<shift>] [SEC4_SHIFT=<shift>] [SEC5_SHIFT=<shift>] [SEC6_SHIFT=<shift>]"
    exit 1
fi

echo 'Setting up environment'
source /group/clas12/packages/setup.sh
module load clas12
module load workflow
echo 'Checking environment'
module list

echo "min = $MIN_RUN; max = $MAX_RUN"
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 dump /calibration/htcc/time -r $MIN_RUN > ccdb_$MIN_RUN.dat

# Call the Python script with the arguments
python3 changeTimeConstantsCCDB.py $MIN_RUN ccdb_$MIN_RUN.dat $SEC1_SHIFT $SEC2_SHIFT $SEC3_SHIFT $SEC4_SHIFT $SEC5_SHIFT $SEC6_SHIFT

# Submit the changes to CCDB
output_file="run${MIN_RUN}_time.dat"
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add /calibration/htcc/time -r ${MIN_RUN}-${MAX_RUN} $output_file
echo "Submitted new time constants using ${MIN_RUN} for run range ${MIN_RUN}-${MAX_RUN}"

echo "PROGRAM DONE"

