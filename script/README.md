# HTCC Calibration Package

## Scripts

#### Included Files
1. `htccCalib.sh`: shell script to run everything (environment and groovy)
2. `HTCC_CalibEnviro.sh`: shell script to set up the environment 
3. `htccCalib.groovy`: groovy file to compute calibration constants

##### Outputs
1. `npeAllC[RUN NUM].png`: plot over all 48 channels showing the gain
2. `npePMT[RUN NUM].dat`: text file with gain constants (submit to ccdb)
3. `nphePMT[RUN NUM].png`: gain of all 48 channels plotted individually
4. `timeAllC[RUN NUM].png`: timing of all 80 channels with a gaussian fit
5. `timePMT[RUN NUM].dat`: text file with timing constants (submit to ccdb)
6. `timePMT[RUN NUM].png`: timing of all 48 channels plotted individually 

*Note: [RUN NUM] represents where the run number will go into the file name*

## Instructions

1. Clone the repository:
```
git clone https://github.com/JeffersonLab/clas12calibration-htcc.git
```
2. Check that `script` has the following 3 files: `htccCalib.sh`, `HTCC_CalibEnviro.sh`, `htccCalib.groovy`
3. Run `htccCalib.sh`
```
bash htccCalib.sh RUN_NUM=[] RUN_DIR=[] SKIM_NUM=[]
```
Currently there are 3 command line arguments the user must provide when running `htccCalib.sh`: 
  - RUN_NUM
  - RUN_DIR
  - SKIM_NUM

Order does not matter as long as the key is used.

*Note: [RUN RANGE] represents where a range of runs should be specified and [RUN NUM] represents where the run number will go into the file name*

**Example**:
```
bash htccCalib.sh RUN_NUM="003219" RUN_DIR="/volatile/clas12/rg-a/production/pass0/Spring18/v1_1.1.77/calib/train/" SKIM_NUM="skim6"
```
4. Upon successful completion of the script the outputs can be found in `$PWD/CalibRes/$RUN_NUM/$TODAY` 
5. Submit the calibration constants (the .dat files in the outputs) for the relevant run range for the gain and timing

**Gain**:
```
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add /calibration/htcc/time -r [RUN RANGE] timePMT[RUN NUM].dat
```
**Timing**:
```
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add /calibration/htcc/gain -r [RUN RANGE] npePMT[RUN NUM].dat
```
**Example**:
```
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add /calibration/htcc/time -r 11093-11243 timePMT11158.dat
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add /calibration/htcc/gain -r 11093-11243 npePMT11158.dat
```
6. Check that calibration constants were successfully submitted to `ccdb` with the following links: 
  - [HTCC Gain](https://clasweb.jlab.org/cgi-bin/ccdb/versions?table=/calibration/htcc/gain)
  - [HTCC Time](https://clasweb.jlab.org/cgi-bin/ccdb/versions?table=/calibration/htcc/time)
