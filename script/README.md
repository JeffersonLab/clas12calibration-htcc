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
- [Adjust CCDB Gain Values by a Constant](#adjust-ccdb-gain-values-by-a-constant)
- [Hardware Status Tables](#hardware-status-tables)
- [Post Processing Visualization](#post-processing-visualization)

---

## Access Permissions

Certain actions and access to specific resources during the calibration process may require special permissions. Here's how you can acquire them:

### CCDB Access

To gain access to the CCDB, please contact Nathan Baltzell at [baltzell@jlab.org](mailto:baltzell@jlab.org).

### Trigger File Access

To access or modify the trigger file, you will need to be added to the `clon_cluster` group and have a home directory created on clon machines. To facilitate this, please contact Serguei Boiarinov at [boiarino@jlab.org](mailto:boiarino@jlab.org).

### CLAS Group Access

Depending on the files you need to modify, you might need to be added to the `clas` group. To request this, please contact Harut Avakian at [avakian@jlab.org](mailto:avakian@jlab.org).

### In order to run files

Please make sure that you have the necessary permissions to run these scripts. You can give yourself execute permissions for the python and bash scripts using the following command:
```bash
chmod u+x [file]
```
**Example**:
```bash
chmod u+x *.py
chmod u+x *.sh
```

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
7. `compareMultipleCCDB.sh`: bash script that compares the output of multiple runs to the current ccdb values
8. `createImages.sh`: bash script that combines multiple plots into single images
9. `changeTimeConstantsCCDB.sh`: bash script that changes time constants in ccdb based on some values
10. `changeTimeConstantsCCDB.py`: python scripts that includes the time shift values and produces new time constants based on said value
11. `OneScriptToRunThemAll.sh`: This new script simplifies the calibration process by executing multiple steps sequentially. It runs the calibration, generates analysis plots for each individual run, and then plots a trend over all runs for gain and time.
12. `changeGainConstantsCCDB.sh` and `changeGainConstantsCCDB.py`: bash and python scripts that changes gain constants in ccdb based on normalization factors for each sector.

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
11. `correctionFactor_NphePMT[RUN NUM].dat`: dat file with NphePMT correction factor, etc
12. `correctionFactor_TimePMT[RUN NUM].dat`: dat file with TimePMT correction factor, etc
13. `infoplots_NPHE[RUN NUM].png`: Infographic plot related to NPHE. Starting from top left is nphe mean per channel, top right is nphe correction factor per channel, middle left is the current ccdb gain value per channel, middle right is the new gain value, bottom left is the difference between new and old gain values, bottom right is the percent difference with lines at 5 and 10% difference
14. `infoplots_Time[RUN NUM].png`: Infographic plot related to Time. Starting from top left is the current ccdb time [ns] per channel, middle top is the new time [ns] per channel, bottom left is the time shift [ns], bottom middle is the time difference from new to old [ns], bottom right is the percent difference with red lines at 5% and 10%. 
15. `nphePMT_ZOOM_[RUN NUM].png`: Zoomed in version of the nphePMT plot (easier to see mean nphe of the nphe distribution)
16. `Combo_GainTime_$RUN.png`: Combination of Gain and Time plots
17. `Combo_Info_$RUN.png`: Combination of Info NPHE and Info Time plots

*Note: [RUN NUM] represents where the run number will go into the file name*

---

## Instructions

(These instructions assume that you are running scripts in this directory)

1. Clone the repository:
```bash
git clone https://github.com/JeffersonLab/clas12calibration-htcc.git
```
2. Check that script has the following files: `htccCalib.sh`, `HTCC_CalibEnviro.sh`, `htccCalib.groovy`, `calibrateMultipleRuns.sh`, and the newly added `OneScriptToRunThemAll.sh`.

3. To run the new script `OneScriptToRunThemAll.sh`, which includes all steps up to and including the generation of plots comparing gain and time constants (Section Python Script to Generate Plots Comparing Gain and Time Constants), use the following command:
  ```
  ./OneScriptToRunThemAll.sh FILE_PATH=[Your File Path Here]
  ```
  For example:
  ```
  ./OneScriptToRunThemAll.sh FILE_PATH=/volatile/clas12/rg-c/production/calib/10.0.2_TBT_11_30_23/calib/train/htcc/
  ```
  
4. Run `htccCalib.sh` for a single run or `calibrateMultipleRuns.sh` for multiple runs.
  
   **Single Run:**
   ```bash
   bash htccCalib.sh RUN_NUM=[] FILE_PATH=[]
   ```
   In this case, `FILE_PATH` should point to a specific HTCC skimmed hipo file.

   **Multiple Runs:**
   ```bash
   bash calibrateMultipleRuns.sh FILE_PATH=[]
   ```
   For multiple runs, `FILE_PATH` should be the directory path where multiple HTCC skimmed hipo files are located.

   Both scripts take command-line arguments, which the user must provide when running them: 
   
   - `RUN_NUM` for `htccCalib.sh`
   - `FILE_PATH` for both scripts

**Note:** For extensive calibration tasks involving multiple runs, you might want to consider running the calibration scripts as batch jobs on the Jefferson Lab computing cluster, which uses the Slurm workload manager. You could write a bash script to submit a Slurm job to the batch farm to automate this process. However, an example script for this purpose is not currently included in this repository. If this is a great concern, I can provide a working example. If you want to see what I use, a bash script to submit slurm jobs can be found at /w/hallb-scshelf2102/clas12/izzy/HTCCcalib/clas12calibration-htcc/script/submitFarmJobHTCC.sh (use at your own risk!).

5. Upon successful completion of the scripts, the outputs can be found in `$PWD/CalibRes/$RUN_NUM/$TODAY`.
6. Submit the calibration constants (the .dat files in the outputs) for the relevant run range for the gain and timing as described in the original instructions.

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
## Comparisons 

### CSV Comparing New Values to CCDB Values

8. Run bash and python script to get comparison between a run's output dat file for time or gain vs the constants currently uploaded in CCDB. There are two bash scripts: `compareRunCCDB.sh` and `compareMultipleCCDB.sh`. 

`compareRunCCDB.sh` runs the python script `compareRunCCDB.py`. The bash script takes 3 key-value arguments (can be input in any order):

**General**
```bash
./compareRunCCDB.sh RUN_NUM=[] PARAMETER=[] FILE_PATH=[]
```
**Example**
```bash
./compareRunCCDB.sh RUN_NUM=016702 PARAMETER=time FILE_PATH=/work/clas12/izzy/HTCCcalib/clas12calibration-htcc/script/CalibRes/016702/08-Sep-2022/timePMT16702.dat
```
`compareMultipleCCDB.sh` runs the `compareRunCCDB.sh` for multiple runs and dates. It takes 3 key-value arguments (can be input in any order):

**General**
```bash
./compareMultipleCCDB.sh DATES=[] SUPERDIR=[] RUNNUMS=[]
```
**Example**
```bash
./compareMultipleCCDB.sh DATES="10-Jul-2023" SUPERDIR="/w/hallb-scshelf2102/clas12/izzy/temp2/clas12calibration-htcc/script/CalibRes" RUNNUMS="004763 004867 004889 004893 005125 005300 005318 005319 005325 005341 005346 005367 005381 005393 005407 005414 005415 005416 005417 005418 005419"
```

And there are 3 outputs: 
  - ccdb_time_run[number].dat: dat file with ccdb info
  - compareRun[number]CCDB.dat: csv file with percent change comparison between run and ccdb values
  - compareRun[number]CCDB_HTML.txt: same info as above but formatted in html so that it can be easily c&p into logbook entry

You can also create images (for better use in powerpoint slides) with the following script:
```bash
./createImages.sh DATES=[] SUPERDIR=[] RUNNUMS=[]
```
**Example**
```bash
./createImages.sh DATES="10-Jul-2023" SUPERDIR="/w/hallb-scshelf2102/clas12/izzy/temp2/clas12calibration-htcc/script/CalibRes" RUNNUMS="004763 004867 004889 004893 005125 005300 005318 005319 005325 005341 005346 005367 005381 005393 005407 005414 005415 005416 005417 005418 005419"
```

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

#### **Please remember to replace `[RUN NUMBERS]`, `[DATES]`, and `[DIRECTORY PATH]` with appropriate values when running the scripts.**
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

10. If you'd like to adjust the time constants in CCDB by a simple addition of a constant, you can use `changeTimeConstantsCCDB.sh` and `changeTimeConstantsCCDB.py`. These scripts now support sector-specific time shifts and accept command-line arguments for the run range and sector-specific shifts.

The bash and python scripts have been updated to accept command line arguments.

**Example**
```bash
./changeTimeConstantsCCDB.sh MIN_RUN=<minRun> MAX_RUN=<maxRun> SEC1_SHIFT=<shift> SEC2_SHIFT=<shift> SEC3_SHIFT=<shift> SEC4_SHIFT=<shift> SEC5_SHIFT=<shift> SEC6_SHIFT=<shift>
```
Now you can specify the time shifts for each sector (rather than having them hardcoded in the python script) and provide a list of the runs. The default behavior is to assume that the time shifts are all 0.

**Example**
```bash
./changeTimeConstantsCCDB.sh MIN_RUN=17279 MAX_RUN=17279 SEC1_SHIFT=0.05576 SEC2_SHIFT=-0.04397 SEC3_SHIFT=-0.01265 SEC4_SHIFT=0.17876 SEC5_SHIFT=6.7847 SEC6_SHIFT=0.07842
```
This will apply the specified time shifts to the corresponding sectors for the run range provided.
#### Instructions
Single Run:
To run the updated script for a single run with sector-specific time shifts:
```bash
./changeTimeConstantsCCDB.sh MIN_RUN=17279 MAX_RUN=17279 SEC1_SHIFT=0.05576 SEC2_SHIFT=-0.04397 SEC3_SHIFT=-0.01265 SEC4_SHIFT=0.17876 SEC5_SHIFT=6.7847 SEC6_SHIFT=0.07842
```
Multiple Runs:
For multiple runs, iterate over the run numbers and call the script for each run with the specific shifts from the CSV file:
```python
import pandas as pd
import subprocess

# Load the CSV file
df = pd.read_csv('RGC_F22_4nsTimeShift.csv')

# Group by run number
grouped = df.groupby('run')

# Path to your bash script
bash_script_path = './changeTimeConstantsCCDB.sh'

# Iterate through each group (run number)
for run, group in grouped:
    # Get the time shifts for each sector
    sector_shifts = group.set_index('sector')['time shift'].to_dict()

    # Construct the command line arguments
    args = [
        f"MIN_RUN={run}",
        f"MAX_RUN={run}",
        f"SEC1_SHIFT={sector_shifts.get(1, 0.0)}",
        f"SEC2_SHIFT={sector_shifts.get(2, 0.0)}",
        f"SEC3_SHIFT={sector_shifts.get(3, 0.0)}",
        f"SEC4_SHIFT={sector_shifts.get(4, 0.0)}",
        f"SEC5_SHIFT={sector_shifts.get(5, 0.0)}",
        f"SEC6_SHIFT={sector_shifts.get(6, 0.0)}",
    ]

    # Run the bash script with the constructed arguments
    command = [bash_script_path] + args
    subprocess.run(command)
```
This Python script automates the process of applying sector-specific time shifts for each run based on the CSV file. Use this as an example for your own calibrations.

---
### ADJUST CCDB GAIN VALUES BY A CONSTANT
11. If you'd like to adjust the gain constants in ccdb by a normalization factor for each sector, you can use changeGainConstantsCCDB.sh and changeGainConstantsCCDB.py. The bash and python scripts have been updated to accept command-line arguments for the normalization factors and run ranges.

The bash and python scripts have been update to accept command line arguments.

**Example**
```bash
./changeGainConstantsCCDB.sh MIN_RUN=<minRun> MAX_RUN=<maxRun> SEC1_NORM=<factor> SEC2_NORM=<factor> SEC3_NORM=<factor> SEC4_NORM=<factor> SEC5_NORM=<factor> SEC6_NORM=<factor>
```
Now you can specify the normalization factors (rather than hardcoded in the python script) and provide a list of the runs.
**Example**
```bash
./changeGainConstantsCCDB.sh MIN_RUN=17482 MAX_RUN=17482 SEC1_NORM=1.08734 SEC2_NORM=1.02248 SEC3_NORM=0.96556 SEC4_NORM=0.87833 SEC5_NORM=0.93707 SEC6_NORM=1.07589
```
The default behavior is to assume that the normalization factors are all 1.

---
## Hardware Status Tables

The Hardware Status Tables are crucial components of the calibration process. They define the status of different hardware components used in the data acquisition. For each component, a status value different from 0 indicates that the component did not operate normally. These tables play an important role in identifying and handling problematic or non-operational detector elements during the reconstruction process.

In the case of HTCC, hardware status tables often do not need frequent updates unless there is a significant change in the operation of the detector components. For example, in the past, there were instances where some channels had no signal until the HV power supply was replaced. Such cases were deemed as not exactly "erratic behavior" that needed to be accounted for in the hardware status tables, and thus, no changes were made to the HTCC status.

Here are some key resources related to the hardware status tables:

- **Hardware Status Tables in CCDB**: The hardware status tables can be found in the CCDB: [[CCDB](https://clasweb.jlab.org/cgi-bin/ccdb/show_request?request=/calibration/htcc/status:0:default:2017-08-17_02-48-53)](https://clasweb.jlab.org/cgi-bin/ccdb/show_request?request=/calibration/htcc/status:0:default:2017-08-17_02-48-53). 

- **Hardware Status Words Convention**: The convention for setting status values in the hardware status tables can be found in this PDF document: [[PDF document](https://clasweb.jlab.org/wiki/images/b/b9/Clas12-hardware-status-words.pdf)](https://clasweb.jlab.org/wiki/images/b/b9/Clas12-hardware-status-words.pdf).

As per the process, if all channels are good, no action is needed. But if there are components that should be excluded from the reconstruction, a new entry should be made in the hardware status tables where their status is set according to the convention described above.

Please be aware that changes to hardware status tables can create "holes" in the acceptance, and hence, usually, this is done only if the behavior of the detector element is so erratic that it would be too difficult to track the efficiency. Therefore, careful consideration and analysis are required before making any changes to these tables.

## Post Processing Visualization
:rotating_light: **New as of 15 Jan 2025** :rotating_light:

### PowerPoint Generation Script
The Python script `htcc_calibration.py` generates PowerPoint presentations to visualize calibration results across multiple runs. It takes calibration outputs, processes them, and creates organized slides with comparisons and trends.

**Required Arguments:**
```bash
python htcc_calibration.py \
  --directories DIRECTORY [DIRECTORY ...] \  # List of directories containing run data
  --dates DATE [DATE ...]               \    # List of dates to process (DD-Mon-YYYY format)
  --output OUTPUT_DIR                   \    # Output directory for the presentation
  --title PRESENTATION_TITLE            \    # Title for the presentation
  --author AUTHOR_NAME                       # Author name for the presentation
```

**Optional Arguments:**
- `--process-only`: Only process directories without creating slides
- `--slides-only`: Only create slides without processing directories

**Example Usage:**
```bash
python htcc_calibration.py \
  --directories "/path/to/HTCC/RGK/8Jan2025/" \
  --dates "08-Jan-2025" \
  --output "/path/to/output/dir/" \
  --title "HTCC Calibrations for RGK Fall 2023" \
  --author "Your Name"
```

**Outputs:**
1. Processes calibration data and generates visualizations
2. Creates a PowerPoint presentation (`HTCC_Calibrations_[DATE].pptx`) containing:
   - Title slide with author and date
   - Overview slides for each run
   - Gain and timing comparison plots
   - Combined visualization slides

**Note:** The script automatically handles image scaling and slide layout to ensure professional-looking presentations. The output filename includes the processing date for easy identification.

#### Script Prerequisites:
- Python 3.x
- Required Python packages:
  - python-pptx
  - pandas
  - matplotlib
  - Pillow
  - numpy

You can install these packages using pip:
```bash
pip install python-pptx pandas matplotlib Pillow numpy
```

:rotating_light: **New as of 15 Jan 2025** :rotating_light:
