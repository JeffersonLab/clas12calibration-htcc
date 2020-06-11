/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.modules;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.clas.viewer.AdjustFit;
import org.clas.viewer.CalibrationModule;
import org.clas.viewer.Constants;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.base.ColorPalette;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.data.GraphErrors;
import org.jlab.clas.pdg.PhysicsConstants;
import org.clas.view.DetectorShape2D;

/**
 *
 * @author devita
 */
public class TimeCalibration extends CalibrationModule {   
    

    
    public TimeCalibration(String name, ConstantsManager ccdb, Map<String,CalibrationConstants> gConstants) {
        super(name, "offset:offset_error:resolution",3, ccdb, gConstants);
        this.setRange(-30.,30.);
    }

    @Override
    public void resetEventListener() {

        H1F htsum = new H1F("htsum", 330, -200.0, 200.0);
        htsum.setTitleX("Time Offset (ns)");
        htsum.setTitleY("Counts");
        htsum.setTitle("Global Time Offset");
        htsum.setFillColor(3);
        H1F htsum_calib = new H1F("htsum_calib", 800, -20.0, 20.0);
        htsum_calib.setTitleX("Time Offset (ns)");
        htsum_calib.setTitleY("counts");
        htsum_calib.setTitle("Global Time Offset");
        htsum_calib.setFillColor(44);

        for (int sec=1; sec<=Constants.nsectors; sec++) {
            for (int lay=1; lay<=Constants.nlayers; lay++) {
                GraphErrors  gtoffsets = new GraphErrors("gtoffsets_" + sec + "_" + lay);
                gtoffsets.setTitle("Timing Offsets"); //  title
                gtoffsets.setTitleX("Crystal ID"); // X axis title
                gtoffsets.setTitleY("Timing (ns)");   // Y axis title
                gtoffsets.setMarkerColor(5); // color from 0-9 for given palette
                gtoffsets.setMarkerSize(5);  // size in points on the screen
        //        gtoffsets.setMarkerStyle(1); // Style can be 1 or 2
                for (int comp=1; comp<=Constants.npaddles[lay-1]; comp++) {
                    // initializa calibration constant table
                    this.getCalibrationTable().addEntry(sec, lay, comp);
                    this.getCalibrationTable().setDoubleValue(0.0, "offset",sec,lay,comp);

                    // initialize data group
                    H1F htime_wide = new H1F("htime_wide_" + sec + "_" + lay + "_" + comp, 400, -200.0, 200.0);
                    htime_wide.setTitleX("Time (ns)");
                    htime_wide.setTitleY("Counts");
                    htime_wide.setTitle("Component " + comp);
                    H1F htime = new H1F("htime_" + sec + "_" + lay + "_" + comp, 400, this.getRange()[0], this.getRange()[1]);
                    htime.setTitleX("Time (ns)");
                    htime.setTitleY("Counts");
                    htime.setTitle("Component " + comp);
                    H1F htime_calib = new H1F("htime_calib_" + sec + "_" + lay + "_" + comp, 400, -20., 20.);
                    htime_calib.setTitleX("Time (ns)");
                    htime_calib.setTitleY("Counts");
                    htime_calib.setTitle("Component " + comp);
                    htime_calib.setFillColor(44);
                    htime_calib.setLineColor(24);
                    F1D ftime = new F1D("ftime_" + sec + "_" + lay + "_" + comp, "[amp]*gaus(x,[mean],[sigma])", -1., 1.);
                    ftime.setParameter(0, 0.0);
                    ftime.setParameter(1, 0.0);
                    ftime.setParameter(2, 2.0);
                    ftime.setLineColor(24);
                    ftime.setLineWidth(2);
                    ftime.setOptStat("1111");
        //            ftime.setLineColor(2);
        //            ftime.setLineStyle(1);
                    DataGroup dg = new DataGroup(3, 2);
                    dg.addDataSet(htsum      , 0);
                    dg.addDataSet(htsum_calib, 1);
                    dg.addDataSet(gtoffsets,   2);
                    dg.addDataSet(htime_wide,  3);
                    dg.addDataSet(htime,       4);
                    dg.addDataSet(ftime,       4);
                    dg.addDataSet(htime_calib, 5);
                    this.getDataGroup().add(dg, sec, lay, comp);

                }
            }
        }
        getCalibrationTable().fireTableDataChanged();
    }

    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
        return Arrays.asList(getCalibrationTable());
    }

    public int getNEvents(int isec, int ilay, int icomp) {
        return this.getDataGroup().getItem(isec, ilay, icomp).getH1F("htime_" + isec + "_" + ilay + "_" + icomp).getEntries();
    }

    public void processEvent(DataEvent event) {
        // loop over FTCAL reconstructed cluster
        double startTime = -100000;
        int   triggerPID = 0;
        // get start time
        if(event.hasBank("REC::Event") && event.hasBank("REC::Particle") && event.hasBank("REC::Cherenkov")) {
            DataBank recEvent = event.getBank("REC::Event");
            DataBank recPart = event.getBank("REC::Particle");
            DataBank recCher = event.getBank("REC::Cherenkov");
            startTime  = recEvent.getFloat("startTime", 0);
            triggerPID = recPart.getInt("pid",0);
            if(triggerPID==11) {
                for(int i=0; i<recCher.rows(); i++) {
                    if(recCher.getShort("pindex", i)==0) {
                        int sector = recCher.getByte("sector",i);
                        int layer  = 1;
                        int comp   = 1; //fixme: this is just to have some numbers
                        double time = recCher.getFloat("time",i);
                        this.getDataGroup().getItem(sector,layer,comp).getH1F("htsum").fill(time-startTime);
                        this.getDataGroup().getItem(sector,layer,comp).getH1F("htime_wide_" + sector + "_" + layer + "_" + comp).fill(time-startTime);
                        this.getDataGroup().getItem(sector,layer,comp).getH1F("htime_" + sector + "_" + layer + "_" + comp).fill(time-startTime);
                    }
                }
            }
        }   
    }

    public void analyze() {
//        System.out.println("Analyzing");
        for (int sec=1; sec<=Constants.nsectors; sec++) {
            for (int lay=1; lay<=Constants.nlayers; lay++) {
                for (int comp=1; comp<=Constants.npaddles[lay-1]; comp++) {
                    this.getDataGroup().getItem(sec,lay,comp).getGraph("gtoffsets_" + sec + "_" + lay).reset();
                }
            }
        }
        for (int sec=1; sec<=Constants.nsectors; sec++) {
            for (int lay=1; lay<=Constants.nlayers; lay++) {
                for (int comp=1; comp<=Constants.npaddles[lay-1]; comp++) {
                    H1F htime = this.getDataGroup().getItem(sec,lay,comp).getH1F("htime_" + sec + "_" + lay + "_" + comp);
                    F1D ftime = this.getDataGroup().getItem(sec,lay,comp).getF1D("ftime_" + sec + "_" + lay + "_" + comp);
                    if(htime.getEntries()>100) {
                        this.initTimeGaussFitPar(ftime,htime);
                        DataFitter.fit(ftime,htime,"LQ");
                    }
                }
            }
        }
    }

    @Override
    public void setCanvasBookData() {
        this.getCanvasBook().setData(this.getDataGroup(), 4);
    }

    private void initTimeGaussFitPar(F1D ftime, H1F htime) {
        double hAmp  = htime.getBinContent(htime.getMaximumBin());
        double hMean = htime.getAxis().getBinCenter(htime.getMaximumBin());
        double hRMS  = 2; //ns
        double rangeMin = (hMean - (0.5*hRMS)); 
        double rangeMax = (hMean + (0.4*hRMS));  
        double pm = (hMean*3.)/100.0;
        ftime.setRange(rangeMin, rangeMax);
        ftime.setParameter(0, hAmp);
        ftime.setParLimits(0, hAmp*0.8, hAmp*1.2);
        ftime.setParameter(1, hMean);
        ftime.setParLimits(1, hMean-pm, hMean+(pm));
        ftime.setParameter(2, 0.2);
        ftime.setParLimits(2, 0.1*hRMS, 0.8*hRMS);
    }    

    @Override
    public void adjustFit() {
        int sector = this.getSelectedKey().getDescriptor().getSector();
        int layer  = this.getSelectedKey().getDescriptor().getLayer();
        int comp   = this.getSelectedKey().getDescriptor().getComponent();
        System.out.println("Adjusting fit for sector " + sector + ", layer " + layer + ", component " + comp);
        H1F htime = this.getDataGroup().getItem(sector,layer,comp).getH1F("htime_" + sector + "_" + layer + "_" + comp);
        F1D ftime = this.getDataGroup().getItem(sector,layer,comp).getF1D("ftime_" + sector + "_" + layer + "_" + comp);
        AdjustFit cfit = new AdjustFit(htime, ftime, "LRQ");
        this.getCanvas().update();
    }

    @Override
    public Color getColor(DetectorShape2D dsd) {
        // show summary
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        ColorPalette palette = new ColorPalette();
        Color col = new Color(100, 100, 100);
        int nent = this.getNEvents(sector, layer, key);
        if (nent > 0) {
            col = palette.getColor3D(nent, this.getnProcessed(), true);
        }
//        col = new Color(100, 0, 0);
        return col;
    }
    
    // USE to adjust plotting option for current module is default is not good    
    @Override
    public void drawDataGroup(int sector, int layer, int component) {
        if(this.getDataGroup().hasItem(sector,layer,component)==true){
            DataGroup dataGroup = this.getDataGroup().getItem(sector,layer,component);
            this.getCanvas().clear();
            this.getCanvas().divide(3,2);
            this.getCanvas().setGridX(false);
            this.getCanvas().setGridY(false);
            this.getCanvas().cd(0);
            this.getCanvas().draw(dataGroup.getH1F("htsum"));
            this.getCanvas().cd(1);
            this.getCanvas().draw(dataGroup.getH1F("htsum_calib"));
            this.getCanvas().cd(2);
            if(dataGroup.getGraph("gtoffsets_" + sector + "_" + layer).getDataSize(0)>1) {
                this.getCanvas().draw(dataGroup.getGraph("gtoffsets_" + sector + "_" + layer));
            }
            this.getCanvas().cd(3);
            this.getCanvas().draw(dataGroup.getH1F("htime_wide_" + sector + "_" + layer + "_" + component));
            this.getCanvas().cd(4);
            this.getCanvas().draw(dataGroup.getH1F("htime_" + sector + "_" + layer + "_" + component));
            this.getCanvas().cd(5);
            this.getCanvas().draw(dataGroup.getH1F("htime_calib_" + sector + "_" + layer + "_" + component));
        }
    }


    @Override
    public void timerUpdate() {
        this.analyze();
        this.updateTable();
    }
    
    @Override
    public void updateTable() {
        for (int sec=1; sec<=Constants.nsectors; sec++) {
            for (int lay=1; lay<=Constants.nlayers; lay++) {
                for (int comp=1; comp<=Constants.npaddles[lay-1]; comp++) {
                    F1D ftime = this.getDataGroup().getItem(sec,lay,comp).getF1D("ftime_" + sec + "_" + lay + "_" + comp);

                    this.getDataGroup().getItem(sec,lay,comp).getGraph("gtoffsets_" + sec + "_" + lay).addPoint(comp, ftime.getParameter(1), 0, ftime.parameter(1).error());

                    getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(sec,lay,comp).getF1D("ftime_" + sec + "_" + lay + "_" + comp).getParameter(1),      "offset",      sec,lay,comp);
                    getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(sec,lay,comp).getF1D("ftime_" + sec + "_" + lay + "_" + comp).parameter(1).error(), "offset_error",sec,lay,comp);
                    getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(sec,lay,comp).getF1D("ftime_" + sec + "_" + lay + "_" + comp).getParameter(2),      "resolution" , sec,lay,comp);
                }
                getCalibrationTable().fireTableDataChanged();
            }
        }
     }
    
    
}
