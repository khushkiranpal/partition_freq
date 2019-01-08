/**
 * 
 */
package TEST;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;

import platform.Processor;
import platform.Reliability;
import queue.ISortedQueue;
import queue.SortedQueuePeriod;
import scheduleRMS.Partitioning;
import taskGeneration.FileTaskReaderTxt;
import taskGeneration.ITask;

/**
 * @author KHUSHKIRAN PAL
 *
 */
public class test {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String filename= "GenerateTaskSetHAQUE10_n_20_MAX_P_100_Utotal_2.0_18_09_2018_13_50";//"TESTfault";//testhaque
		 String inputFolder = "D:/CODING/MULTI_PROC/TASKSET/";
		String outputFolder= "D:/CODING/ENERGY_RMS_DPM/ZHANG_IMPROVED/zhangmixedtest/" ;//+hyper+"_"+proc ;     //MIXED OVERLOADING/14-2-18/DUAL PROCESSOR/
		String inputfilename=  filename+".txt";
		//  String filenamea= outputFolder+"allocationMixed"+"_"+inputfilename+"_";//+date;
		  FileTaskReaderTxt reader = new FileTaskReaderTxt(inputFolder+inputfilename); // read taskset from file
		   ITask[] set = null;
		  // set = reader.nextTaskset();
		  int  total_no_tasksets=0;
		  while ((set = reader.nextTaskset()) != null) // SCHEDULING STARTS FOR ALL TASKSETS IN FILE
		    {
			  
			  String filenamea= outputFolder+"allocationMixed"+"_"+inputfilename+"_"+total_no_tasksets;//+date;
				
			  System.out.println("  tasksets  "+total_no_tasksets++);
		//String outputFolder= "D:/CODING/ENERGY_RMS_DPM/ZHANG_IMPROVED/zhangmixedtest/" +hyper+"_"+proc ;     //MIXED OVERLOADING/14-2-18/DUAL PROCESSOR/
		int m=8;
		
		   ISortedQueue queue = new SortedQueuePeriod ();
	    	queue.addTasks(set);
	    	ArrayList<ITask> taskset = new ArrayList<ITask>();
	    	taskset = queue.getSortedSet();
	    	double f=0.9;
	    
	    	//LIST OF FREE PROCESSORS
			Comparator<Processor> comparator = new Comparator<Processor>() {
		    	 public int compare(Processor p1, Processor p2) {
					int cmp =  (int) (p1.getId()-p2.getId());
					return cmp;
				}
			  };
	  	  ArrayList<Processor> freeProcList = new ArrayList<Processor>(); //LIST OF FREE PROCESSORS
		  freeProcList.sort(comparator);
	
		  ArrayList<Processor> no_of_proc = new ArrayList<Processor>(); //total processor list
		
		for(int i = 1;i<=m;i++)  // m is number of processors
		 {
			 Processor p = new Processor(i,false); // i gives the processor id value , false means processor is free
			 freeProcList.add(p);
			 no_of_proc.add(p);
		 }
	
	    	Partitioning partition  = new Partitioning();
	    	partition.alloc_Prioritywise_threshold(taskset, freeProcList, filenamea, 50);
	    //	partition.allocatio_WFD(taskset, freeProcList, filenamea);	
	 // 	partition.allocation_WFD_fixedThresh(taskset, freeProcList, filenamea, 60);
	    //	partition.allocation_M_WFD(taskset, freeProcList, filenamea);
	    	
/*////////////////RELIABILITY////////////
	Reliability reliab = new Reliability();
///////setRelib_Mogdhaddas(ArrayList<ITask> taskset, double fMin, double freq, int d)
	reliab.setRelib_original(taskset, 0.4, 1, 4);
	double PoF_1= 1- reliab.reliabilitySystem(taskset);
	while(f>=0.4)
	{
		System.out.println("f       "+ f);
		reliab.setRelib_Mogdhaddas(taskset, 0.4, f, 4);
	double PoF_f = 1- reliab.reliabilitySystem(taskset);
	System.out.println("PoF_1 "+String.format("%.16f", PoF_1)+" \n sqrt PoF_1 "+String.format("%.16f", Math.sqrt(PoF_1))
	+ " \n PoF_f  "+String.format("%.16f", PoF_f));
	
	if(PoF_f<=Math.sqrt(PoF_1))
		System.out.println("PoF_f<=Math.sqrt(PoF_1) "+ " f "+f );
	f=f-0.1;
	}*/
	}

}}
