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

Note: [RUN NUM] represents where the run number will go into the file name

## Instructions

1. Clone the repository:
```
git clone https://github.com/JeffersonLab/clas12calibration-htcc.git
```

2. Check that `script` has the following 3 files: `htccCalib.sh`, `HTCC_CalibEnviro.sh`, `htccCalib.groovy`
