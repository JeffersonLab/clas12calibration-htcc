# HTCC Calibration Package

- [Access Permissions](#access-permissions)
- [Scripts](#scripts)
  - [Included Files](#included-files)
  - [Outputs](#outputs)
- [Instructions](#instructions)
- [Trigger Values](#trigger-values)
- [Comparing New Calibration Constants to old CCDB Values](#comparisons)
  - [CSV format](#csv-comparing-new-values-to-ccdb-values)
  - [Plots](#python-script-to-generate-plots-comparing-gain-and-time-constants)
- [CCDB Value Update History](#ccdb-value-update-history)
- [Adjust CCDB Time Values by a Constant](#adjust-ccdb-time-values-by-a-constant)
- [Hardware Status Tables](#hardware-status-tables)

---

## Access Permissions

Certain actions and access to specific resources during the calibration process may require special permissions. Here's how you can acquire them:

### CCDB Access

To gain access to the CCDB, please contact Nathan Baltzell at [baltzell@jlab.org](mailto:baltzell@jlab.org).

### Trigger File Access

To access or modify the trigger file, you will need to be added to the `clon_cluster` group and have a home directory created on clon machines. To facilitate this, please contact Serguei Boiarinov at [boiarino@jlab.org](mailto:boiarino@jlab.org).

### CLAS Group Access

Depending on the files you need to modify, you might need to be added to the `clas` group. To request this, please contact Harut Avakian at [avakian@jlab.org](mailto:avakian@jlab.org).

#### Remember, obtaining these permissions might take some time, so plan accordingly.
---

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

Newly added output files:

11. `correctionFactor_NphePMT[RUN NUM].dat`: dat file with NphePMT correction factor, etc
12. `correctionFactor_TimePMT[RUN NUM].dat`: dat file with TimePMT correction factor, etc
13. `infoplots_NPHE[RUN NUM].png`: Infographic plot related to NPHE. Starting from top left is nphe mean per channel, top right is nphe correction factor per channel, middle left is the current ccdb gain value per channel, middle right is the new gain value, bottom left is the difference between new and old gain values, bottom right is the percent difference with lines at 5 and 10% difference
14. `infoplots_Time[RUN NUM].png`: Infographic plot related to Time. Starting from top left is the current ccdb time [ns] per channel, middle top is the new time [ns] per channel, bottom left is the time shift [ns], bottom middle is the time difference from new to old [ns], bottom right is the percent difference with red lines at 5% and 10%. 
15. `nphePMT_ZOOM_[RUN NUM].png`: Zoomed in version of the nphePMT plot (easier to see mean nphe of the nphe distribution)

*Note: [RUN NUM] represents where the run number will go into the file name*

---

## Instructions

(These instruction assume that you are running scripts in this directory)

1. Clone the repository:
```bash
git clone https://github.com/JeffersonLab/clas12calibration-htcc.git
```
2. Check that `script` has the following 3 files: `htccCalib.sh`, `HTCC_CalibEnviro.sh`, `htccCalib.groovy`
3. Run `htccCalib.sh`
```bash
bash htccCalib.sh RUN_NUM=[] FILE_PATH=[]
```
Currently there are 2 command line arguments the user must provide when running `htccCalib.sh`: 
  - RUN_NUM
  - FILE_PATH

Order does not matter as long as the key is used. 

*Note: [RUN RANGE] represents where a range of runs should be specified and [RUN NUM] represents where the run number will go into the file name*

**Example**:
```bash
bash htccCalib.sh RUN_NUM=004143 FILE_PATH=/lustre19/expphy/volatile/clas12/rg-a/production/pass0/Spring18/v1_1.1.86/calib/train/skim6/skim6_004143.hipo
```
4. Upon successful completion of the script the outputs can be found in `$PWD/CalibRes/$RUN_NUM/$

TODAY` 
5. Submit the calibration constants (the .dat files in the outputs) for the relevant run range for the gain and timing

**Gain**:
```bash
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add /calibration/htcc/gain -r [RUN RANGE] npePMT[RUN NUM].dat
```
**Timing**:
```bash
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add /calibration/htcc/time -r [RUN RANGE] timePMT[RUN NUM].dat
```
**Example**:
```bash
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add /calibration/htcc/gain -r 11093-11243 npePMT11158.dat
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add /calibration/htcc/time -r 11093-11243 timePMT11158.dat
```
6. Check that calibration constants were successfully submitted to `ccdb` with the following links: 
  - HTCC Gain: [https://clasweb.jlab.org/cgi-bin/ccdb/versions?table=/calibration/htcc/gain](https://clasweb.jlab.org/cgi-bin/ccdb/versions?table=/calibration/htcc/gain)
  - HTCC Time: [https://clasweb.jlab.org/cgi-bin/ccdb/versions?table=/calibration/htcc/time](https://clasweb.jlab.org/cgi-bin/ccdb/versions?table=/calibration/htcc/time)

---

### TRIGGER VALUES

7. Run python script to get trigger values. Use Python 3. Try command `module load python/3.9.5` or `module switch python/3.9.5` if a python module is already loaded. Run this script with the npePMT dat file generated via calibrations scripts in order to generate trigger values. It takes one command line input: file path of npePMT dat 

**General**
```bash
python3 GenTrigVals.py [file]
```
**Example**
```bash
python3 GenTrigVals.py /w/hallb-scifs17exp/clas12/izzy/HTCCcalib/clas12calibration-htcc/script/CalibRes/015045/13-Nov-2021/npePMT15045.dat
```
---
## Comparisons 

### CSV Comparing New Values to CCDB Values

8. Run bash and python script to get comparison between a run's output dat file for time or gain vs the constants currently uploaded in CCDB. The bash script `compareRunCCDB.sh` runs the python script `compareRunCCDB.py`. The bash script takes 3 key-value arguments (can be input in any order):

**General**
```bash
./compareRunCCDB.sh RUN_NUM=[] PARAMETER=[] FILE_PATH=[]
```
**Example**
```bash
./compareRunCCDB.sh RUN_NUM=016702 PARAMETER=time FILE_PATH=/work/clas12/izzy/HTCCcalib/clas12calibration-htcc/script/CalibRes/016702/08-Sep-2022/timePMT16702.dat
```
And there are 3 outputs: 
  - ccdb_time_run[number].dat: dat file with ccdb info
  - compareRun[number]CCDB.dat: csv file with percent change comparison between run and ccdb values
  - compareRun[number]CCDB_HTML.txt: same info as above but formatted in html so that it can be easily c&p into logbook entry

### Python Script to Generate Plots Comparing Gain and Time Constants

The Python script `generateComparisonPlots.py` generates plots comparing gain and time constants between different runs. It outputs these plots to a specified directory and also displays them on the screen. It takes three arguments: run numbers, dates, and the top directory. The script requires Python 3.x and the following modules: `os`, `glob`, `argparse`, `matplotlib`, `seaborn`, `pandas`.

**General**
```bash
python3 generateComparisonPlots.py --run_nums [RUN NUMBERS] --dates [DATES] --top_dir [DIRECTORY PATH]
```
**Example**
```bash
python3 generateComparisonPlots.py --run_nums 004763 005423 --dates 28-Jun-2023 29-Jun-2023 30-Jun-2023 --top_dir /w/hallb-scshelf2102/clas12/izzy/temp/clas12calibration-htcc/script/CalibRes
```
The script will generate and save a plot named 'correction_factors_and_percent_changes_by_run_number_and_sector.png' in the current directory, which compares gain and time constants for different sectors across the specified run numbers and dates. *Note*: X11 may pull up the plot as an image and you will need to save it manually. The save button should be on the bottom left. In this scenario the python script will not save the image automatically. 

**Please remember to replace `[RUN NUMBERS]`, `[DATES]`, and `[DIRECTORY PATH]` with appropriate values when running the scripts.**
---

### CCDB VALUE UPDATE HISTORY
9. If you'd like to check the change history for a parameter for a run range you can use `ccdb-ranges.py` from https://github.com/JeffersonLab/clas12-utilities/blob/master/bin/ccdb-ranges.py 

**General**
```bash
ccdb-ranges.py -min [RUN] -max [RUN] -table [parameter: gain or time] -dump
```
**Example**
```bash
ccdb-ranges.py -min 6608 -max 6783 -table /calibration/htcc/time -dump
```
---
### ADJUST CCDB TIME VALUES BY A CONSTANT
10. If you'd like to adjust the time constants in ccdb by a simple addition of a constants you can use `changeTimeConstantsCCDB.sh` and `changeTimeConstantsCCDB.py`. You will need to change the `timeShift` value in the python script to whatever value you need. Currently the bash script is must be hardcoded for the run ranges in the script itself so you need to edit the arrays `MIN_RUNS` and `MAX_RUNS`. The bash script will call the python script itself and commit the changes to ccdb. To run these is fairly straight forward (after the appropriate changes have been made):

**Example**
```bash
./changeTimeConstantsCCDB.sh
```
---
Sure, here is a draft for the section about the hardware status tables in your markdown:

---
## Hardware Status Tables

The Hardware Status Tables are crucial components of the calibration process. They define the status of different hardware components used in the data acquisition. For each component, a status value different from 0 indicates that the component did not operate normally. These tables play an important role in identifying and handling problematic or non-operational detector elements during the reconstruction process.

In the case of HTCC, hardware status tables often do not need frequent updates unless there is a significant change in the operation of the detector components. For example, in the past, there were instances where some channels had no signal until the HV power supply was replaced. Such cases were deemed as not exactly "erratic behavior" that needed to be accounted for in the hardware status tables, and thus, no changes were made to the HTCC status.

Here are some key resources related to the hardware status tables:

- **Hardware Status Tables in CCDB**: The hardware status tables can be found in the CCDB: [[CCDB](https://clasweb.jlab.org/cgi-bin/ccdb/show_request?request=/calibration/htcc/status:0:default:2017-08-17_02-48-53)](https://clasweb.jlab.org/cgi-bin/ccdb/show_request?request=/calibration/htcc/status:0:default:2017-08-17_02-48-53). 

- **Hardware Status Words Convention**: The convention for setting status values in the hardware status tables can be found in this PDF document: [[PDF document](https://clasweb.jlab.org/wiki/images/b/b9/Clas12-hardware-status-words.pdf)](https://clasweb.jlab.org/wiki/images/b/b9/Clas12-hardware-status-words.pdf).

As per the process, if all channels are good, no action is needed. But if there are components that should be excluded from the reconstruction, a new entry should be made in the hardware status tables where their status is set according to the convention described above.

Please be aware that changes to hardware status tables can create "holes" in the acceptance, and hence, usually, this is done only if the behavior of the detector element is so erratic that it would be too difficult to track the efficiency. Therefore, careful consideration and analysis are required before making any changes to these tables.
