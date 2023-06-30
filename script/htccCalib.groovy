import org.jlab.clas.physics.Particle;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.detector.calib.tasks.CalibrationEngine;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.clas.physics.LorentzVector;
import org.jlab.clas.physics.Vector3;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.detector.decode.CodaEventDecoder;
import org.jlab.detector.decode.DetectorEventDecoder;
import org.jlab.detector.view.DetectorListener;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.H2F;
import org.jlab.groot.data.TDirectory;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.TDirectory;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.io.PrintWriter;

System.setProperty("java.awt.headless", "true");

public class Inclusive {
    public static final int NUMBER_OF_CHANNELS = 48;
    public static final int CHANNELS_PER_RING = 12;
    public static final int HALF_SECTORS_PER_SECTOR = 2;

    public int runNumber;
    int ring, sector, hs;	
    List<H1F> hiNphePMTOneHit = new ArrayList();
    List<H1F> hiNphePMTOneHit_ZOOM = new ArrayList();
    List<H1F> hiTimePMTOneHit = new ArrayList();
    H1F timeAll;
    H1F npeAll;
    static int nBinsTime = 4000;
    static float lowTime = -500;
    static float highTime = 500;

	public Inclusive(){
        for (int t = 0; t < 48; t++) {
            hiNphePMTOneHit.add(new H1F("hiNphePMTOneHit" + t, 80, 0.5, 400.5));
            hiNphePMTOneHit_ZOOM.add(new H1F("hiNphePMTOneHitZOOM" + t, 80, 0.5, 40.5));
            ring = (int) (t/12) + 1;
            hs = (int) (t%2) + 1;
            sector = (int)(t%12)/2 + 1;
            hiNphePMTOneHit.get(t).setTitle("S"  + sector + " HS " + hs + " R " + ring);
            hiNphePMTOneHit.get(t).setTitle("S"  + sector + " HS " + hs + " R " + ring);
            hiNphePMTOneHit.get(t).setOptStat(110);
            hiNphePMTOneHit.get(t).setOptStat(110);

            hiNphePMTOneHit_ZOOM.get(t).setTitle("S"  + sector + " HS " + hs + " R " + ring);
            hiNphePMTOneHit_ZOOM.get(t).setTitle("S"  + sector + " HS " + hs + " R " + ring);
            hiNphePMTOneHit_ZOOM.get(t).setOptStat(110);
            hiNphePMTOneHit_ZOOM.get(t).setOptStat(110);            

            hiTimePMTOneHit.add(new H1F("hiTimePMTOneHit" + t, nBinsTime, lowTime, highTime));
            ring = (int) (t/12) + 1;
            hs = (int) (t%2) + 1;
            sector = (int)(t%12)/2 + 1;
            hiTimePMTOneHit.get(t).setTitle("S"  + sector + " HS " + hs + " R " + ring);
            hiTimePMTOneHit.get(t).setTitle("S"  + sector + " HS " + hs + " R " + ring);
        }
        
        timeAll = new H1F("timeAll", 2000, -4, 4);
        timeAll.setOptStat(110);
        timeAll.setTitle("Combined HTCC timing");
        timeAll.setTitleX("Time, ns");

        npeAll = new H1F("npeAll", "npeAll", 50, 0, 50);
        npeAll.setOptStat(110);
    }

    int returnSector(double phi) {
        double phiLoc = phi;
        int sector = -100;
        if (phiLoc< -30) phiLoc = phiLoc + 360;
        if (phiLoc > 30 && phiLoc < 90) sector = 2;
        if (phiLoc > 90 && phiLoc < 150) sector = 3;
        if (phiLoc > 150 && phiLoc < 210) sector = 4;
        if (phiLoc > 210 && phiLoc < 270) sector = 5;
        if (phiLoc > 270 && phiLoc < 330) sector = 6;
        if (phiLoc > 330 || phiLoc < 30) sector = 1;
        return sector;
    }

