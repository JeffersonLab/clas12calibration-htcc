#!/bin/bash

# Default values for normalization factors
SEC1_NORM=1.0
SEC2_NORM=1.0
SEC3_NORM=1.0
SEC4_NORM=1.0
SEC5_NORM=1.0
SEC6_NORM=1.0

# Parse key-value arguments
for ARG in "$@"; do
    KEY=${ARG%%=*}
    VALUE=${ARG#*=}
    case "$KEY" in
        MIN_RUN) MIN_RUN=$VALUE ;;
        MAX_RUN) MAX_RUN=$VALUE ;;
        SEC1_NORM) SEC1_NORM=$VALUE ;;
        SEC2_NORM) SEC2_NORM=$VALUE ;;
        SEC3_NORM) SEC3_NORM=$VALUE ;;
        SEC4_NORM) SEC4_NORM=$VALUE ;;
        SEC5_NORM) SEC5_NORM=$VALUE ;;
        SEC6_NORM) SEC6_NORM=$VALUE ;;
        *) echo "Invalid argument: $KEY"; exit 1 ;;
    esac
done

# Check for mandatory arguments
if [ -z "$MIN_RUN" ] || [ -z "$MAX_RUN" ]; then
    echo "Usage: $0 MIN_RUN=<minRun> MAX_RUN=<maxRun> [SEC1_NORM=<factor>] [SEC2_NORM=<factor>] [SEC3_NORM=<factor>] [SEC4_NORM=<factor>] [SEC5_NORM=<factor>] [SEC6_NORM=<factor>]"
    exit 1
fi

echo 'Setting up environment'
source /group/clas12/packages/setup.sh
module load clas12
module load workflow
echo 'Checking environment'
module list

echo "min = $MIN_RUN; max = $MAX_RUN"
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 dump /calibration/htcc/gain -r $MIN_RUN > ccdb_$MIN_RUN.dat

# Call the Python script with the arguments
python3 changeGainConstantsCCDB.py $MIN_RUN ccdb_$MIN_RUN.dat $SEC1_NORM $SEC2_NORM $SEC3_NORM $SEC4_NORM $SEC5_NORM $SEC6_NORM

# Submit the changes to CCDB
output_file="run${MIN_RUN}_gain.dat"
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add /calibration/htcc/gain -r ${MIN_RUN}-${MAX_RUN} $output_file
echo "Submitted new gain constants using ${MIN_RUN} for run range ${MIN_RUN}-${MAX_RUN}"

echo "PROGRAM DONE"

