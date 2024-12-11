#!/bin/bash -f

###############################################################################
# HTCC_CalibEnviro.sh
#
# Purpose:
#   This script sets up the CLAS12 environment for HTCC calibrations on the
#   JLab ifarm systems running AlmaLinux9. It replaces the older method of
#   sourcing /group/clas12/packages/setup.sh and loading clas12/pro, as that
#   is now deprecated.
#
# Changes:
#   - Updated to the new CLAS12 environment setup using CVMFS modulefiles.
#   - Removed "clas12/pro" since it no longer exists; now just use "module load clas12".
#   - Added informational output: shell, OS version, and date.
#   - Added link to the new environment documentation at:
#     https://github.com/JeffersonLab/clas12-env/blob/main/README.md
#
# Date:   11 Dec 2024
# Author: izzy
###############################################################################

echo "======================================="
echo "  HTCC Calibration Environment Setup"
echo "======================================="

# Print current date/time for record-keeping
echo "Date and Time: $(date)"

# Check the shell being used
echo "Current Shell: $SHELL"

# Check the OS version
echo "Operating System:"
cat /etc/redhat-release

# Provide a link to the new environment instructions
echo "For CLAS12 environment documentation, see:"
echo "  https://github.com/JeffersonLab/clas12-env/blob/main/README.md"
echo ""

###############################################################################
# Environment Setup Steps:
#
# On AlmaLinux9 (EL9) systems at JLab, the CLAS12 software environment is now
# provided via CVMFS. The old setup scripts (like setup.sh) are no longer needed.
#
# The recommended approach is:
#  1. Make sure CVMFS is accessible.
#  2. Update MODULEPATH to include the CLAS12 environment modulefiles.
#  3. Load the desired CLAS12 environment.
###############################################################################

echo "SETTING ENVIRONMENT..."

# Load the modules package initialization
source /etc/profile.d/modules.sh

# Clear all previously loaded modules
module purge

# Add the CLAS12 CVMFS-based modulefiles to the MODULEPATH
module use /cvmfs/oasis.opensciencegrid.org/jlab/hallb/clas12/sw/modulefiles

# Check what modules are available (optional, for debug/info)
echo "Listing available modules:"
module avail

# Load the default CLAS12 environment (which includes ROOT, coatjava, etc.)
# Note: 'clas12/pro' no longer exists; now 'clas12' points to the production environment.
module load clas12

# Switch to a newer groovy version
module switch groovy/4.0.3 groovy/4.0.20

# List currently loaded modules for verification
module list

echo "ENVIRONMENT SET"
echo "You can now run CLAS12 software tools (e.g., ccdb, clas12root, hipo-utils, etc.)"
echo ""
###############################################################################
# End of HTCC_CalibEnviro.sh
###############################################################################
