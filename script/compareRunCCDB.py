import pandas as pd
import sys

fileRun = sys.argv[4]
fileCCDB = sys.argv[3]
paramGorT = sys.argv[2]
runNum = int(sys.argv[1])

print("Run number %d for parameter %s" % (runNum, paramGorT))
print("File to compare %s" % fileRun)
print("ccdb info %s" % fileCCDB)

def readDatFile(filePath, ccdbBool, paramName):
    """
    function to read in a dat file and return a filled dataframe 

    Parameters
    ----------
    filePath: STRING, dat file
    ccdbBool: BOOLEAN, is dat file from ccdb or not (if TRUE, skip header)
    paramName: STRING, gain or time
    
    Returns
    ----------
    df_out: filled dataframe
    """
    channels = 48
    count = 0

    sector = []
    halfsector = []
    ring = []
    parameter = []

    with open(filePath) as f:
        if (ccdbBool == True):
            next(f) #skip header
        for i, line in enumerate(f):
            percentage = 100*(count/channels)
            boxes_blk = ["\u2588"] * count
            boxes_yt = [" "] * (channels-count)
            if (percentage%5 == 0):
                if (count == 0):
                    print("Read %d channels\t\t[%s%s]" % (count, "".join(boxes_blk), "".join(boxes_yt)))
                else:
                    print("Read %d channels\t[%s%s]" % (count, "".join(boxes_blk), "".join(boxes_yt)))
            # print(line)
            splitd = line.split()
            # print(splitd)
            sector.append(int(splitd[0]))
            halfsector.append(int(splitd[1]))
            ring.append(int(splitd[2]))
            parameter.append(float(splitd[3]))

            count = count + 1
        print("Read %d channels\t[%s%s]" % (count, "".join(boxes_blk), "".join(boxes_yt)))
        
        dict_info = {
        "S": sector, 
        "HS": halfsector, 
        "R": ring, 
        paramName: parameter
        }
        
        df_info = pd.DataFrame(dict_info)
        df_info = df_info.sort_values(["R", "S"])
        df_info.reset_index(inplace = True, drop = True)
        
        return df_info

df_run = readDatFile(fileRun, False, paramGorT)
print(df_run.head())
df_ccdb = readDatFile(fileCCDB, True, paramGorT)
print(df_ccdb.head())

def percent_change(col1,col2):
    return ( (col2 - col1) / col1) * 100

colNames = ["S", "HS", "R", "ccdb_%d" % runNum, "%s_%d" % (paramGorT,runNum), "PercentChange"]

df_compare = pd.DataFrame(columns=colNames)
df_compare["S"] = df_ccdb["S"]
df_compare["HS"] = df_ccdb["HS"]
df_compare["R"] = df_ccdb["R"]
df_compare["ccdb_%d" % runNum] = df_ccdb["%s" % paramGorT]
df_compare["%s_%d" % (paramGorT,runNum)] = df_run["%s" % paramGorT]
df_compare["PercentChange"] = round(percent_change(df_run["%s" % paramGorT], df_ccdb["%s" % paramGorT]),5)

print(df_compare.tail())
df_compare.to_csv("compareRun%dCCDB.dat" % runNum, header=True, index=False)

with open("compareRun%dCCDB_HTML.txt" % runNum, "w") as fwrite:
    fwrite.write(df_compare.to_html())

print("\nOUTPUTS ARE 1.) %s AND 2.) %s" % ("compareRun%dCCDB.dat" % runNum, "compareRun%dCCDB_HTML.txt" % runNum))
print("\nPYTHON SCRIPT COMPLETE\n")