    int returnHalfSector(float phi){
        int halfSector = 0;
        halfSector = (int) ((phi + 166.0) / 30);
        if (halfSector > 4) halfSector = halfSector - 5;
        else halfSector = halfSector + 7;
        return halfSector + 1;
    }

    int returnRing(float theta){
        int ring = 0;
        if (theta <= 10) ring = 1;
        if (theta > 10 &&theta <= 20) ring = 2;
        if (theta > 20 && theta <= 30) ring = 3;
        if (theta > 30) ring = 4;
        return ring;
    }

    int returnPMT(int ring, int halfSector){
        int pmt = 0;
        pmt = (ring - 1) * 12 + halfSector;
        return pmt;
    }

    int returnNHits(float theta, float phi){
        int nhits = 0;
        if (
            ((int)Math.round(theta*100) == 875 || (int)Math.round(theta*100) == 1625 || (int)Math.round(theta*100) == 2375 || (int)Math.round(theta*100) == 3125) &&
            (((int)Math.round(phi) + 165)%15 == 0)
        ) nhits = 1;
        return nhits;
    }

    public void plot(runNumber){
        F1D f_factor = new F1D("factor = 1", "[a]", 0, 47); 
        f_factor.setParameter(0, 1);
        f_factor.setLineColor(2);
        f_factor.setLineWidth(2);

        F1D f_shift = new F1D("time shift = 0", "[a]", 0, 47); 
        f_shift.setParameter(0, 0);
        f_shift.setLineColor(2);
        f_shift.setLineWidth(2);

        F1D f_5per = new F1D("perchange = 5", "[a]", 0, 47); 
        f_5per.setParameter(0, 5);
        f_5per.setLineColor(2);
        f_5per.setLineWidth(2);

        F1D f_10per = new F1D("perchange = 10", "[a]", 0, 47); 
        f_10per.setParameter(0, 10);
        f_10per.setLineColor(2);
        f_10per.setLineWidth(2);

        List<F1D> timeIndPMT  = new ArrayList<>();
        EmbeddedCanvas oneHitHTCCOnly = new EmbeddedCanvas();
        EmbeddedCanvas oneHitHTCCOnly_ZOOM = new EmbeddedCanvas();
        EmbeddedCanvas npeAllC = new EmbeddedCanvas();
        EmbeddedCanvas timeAllC = new EmbeddedCanvas();
        float maxV = 0;
        F1D deltaTF;
        IndexedTable gainTable;
        IndexedTable timeTable;
        ConstantsManager ccdb;
        float averageNPE = 0;

        ccdb = new ConstantsManager();
        ccdb.init(Arrays.asList(new String[]{"/calibration/htcc/gain", "/calibration/htcc/time"}));
        gainTable = ccdb.getConstants(runNumber,"/calibration/htcc/gain");
        timeTable = ccdb.getConstants(runNumber,"/calibration/htcc/time");

        oneHitHTCCOnly.setSize(2400,600);
        oneHitHTCCOnly.divide(12,4);
        oneHitHTCCOnly.setAxisTitleSize(14);
        oneHitHTCCOnly.setAxisFontSize(14);
        oneHitHTCCOnly.setTitleSize(14);

        oneHitHTCCOnly_ZOOM.setSize(2400,600);
        oneHitHTCCOnly_ZOOM.divide(12,4);
        oneHitHTCCOnly_ZOOM.setAxisTitleSize(14);
        oneHitHTCCOnly_ZOOM.setAxisFontSize(14);
        oneHitHTCCOnly_ZOOM.setTitleSize(14);

        npeAllC.setSize(600,600);
        npeAllC.cd(0);
        npeAllC.draw(npeAll);

        timeAllC.setSize(600,600);
        timeAllC.cd(0);
        timeAllC.draw(timeAll);

        deltaTF = new F1D("deltaTF", "[amp]*gaus(x,[mean],[sigma])",  maxV - 1, maxV + 1);
        deltaTF.setRange(maxV - 1, maxV + 1);
        deltaTF.setParameter(0, 20000);
        deltaTF.setParameter(1, 0);
        deltaTF.setParameter(2, 1);
        deltaTF.setLineColor(2);
        deltaTF.setLineWidth(2);
        deltaTF.setOptStat("1100");

        for (int t = 0; t < 48; t++) {
            timeIndPMT.add(new F1D("timeIndPMT" + t, "[amp]*gaus(x,[mean],[sigma])", lowTime, -highTime));
            timeIndPMT.get(t).setParameter(0, 500);
            timeIndPMT.get(t).setParameter(1, -0.0);
            timeIndPMT.get(t).setParameter(2, 0.7);
            timeIndPMT.get(t).setLineColor(2);
            timeIndPMT.get(t).setLineWidth(2);
            timeIndPMT.get(t).setOptStat(1101);

            oneHitHTCCOnly.cd(t);
            oneHitHTCCOnly.draw(hiNphePMTOneHit.get(t));
            oneHitHTCCOnly_ZOOM.cd(t);
            oneHitHTCCOnly_ZOOM.draw(hiNphePMTOneHit_ZOOM.get(t));

            oneHitHTCCOnly.cd(t);
            oneHitHTCCOnly.draw(hiTimePMTOneHit.get(t));
            timeIndPMT.get(t).setParameter(0, hiTimePMTOneHit.get(t).getMax());
            maxV = hiTimePMTOneHit.get(t).getMaximumBin();
            maxV = lowTime + (maxV + 0.5)*(highTime - lowTime)/nBinsTime;
            timeIndPMT.get(t).setParameter(1, maxV);
            timeIndPMT.get(t).setParameter(2, 0.6);
            timeIndPMT.get(t).setRange(maxV - 1, maxV + 1.3);
            oneHitHTCCOnly.draw(hiTimePMTOneHit.get(t));
            oneHitHTCCOnly.getPad(t).getAxisX().setRange(maxV - 10, maxV + 10);
            DataFitter.fit(timeIndPMT.get(t), hiTimePMTOneHit.get(t), "");
            oneHitHTCCOnly.draw(timeIndPMT.get(t), "same");

            averageNPE = averageNPE + hiNphePMTOneHit.get(t).getMean();
        }
        averageNPE = averageNPE/48.0;

        F1D timeAllFit = new F1D("timeAllFit", "[amp]*gaus(x,[mean],[sigma])",  -1, 1);
        timeAllFit.setRange(-1, 1);
        timeAllFit.setParameter(0, 20000);
        timeAllFit.setParameter(1, 0);
        timeAllFit.setParameter(2, 1);
        timeAllFit.setLineColor(2);
        timeAllFit.setLineWidth(2);
        timeAllFit.setOptStat("1100");
        DataFitter.fit(timeAllFit, timeAll, "");
        timeAllC.draw(timeAllFit, "same");

        oneHitHTCCOnly.save("nphePMT" + runNumber + ".png");
        oneHitHTCCOnly_ZOOM.save("nphePMT_ZOOM_" + runNumber + ".png");
        oneHitHTCCOnly.save("timePMT" + runNumber + ".png");
        npeAllC.save("npeAllC" + runNumber + ".png");
        timeAllC.save("timeAllC" + runNumber + ".png");

        try (
            BufferedWriter genWriter = new BufferedWriter(new FileWriter("npePMT" + runNumber + ".dat"));
            BufferedWriter normalizationWriter = new BufferedWriter(new FileWriter("correctionFactor_NphePMT" + runNumber + ".dat"))
        ) {
            normalizationWriter.write("Sector,HalfSector,Ring,Mean,StdDev,AverageNPE,Factor,gainTableValue,NewNphe,Difference,PercentChange\n");

            GraphErrors graphNpheMeanStdDev = new GraphErrors();
            graphNpheMeanStdDev.setTitle("nphe mean per HTCC channel"); // plot title
            graphNpheMeanStdDev.setTitleX("Channel"); // x-axis title
            graphNpheMeanStdDev.setTitleY("NPHE Mean"); // y-axis title

            GraphErrors graph_FactorNPHE = new GraphErrors();
            graph_FactorNPHE.setTitle("mean nphe per channel to average nphe over all channels per HTCC channel"); // plot title
            graph_FactorNPHE.setTitleX("Channel"); // x-axis title
            graph_FactorNPHE.setTitleY("correction factor"); // y-axis title

            GraphErrors graph_CurrentGain = new GraphErrors();
            graph_CurrentGain.setTitle("current ccdb gain constants per HTCC channel"); // plot title
            graph_CurrentGain.setTitleX("Channel"); // x-axis title
            graph_CurrentGain.setTitleY("current ccdb gain constants"); // y-axis title

            GraphErrors graph_NpheNew = new GraphErrors();
            graph_NpheNew.setTitle("new gain constants per HTCC channel"); // plot title
            graph_NpheNew.setTitleX("Channel"); // x-axis title
            graph_NpheNew.setTitleY("new gain constants"); // y-axis title

            GraphErrors graph_Difference = new GraphErrors();
            graph_Difference.setTitle("difference in new and current gain constants per HTCC channel"); // plot title
            graph_Difference.setTitleX("Channel"); // x-axis title
            graph_Difference.setTitleY("difference"); // y-axis title

            GraphErrors graph_PercentDiff = new GraphErrors();
            graph_PercentDiff.setTitle("percent difference in new and current gain constants per HTCC channel"); // plot title
            graph_PercentDiff.setTitleX("Channel"); // x-axis title
            graph_PercentDiff.setTitleY("percent difference"); // y-axis title

            for (int t = 0; t < 48; t++) {
                int ring = (int) (t / 12) + 1;
                int hs = (int) (t % 2) + 1;
                int sector = (int) (t % 12) / 2 + 1;

                double mean = hiNphePMTOneHit.get(t).getMean();
                double stdDev = hiNphePMTOneHit.get(t).getRMS();
                double factor = mean / averageNPE;
                double gainTableValue = gainTable.getDoubleValue("gain", sector, hs, ring);
                double newNphe;

                graphNpheMeanStdDev.addPoint(t, mean, 0, stdDev);
                graph_FactorNPHE.addPoint(t, factor, 0, 0);

                if (hiNphePMTOneHit.get(t).getMean() > 0) {
                    newNphe = factor * gainTableValue;
                } else {
                    newNphe = gainTableValue;
                }
                graph_CurrentGain.addPoint(t, gainTableValue, 0, 0);
                graph_NpheNew.addPoint(t, newNphe, 0, 0);

                double diff = newNphe - gainTableValue;
                double perdiff = 100*((newNphe - gainTableValue)/gainTableValue);
                graph_Difference.addPoint(t, diff, 0, 0);
                graph_PercentDiff.addPoint(t, perdiff, 0, 0);

                normalizationWriter.write(sector + "," + hs + "," + ring + "," + mean + "," + stdDev + "," + averageNPE + "," + factor + "," + gainTableValue + "," + newNphe + "," + diff + "," + perdiff + "\n");
                genWriter.write(sector + " " + hs + " " + ring + " " + newNphe + "\n");
            }

            F1D f_avgNPHE = new F1D("averageNPE", "[a]", 0, 47); 
            f_avgNPHE.setParameter(0, averageNPE);
            f_avgNPHE.setLineColor(2);
            f_avgNPHE.setLineWidth(2);

            EmbeddedCanvas canvas_infoplots = new EmbeddedCanvas();
            canvas_infoplots.setSize(2*800,3*800);
            canvas_infoplots.divide(2,3); //2 columns 3 rows

            canvas_infoplots.cd(0);
            canvas_infoplots.draw(f_avgNPHE);
            canvas_infoplots.draw(graphNpheMeanStdDev,"same");
            canvas_infoplots.cd(1);
            canvas_infoplots.draw(f_factor);
            canvas_infoplots.draw(graph_FactorNPHE,"same");

            canvas_infoplots.cd(2);
            canvas_infoplots.draw(graph_CurrentGain);
            canvas_infoplots.cd(3);
            canvas_infoplots.draw(graph_NpheNew);

            canvas_infoplots.cd(4);
            canvas_infoplots.draw(graph_Difference);

            canvas_infoplots.cd(5);
            canvas_infoplots.draw(f_5per);
            canvas_infoplots.draw(f_10per, "same");
            canvas_infoplots.draw(graph_PercentDiff,"same");

            canvas_infoplots.save("infoplots_NPHE" + runNumber + ".png");

        } catch (IOException e) {
            // Handle exception
        }

        try (
            BufferedWriter genWriter = new BufferedWriter(new FileWriter("timePMT" + runNumber + ".dat"))
            BufferedWriter normalizationWriter = new BufferedWriter(new FileWriter("correctionFactor_TimePMT" + runNumber + ".dat"))
            ) {
                normalizationWriter.write("Sector,HalfSector,Ring,TimeShift,CurrentTime,NewTime,Difference,PercentChange\n");

                GraphErrors graph_ccdbtime = new GraphErrors();
                graph_ccdbtime.setTitle("current ccdb time per channel"); // plot title
                graph_ccdbtime.setTitleX("Channel"); // x-axis title
                graph_ccdbtime.setTitleY("ccdb time [ns]"); // y-axis title

                GraphErrors graph_newtime = new GraphErrors();
                graph_newtime.setTitle("new time per channel"); // plot title
                graph_newtime.setTitleX("Channel"); // x-axis title
                graph_newtime.setTitleY("new time [ns]"); // y-axis title

                GraphErrors graph_timeshift = new GraphErrors();
                graph_timeshift.setTitle("time shift per channel"); // plot title
                graph_timeshift.setTitleX("Channel"); // x-axis title
                graph_timeshift.setTitleY("time shift [ns]"); // y-axis title

                GraphErrors graph_timediff = new GraphErrors();
                graph_timediff.setTitle("difference between new and ccdb time per channel"); // plot title
                graph_timediff.setTitleX("Channel"); // x-axis title
                graph_timediff.setTitleY("difference [ns]"); // y-axis title

                GraphErrors graph_timeperdiff = new GraphErrors();
                graph_timeperdiff.setTitle("percent difference between new and ccdb time per channel"); // plot title
                graph_timeperdiff.setTitleX("Channel"); // x-axis title
                graph_timeperdiff.setTitleY("difference"); // y-axis title

                for (int t = 0; t < 48; t++) {
                    int ring = (int) (t / 12) + 1;
                    int hs = (int) (t % 2) + 1;
                    int sector = (int) (t % 12) / 2 + 1;
                    double timeTableValue = timeTable.getDoubleValue("shift", sector, hs, ring);
                    float tCurr = timeIndPMT.get(t).getParameter(1);
                    float tCurrShifted = tCurr + timeTableValue;

                    double timediff = tCurrShifted-timeTableValue;
                    double timeperdiff = 100*((tCurrShifted-timeTableValue)/timeTableValue);

                    normalizationWriter.write(sector + "," + hs + "," + ring + "," + tCurr + "," + timeTableValue + "," + tCurrShifted + "," + timediff + "," + timeperdiff + "\n");
                    genWriter.write(sector + " " + hs + " " + ring + " " + tCurrShifted + "\n");

                    graph_ccdbtime.addPoint(t, timeTableValue, 0, 0);
                    graph_newtime.addPoint(t, tCurrShifted, 0, 0);
                    graph_timeshift.addPoint(t, tCurr, 0, 0);
                    graph_timediff.addPoint(t, timediff, 0, 0);
                    graph_timeperdiff.addPoint(t, timeperdiff, 0, 0);
                }

                EmbeddedCanvas canvas_infoplotstime = new EmbeddedCanvas();
                canvas_infoplotstime.setSize(3*800,2*800);
                canvas_infoplotstime.divide(3,2); //3 columns 2 rows

                canvas_infoplotstime.cd(0);
                canvas_infoplotstime.draw(graph_ccdbtime);
                canvas_infoplotstime.cd(1);
                canvas_infoplotstime.draw(graph_newtime);
                canvas_infoplotstime.cd(2);

                canvas_infoplotstime.cd(3);
                canvas_infoplotstime.draw(f_shift);
                canvas_infoplotstime.draw(graph_timeshift, "same");
                canvas_infoplotstime.cd(4);
                canvas_infoplotstime.draw(graph_timediff);
                canvas_infoplotstime.cd(5);
                canvas_infoplotstime.draw(f_5per);
                canvas_infoplotstime.draw(f_10per, "same");
                canvas_infoplotstime.draw(graph_timeperdiff,"same");

                canvas_infoplotstime.save("infoplots_Time" + runNumber + ".png");
        } catch (IOException e) {
            // Handle exception
        }

    }

