import org.jlab.clas.physics.Particle;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.detector.calib.tasks.CalibrationEngine;
import org.jlab.detector.calib.utils.CalibrationConstants;
import java.io.IOException;
import java.io.PrintWriter;
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
System.setProperty("java.awt.headless", "true");

public class Inclusive {
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
	    hiNphePMTOneHit.add(new H1F("hiNphePMTOneHit" + t, 800, 0.5, 200.5));
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
   if (phiLoc > 30 && phiLoc < 90) {
      sector = 2;
   }
   if (phiLoc > 90 && phiLoc < 150) {
      sector = 3;
   }
   if (phiLoc > 150 && phiLoc < 210) {
      sector = 4;
   }
   if (phiLoc > 210 && phiLoc < 270) {
       sector = 5;
   }
   if (phiLoc > 270 && phiLoc < 330) {
       sector = 6;
   }
   if (phiLoc > 330 || phiLoc < 30) {
      sector = 1;
   }
   return sector;
}

int returnHalfSector(float phi){
	int halfSector = 0;
	halfSector = (int) ((phi + 166.0) / 30);
	if (halfSector > 4) {
		halfSector = halfSector - 5;
	} 
	else {
		halfSector = halfSector + 7;
	}
	return halfSector + 1;
}

int returnRing(float theta){
	int ring = 0;
	if (theta <= 10) {
		ring = 1;
	}
	if (theta > 10 &&theta <= 20) {
		ring = 2;
	}
	if (theta > 20 && theta <= 30) {
		ring = 3;
	}
	if (theta > 30) {
		ring = 4;
	}
	return ring;
}

int returnPMT(int ring, int halfSector){
	int pmt = 0;
	pmt = (ring - 1) * 12 + halfSector;
	return pmt;
}

int returnNHits(float theta, float phi){
	int nhits = 0;
	if (((int)Math.round(theta*100) == 875 || (int)Math.round(theta*100) == 1625 || (int)Math.round(theta*100) == 2375 || (int)Math.round(theta*100) == 3125) &&(((int)Math.round(phi) + 165)%15 == 0)) nhits = 1;
	return nhits;
}

