# HTCC Calibration Package

## Scripts

#### Included Files
1. `htccCalib.sh`: shell script to run everything (environment and groovy)
2. `HTCC_CalibEnviro.sh`: shell script to set up the environment 
3. `htccCalib.groovy`: groovy file to compute calibration constants
4. `GenTrigVals.py`: python script to compute values for adcctof1_gain.cnf trigger file
5. `compareRunCCDB.sh`: bash script that gets ccdb values for a specific run and then calls a python script
6. `compareRunCCDB.py`: python script that compares the output of a specific run to the current ccdb values of that run
7. `changeTimeConstantsCCDB.sh`: bash script that changes time constants in ccdb based on some values
8. `changeTimeConstantsCCDB.py`: python scripts that includes the time shift values and produces new time constants based on said value

##### Outputs
1. `npeAllC[RUN NUM].png`: plot over all 48 channels showing the gain
2. `npePMT[RUN NUM].dat`: text file with gain constants (submit to ccdb)
3. `nphePMT[RUN NUM].png`: gain of all 48 channels plotted individually
4. `timeAllC[RUN NUM].png`: timing of all 80 channels with a gaussian fit
5. `timePMT[RUN NUM].dat`: text file with timing constants (submit to ccdb)
6. `timePMT[RUN NUM].png`: timing of all 48 channels plotted individually 
7. `adcctof1_gain_Run[RUN NUM].txt`: values for trigger file
8. `ccdb_time_run[RUN NUM].dat`: dat file with ccdb info
9. `compareRun[RUN NUM]CCDB.dat`: csv file with percent change comparison between run and ccdb values
10. `compareRun[RUN NUM]CCDB_HTML.txt`: same info as above but formatted in html so that it can be easily c&p into logbook entry

*Note: [RUN NUM] represents where the run number will go into the file name*

## Instructions

1. Clone the repository:
```
git clone https://github.com/JeffersonLab/clas12calibration-htcc.git
```
2. Check that `script` has the following 3 files: `htccCalib.sh`, `HTCC_CalibEnviro.sh`, `htccCalib.groovy`
3. Run `htccCalib.sh`
```
bash htccCalib.sh RUN_NUM=[] FILE_PATH=[]
```
Currently there are 2 command line arguments the user must provide when running `htccCalib.sh`: 
  - RUN_NUM
  - FILE_PATH

Order does not matter as long as the key is used. 

*Note: [RUN RANGE] represents where a range of runs should be specified and [RUN NUM] represents where the run number will go into the file name*

**Example**:
```
bash htccCalib.sh RUN_NUM=004143 FILE_PATH=/lustre19/expphy/volatile/clas12/rg-a/production/pass0/Spring18/v1_1.1.86/calib/train/skim6/skim6_004143.hipo
```
4. Upon successful completion of the script the outputs can be found in `$PWD/CalibRes/$RUN_NUM/$TODAY` 
5. Submit the calibration constants (the .dat files in the outputs) for the relevant run range for the gain and timing

**Gain**:
```
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add /calibration/htcc/gain -r [RUN RANGE] npePMT[RUN NUM].dat
```
**Timing**:
```
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add /calibration/htcc/time -r [RUN RANGE] timePMT[RUN NUM].dat
```
**Example**:
```
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add /calibration/htcc/gain -r 11093-11243 npePMT11158.dat
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add /calibration/htcc/time -r 11093-11243 timePMT11158.dat
```
6. Check that calibration constants were successfully submitted to `ccdb` with the following links: 
  - [HTCC Gain](https://clasweb.jlab.org/cgi-bin/ccdb/versions?table=/calibration/htcc/gain)
  - [HTCC Time](https://clasweb.jlab.org/cgi-bin/ccdb/versions?table=/calibration/htcc/time)
---
7. Run python script to get trigger values. Use Python 3. Try command `module load python/3.9.5` or `module switch python/3.9.5` if a python module is already loaded. Run this script with the npePMT dat file generated via calibrations scripts in order to generate trigger values. It takes one command line input: file path of npePMT dat 
```
python3 GenTrigVals.py /w/hallb-scifs17exp/clas12/izzy/HTCCcalib/clas12calibration-htcc/script/CalibRes/015045/13-Nov-2021/npePMT15045.dat
```
---
8. Run bash and python script to get comparison between a run's output dat file for time or gain vs the constants currently uploaded in CCDB. The bash script `compareRunCCDB.sh` runs the python script `compareRunCCDB.py`. The bash script takes 3 key-value arguments (can be input in any order):
```
./compareRunCCDB.sh RUN_NUM=[] PARAMETER=[] FILE_PATH=[]
```
Example:
```
./compareRunCCDB.sh RUN_NUM=016702 PARAMETER=time FILE_PATH=/work/clas12/izzy/HTCCcalib/clas12calibration-htcc/script/CalibRes/016702/08-Sep-2022/timePMT16702.dat
```
And there are 3 outputs: 
  - ccdb_time_run[number].dat: dat file with ccdb info
  - compareRun[number]CCDB.dat: csv file with percent change comparison between run and ccdb values
  - compareRun[number]CCDB_HTML.txt: same info as above but formatted in html so that it can be easily c&p into logbook entry
---
9. If you'd like to check the change history for a parameter for a run range you can use `ccdb-ranges.py` from https://github.com/JeffersonLab/clas12-utilities/blob/master/bin/ccdb-ranges.py 
Example:
```
ccdb-ranges.py -min 6608 -max 6783 -table /calibration/htcc/time -dump
```
---
10. If you'd like to adjust the time constants in ccdb by a simple addition of a constants you can use `changeTimeConstantsCCDB.sh` and `changeTimeConstantsCCDB.py`. You will need to change the `timeShift` value in the python script to whatever value you need. Currently the bash script is must be hardcoded for the run ranges in the script itself so you need to edit the arrays `MIN_RUNS` and `MAX_RUNS`. The bash script will call the python script itself and commit the changes to ccdb. To run these is fairly straight forward (after the appropriate changes have been made):
```
./changeTimeConstantsCCDB.sh
```