    public void processEvent(DataEvent event) {
        double startTime = 0;
        float htccTime = 0;
        int sector = 0;
        int layer = 0;
        int segment = 0;
        double deltaTime = 0;
        int halfSector = 0;
        int ring = 0;
        int pmt = 0;

        if (event.hasBank("REC::Particle") && event.hasBank("REC::Cherenkov") 
            && event.hasBank("REC::Event") && event.hasBank("HTCC::rec")) {  
            
            DataBank recBankPart = event.getBank("REC::Particle");
            DataBank recDeteHTCC = event.getBank("REC::Cherenkov");
            DataBank recEvenEB = event.getBank("REC::Event");
            DataBank recHTCC = event.getBank("HTCC::rec");
            startTime = recEvenEB.getFloat("startTime", 0);
            DataBank configBank = event.getBank("RUN::config");
            runNumber = configBank.getInt("run", 0);
            
            for (int loopE = 0; loopE < 1; loopE++) {
                float px = recBankPart.getFloat("px", loopE);
                float py = recBankPart.getFloat("py", loopE);
                float pz = recBankPart.getFloat("pz", loopE);
                float p = (float) Math.sqrt(px*px + py*py + pz*pz);
                float vz = recBankPart.getFloat("vz", loopE);
                int status = recBankPart.getInt("status", 0);
                
                if (recBankPart.getInt("pid", loopE) == 11 && p > 1.5 && status < -1999 && status > -4000 && vz > -10 && vz < 10) {
                    for (int j = 0; j < recDeteHTCC.rows(); j++) {
                        if (recDeteHTCC.getShort("pindex", j) == loopE && recDeteHTCC.getByte("detector", j) == 15) {
                            float nphe = recDeteHTCC.getFloat("nphe", j);
                            float thetaHTCC = (float) Math.toDegrees(recHTCC.getFloat("theta", recDeteHTCC.getInt("index", j)));
                            float phiHTCC = (float) Math.toDegrees(recHTCC.getFloat("phi", recDeteHTCC.getInt("index", j)));
                            float timeCC = recDeteHTCC.getFloat("time", j);
                            float path = recDeteHTCC.getFloat("path", j); //cm
                            double c = 29.98; //cm per ns
                            npeAll.fill(nphe);

                            if (returnNHits(thetaHTCC, phiHTCC) == 1){
                                double deltaTimeCC = timeCC - (path/c) - startTime;
                                halfSector = returnHalfSector(phiHTCC);
                                ring = returnRing(thetaHTCC);
                                pmt = returnPMT(ring, halfSector);
                                hiNphePMTOneHit.get(pmt - 1).fill(nphe);
                                hiNphePMTOneHit_ZOOM.get(pmt - 1).fill(nphe);
                                hiTimePMTOneHit.get(pmt - 1).fill(deltaTimeCC);
                                timeAll.fill(deltaTimeCC);
                            }
                        }
                    }
                }
            }
        }
    }

}

Inclusive_ana = new Inclusive();
HipoDataSource reader = new HipoDataSource();
String filename ;
filename = args[0];

reader.open(filename);
int count = 0;
while(reader.hasEvent()){
	count++;
	DataEvent event = reader.getNextEvent();
	Inclusive_ana.processEvent(event);
	if(count%10000 == 0) System.out.println(count);
//	if(count%1000000 == 0) break;
}
Inclusive_ana.plot(Inclusive_ana.runNumber);
