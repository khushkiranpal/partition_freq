package TEST;

import java.io.IOException;

import scheduleRMS.MixedConstantBackFreq_1___03_12_18;
import scheduleRMS.MixedConstantBackFreq_fq___06_12_19;
import scheduleRMS.MixedNonUniformFeasibleFreq;
import scheduleRMS.MixedNonUniformFreq;
import scheduleRMS.RMS_DPM_FEASIBLE_FREQ;
import scheduleRMS.RMS_DPM_OLD;
import scheduleRMS.RMS_LLB_N;
import scheduleRMS.RMS_MOG_FREQ_U_SUM;
import scheduleRMS.RMS_NoPowerManag;
import scheduleRMS.RMS_Sysclock;
import scheduleRMS.RMS_ZHANGmin_freq;


public class TestMixed {
	public static final  long hyperperiod_factor= 10;	//
	public static final   double  CRITICAL_TIME= 0.47*hyperperiod_factor;///1500;  //1.5 *(P)100=1500
	public static final   double  CRITICAL_freq= 0.30;   //0.50;//
	public static final   double  min_freq= 0.15;
	public static final   double  bcetRatio= 0.5;   //0.50;//
	public static final boolean faultFromFile=false;
	public static final int d =4;  // FAULT TOLERANCE PARAMETER
	public static final long hyper =100000;
	private double freq=1; // TEMP PARAMETER
	public static final int proc =32; 
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//TEST   TEST/";//
		String filename= "TEST";//"TESTfault";//testhaque
		String partitioning= "WFD_THRES";//"WFD" "WFD_THRES" "M_WFD" "PRIORITY" /////"PRIORITY_THRES"
		String inputFolder = "D:/CODING/ENERGY_RMS_DPM11-12-18/MULTI 17-12-18/TASKSET/0.1/";
		String outputFolder= "D:/CODING/ENERGY_RMS_DPM11-12-18/MULTI 17-12-18/TEST/";//OUTPUT/32 proc/0.1/"+partitioning;//11-12-18+hyper+"_"+proc ;     //MIXED OVERLOADING/14-2-18/DUAL PROCESSOR/
		String inputfilename=  filename+".txt";
			//long hyper = 100000;
	
		MixedConstantBackFreq_1___03_12_18  schedul1 = new MixedConstantBackFreq_1___03_12_18();
		MixedConstantBackFreq_fq___06_12_19  schedul2 = new MixedConstantBackFreq_fq___06_12_19();
//not working		MixedConstantBackFreq_mogdass___05_12_20  schedul3 = new MixedConstantBackFreq_mogdass___05_12_20();
		MixedNonUniformFreq schedul3 = new MixedNonUniformFreq();
		MixedNonUniformFeasibleFreq schedul4 = new MixedNonUniformFeasibleFreq();
		RMS_NoPowerManag schedul5 = new RMS_NoPowerManag();
		RMS_Sysclock schedul6 = new RMS_Sysclock();
		RMS_LLB_N schedul7 = new RMS_LLB_N();
		RMS_MOG_FREQ_U_SUM schedul8 = new RMS_MOG_FREQ_U_SUM();
		RMS_DPM_OLD schedul9 = new RMS_DPM_OLD();
		RMS_DPM_FEASIBLE_FREQ schedul10 = new RMS_DPM_FEASIBLE_FREQ();
		RMS_ZHANGmin_freq schedul11 = new RMS_ZHANGmin_freq();
			/*schedul1.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc,partitioning);*/
		schedul2.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc,  partitioning);
		/*schedul3.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc,  partitioning);*/
		/*schedul4.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc, partitioning);*/
		/*schedul5.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc);
		schedul6.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc);
		schedul7.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc);
		schedul8.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc);
		schedul9.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc);
		schedul10.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc);
		schedul11.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc);*/
	
	}

}
