#!/bin/bash -f
#echo "$PATH"

echo "SETTING ENVIRONMENT..."
source /group/clas12/packages/setup.sh
module avail
module load clas12/pro
module list
echo "ENVIRONMENT SET"
echo ""
