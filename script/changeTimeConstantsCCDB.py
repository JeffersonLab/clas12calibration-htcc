import pandas as pd
import sys

runNum = int(sys.argv[1])
fileCCDB = sys.argv[2]
print(sys.argv[1])
print(sys.argv[2])

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

df_ccdb = readDatFile(fileCCDB, True, "time")
print(df_ccdb.head())

colNames = ["S", "HS", "R", "time"]
df_compare = pd.DataFrame(columns=colNames)
df_compare["S"] = df_ccdb["S"]
df_compare["HS"] = df_ccdb["HS"]
df_compare["R"] = df_ccdb["R"]

timeShift = -5.921569
#timeShift = -5.905573636363638
df_compare["time"] = df_ccdb["time"] + timeShift

print(df_compare.tail())
df_compare.to_csv("run%d_%s.dat" % (runNum, "time"), sep=" ", header=False, index=False)