public void plot(runNumber){
       List<F1D> timeIndPMT  = new ArrayList();
       for (int t = 0; t < 48; t++) {
	timeIndPMT.add(new F1D("timeIndPMT" + t, "[amp]*gaus(x,[mean],[sigma])", lowTime, -highTime));
        timeIndPMT.get(t).setParameter(0, 500);
        timeIndPMT.get(t).setParameter(1, -0.0);
        timeIndPMT.get(t).setParameter(2, 0.7);
        timeIndPMT.get(t).setLineColor(2);
        timeIndPMT.get(t).setLineWidth(2);
        timeIndPMT.get(t).setOptStat(1101);
	}

	EmbeddedCanvas oneHitHTCCOnly = new EmbeddedCanvas();
        oneHitHTCCOnly.setSize(2400,600);
	oneHitHTCCOnly.divide(12,4);

	oneHitHTCCOnly.setAxisTitleSize(14);
	oneHitHTCCOnly.setAxisFontSize(14);
	oneHitHTCCOnly.setTitleSize(14);
        for (int t = 0; t < 48; t++){
                oneHitHTCCOnly.cd(t);
		oneHitHTCCOnly.draw(hiNphePMTOneHit.get(t));
		}
        oneHitHTCCOnly.save("nphePMT" + runNumber + ".png");

	EmbeddedCanvas oneHitHTCCOnly_ZOOM = new EmbeddedCanvas();
        oneHitHTCCOnly_ZOOM.setSize(2400,600);
        oneHitHTCCOnly_ZOOM.divide(12,4);

        oneHitHTCCOnly_ZOOM.setAxisTitleSize(14);
        oneHitHTCCOnly_ZOOM.setAxisFontSize(14);
        oneHitHTCCOnly_ZOOM.setTitleSize(14);
        for (int t = 0; t < 48; t++){
                oneHitHTCCOnly_ZOOM.cd(t);
                oneHitHTCCOnly_ZOOM.draw(hiNphePMTOneHit_ZOOM.get(t));
                }
        oneHitHTCCOnly_ZOOM.save("nphePMT_ZOOM_" + runNumber + ".png");
	
	for (int t = 0; t < 48; t++){
                oneHitHTCCOnly.cd(t);oneHitHTCCOnly.draw(hiTimePMTOneHit.get(t));
		timeIndPMT.get(t).setParameter(0, hiTimePMTOneHit.get(t).getMax());
                float maxV = hiTimePMTOneHit.get(t).getMaximumBin();
                maxV = lowTime + (maxV + 0.5)*(highTime - lowTime)/nBinsTime;
                timeIndPMT.get(t).setParameter(1, maxV);
                timeIndPMT.get(t).setParameter(2, 0.6);
                timeIndPMT.get(t).setRange(maxV - 1, maxV + 1.3);
		oneHitHTCCOnly.draw(hiTimePMTOneHit.get(t));
		oneHitHTCCOnly.getPad(t).getAxisX().setRange(maxV - 10, maxV + 10);
		DataFitter.fit(timeIndPMT.get(t), hiTimePMTOneHit.get(t), "");
		oneHitHTCCOnly.draw(timeIndPMT.get(t), "same");
                }
	oneHitHTCCOnly.save("timePMT" + runNumber + ".png");

       EmbeddedCanvas npeAllC = new EmbeddedCanvas();
       npeAllC.setSize(600,600);
       npeAllC.cd(0);
       npeAllC.draw(npeAll);
       npeAllC.save("npeAllC" + runNumber + ".png");

       EmbeddedCanvas timeAllC = new EmbeddedCanvas();
       timeAllC.setSize(600,600);
       timeAllC.cd(0);
       timeAllC.draw(timeAll);

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
       
       timeAllC.save("timeAllC" + runNumber + ".png");


float maxV = 0;
F1D deltaTF;
deltaTF = new F1D("deltaTF", "[amp]*gaus(x,[mean],[sigma])",  maxV - 1, maxV + 1);
deltaTF.setRange(maxV - 1, maxV + 1);
deltaTF.setParameter(0, 20000);
deltaTF.setParameter(1, 0);
deltaTF.setParameter(2, 1);
deltaTF.setLineColor(2);
deltaTF.setLineWidth(2);
deltaTF.setOptStat("1100");

   IndexedTable gainTable;
        IndexedTable timeTable;
        ConstantsManager ccdb;
        ccdb = new ConstantsManager();
        ccdb.init(Arrays.asList(new String[]{"/calibration/htcc/gain", "/calibration/htcc/time"}));
        gainTable = ccdb.getConstants(runNumber,"/calibration/htcc/gain");
        timeTable = ccdb.getConstants(runNumber,"/calibration/htcc/time");


float averageNPE = 0;
for (int t = 0; t < 48; t++) {
averageNPE = averageNPE + hiNphePMTOneHit.get(t).getMean();
}
averageNPE = averageNPE/48.0;
BufferedWriter genWriter = null;

genWriter = null;
try {
    genWriter = new BufferedWriter(new FileWriter("npePMT" + runNumber + ".dat"));
    for (int t = 0; t < 48; t++) {
    	ring = (int) (t/12) + 1;
        hs = (int) (t%2) + 1;
        sector = (int)(t%12)/2 + 1;
	if (hiNphePMTOneHit.get(t).getMean() > 0){
      	   genWriter.write(sector + " " + hs + " " + ring  + " " + hiNphePMTOneHit.get(t).getMean()*gainTable.getDoubleValue("gain",sector, hs, ring)/averageNPE + "\n");
	}
	else {
	   genWriter.write(sector + " " + hs + " " + ring  + " " + gainTable.getDoubleValue("gain",sector, hs, ring) + "\n");
       }
   }

}
catch (IOException e) {
}
finally
{
        try
        {
                if (genWriter != null)
                   {
                          genWriter.close();
                   }
                   } catch (IOException e)
                   {
        }
}



genWriter = null;
try {
    genWriter = new BufferedWriter(new FileWriter("timePMT" + runNumber  + ".dat"));
    for (int t = 0; t < 48; t++) {
        ring = (int) (t/12) + 1;
	hs = (int) (t%2) + 1;
        sector = (int)(t%12)/2 + 1;
	float tCurrShifted = timeIndPMT.get(t).getParameter(1) + timeTable.getDoubleValue("shift",sector, hs, ring);
	float tCurr = timeIndPMT.get(t).getParameter(1);
        genWriter.write(sector + " " + hs + " " + ring  + " " + tCurrShifted + "\n");
   }

}
catch (IOException e) {
}
finally
{
        try
        {
		if (genWriter != null)
                   {
                          genWriter.close();
                   }
                   } catch (IOException e)
                   {
		   }
}
}

 public int isSingle(double theta, double phi) {
        int single = 0;
        int isSigleTheta = 0;
        int isSiglePhi = 0;
        double resPhi = (phi + 166.0) % 30;
        if ((theta > 8 && theta < 9) || (theta > 16 && theta < 17) || (theta > 23 && theta < 24) || (theta > 31 && theta < 32)) {
            isSigleTheta = 1;
        }
        if (resPhi < 2 && resPhi > -2) {
            isSiglePhi = 1;
        }
        return isSigleTheta * isSiglePhi;
}

public void processEvent(DataEvent event
	) {
        double startTime = 0;
        float htccTime = 0;
        int sector = 0;
        int layer = 0;
        int segment = 0;
        double deltaTime = 0;
	int halfSector = 0;
	int ring = 0;
	int pmt = 0;
	
	if (event.hasBank("REC::Particle") == true && event.hasBank("REC::Cherenkov")  == true && event.hasBank("REC::Event") ==  true && event.hasBank("HTCC::rec")){  
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
		    	float p = Math.sqrt(px*px + py*py + pz*pz);
			float vz = recBankPart.getFloat("vz", loopE);
 			int status = recBankPart.getInt("status", 0);
		        if (recBankPart.getInt("pid", loopE) == 11 && p > 1.5 && status < -1999 && status > -4000 && vz > -10 && vz < 10) {
                                for (int j = 0; j < recDeteHTCC.rows(); j++) {
                                        if (recDeteHTCC.getShort("pindex", j) == loopE && recDeteHTCC.getByte("detector", j) == 15) {
						float nphe = recDeteHTCC.getFloat("nphe", j);
                                                //change pindex to index
                                                float thetaHTCC =  Math.toDegrees(recHTCC.getFloat("theta", recDeteHTCC.getInt("index", j)));
                                                float phiHTCC = Math.toDegrees(recHTCC.getFloat("phi", recDeteHTCC.getInt("index", j)));
						float timeCC = recDeteHTCC.getFloat("time", j);
                                                float path = recDeteHTCC.getFloat("path", j); //cm
                                                double c = 29.98; //cm per ns
						npeAll.fill(nphe);
                                                if (returnNHits(thetaHTCC, phiHTCC) == 1){
						        //double deltaTimeCC = timeCC - startTime;
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




