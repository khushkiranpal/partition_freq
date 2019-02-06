package TEST;

import java.io.IOException;

import multiEnergyFault.DvfsDPMmix_LLB;
import multiEnergyFault.DvfsDPMmix_u;
import multiEnergyFault.DvfsOnly_LLB;
import multiEnergyFault.DvfsOnly_u;
import multiEnergyFault.NPM_multiP;
import multiEnergyFault.SETS;


public class TestMixed {
	public static final  long hyperperiod_factor= 10;	//
	public static final   double  CRITICAL_TIME= 1.5*hyperperiod_factor;///1500;  //1.5 *(P)100=1500
	public static final   double  CRITICAL_freq= 0.40;   //0.50;//
	public static final   double  min_freq= 0.15;
	public static final   double  bcetRatio= 0.5;   //0.50;//
	public static final boolean faultFromFile=false;
	public static final int d =2;  // FAULT TOLERANCE PARAMETER
	public static final long hyper =100000;
	private double freq=1; // TEMP PARAMETER
	public static final int proc =2 ;
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//TEST   TEST/";//
		String filename= "test";//"TESTfault";//testhaque
		//"WFD" "WFD_THRES"  "M_WFD" "PRIORITY" //"PRIORITY_THRES"
		String partitioning= "M_WFD";
		String inputFolder = "D:/CODING/ENERGY_RMS_DPM11-12-18/MULTI 17-12-18/TASKSET/0.05/16 proc/";
		String outputFolder= "D:/CODING/ENERGY_RMS_DPM11-12-18/MULTI 17-12-18/OUTPUT/16 proc/0.05/"+partitioning;//11-12-18+hyper+"_"+proc ;     //MIXED OVERLOADING/14-2-18/DUAL PROCESSOR/
		String inputfilename=  filename+".txt";
			//long hyper = 100000;
	
		NPM_multiP  schedul1 = new NPM_multiP();
		SETS  schedul2 = new SETS();
		DvfsOnly_u  schedul3 = new DvfsOnly_u();
		DvfsOnly_LLB  schedul4 = new DvfsOnly_LLB();
		DvfsDPMmix_u  schedul5 = new DvfsDPMmix_u();
		DvfsDPMmix_LLB  schedul6 = new DvfsDPMmix_LLB();
		
		
		
		
		
	/*	//not working		MixedConstantBackFreq_mogdass___05_12_20  schedul3 = new MixedConstantBackFreq_mogdass___05_12_20();
		MixedNonUniformFreq schedul3 = new MixedNonUniformFreq();
		MixedNonUniformFeasibleFreq schedul4 = new MixedNonUniformFeasibleFreq();
		
		RMS_NoPowerManag schedul5 = new RMS_NoPowerManag();
		RMS_Sysclock schedul6 = new RMS_Sysclock();
		RMS_LLB_N schedul7 = new RMS_LLB_N();
		RMS_MOG_FREQ_U_SUM schedul8 = new RMS_MOG_FREQ_U_SUM();
		RMS_DPM_OLD schedul9 = new RMS_DPM_OLD();
		RMS_DPM_FEASIBLE_FREQ schedul10 = new RMS_DPM_FEASIBLE_FREQ();
		RMS_ZHANGmin_freq schedul11 = new RMS_ZHANGmin_freq();*/
		schedul1.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc,partitioning);
		schedul2.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc,partitioning);
		
		schedul3.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc,  partitioning);
		schedul4.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc, partitioning);
		schedul5.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc, partitioning);
		schedul6.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
				d,CRITICAL_TIME,CRITICAL_freq,min_freq,
				faultFromFile,bcetRatio, hyper, proc, partitioning);
	/*	schedul7.schedule(inputfilename,outputFolder,inputFolder,hyperperiod_factor,
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
