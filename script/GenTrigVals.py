import datetime
x_dat = datetime.datetime.now()
date = x_dat.strftime("%d %b %G")
print("DATE: " + date)

import getpass
user = getpass.getuser()
print("USER: " + user)

import argparse
parser = argparse.ArgumentParser()
parser.add_argument("filepath", type = str, help = "opens a file when given a path")
args = parser.parse_args()

print()
print("Opening " + args.filepath)

import os
filename = os.path.basename(args.filepath)

with open(args.filepath, "r") as fileObject:
    data_NPE = fileObject.read()
    fileObject.close()

print()
print(data_NPE)

lines = data_NPE.splitlines()
listNPE = []
for i in lines:
    temp = i.split()
    listNPE.append(temp)

listNPE = sorted(listNPE, key=lambda v: (v[0], v[1], v[2]))
flatListNPE = [item for sublist in listNPE for item in sublist]
flatListNPE = [float(x) for x in flatListNPE]

print()
print("Generating trigger values for fadc file...")

sec = []
lay = []
ring = []
gain = []
trig = []
for i in range(0, len(flatListNPE)):
    if i%4 == 0:
        sec.append(flatListNPE[i])
    elif i%4 == 1:
        lay.append(flatListNPE[i])
    elif i%4 == 2:
        ring.append(flatListNPE[i])
    elif i%4 == 3:
        gain.append( round(flatListNPE[i],4) )
        nphe = flatListNPE[i]
        trig.append( round((0.1*nphe)**(-1),3) )

slot_13 = []
slot_14 = []
slot_15 = []
for i in range(0, 48):
    slot = int(i/16)
    if slot == 0:
        slot_13.append(trig[i])
    elif slot == 1:
        slot_14.append(trig[i])
    elif slot == 2:
        slot_15.append(trig[i])

slot13_gains = " ".join(str(e) for e in slot_13)
slot14_gains = " ".join(str(e) for e in slot_14)
slot15_gains = " ".join(str(e) for e in slot_15)
slots_all = [slot13_gains, slot14_gains, slot15_gains]

lines = ["FADC250_SLOT 13", "FADC250_SLOT 14", "FADC250_SLOT 15"]
line_gain = ["FADC250_ALLCH_GAIN"]
runNum = int(''.join([i for i in filename if i.isdigit()]))
comment  = "#" + user + " " + date + " values calculated as (0.1phe)^(-1) from run " + str(runNum)
output = "adcctof1_gain_Run" + str(runNum) + ".txt"  

with open(output, "w") as f:
    f.write(comment)
    f.write("\n")
    for i in range(0,3):
        f.write(lines[i])
        f.write('\n')
        f.write(line_gain[0] + "  " + slots_all[i])
        f.write('\n')

print("")
print("GENERATED OUTPUT. SEE FILE " + output)
