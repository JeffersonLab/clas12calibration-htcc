import pandas as pd
import sys

# Check if enough arguments are provided
if len(sys.argv) < 9:
    print("Usage: python3 changeGainConstantsCCDB.py runNum fileCCDB sec1_factor sec2_factor sec3_factor sec4_factor sec5_factor sec6_factor")
    sys.exit(1)

runNum = int(sys.argv[1])
fileCCDB = sys.argv[2]
sector_factors = [float(sys.argv[i]) for i in range(3, 9)]

print(f"Run Number: {runNum}")
print(f"CCDB File: {fileCCDB}")
print(f"Normalization Factors: {sector_factors}")

def readDatFile(filePath, ccdbBool, paramName):
    channels = 48
    count = 0

    sector = []
    halfsector = []
    ring = []
    parameter = []

    with open(filePath) as f:
        if ccdbBool:
            next(f)  # Skip header
        for i, line in enumerate(f):
            percentage = 100 * (count / channels)
            boxes_blk = ["\u2588"] * count
            boxes_yt = [" "] * (channels - count)
            if percentage % 5 == 0:
                if count == 0:
                    print("Read %d channels\t\t[%s%s]" % (count, "".join(boxes_blk), "".join(boxes_yt)))
                else:
                    print("Read %d channels\t[%s%s]" % (count, "".join(boxes_blk), "".join(boxes_yt)))

            splitd = line.split()
            sector.append(int(splitd[0]))
            halfsector.append(int(splitd[1]))
            ring.append(int(splitd[2]))
            parameter.append(float(splitd[3]))

            count += 1
        print("Read %d channels\t[%s%s]" % (count, "".join(boxes_blk), "".join(boxes_yt)))
        
        dict_info = {
            "S": sector, 
            "HS": halfsector, 
            "R": ring, 
            paramName: parameter
        }
        
        df_info = pd.DataFrame(dict_info)
        df_info = df_info.sort_values(["R", "S"])
        df_info.reset_index(inplace=True, drop=True)
        
        return df_info

df_ccdb = readDatFile(fileCCDB, True, "gain")
print("Head of original CCDB file:")
print(df_ccdb.head())
print("Tail of original CCDB file:")
print(df_ccdb.tail())

colNames = ["S", "HS", "R", "gain"]
df_compare = pd.DataFrame(columns=colNames)
df_compare["S"] = df_ccdb["S"]
df_compare["HS"] = df_ccdb["HS"]
df_compare["R"] = df_ccdb["R"]

# Ensure the 'S' column is integer type
df_compare["S"] = df_compare["S"].astype(int)
df_compare["HS"] = df_compare["HS"].astype(int)
df_compare["R"] = df_compare["R"].astype(int)

# Apply the normalization factors to the gains
df_compare["gain"] = df_ccdb.apply(lambda row: row["gain"] * sector_factors[int(row["S"]) - 1], axis=1)

print("Head of new CCDB file:")
print(df_compare.head())
print("Tail of new CCDB file:")
print(df_compare.tail())

output_file = f"run{runNum}_gain.dat"
df_compare.to_csv(output_file, sep=" ", header=False, index=False)
print("New gain constants written to %s" % output_file)

