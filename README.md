# CalibrationTemplate
Template of Calibration Suite

Calibration GUI based on COATJAVA CalibrationEngine class.
Main elements are:
- dataGroups: IndexedList<DataGroup> that contains all the relevant data set for the calibration, indexed based on the detector element identifier, sector, layer, component;
- calib:      instance of CalibrationConstants that contains the calibration constants derived from the analysis of the above dataGroups.
