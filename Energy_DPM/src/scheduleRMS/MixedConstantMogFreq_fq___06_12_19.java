package scheduleRMS;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import energy.ParameterSetting;
import energy.SysClockFreq;
import platform.Energy;
import platform.Fault;
import platform.Processor;
import platform.ProcessorState;
import platform.Reliability;
import queue.ISortedJobQueue;
import queue.ISortedQueue;
import queue.SortedJobQueuePeriod;
import queue.SortedQueuePeriod;
import taskGeneration.FileTaskReaderTxt;
import taskGeneration.ITask;
import taskGeneration.Job;
import taskGeneration.SystemMetric;

/**
 * @author KHUSHKIRAN PAL
 *  DYNAMIC 
 */
public class MixedConstantMogFreq_fq___06_12_19 {
	//GLOBAL PARAMETERS
	/*public static final  long hyperperiod_factor= 10;	//
			public static final   double  CRITICAL_TIME=  1.5*hyperperiod_factor;///1500;  //
			public static final   double  CRITICAL_freq= 0.50;   //0.50;//

			public static final int d = 0;  // FAULT TOLERANCE PARAMETER
			private double freq=1; // TEMP PARAMETER
	 */	
	/**
	 * @throws IOException
	 */
	/**
	 * @throws IOException
	 */
	/**
	 * @throws IOException
	 */
	public void schedule(String inputfilename,String outputFolder,String inputFolder,
			long hyperperiod_factor, int d,double CRITICAL_TIME,double CRITICAL_freq,double  min_freq,
			boolean faultFromFile,double bcetRatio, long hyper, int n_proc, String partitioning) throws IOException
	{
		int m =n_proc;			///////////////// no. of processors/////////////////
		System.out.println("STARTING MixedConstantBackFreq_fq___06_12_19  no. of processors "+m);
		//String inputfilename= "testhaque";
		FileTaskReaderTxt reader = new FileTaskReaderTxt(inputFolder+inputfilename); // read taskset from file
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
		Calendar cal = Calendar.getInstance();
		String date = dateFormat.format(cal.getTime());
		String faultFilename= inputFolder+"fault"+".txt";
		String filename= outputFolder+"allocationmogMULTI_6_12_18 "+"_"+inputfilename+"_"+date;
		String filename4= outputFolder+"taskProcmogMULTI_6_12_18 "+"_"+inputfilename+"_"+date+".txt"; // TEMP USE
		String filename1= outputFolder+"schedulemogMULTI_6_12_18 "+"_"+inputfilename+"_"+date+".txt";
		String filename2= outputFolder+"energymogMULTI_6_12_18 "+"_"+inputfilename+"_"+date+".txt";
		String filename3= outputFolder+"tasksmogMULTI_6_12_18 "+"_"+inputfilename+"_"+date+".txt";
		String filename5= outputFolder+"analysismogMULTI_6_12_18 "+"_"+inputfilename+"_"+date+".txt";
		String filename7= outputFolder+"faultmogMULTI_6_12_18 "+"_"+inputfilename+"_"+date+".txt";
		// //System.out.println(filename);
		//  Writer writer_allocation = new FileWriter(filename);
		//Writer writer_schedule = new FileWriter(filename1);
		Writer writer_energy = new FileWriter(filename2);
	//	Writer writer_tasks = new FileWriter(filename3);
	//	Writer writer_analysis = new FileWriter(filename5);
	//	Writer writer_fault = new FileWriter(filename7);
		//  Writer writer_taskProcWise = new FileWriter(filename4);
		DecimalFormat twoDecimals = new DecimalFormat("#.##");  // upto 1 decimal points
		DecimalFormat fourDecimals = new DecimalFormat("#.###");
		Energy energyConsumed = new Energy();
		SysClockFreq frequency = new SysClockFreq();
		Job[] current= new Job[2], spare_current = new Job[2];  // FOR SAVING THE NEWLY INTIAlIZED JOB  FROM JOBQUEUE SO THAT IT 
		// IS VISIBLE OUTSIDE THE BLOCK
		ITask task;
		ITask[] set = null;
		double U_SUM;
		//  int m =2;// no. of processors
		int total_no_tasksets=1;
		writer_energy.write(partitioning+"mog UTILIZATION TOTAL_E success fail pof_1 pof_MOG\n");
		//	+ "P_ACTIVE_TIME P_IDLE_TIME P_SLEEP S_ACTIVE_TIME S_IDLE_TIME S_SLEEP");
		/*writer_tasks.write("MixedfullBackupsExecuted partialBackupsExecuted fullBackupsCancelled"
				+ "	 cancelledPrimariesFull   cancelledPrimariesPartial  fullPrimariesExecuted totalPrimaries noOfFaults");
	*/	//  writer_taskProcWise.write("proc primary  backup  total"); //  TEMP USE
	//	writer_fault.write("\nMixedTASKSET TIME PRI_PROC QUE TASKID JOBID WCET PROM DEADLINE");

		while ((set = reader.nextTaskset()) != null) // SCHEDULING STARTS FOR ALL TASKSETS IN FILE
		{
				long fullBackupsExecuted=0;
			long partialBackupsExecuted=0;
			long fullBackupsCancelled=0;
			long cancelledPrimariesFull=0;
			long cancelledPrimariesPartial=0;
			long fullPrimariesExecuted=0;

			long totalPrimaries=0;

			long noOfFaults=0;
			double energyTotal=0;
			boolean deadlineMissed = false;
			Job lastExecutedJob= null, primaryJob, backupJob;
			ProcessorState proc_state = null;
			long time=0 ;
			long timeToNextPromotion=0, spareActiveTime = 0;
			long timeToNextArrival=0;
			long endTime = 0; // endtime of job
			long spareEndTime=0;
			long idle = 0;  // idle time counter for processor idle slots
			SchedulabilityCheck schedule = new SchedulabilityCheck();
			int response_zero=0;
			Processor primary = new Processor();
			Processor spare = new Processor();

			spare.setBusy(false);
			spare.setProc_state(proc_state.SLEEP);

			primary.setBusy(false);
			primary.setProc_state(proc_state.SLEEP);


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

			ISortedQueue queue = new SortedQueuePeriod ();
			queue.addTasks(set);
			ArrayList<ITask> taskset = new ArrayList<ITask>();
			ArrayList<Job> completedJobs = new ArrayList<Job>();
			taskset = queue.getSortedSet();
			U_SUM= (SystemMetric.utilisation(taskset));
			//	total_no_tasks=total_no_tasks+ tasks.size();
			prioritize(taskset);
			ArrayList<Integer> fault = new ArrayList<Integer>();
			Fault f = new Fault();
		
			/*for(ITask t : taskset)

			{

				System.out.println("task   "+t.getId()+"  u  "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
					+"   primary  "+t.isPrimary());

			}*/
			hyper =SystemMetric.hyperPeriod(taskset);  /////////////// HYPER PERIOD////////////
			
			System.out.println(" hyper  "+hyper);  

			for(ITask t : taskset)
			{
				t.setWcet(t.getWcet()*hyperperiod_factor);
				t.setWCET_orginal(t.getWCET_orginal()*hyperperiod_factor);
				t.setPeriod(t.getPeriod()*hyperperiod_factor);
				t.setDeadline(t.getDeadline()*hyperperiod_factor);
				t.setD(t.getD()*hyperperiod_factor);
				t.setT(t.getT()*hyperperiod_factor);
				t.setC(t.getC()*hyperperiod_factor);

			}

			ParameterSetting ps = new ParameterSetting();
			boolean unschedulable = false,schedulability= false;
			ps.setBCET(taskset, bcetRatio);
			ps.setACET(taskset);
			double fq = 1, minfq=1, maxfq=0.1,reliable_freq=0;
			
			////////////////RELIABILITY////////////5-12-18
        	Reliability reliab = new Reliability();
        ///////setRelib_Mogdhaddas(ArrayList<ITask> taskset, double fMin, double freq, int d)
        	reliab.setRelib_original(taskset, CRITICAL_freq, 1, d);
        	double pof_1 = reliab.POFSystem_ORIGINAL(taskset);
       // 	System.out.printf("  pof_1 %.25f\n ",pof_1);
        	double maxRelFreq=reliab.setRelib_Mogdhaddas_25_1_19(taskset, CRITICAL_freq, 
        			d,pof_1, hyper*hyperperiod_factor);
        	double pof_MOG = reliab.POFSystem_FREQ_MOG(taskset, Double.valueOf(twoDecimals.format(maxRelFreq) ), CRITICAL_freq, 
        			d, pof_1, hyper*hyperperiod_factor);
        //	System.out.printf("  pof_MOG %.25f\n ",pof_MOG);
     /*   	double pof_ki = reliab.POFSystem_FREQ_KIRAN(taskset,  CRITICAL_freq
        			, CRITICAL_freq, d, pof_1, hyper*hyperperiod_factor);
        	System.out.printf("  pof_ki %.25f\n ",pof_ki);*/
    		/////////////////////PARTIONING /////////////////////
			Partitioning partition  = new Partitioning();
		//	String partitioning= "M_WFD";
			double threshold  = Math.max((SystemMetric.utilisation(taskset)/(m)), 0);
		//	System.out.println("threshold  "+threshold );

			switch (partitioning) { 
	        case "WFD": 
	        	partition.allocatio_WFD(taskset, freeProcList, filename,0.7);
	            break; 
	        case "WFD_THRES": 
	        	partition.allocation_WFD_fixedThresh(taskset, freeProcList, filename,threshold );
	            break; 
	        case "WFD_THRES_UN": 
	        	partition.allocation_WFD_fixedThresh(taskset, freeProcList, filename,threshold );
	            break; 
	        case "M_WFD": 
	        	partition.allocation_M_WFD(taskset, freeProcList, filename);
	            break; 
	        case "PRIORITY": 
	        	partition.alloc_Prioritywise(taskset, freeProcList, filename);
	            break; 
	        case "PRIORITY_THRES": 
	        	partition.alloc_Prioritywise_threshold(taskset, freeProcList, filename,threshold); 
	            break; 
	        case "PRIORITY_THRES_UN": 
	        	partition.alloc_Prioritywise_threshold(taskset, freeProcList, filename,threshold); 
	            break; 
	            
	        
	        }

		//	System.out.println("freeProcList  "+freeProcList.size());
			
			// SORT TASKSET ON EACH PROCESSOR RATE MONOTONICALLY 4-12-18
			for(Processor p : freeProcList)
			{
				p.taskset.sort( new Comparator<ITask>() {
			          @Override
			          public int compare(ITask t1, ITask t2) {
			                         
			              if( t1.getPeriod()!= t2.getPeriod())
			                  return (int)( t1.getPeriod() - t2.getPeriod());
			              
			              return (int) (t1.getId() - t2.getId());
			          }
			      });
				
			}
			// REMOVE PROCESSORS WITH ZERO LOAD 4-12-18
			Iterator<Processor> itrProc = freeProcList.iterator();
			while (itrProc.hasNext())
			{
				Processor p = itrProc.next();
				
				if(p.taskset.size()==0)
					itrProc.remove();
			}
			
		/*	for(Processor p : freeProcList)
			{
				System.out.println("after taskset  "+p.taskset.size());
				
			}
*/
			
				
	     	
			/////////////////FREQUENCY ALLOCATION//////////////////

			
				
	     	for(Processor p : freeProcList)
			{
	     		/*double maxRelFreq=0;
	     		for(ITask t : p.taskset)
	     		{
	     			if(t.getReliableFreq()>maxRelFreq)
	     				maxRelFreq= t.getReliableFreq();
	     		}
	     		*/
				double load = (SystemMetric.utilisation(p.taskset));
			
				double LLB_N = p.taskset.size()*(Math.pow(2, ((double)1/(double)(p.taskset.size())))-1);
			//	System.out.printf("   load %.2f  " ,(load));
				double slack = Math.max(0, (LLB_N- load));
			//	System.out.println("   maxRelFreq   " +maxRelFreq);
			
				
				// MAIN SETTING OF FREQUENCY FOR ALL TASKS TO  RELIABLE FREQ
				fq = Math.max(CRITICAL_freq, Math.max((load/LLB_N),
						Double.valueOf(twoDecimals.format(maxRelFreq) )));
				fq= discreteFreq(fq);
				
				
		//	System.out.println("  fq   "+fq);

				/*do
				{*/
			//		System.out.println("   frequency   "+fq+" P "+p.getId());
				
								  	
			//	  	System.out.println("   frequency   "+fq+" P "+p.getId());		
				
				
				// MAIN SETTING OF FREQUENCY FOR ALL TASKS (P+B ) TO ....RELIABLE FREQ
				//fq = Math.max(CRITICAL_freq, Math.max((load/LLB_N),maxRelFreq) );
					ps.set_freq(p.taskset,Double.valueOf(twoDecimals.format(fq)));   // set frequency
					ps.setResponseTimeMixed(p.taskset);
					ps.setPromotionTime(p.taskset);
					schedulability = schedule.worstCaseResp_TDA_RMS_multi(p.taskset);
					System.out.println("schedulability  "+schedulability);
					p.setFrequency(fq); 
					System.out.println(" p "+p.getId()+"  f "+p.getFrequency() );
/*
					schedulability = schedule.worstCaseResp_TDA_RMS_multi(p.taskset);//, fq);
					
					//   	//System.out.println("   schedulability   "+schedulability);
					if (schedulability)
					{
						//		//System.out.println("setting response");
						ps.setResponseTimeForMWFD(p.taskset);
						ps.setPromotionTime(p.taskset);
						//	//System.out.println("processor   "+p.getId()+"   size  "+p.taskset.size()+
						//			"  w  "+p.getWorkload());
							
					}
					else
					{
						fq= fq+0.05;
						System.out.println("unschedulable "+p.getId()+"  fq  "+fq);
						//	//System.exit(0);
					}
					if (fq>1)
					{
						writer_analysis.write("fq  "+fq);
						fq=1;
						ps.set_freq_MixedAlloc(p.taskset,Double.valueOf(twoDecimals.format(fq)));
						schedulability = schedule.worstCaseResp_TDA_RMS_multi(p.taskset);
						ps.setResponseTimeForMWFD(p.taskset);
						ps.setPromotionTime(p.taskset);
					//	writer_analysis.write("fq  "+fq+" schedulability "+schedulability+"\n");
						//break;
					}*/

				/*}while(!schedulability );*/
			//	p.setFrequency(fq); 
				
				
			}

			/*if (!schedulability || fq>1)
			{
				System.out.println(total_no_tasksets +" UNSCHEDULABLE\n"+" fq "+fq);
				writer_energy.write("\n"+total_no_tasksets++ +"UNSCHEDULABLE"+ fq +" \n");
		//		writer_tasks.write("\n"+total_no_tasksets++ +"UNSCHEDULABLE "+ fq +"\n");
				continue;


			}
			*/
			/*
			for(Processor p : freeProcList)
			{
					for(ITask tp : p.taskset)
				{
						System.out.println("p "+ p.getId()+" t "+tp.getId()+ "  freq "+tp.getFrequency());
				}
					
			}*/
			
	     	
			
			//////////////FAULT///////////////////////////
			//	fault = f.lamda_F(hyper, CRITICAL_freq, minfq, d);        //////////////FAULT////////////
			if(! faultFromFile)
				fault = f.lamda_F(hyper, CRITICAL_freq, CRITICAL_freq, d);        //////////////FAULT////////////
			else
				fault=	f.readFromFile(faultFilename);
	     /*	 int procount=1;
			fault.add(5);
			fault.add(10);
			fault.add(15);
			fault.add(20);*/
			 	//TEMP FAULT INDUCTION
 /*    	ArrayList<Long> tempFault = new ArrayList<Long>();
    	ArrayList<Long> tempProcCheck = new ArrayList<Long>();
    	tempFault.add((long)12); 	tempFault.add((long)495);   
    	tempFault.add((long)330);     	tempFault.add((long)363); 
		tempFault.add((long)613);	        	tempFault.add((long)627); 
    		tempFault.add((long)67686);     	tempFault.add((long)146601); 
    		tempFault.add((long)362765);	        	tempFault.add((long)82600);  
    		tempFault.add((long)101549);        	tempFault.add((long)128175);
    		tempFault.add((long)129303);        	tempFault.add((long)141364);        
    		tempFault.add((long)169909);        	tempFault.add((long)203775);        
    	tempFault.add((long)203799);        	tempFault.add((long)211584);  
    	tempFault.add((long)222475);        	tempFault.add((long)224804);  
    	tempFault.add((long)268401);        
  //  	tempFault.add((long)302960);    
   	tempFault.add((long)6165);
		tempProcCheck.add((long)4);  tempProcCheck.add((long)4);  
		
tempProcCheck.add((long)6);   	tempProcCheck.add((long)8);
tempProcCheck.add((long)4);tempProcCheck.add((long)4);   
    	tempProcCheck.add((long)2);   	tempProcCheck.add((long)2);
    	tempProcCheck.add((long)1);tempProcCheck.add((long)2);     
    		tempProcCheck.add((long)1);tempProcCheck.add((long)2);
    	tempProcCheck.add((long)1);tempProcCheck.add((long)2);
    	tempProcCheck.add((long)2);tempProcCheck.add((long)1);
    	tempProcCheck.add((long)2);tempProcCheck.add((long)2);  
    	tempProcCheck.add((long)2);tempProcCheck.add((long)2);
    	tempProcCheck.add((long)1);    
    	tempProcCheck.add((long)1);    	tempProcCheck.add((long)3);
    	tempProcCheck.add((long)3);
    	tempProcCheck.add((long)1);        	

		Iterator<Long> tempProcItr = tempProcCheck.iterator(); 

		while (tempProcItr.hasNext())
		{
			System.out.println(" proc "+tempProcItr.next());
		}
		Iterator<Long> tempFaultItr = tempFault.iterator(); 

		while (tempFaultItr.hasNext())
		{
			System.out.println(" tempFault "+tempFaultItr.next()+"  size  "+tempFault.size());
		}
*/
			 


		//	writer_analysis.write("Proc T U_O U_F WCET_O WCET Period F RESPONSE PROM ISPRIMARY BACK_P priority\n");

			for(Processor pMin : freeProcList)
			{
		//		writer_analysis.write("\n\nprocessor   "+pMin.getId()+"\t frequency   "+pMin.getFrequency()+"\n");

				pMin.taskset.sort(new Comparator<ITask>() {
					public int compare(ITask p1, ITask p2) {
						int cmp;
						cmp= (int)(p1.getPriority()-p2.getPriority());
						return cmp;
					}
				});	

			/*	for(ITask t : pMin.taskset)

				{

					writer_analysis.write(pMin.getId()+" "+t.getId()+" "+ Double.valueOf(twoDecimals.format(((double)t.getWCET_orginal()/(double)t.getDeadline())))
					+" "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
					+" "+t.getWCET_orginal()+" "+t.getWcet()+" "+t.getPeriod()+" "+t.getFrequency()+" "+
					" "+t.getResponseTime()+" "+t.getSlack()+
					" "+t.isPrimary()+	" "+t.getBackupProcessor().getId()+" "+t.getPriority()+"\n");

					//System.out.println("task   "+t.getId()+"  u  "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
					//	+"   primary  "+t.isPrimary()+"  Proc   "+t.getP().getId()+	"   backup p  "+t.getBackupProcessor().getId()+
					//		"   primary  "+t.getPrimaryProcessor().getId());

				}*/
			//	writer_analysis.write("\nProc task prom");
			/*	for(ITask t : pMin.taskset)

				{
					if(!t.isPrimary())
						writer_analysis.write("\n"+pMin.getId()+" "+t.getId()+" "+t.getSlack());
				}*/

			}
			/*	writer_allocation.write("\n"+"FAULT  \t\t");
		for(int fa: pMin.getFault())
		{
			writer_allocation.write(fa+"\t\t");
		}
		writer_allocation.write("\nU "+Double.valueOf(twoDecimals.format((//SystemMetric.utilisation(pMin.taskset)))));
	}*/





			long temp=0;
			ISortedJobQueue activeJobQ = new SortedJobQueuePeriod(); // dynamic jobqueue 
			TreeSet<Job> backupQueue = new TreeSet<Job>(new Comparator<Job>() {
				@Override
				public int compare(Job t1, Job t2) {

					if( t1.getPromotionTime()!= t2.getPromotionTime())
						return (int)( t1.getPromotionTime()- t2.getPromotionTime());

					return (int) (t1.getPeriod() - t2.getPeriod());
				}
			}); 



			Job j;//,  backupJob = null; //job
			TreeSet<Long> activationTimes = new TreeSet<Long>();
			//	TreeSet<Long> promotionTimes = new TreeSet<Long>();
			ArrayList <Long> promotionTimes = new ArrayList<Long>();

			long nextActivationTime=0;

			long executedTime=0, remain_time=0;
			double energy_consumed=0;
			
			taskset = queue.getSortedSet();

			// ACTIVATE ALL TASKS AT TIME 0 INITIALLY IN QUEUE  

			for(ITask t : taskset)  // activate all tasks at time 0
			{
				temp=0;
				j =  t.activate_MWFD_RMS_EEPS(time);  
				totalPrimaries++;
				////System.out.println("t "+t.getId()+"   j  "+j.getJobId());
				j.setPriority(t.getPriority());
				j.setCompletionSuccess(false);
				j.setFrequency(t.getFrequency());
				j.setExec_frequency(j.getFrequency());
				Processor p;
				
				p= j.getProc();  // get the processor on which task has been allocated
			
				p.primaryJobQueue.addJob(j);
				//			//System.out.println("task  "+t.getId()+"  job  "+j.getJobId()+"  p  "+p.getId()+"  queue size  "+p.primaryJobQueue.size());
				//backup addition
				backupJob = j.cloneJob_MWFD_RMS_EEPS();
				backupJob.setPrimary(false);
				backupJob.setCompletionSuccess(false);
				backupJob.setFrequency(t.getFrequency());
				backupJob.setExec_frequency(t.getFrequency());
				p=j.getBackupProcessor();

				Iterator<ITask> itr1 = p.taskset.iterator();
				while (itr1.hasNext())
				{
					ITask t1 = itr1.next();
					if(backupJob.getTaskId()==t1.getId())
					{
						backupJob.setPromotionTime((long) t1.getSlack());
						//backupJob.setFrequency(t1.getFrequency());
						//				//System.out.println("  task    "+backupJob.getTaskId()+ "  p time  "+backupJob.getPromotionTime());
					}}

				p.backupJobQueue.addJob(backupJob);
				/*		//System.out.println("task  "+t.getId()+"  backup job  "+backupJob.getJobId()+" primary  "+backupJob.isPrimary()+
							"  p  "+p.getId()+"  queue size  "+p.backupJobQueue.size()+" P TIME  "+backupJob.getPromotionTime());
				 */		

				activeJobQ.addJob(j);  //////ADD TO PRIMARY QUEUE
				backupQueue.add(backupJob);   /////ADD TO SPARE  QUEUE
				while (temp<=hyper*hyperperiod_factor)
				{


					temp+=t.getPeriod();
					activationTimes.add(temp);
					promotionTimes.add((long) (t.getSlack()));
					promotionTimes.add((long) (t.getSlack()+temp));
				}

			}

			//		//System.out.println("activationTimes  "+activationTimes.size()+"  promotionTimes  "+promotionTimes.size());
			promotionTimes.sort(new Comparator <Long>() {
				@Override
				public int compare(Long t1, Long t2) {
					if(t1!=t2)
						return (int) (t1-t2);
					else 
						return 0;

				}
			});
			/*Iterator itr = promotionTimes.iterator();
		while(itr.hasNext())
			//System.out.println("promotionTimes   "+itr.next());
			 */
		/*	writer_schedule.write("\nmogMULTI_6_12_18P_ID TASKID FREQ WCET wcet_o ACET BCET DEADLINE P/B RESP promo\n");

			for(Processor p : freeProcList)
			{
			//	System.out.println(" p "+p.getId()+"  f "+p.getFrequency() );
				for(ITask t :p.taskset)
				{
					if(t.getResponseTime()==0)
						response_zero++;
					writer_schedule.write("\n"+p.getId()+" "+t.getId()+" "+t.getFrequency()+" "+t.getWcet()+ " "+t.getWCET_orginal()+" "+t.getACET() 
					+" "+t.getBCET()+" "+t.getDeadline()+" "+t.isPrimary()+" "+t.getResponseTime()+" "+t.getSlack());
				}
			}
			if(response_zero>0)
				writer_schedule.write(" \nResponse time zero "+response_zero);

			writer_schedule.write("\ns/e P_ID TASKID  JOBID PR/BK FREQ EX_FRQ WCET wcet_o DEADLINE  isPreempted STARTTIME ENDTIME "
					+ "executed remain energy "
					+ "FAULTY fullBackupsExecuted partialBackupsExecuted fullBackupsCancelled"
					+ "	 cancelledPrimariesFull   cancelledPrimariesPartial  fullPrimariesExecuted noOfFaults \n");
*/
			//	writer_analysis.write("P_ID TASKID JOBID PR/BK TIME");
			nextActivationTime=  activationTimes.pollFirst();
			// //System.out.println("nextActivationTime  "+nextActivationTime);
			timeToNextPromotion = promotionTimes.get(0);
			

			// ADD JOB TO READY QUEUE
			for (Processor proc : freeProcList)
			{
				double sys_temp_freq_ini=0;
				// 		//System.out.println(" time  "+time+"   primary size  "+proc.primaryJobQueue.size());
				if (!proc.primaryJobQueue.isEmpty())
				{
					//			//System.out.println(" time  "+time);
					while (!proc.primaryJobQueue.isEmpty() && time == proc.primaryJobQueue.first().getActivationDate())
					{
						//			//System.out.println(" time adding primary  to ready  "+time);
						proc.readyQueue.addJob(proc.primaryJobQueue.pollFirst());

						//   	 totalPrimaries++;
					}

				}
				if (!proc.backupJobQueue.isEmpty())
				{
					//	//System.out.println(" time  "+time+"  proc.backupJobQueue.first().getPromotionTime()   "+proc.backupJobQueue.first().getPromotionTime());
					while (!proc.backupJobQueue.isEmpty() && 
							time== proc.backupJobQueue.first().getPromotionTime())
					{
						//		//System.out.println(" time  "+time);
						Job newBJob = proc.backupJobQueue.pollFirst();
						//		totalBackups++;

						if (!newBJob.isCompletionSuccess() || newBJob.isFaulty())
							proc.readyQueue.addJob(newBJob);
						//		//System.out.println("time  "+time+"   backup job in ready queue  "+newBJob.getTaskId()+"  job  "+newBJob.getJobId());
					}

				}
				
			
			}
			
			
			
			//START SCHEDULING///////////////////////////////START SCHEDULING///////////////////

			while(time<hyper*hyperperiod_factor+2)//5000)//
			{

				//new activation
				if( (long)time== (long)nextActivationTime) // AFTER 0 TIME JOB ACTIVAIONS
				{

					if (!activationTimes.isEmpty())
						nextActivationTime=  activationTimes.pollFirst();

					// //System.out.println("//new activation  nextActivationTime  "+nextActivationTime+" size  "+activationTimes.size());

					for (ITask t : taskset) 
					{

						Job n = null;
						long activationTime;
						activationTime = t.getNextActivation(time-1);  //GET ACTIVATION TIME

						//	//System.out.print("  activationTime  "+activationTime);
						long temp1= (long) activationTime, temp2 =(long) time;
						if (temp1==temp2)
							n= t.activate_MWFD_RMS_EEPS(time); ///	remainingTime =  (long)ACET;  ////////////////

						if (n!=null)
						{
							totalPrimaries++;
							n.setPriority(t.getPriority());
							n.setCompletionSuccess(false);
							n.setFrequency(t.getFrequency());
							n.setExec_frequency(n.getFrequency());
							Processor p;
							p= n.getProc();  // get the processor on which task has been allocated
							p.primaryJobQueue.addJob(n);
							/*		 //System.out.println("//new activation activated   task  "+t.getId()+"   time   "
						+time+"  job  "+n.getJobId()+"  p  "+p.getId()+"  primaryJobQueue queue size  "+p.primaryJobQueue.size());
							 */			//backup addition
							backupJob = n.cloneJob_MWFD_RMS_EEPS();
							backupJob.setPrimary(false);
							backupJob.setCompletionSuccess(false);
							backupJob.setFrequency(t.getFrequency());
							backupJob.setExec_frequency(t.getFrequency());

							p=n.getBackupProcessor();



							Iterator<ITask> itr1 = p.taskset.iterator();
							while (itr1.hasNext())
							{
								ITask t1 = itr1.next();
								if(backupJob.getTaskId()==t1.getId())
								{
									long temp11 = (backupJob.getJobId()-1)*backupJob.getPeriod();// no. of jobs run till this time
									backupJob.setPromotionTime((long) t1.getSlack()+temp11);
								//	backupJob.setFrequency(t1.getFrequency());
									//	//System.out.println("  task    "+backupJob.getTaskId()+ "  p time  "+backupJob.getPromotionTime());
								}}

							p.backupJobQueue.addJob(backupJob);
							/*	 //System.out.println("//new activation  task  "+t.getId()+"  backup job  "+backupJob.getJobId()+" primary  "+backupJob.isPrimary()+
								"  p  "+p.getId()+"  backupJobQueue queue size  "+p.backupJobQueue.size());
							 */	

							activeJobQ.addJob(n);  //////ADD TO PRIMARY QUEUE
							backupQueue.add(backupJob);   /////ADD TO SPARE  QUEUE

						}
					}

				} 
				
				
				// ADD JOB TO READY QUEUE
				for (Processor proc : freeProcList)
				{
					boolean added_task=false;



					// 		//System.out.println(" time  "+time+"   primary size  "+proc.primaryJobQueue.size());
					if (!proc.primaryJobQueue.isEmpty())
					{
						//			//System.out.println(" time  "+time);
						while (!proc.primaryJobQueue.isEmpty() && time == proc.primaryJobQueue.first().getActivationDate())
						{
							//	System.out.println(" time adding primary  to ready  "+time);
							if(proc.primaryJobQueue.size()>0)
								added_task=true;

							proc.readyQueue.addJob(proc.primaryJobQueue.pollFirst());

							//   	 totalPrimaries++;
						}

					}
					if (!proc.backupJobQueue.isEmpty())
					{
						//System.out.println(" time  "+time+"  proc.backupJobQueue.first().getPromotionTime()   "+proc.backupJobQueue.first().getPromotionTime());
						while (!proc.backupJobQueue.isEmpty() && 
								time== proc.backupJobQueue.first().getPromotionTime())
						{
						//	System.out.println(" time  "+time);
							Job newBJob = proc.backupJobQueue.pollFirst();
							//		totalBackups++;

							if (!newBJob.isCompletionSuccess() || newBJob.isFaulty())
							{
								proc.readyQueue.addJob(newBJob);
								///////////////////	newBJob.setFrequency(1);
								//newBJob.setExec_frequency(1);
								added_task=true;
							//	System.out.println("time  "+time+" p "+proc.getId()+"   backup job in ready queue  "+newBJob.getTaskId()+"  job  "+newBJob.getJobId());
							}

						}
					}
				}


				//////////////////PREEMPTION////////////////////////
				for (Processor proc : freeProcList)
				{
					//System.out.println(" preempt p"+proc.getId()+"  size "+proc.readyQueue.size());
					long remain_time_p;
					//preempt primary by primary
					if(time>0 && proc.getCurrentJob()!=null
							&& !proc.readyQueue.isEmpty() 
							&&	!proc.getCurrentJob().isCompletionSuccess()	&&  
							proc.getCurrentJob().getPeriod()>proc.readyQueue.first().getPeriod()
							&& proc.isBusy())
							
					{
					//	System.out.println("  //PREEMPTION/ time "+time);	
						Job lowP = proc.getCurrentJob() , highP = proc.readyQueue.first();
						
						
					/*	if(lowP.isPrimary())
						{*/
							 executedTime = time-lowP.getStartTime();
							 // mog 30/1/19
							 remain_time_p = (long) ((lowP.getRemainingTime()-executedTime)*proc.getFrequency());
								
							// remain_time_p = (long) ((lowP.getRemainingTime()-executedTime)*lowP.getExec_frequency());
						//	System.out.println("p remain_time_p "+remain_time_p);
							 if(remain_time_p>0)
								{
							energy_consumed= energyConsumed.energyActive((executedTime), proc.getFrequency());
							lowP.setRemainingTime(lowP.getRemainingTime()- (executedTime));
							lowP.setRomainingTimeCost(remain_time_p);
							proc.setActiveEnergy(energy_consumed);
								}
								else
								{
									lowP.setEndTime(time);
									proc.setEndTimeCurrentJob(time);
								}
						//	System.out.println("in preemption TIME  "+time+"  p  "+proc.getId()+" active  "+ proc.activeTime+"  energy  "+proc.getActiveEnergy());
							 /*		}
						else
						{
							 executedTime = time-lowP.getStartTime();
							 remain_time_p = (long) ((lowP.getRomainingTimeCost()-executedTime)*lowP.getExec_frequency());
					//			System.out.println("b remain_time_p "+remain_time_p);
								
							 if(remain_time_p>0)
								{
							 energy_consumed= energyConsumed.energyActive((time-lowP.getStartTime()), lowP.getExec_frequency());
				//			System.out.println("time "+time+"  proc "+ proc.getId()+" t "+lowP.getTaskId()+" p "+lowP.isPrimary()+" frequency()  "+lowP.getExec_frequency());
							lowP.setRomainingTimeCost(lowP.getRomainingTimeCost()- (time-lowP.getStartTime()));
							proc.setActiveEnergy(energy_consumed);
								}
							 else
								{
									lowP.setEndTime(time);
									proc.setEndTimeCurrentJob(time);
								}
							
        		    	if(time>3627100 && proc.getId()==4)
        		    	System.out.println("in preemption TIME  "+time+"  p  "+proc.getId()+" t  "+ lowP.getTaskId()+
        		    			"  rem time  "+lowP.getRomainingTimeCost());
							   
						}*/

			//			System.out.println("o remain_time_p "+remain_time_p);
						
						if (remain_time_p>0) {
							lowP.isPreempted=true;
							proc.setBusy(false);
							proc.readyQueue.addJob(lowP);
					//		if (lowP.isPrimary())
	/*							writer_schedule.write("\np " + proc.getId() + " " + proc.getCurrentJob().getTaskId()
										+ " " + proc.getCurrentJob().getJobId() + " " + proc.getCurrentJob().isPrimary()
										+ " " + Double.valueOf(twoDecimals.format(proc.getCurrentJob().getFrequency()))
										+ " " + Double.valueOf(twoDecimals.format(proc.getCurrentJob().getExec_frequency())) + " "
										+ proc.getCurrentJob().getRemainingTime() + " "
										+ proc.getCurrentJob().getRomainingTimeCost() + " "
										+ proc.getCurrentJob().getDeadline() + " " + proc.getCurrentJob().isPreempted
										+ " " + proc.getCurrentJob().getStartTime());
	
			writer_schedule.write("\t " + time + " " + executedTime + " " + remain_time_p + " "
									+ energy_consumed + "  preempted ");
					
		*/			/*	//	else mog 
							//	writer_schedule.write("\np " + proc.getId() + " " + proc.getCurrentJob().getTaskId()
										+ " " + proc.getCurrentJob().getJobId() + " " + proc.getCurrentJob().isPrimary()
										+ " " + Double.valueOf(twoDecimals.format(proc.getCurrentJob().getFrequency()))
										+ " " +Double.valueOf(twoDecimals.format( proc.getCurrentJob().getExec_frequency() ))+ " "
										+ proc.getCurrentJob().getRomainingTimeCost() + " "
										+ proc.getCurrentJob().getRomainingTimeCost() + " "
										+ proc.getCurrentJob().getDeadline() + " " + proc.getCurrentJob().isPreempted
										+ " " + proc.getCurrentJob().getStartTime());*/
							}
						
						

					}

				}

				// PRIMARY JOB CHECKING AND EXECUTIOM
				for (Processor proc : freeProcList)
				{
				//	System.out.println(" p "+proc.getId()+"  f "+proc.getFrequency() );
					if(!proc.readyQueue.isEmpty() && proc.isBusy()==false )
					{

						proc.setCurrentJob( proc.readyQueue.pollFirst());


						if (proc.getCurrentJob()!=null && 
								proc.getCurrentJob().isCompletionSuccess()==false)      // if job in queue is not null 
						{
					/*		System.out.println("time   "+time+"   p  "+proc.getId()+"  task  "+proc.getCurrentJob().getTaskId()+
									"  job   "+proc.getCurrentJob().getJobId()+" getRomainingTimeCost "+proc.getCurrentJob().getRomainingTimeCost()
									+" getRemainingTime  "+proc.getCurrentJob().getRemainingTime());
						*/		/*			 writer_analysis.write("\n"+proc.getId()+" "+proc.getCurrentJob().getTaskId()+" "
        						 +proc.getCurrentJob().getJobId()+" "+proc.getCurrentJob().isPrimary()
        						 +" "+time);	
							*/ 		proc.setIdleEndTime(time); // IF PROCESSOR WAS FREE , END IDLE SLOT

							 // RECORD THE SLOT LENGTH
							 if (proc.getIdleSlotLength()>0)
							 {
								 //	writer.write("\n\t\t\t\t\t\t\t"+processor.getId()+"\t\t\t\t\t"+processor.getIdleStartTime()+"\t"+time+" \t"+processor.getIdleSlotLength());
								if(proc.getIdleSlotLength()>CRITICAL_TIME)
								{
									
									proc.setSleepEnergy(energyConsumed.energySLEEP(proc.getIdleSlotLength()));
								}
								else
								{
									proc.setIdleEnergy(energyConsumed.energy_IDLE(proc.getIdleSlotLength()));
								}
								 
								 
			/*					 writer_schedule.write("\n"+proc.getId()+" "+proc.getIdleStartTime()+" "+time+
										 " "+proc.getIdleSlotLength()+" idleend");
			*/					 proc.setIdleSlotLength(0); // REINITIALIZE THE IDLE LENGTH
							
							 }

							 if(!proc.getCurrentJob().isPreempted && proc.getCurrentJob().isPrimary())
								 proc.setNoOfPriJobs(proc.getNoOfPriJobs()+1);
							 else if (!proc.getCurrentJob().isPreempted && !proc.getCurrentJob().isPrimary())
								 proc.setNoOfBackJobs(proc.getNoOfBackJobs()+1);

							 proc.setProc_state(proc_state.ACTIVE);
							 proc.getCurrentJob().setStartTime(time);
							 //set end time
							 //26/1/19  due to mogdas backup= primary
						/*	 if(proc.getCurrentJob().getTaskId()==230)
							 System.out.println("getRomainingTimeCost  "+proc.getCurrentJob().getRomainingTimeCost()
									+ "  getExec_frequency  "+proc.getCurrentJob().getExec_frequency()
									+" proc f "+proc.getFrequency());
					*/			 proc.getCurrentJob().setRemainingTime( (long)(proc.getCurrentJob().getRomainingTimeCost()/ proc.getFrequency()));
							 proc.getCurrentJob().setEndTime(time+proc.getCurrentJob().getRemainingTime());
				
							/* if(proc.getCurrentJob().isPrimary())
							 {
								 proc.getCurrentJob().setRemainingTime( (long)(proc.getCurrentJob().getRomainingTimeCost()/ proc.getCurrentJob().getExec_frequency()));
								 proc.getCurrentJob().setEndTime(time+proc.getCurrentJob().getRemainingTime());
						//		 System.out.println("F "+proc.getCurrentJob().getExec_frequency()+"  .getRomainingTimeCost()"+proc.getCurrentJob().getRomainingTimeCost()+" end time "+proc.getCurrentJob().getEndTime());
							 }
							 else
							 {
								 proc.getCurrentJob().setEndTime(time+proc.getCurrentJob().getRomainingTimeCost());
							 }*/
							 proc.setEndTimeCurrentJob(proc.getCurrentJob().getEndTime()-1);
							 proc.setBusy(true);


					/*		 	 if(proc.getCurrentJob().getTaskId()==230)
     	        			System.out.println(time+ " start p  "+proc.getId()+" current task "+proc.getCurrentJob().getTaskId() +
     	        					"   job  "+proc.getCurrentJob().getJobId()+ " isCompletionSuccess() "+proc.getCurrentJob().isCompletionSuccess()+
     	        					 "  p/b "+proc.getCurrentJob().isPrimary()+" end  "+ proc.getCurrentJob().getEndTime()
     	        					 +" prom time "+ proc.getCurrentJob().getPromotionTime());
					*/		  

						//	 if(proc.getCurrentJob().isPrimary())
			/*					 writer_schedule.write("\ns "+proc.getId()+" "+proc.getCurrentJob().getTaskId()+" "+proc.getCurrentJob().getJobId()+" "+
										 proc.getCurrentJob().isPrimary()+" "+Double.valueOf(twoDecimals.format(	proc.getCurrentJob().getFrequency()))
										 +" "+Double.valueOf(twoDecimals.format(	proc.getCurrentJob().getExec_frequency()))+" "+proc.getCurrentJob().getRemainingTime()+" "+
										 proc.getCurrentJob().getRomainingTimeCost()+" "+ proc.getCurrentJob().getDeadline()
										 +" "+	proc.getCurrentJob().isPreempted+" "+time+" ");
			*/				/* else // mog
								 writer_schedule.write("\ns "+proc.getId()+" "+proc.getCurrentJob().getTaskId()+" "+proc.getCurrentJob().getJobId()+" "+
										 proc.getCurrentJob().isPrimary()+" "+Double.valueOf(twoDecimals.format(	proc.getCurrentJob().getFrequency()))
										 +" "+Double.valueOf(twoDecimals.format(proc.getCurrentJob().getExec_frequency()))+" "+	proc.getCurrentJob().getRomainingTimeCost()+" "+proc.getCurrentJob().getRomainingTimeCost()+" "+
										 proc.getCurrentJob().getDeadline()
										 +" "+	proc.getCurrentJob().isPreempted+" "+time+" ");*/


						}
					}
					else if (proc.readyQueue.isEmpty() && proc.isBusy()==false )
					{//---------------


						// //System.out.println("  p  " +proc.getId()+"  proc.getIdleSlotLength()  "+proc.getIdleSlotLength());
						if (proc.getIdleSlotLength()==0)
						{
				/*		System.out.println("idle slot started");
						System.out.println(" current  "+proc.getCurrentJob().getTaskId());
					*/	proc.setCurrentJob(null);
					//	System.out.println(" current  "+proc.getCurrentJob());
						
				//			writer_schedule.write("\n"+proc.getId()+ " "+time+" idlestart");
							proc.setIdleSlotLength(proc.getIdleSlotLength()+1);// INCREMENT THE  LENGTH OF IDLE SLOT FROM 0 TO 1
							proc.setIdleStartTime(time);
							proc.freq_set_tasks.clear();
						}
						else
							proc.setIdleSlotLength(proc.getIdleSlotLength()+1); // INCREMENT THE  LENGTH OF IDLE SLOT 


						////////////	writer1.write("\n setIdleStartTime "+time);
						//		proc.setIdleStartTime(time);
						if (proc.getTimeToNextArrival()>CRITICAL_TIME)
						{
							proc.setProc_state(proc_state.SLEEP);
							//proc.setFrequency(0);
							
						}
						else
						{
							proc.setProc_state(proc_state.IDLE);
						//	proc.setFrequency(min_freq);
						}// //System.out.println("// PRIMARY JOB CHECKING AND EXECUTIOM ELSE PART   p  "+proc.getId()+"   timeToNextArrival   "+proc.getTimeToNextArrival()+"   Proc_state  "+proc.getProc_state());
					}
				}


				// count busy time
				for (Processor proc : freeProcList)
				{
					if (proc.getProc_state()==proc_state.ACTIVE)
					{
						proc.activeTime++;
						//	 //System.out.println("TIME  "+time+"  p  "+proc.getId()+" active  "+ proc.activeTime);

					}
					if (proc.getProc_state()==proc_state.IDLE)
					{
						proc.idleTime++;
						//   	System.out.println("TIME  "+time+"  p  "+proc.getId()+"  idle "+ proc.idleTime);;

					}
					if (proc.getProc_state()==proc_state.SLEEP)
					{	
						proc.sleepTime++;
						//	 //System.out.println("TIME  "+time+"  p  "+proc.getId()+" sleep  "+proc.sleepTime );;

					}
				}

				/////////////////////////////FAULT INDUCTION///////////////////////
				//			if(time == 			11000)
				//{
				Random rand = new Random();
				
			///	System.out.println(" fault   "+fault.size());
				
				if ( fault.size()>0 )
				{
					//	System.out.println("out fault time  "+time);//+"  task  "+lastExecutedJob.getTaskId()+" job  "+lastExecutedJob.getJobId());

					if(time==fault.get(0)*hyperperiod_factor)

					{
						int tempPr= 1+rand.nextInt(m), count = m;
						for ( Processor p : freeProcList)
						{
							boolean found = false;
							if (p.getId()==tempPr )
							{
								count--;
								if (p.getProc_state()==ProcessorState.ACTIVE && p.getCurrentJob().isPrimary() && !p.getCurrentJob().isFaulty())
								{	
									System.out.println("                    fault time  "+time+"  proc  "+p.getId()+"            task  "+
											p.getCurrentJob().getTaskId()+" job  "+p.getCurrentJob().getJobId() + "  prom time  "+p.getCurrentJob().getPromotionTime());

									p.getCurrentJob().setCompletionSuccess(false);
									p.getCurrentJob().setFaulty(true);
									//	noOfFaults++;
									Iterator<Job> spareItr = p.getCurrentJob().getBackupProcessor().backupJobQueue.iterator();
									while(spareItr.hasNext())
									{
										Job temp1;
										temp1  = spareItr.next();
										// System.out.println("primaary pending  task  "+temp1.getTaskId());

										if(temp1.getTaskId()== p.getCurrentJob().getTaskId() && temp1.getJobId()== p.getCurrentJob().getJobId())
										{
											temp1.setFaulty(true);
											noOfFaults++;
//											writer_fault.write("\n"+total_no_tasksets+" "+time+" "+p.getId()+" "+ " B "+" "+temp1.getTaskId()+" "+temp1.getJobId()+" "
//													+temp1.getRemainingTime()+" "+temp1.getPromotionTime()+" "+temp1.getDeadline());
										found=true;
											//			 System.out.println("time    "+time+"backupJobQueue task  "+temp1.getTaskId()+"  job   "+temp1.getJobId() );
											//			System.out.println("noOfFaults "+noOfFaults+" is primary "+temp1.isPrimary());
											break;
										}
									}


									// faulty job's backup copy may be in ready queue
									Iterator<Job> spareItready = p.getCurrentJob().getBackupProcessor().readyQueue.iterator();
									while(spareItready.hasNext())
									{
										Job temp1;
										temp1  = spareItready.next();
										// System.out.println("primaary pending  task  "+temp1.getTaskId());

										if(temp1.getTaskId()== p.getCurrentJob().getTaskId() && temp1.getJobId()== p.getCurrentJob().getJobId())
										{
											temp1.setFaulty(true);
											noOfFaults++;
//											writer_fault.write("\n"+total_no_tasksets+" "+time+" "+p.getId()+" "+ " R "+" "+temp1.getTaskId()+" "+temp1.getJobId()+" "
//													+temp1.getRemainingTime()+" "+temp1.getPromotionTime()+" "+temp1.getDeadline());
											found=true;
											System.out.println("time    "+time+"readyQueue task  "+temp1.getTaskId()+"  job   "+temp1.getJobId() );
											System.out.println("noOfFaults "+noOfFaults+" is primary "+temp1.isPrimary());
											break;
										}
									}

									// faulty job may be running 
									if(!found && p.getCurrentJob().getBackupProcessor().getCurrentJob().getTaskId()== p.getCurrentJob().getTaskId() &&
											p.getCurrentJob().getBackupProcessor().getCurrentJob().getJobId()== p.getCurrentJob().getJobId()
											&& !p.getCurrentJob().getBackupProcessor().getCurrentJob().isPrimary() )
									{
										Job temp1 = p.getCurrentJob().getBackupProcessor().getCurrentJob();
										p.getCurrentJob().getBackupProcessor().getCurrentJob().setFaulty(true);

										noOfFaults++;
//										writer_fault.write("\n"+total_no_tasksets+" "+time+" "+p.getId()+" "+ " RUN "+" "+temp1.getTaskId()+" "+temp1.getJobId()+" "
//												+temp1.getRemainingTime()+" "+temp1.getPromotionTime()+" "+temp1.getDeadline());

										//		System.out.println("time    "+time+" running task  "+p.getCurrentJob().getBackupProcessor().getCurrentJob().getTaskId()+"  job   "+p.getCurrentJob().getBackupProcessor().getCurrentJob().getJobId() );
										//		System.out.println("noOfFaults "+noOfFaults+" is primary "+p.getCurrentJob().getBackupProcessor().getCurrentJob().isPrimary());

									}
									break;
								}
								else
								{
									tempPr= 1+rand.nextInt(m);
									continue;
								}



							}
							//			System.out.println("count  "+count);
							if (count==0)
								break;
						}

						fault.remove(0);
					}
				}
					   	// TEMP FAULT INDUCTION
/*
            	if ( tempFault.size()>0 )
            	{
          //  	System.out.println("out fault time  "+time+"  TEMPFAULT SIZE "+tempFault.size()+"  tempProcCheck size  "+tempProcCheck.size());

            		if(time==tempFault.get(0)*hyperperiod_factor)

            		{

            			for ( Processor p : freeProcList)
            			{
            				boolean found = false;
            				if (p.getId()==tempProcCheck.get(0) )
            				{

            					if (p.getProc_state()==ProcessorState.ACTIVE && p.getCurrentJob().isPrimary() && !p.getCurrentJob().isFaulty())
            					{	
            						System.out.println("                    fault time  "+time+"  proc  "+p.getId()+"            task  "+
            								p.getCurrentJob().getTaskId()+" job  "+p.getCurrentJob().getJobId() + "  prom time  "+p.getCurrentJob().getPromotionTime());

            						p.getCurrentJob().setCompletionSuccess(false);
            						p.getCurrentJob().setFaulty(true);
            					//	noOfFaults++;
            						Iterator<Job> spareItr = p.getCurrentJob().getBackupProcessor().backupJobQueue.iterator();
            						while(spareItr.hasNext())
            						{
            							Job temp1;
            							temp1  = spareItr.next();
            							// System.out.println("primaary pending  task  "+temp1.getTaskId());

            							if(temp1.getTaskId()== p.getCurrentJob().getTaskId() && temp1.getJobId()== p.getCurrentJob().getJobId())
            							{
            								temp1.setFaulty(true);
            								noOfFaults++;
            								found=true;
            								writer_fault.write("\n"+total_no_tasksets+" "+time+" "+temp1.getTaskId()+" "+temp1.getJobId()+" "
            										+temp1.getRemainingTime()+" "+temp1.getPromotionTime()+" "+temp1.getDeadline());

            					//			 System.out.println("time    "+time+"backupJobQueue task  "+temp1.getTaskId()+"  job   "+temp1.getJobId() );
            					//			System.out.println("noOfFaults "+noOfFaults+" is primary "+temp1.isPrimary());
            								 break;
            							}
            						}


            						// faulty job's backup copy may be in ready queue
            						Iterator<Job> spareItready = p.getCurrentJob().getBackupProcessor().readyQueue.iterator();
            						while(spareItready.hasNext())
            						{
            							Job temp1;
            							temp1  = spareItready.next();
            							// System.out.println("primaary pending  task  "+temp1.getTaskId());

            							if(temp1.getTaskId()== p.getCurrentJob().getTaskId() && temp1.getJobId()== p.getCurrentJob().getJobId())
            							{
            								temp1.setFaulty(true);



            								noOfFaults++;
            								found=true;
            					System.out.println("time    "+time+"readyQueue task  "+temp1.getTaskId()+"  job   "+temp1.getJobId() );
            								System.out.println("noOfFaults "+noOfFaults+" is primary "+temp1.isPrimary());
            								break;
            							}
            						}

            						// faulty job may be running 
            						if(!found && p.getCurrentJob().getBackupProcessor().getCurrentJob().getTaskId()== p.getCurrentJob().getTaskId() &&
            								p.getCurrentJob().getBackupProcessor().getCurrentJob().getJobId()== p.getCurrentJob().getJobId()
            								&& !p.getCurrentJob().getBackupProcessor().getCurrentJob().isPrimary() )
        							{
            							p.getCurrentJob().getBackupProcessor().getCurrentJob().setFaulty(true);





            							noOfFaults++;
            					//		System.out.println("time    "+time+" running task  "+p.getCurrentJob().getBackupProcessor().getCurrentJob().getTaskId()+"  job   "+p.getCurrentJob().getBackupProcessor().getCurrentJob().getJobId() );
        						//		System.out.println("noOfFaults "+noOfFaults+" is primary "+p.getCurrentJob().getBackupProcessor().getCurrentJob().isPrimary());

        							}
            						break;
            					}

            				}

            			}
            			tempProcCheck.remove(0);
            			tempFault.remove(0);
            		}
            	}
*/
				// CHECK DEADLINE MISS
				for (Processor proc : freeProcList)
				{
					if(proc.isBusy())
					{
						// System.out.println("// CHECK DEADLINE MISS   time  "+time +"task id "+proc.getCurrentJob().getTaskId()+" job id " + proc.getCurrentJob().getJobId());

						//+ "  job id  "+j1.getJobId()+  "   task id  " + j1.getTaskId() +"  deadline  "+j1.getAbsoluteDeadline());
						if (proc.getCurrentJob()!=null && !proc.getCurrentJob().isCompletionSuccess() && 
								proc.getCurrentJob().getAbsoluteDeadline()<time) // IF TIME IS MORE THAN THE DEADLINE, ITS A MISSING DEADLINE
						{
							deadlineMissed= true;
							System.out.println("  time "+time+"getCurrentJob deadline missed  task id "+proc.getCurrentJob().getTaskId()+" job id " + proc.getCurrentJob().getJobId()
									+"\tactivation "+proc.getCurrentJob().getActivationDate()+"  deadline time  "+proc.getCurrentJob().getAbsoluteDeadline()
									+" proc "+proc.getId() );
							writer_energy.write("deadline missed  task id "+proc.getCurrentJob().getTaskId()+" job id " + proc.getCurrentJob().getJobId()
									+"\tactivation "+proc.getCurrentJob().getActivationDate()+"  deadline time  "+proc.getCurrentJob().getAbsoluteDeadline()
									+"  time "+time+" proc "+proc.getId());	
							writer_energy.close();
						/*	writer_tasks.write("deadline missed  task id "+proc.getCurrentJob().getTaskId()+" job id " + proc.getCurrentJob().getJobId()
									+"\tactivation "+proc.getCurrentJob().getActivationDate()+"  deadline time  "+proc.getCurrentJob().getAbsoluteDeadline()
									+"  time "+time);*/	
					/*		writer_fault.write("\ndeadline missed  task id "+proc.getCurrentJob().getTaskId()+" job id " + proc.getCurrentJob().getJobId()
									+"\tactivation "+proc.getCurrentJob().getActivationDate()+"  deadline time  "+proc.getCurrentJob().getAbsoluteDeadline()
									+"  time "+time+" proc "+proc.getId());
*/
			/*				writer_schedule.write("deadline missed  task id "+proc.getCurrentJob().getTaskId()+" job id " + proc.getCurrentJob().getJobId()
									+"\tactivation "+proc.getCurrentJob().getActivationDate()+"  deadline time  "+proc.getCurrentJob().getAbsoluteDeadline()
									+"  time "+time+" proc "+proc.getId());
							writer_schedule.write("\n "+time+"\t"+"\t"+proc.getCurrentJob().getTaskId()+"\t"+proc.getCurrentJob().getJobId()+"\t"+proc.getCurrentJob().getActivationDate()+
									"\t"+proc.getCurrentJob().getRemainingTime()+"\t"+proc.getCurrentJob().getAbsoluteDeadline()+"\t"+proc.getCurrentJob().getProc().getId()+
									"\t"+proc.getCurrentJob().getStartTime()+"\t"+proc.getCurrentJob().getEndTime()+"\t"+proc.getCurrentJob().NoOfPreemption+" proc "+proc.getId());

							
							writer_schedule.close();*/
							
						//	writer_tasks.close();
						//	writer_fault.close();
							System.exit(0);

						}
					}

					if(!proc.readyQueue.isEmpty()&& time >=hyper-1)
					{
						//	System.out.println("time   "+time+"  p   "+proc.getId()+"   proc.readyQueue   "+proc.readyQueue.size());
						Iterator<Job> itrtemp =  proc.readyQueue.iterator();
						while(itrtemp.hasNext())
						{
							Job jtemp =itrtemp.next();
							//	System.out.println("t  "+jtemp.getTaskId()+"   job  "+jtemp.getJobId());

							if (jtemp!=null && !jtemp.isCompletionSuccess() && jtemp.getAbsoluteDeadline()<time) // IF TIME IS MORE THAN THE DEADLINE, ITS A MISSING DEADLINE
							{
								deadlineMissed= true;
								System.out.println(" time "+time +" jtemp p "+proc.getId() +"  primry "+jtemp.isPrimary()+ " completion "+jtemp.isCompletionSuccess());
								System.out.println("readyQueue deadline missed  task id "+jtemp.getTaskId()+" job id " + jtemp.getJobId()
								+"\tactivation "+jtemp.getActivationDate()+"  deadline time  "+jtemp.getAbsoluteDeadline()
								+"  time "+time+" proc "+proc.getId());
								writer_energy.write("deadline missed  task id "+jtemp.getTaskId()+" job id " + jtemp.getJobId()
								+"\tactivation "+jtemp.getActivationDate()+"  deadline time  "+jtemp.getAbsoluteDeadline()
								+"  time "+time+" proc "+proc.getId());	
								writer_energy.close();
							/*	writer_tasks.write("deadline missed  task id "+jtemp.getTaskId()+" job id " + jtemp.getJobId()
								+"\tactivation "+jtemp.getActivationDate()+"  deadline time  "+jtemp.getAbsoluteDeadline()
								+"  time "+time);	*/
				/*				writer_schedule.write("\ndeadline missed  task id "+jtemp.getTaskId()+"  deadline time  "+jtemp.getAbsoluteDeadline()+"  time "+time);
								writer_schedule.write("\n "+time+"\t"+"\t"+jtemp.getTaskId()+"\t"+jtemp.getJobId()+"\t"+jtemp.getActivationDate()+
										"\t"+jtemp.getRemainingTime()+"\t"+jtemp.getAbsoluteDeadline()+"\t"+jtemp.getProc().getId()+
										"\t"+jtemp.getStartTime()+"\t"+jtemp.getEndTime()+"\t"+jtemp.NoOfPreemption+" proc "+proc.getId());

							
								writer_schedule.close();
			*/					
							//	writer_tasks.close();
							//	writer_fault.close();
								System.exit(0);


							}
						}

					}	

					if(!proc.backupJobQueue.isEmpty()&& time >=hyper-1)
					{
						//	System.out.println("time   "+time+"  p   "+proc.getId()+"   proc.readyQueue   "+proc.readyQueue.size());
						Iterator<Job> itrtemp =  proc.readyQueue.iterator();
						while(itrtemp.hasNext())
						{
							Job jtemp =itrtemp.next();
							//	System.out.println("t  "+jtemp.getTaskId()+"   job  "+jtemp.getJobId());

							if (jtemp!=null && !jtemp.isCompletionSuccess() && jtemp.getAbsoluteDeadline()<time) // IF TIME IS MORE THAN THE DEADLINE, ITS A MISSING DEADLINE
							{
								deadlineMissed= true;
								System.out.println("backupJobQueue deadline missed  task id "+jtemp.getTaskId()+" job id " + jtemp.getJobId()
								+"\tactivation "+jtemp.getActivationDate()+"  deadline time  "+jtemp.getAbsoluteDeadline()
								+"  time "+time+" proc "+proc.getId());
								writer_energy.write("deadline missed  task id "+jtemp.getTaskId()+" job id " + jtemp.getJobId()
								+"\tactivation "+jtemp.getActivationDate()+"  deadline time  "+jtemp.getAbsoluteDeadline()
								+"  time "+time+" proc "+proc.getId());	
								writer_energy.close();
								/*writer_tasks.write("deadline missed  task id "+jtemp.getTaskId()+" job id " + jtemp.getJobId()
								+"\tactivation "+jtemp.getActivationDate()+"  deadline time  "+jtemp.getAbsoluteDeadline()
								+"  time "+time);*/	
				/*				writer_schedule.write("\ndeadline missed  task id "+jtemp.getTaskId()+"  deadline time  "+jtemp.getAbsoluteDeadline()+"  time "+time+" proc "+proc.getId());
								writer_schedule.write("\n "+time+"\t"+"\t"+jtemp.getTaskId()+"\t"+jtemp.getJobId()+"\t"+jtemp.getActivationDate()+
										"\t"+jtemp.getRemainingTime()+"\t"+jtemp.getAbsoluteDeadline()+"\t"+jtemp.getProc().getId()+
										"\t"+jtemp.getStartTime()+"\t"+jtemp.getEndTime()+"\t"+jtemp.NoOfPreemption+" proc "+proc.getId());

							
								writer_schedule.close();*/
								
							//	writer_tasks.close();
							//	writer_fault.close();
								System.exit(0);
							}
						}

					}
				}


				//at end time of any job in any processor
				for (Processor proc : freeProcList)
				{
					if(time== proc.getEndTimeCurrentJob() && proc.isBusy())
					{
				/*		if(proc.getCurrentJob().getTaskId()==230)
						System.out.println("inend  TIME  "+time+"  p  "+proc.getId()+" active  "+ proc.activeTime+"  energy  "+proc.getActiveEnergy());
*/
						proc.setProc_state(proc_state.IDLE);
						proc.setBusy(false);
						proc.getCurrentJob().setCompletionSuccess(true);
						executedTime = (time-proc.getCurrentJob().getStartTime())+1;
						remain_time = 0;
						energy_consumed= energyConsumed.energyActive(((time-proc.getCurrentJob().getStartTime())+1),
								proc.getFrequency());
						proc.setActiveEnergy(energy_consumed);
						
						//System.out.println("in TIME  "+time+"  p  "+proc.getId()+" active  "+ proc.activeTime+"  energy  "+proc.getActiveEnergy());
		/*				if(proc.getCurrentJob().getTaskId()==230)
							 System.out.println(" //at end time of any job    p  "+proc.getId()+"   end time  "+proc.getEndTimeCurrentJob()
        		+"  primary   "+proc.getCurrentJob().isPrimary()+"  task  "+proc.getCurrentJob().getTaskId()+"  job  "+proc.getCurrentJob().getJobId());
		*/				 	if(proc.getCurrentJob().isPrimary())
						 {


							 fullPrimariesExecuted++;
		/*					 writer_schedule.write("\ne "+proc.getId()+" "+proc.getCurrentJob().getTaskId()+" "+proc.getCurrentJob().getJobId()+" "+
									 proc.getCurrentJob().isPrimary()+" "+Double.valueOf(twoDecimals.format(	proc.getCurrentJob().getFrequency()))
									 +" "+	Double.valueOf(twoDecimals.format(proc.getCurrentJob().getExec_frequency()))+" "+proc.getCurrentJob().getRemainingTime()+" "+proc.getCurrentJob().getRomainingTimeCost()+" "+
									 proc.getCurrentJob().getDeadline()
									 +" "+	proc.getCurrentJob().isPreempted+" "+proc.getCurrentJob().getStartTime()+" ");
							 writer_schedule.write(proc.getCurrentJob().getEndTime()+" "+executedTime+" "+remain_time+" "+energy_consumed+
									 " "+proc.getCurrentJob().isFaulty() );
		*/				
						 }
						 else
						 {

							 fullBackupsExecuted++;
							 // 	//System.out.println("time  "+time  +"  proc  "+proc.getId()+"  fullBackupsExecuted   "+fullBackupsExecuted);

			/*				 writer_schedule.write("\ne "+proc.getId()+" "+proc.getCurrentJob().getTaskId()+" "+proc.getCurrentJob().getJobId()+" "+
									 proc.getCurrentJob().isPrimary()+" "+Double.valueOf(twoDecimals.format(	proc.getCurrentJob().getFrequency()))
									 +" "+Double.valueOf(twoDecimals.format(proc.getCurrentJob().getExec_frequency()))+" "+	proc.getCurrentJob().getRomainingTimeCost()+" "+proc.getCurrentJob().getRomainingTimeCost()+" "+	proc.getCurrentJob().getDeadline()
									 +" "+	proc.getCurrentJob().isPreempted+" "+proc.getCurrentJob().getStartTime()+" ");
							 writer_schedule.write(proc.getCurrentJob().getEndTime()+" "+executedTime+" "+remain_time+" "+energy_consumed+
									 " "+proc.getCurrentJob().isFaulty() );
							 writer_schedule.write(" "+fullBackupsExecuted +" "+partialBackupsExecuted +" "+fullBackupsCancelled+" "
									 + cancelledPrimariesFull +" "+  cancelledPrimariesPartial +" "+ fullPrimariesExecuted +" "+noOfFaults);
*/
						 }

						 ////////////// DELETE THE BACKUP JOB IF NOT STARTED///////////////////
						 if(proc.getCurrentJob().isPrimary() && !proc.getCurrentJob().isFaulty())
						 {
							 // delete the backup job if not started
							 boolean cancel = false;
							 //		//System.out.println("time  "+time  +"   //at end time of any job    delete the backup job if not started ");
							 //			 //System.out.println("p  "+proc.getCurrentJob().getBackupProcessor().getId()+"  size  "+proc.getCurrentJob().getBackupProcessor().backupJobQueue.size());
							 Iterator<Job> itr_backup = proc.getCurrentJob().getBackupProcessor().backupJobQueue.iterator();
							 while(itr_backup.hasNext())
							 {
								 Job backup = itr_backup.next();

								 //	//System.out.println("backup.isFaulty()  "+backup.isFaulty());
								 if(!backup.isFaulty() && backup.getTaskId()==proc.getCurrentJob().getTaskId() && backup.getJobId()==proc.getCurrentJob().getJobId())
								 {
									 /*	 //System.out.println(" time  "+time+"   p  "+proc.getId()+ "  backup p  " +proc.getCurrentJob().getBackupProcessor().getId()+
        						"  delete task  "+	backup.getTaskId() +"  job  "+ backup.getJobId());
									  */		backup.setCompletionSuccess(true);
									  proc.getCurrentJob().getBackupProcessor().backupJobQueue.remove(backup);

									  /*if(backup.isPreempted==true)
        						partialBackupsExecuted++;
        					else*/
									  cancel=true;
									  fullBackupsCancelled++;
				/*					  writer_schedule.write(" "+fullBackupsExecuted +" "+partialBackupsExecuted +" "+fullBackupsCancelled+" "
											  + cancelledPrimariesFull +" "+  cancelledPrimariesPartial +" "+ fullPrimariesExecuted +" "+noOfFaults);
				*/					  /*		//System.out.println("time   "+time+"   fullPrimariesExecuted  "+fullPrimariesExecuted+
            		    			"  proc.getCurrentJob().getEndTime()  "+proc.getCurrentJob().getEndTime());
									   */		break;
								 }
							 }
							 if (!cancel) // in ready queue
							 {
								 Iterator<Job> itr_back = proc.getCurrentJob().getBackupProcessor().readyQueue.iterator();
								 while(itr_back.hasNext())
								 {
									 Job backup = itr_back.next();

									 //	//System.out.println("backup.isFaulty()  "+backup.isFaulty());
									 if(!backup.isFaulty() && backup.getTaskId()==proc.getCurrentJob().getTaskId() && backup.getJobId()==proc.getCurrentJob().getJobId())
									 {
										 /*			 //System.out.println(" time  "+time+"   p  "+proc.getId()+ "  backup p  " +proc.getCurrentJob().getBackupProcessor().getId()+
             						"  delete task  "+	backup.getTaskId() +"  job  "+ backup.getJobId());
										  */			backup.setCompletionSuccess(true);
										  proc.getCurrentJob().getBackupProcessor().readyQueue.remove(backup);
										  cancel=true;
										  if(backup.isPreempted==true)
											  partialBackupsExecuted++;
										  else
											  fullBackupsCancelled++;
		/*								  writer_schedule.write(" "+fullBackupsExecuted +" "+partialBackupsExecuted +" "+fullBackupsCancelled+" "
												  + cancelledPrimariesFull +" "+  cancelledPrimariesPartial +" "+ fullPrimariesExecuted +" "+noOfFaults);
		*/								  /*//System.out.println("time   "+time+"   fullPrimariesExecuted  "+fullPrimariesExecuted+
                 		    			"  proc.getCurrentJob().getEndTime()  "+proc.getCurrentJob().getEndTime());
										   */	
										  break;
									 }
								 }
							 }
							 //DELETE THE BACKUP JOB IF RUNNING

							 Job onPrimary, onBackup;
							 onPrimary = proc.getCurrentJob();
							 onBackup=onPrimary.getBackupProcessor().getCurrentJob();
						//	 System.out.println("time  "+time+ " onPrimary "+onPrimary.getTaskId());
									//+ " onBackup  "+onBackup.getTaskId());
							// !cancel means backup has not been cancel above in queue and is running
							// for WFD where a processor may have only backups or may be a empty processor
							 if(				 !cancel&&
									 onBackup!=null 
									 && !onBackup.isCompletionSuccess() && onBackup.getTaskId()==onPrimary.getTaskId()
									 && onBackup.getJobId()==onPrimary.getJobId())
							 {

								 partialBackupsExecuted++;


								 // //System.out.println("//at end time of any job  //delete the backup job if running");
								 onPrimary.getBackupProcessor().setBusy(false);
								 onBackup.setCompletionSuccess(true);
								 onPrimary.getBackupProcessor().setProc_state(proc_state.IDLE);
								 executedTime = (time-onBackup.getStartTime())+1;
									remain_time = onBackup.getRomainingTimeCost()-executedTime;
									energy_consumed= energyConsumed.energyActive
											 (((time-onBackup.getStartTime())+1), 
													 onPrimary.getBackupProcessor().getFrequency());
								
								 onPrimary.getBackupProcessor().setActiveEnergy(energy_consumed);
								 //System.out.println("in running TIME  "+time+"  p  "+onPrimary.getBackupProcessor().getId()+" active  "+ onPrimary.getBackupProcessor().activeTime+"  energy  "+onPrimary.getBackupProcessor().getActiveEnergy());

								 //			proc.setActiveEnergy(energyConsumed.energyActive((time-onBackup.getStartTime()), onBackup.getFrequency()));
					/*			 writer_schedule.write("\n//deletethebackup "+proc.getCurrentJob().getBackupProcessor().getId()+
										 " "+onBackup.getTaskId()+" "+onBackup.getJobId()+" "+
										 onBackup.isPrimary()+" "+Double.valueOf(twoDecimals.format(	onBackup.getFrequency()))
										 +" "+Double.valueOf(twoDecimals.format(onBackup.getExec_frequency()))+" "+	onBackup.getRomainingTimeCost()+" "+onBackup.getRomainingTimeCost()+" "+onBackup.getDeadline()
										 +" "+	onBackup.isPreempted+" "+onBackup.getStartTime());
								 writer_schedule.write(" "+(time+1)+" "+executedTime+" "+remain_time+" "+energy_consumed+
										" " +proc.getCurrentJob().isFaulty());
								 writer_schedule.write(" "+fullBackupsExecuted +" "+partialBackupsExecuted +" "+fullBackupsCancelled+" "
										 + cancelledPrimariesFull +" "+  cancelledPrimariesPartial +" "+ fullPrimariesExecuted +" "+noOfFaults);
 */
							 }

						 }  // end if(proc.getCurrentJob().isPrimary())

						 ///////////	BACKUP HAS COMPLETED/////
						 else if (!proc.getCurrentJob().isPrimary()) //backup has completed
						 {
							 // delete the primary job if not started
							 // //System.out.println("delete the primary job if not started");
							 Iterator<Job> itr_primary = proc.getCurrentJob().getPrimaryProcessor().readyQueue.iterator();
							 while(itr_primary.hasNext())
							 {
								 Job primaryTask = itr_primary.next();
								 if(primaryTask.getTaskId()==proc.getCurrentJob().getTaskId() && primaryTask.getJobId()==proc.getCurrentJob().getJobId())
								 {
									 // //System.out.println(" time  "+time+"   p  "+proc.getId()+ "  primary p  " +proc.getCurrentJob().getPrimaryProcessor().getId()+
									 //					"  delete task  "+	primaryTask.getTaskId() +"  job  "+ primaryTask.getJobId());
									 primaryTask.setCompletionSuccess(true);
									 if(primaryTask.isPreempted==true)
										 cancelledPrimariesPartial++;
									 else
										 cancelledPrimariesFull++;
									 proc.getCurrentJob().getPrimaryProcessor().readyQueue.remove(primaryTask);


									 break;
								 }
							 }

							 //delete the primary job if running
							 // //System.out.println("delete the primary job if running");
							 Job onPrimary, onBackup;
							 onBackup = proc.getCurrentJob();
							 onPrimary=onBackup.getPrimaryProcessor().getCurrentJob();
							 if(onPrimary!=null &&
									 !onPrimary.isCompletionSuccess()
									 && onBackup.getTaskId()==onPrimary.getTaskId() 
									 && onBackup.getJobId()==onPrimary.getJobId())
							 {

								 cancelledPrimariesPartial++;
								 onBackup.getPrimaryProcessor().setProc_state(proc_state.IDLE);
								 executedTime = (time-onPrimary.getStartTime())+1;
									remain_time = onPrimary.getRemainingTime()-executedTime;
									energy_consumed= energyConsumed.energyActive
											 (((time-onPrimary.getStartTime())+1),
													 onBackup.getPrimaryProcessor().getFrequency());
								
								 onBackup.getPrimaryProcessor().setActiveEnergy(energy_consumed);
								 //System.out.println("in TIME  "+time+"  p  "+proc.getId()+" active  "+ proc.activeTime+"  energy  "+proc.getActiveEnergy());

								 onBackup.getPrimaryProcessor().setBusy(false);
								 //	 onPrimary.getPrimaryProcessor().setBusy(false);
								 onPrimary.setCompletionSuccess(true);
								 //		proc.setActiveEnergy(energyConsumed.energyActive((time-onPrimary.getStartTime()), onPrimary.getFrequency()));
				/*				 writer_schedule.write("\ndeletetheprimary "+onBackup.getPrimaryProcessor().getId()+" "+onPrimary.getTaskId()+" "+onPrimary.getJobId()+" "+
										 onPrimary.isPrimary()+" "+Double.valueOf(twoDecimals.format(	onPrimary.getFrequency()))
										 +" "+	Double.valueOf(twoDecimals.format(onPrimary.getExec_frequency()))+" "+	onPrimary.getRemainingTime()+" "+onPrimary.getRomainingTimeCost()+	" "+onPrimary.getDeadline()
										 +" "+	onPrimary.isPreempted+" "+onPrimary.getStartTime()+" ");
								 writer_schedule.write(""+(time+1)+" "+executedTime+" "+remain_time+" "+energy_consumed+" "+proc.getCurrentJob().isFaulty());
								 writer_schedule.write(" "+fullBackupsExecuted +" "+partialBackupsExecuted +" "+fullBackupsCancelled+" "
										 + cancelledPrimariesFull +" "+  cancelledPrimariesPartial +" "+ fullPrimariesExecuted +" "+noOfFaults);
*/
							 }
						 }
						 //	//System.out.println("p  "+proc.getId());
						 proc.setNextActivationTime(time);
						 proc.setTimeToNextArrival( proc.getNextActivationTime()-time-1);
					}
				}
				time++;
				if (deadlineMissed)
					break;

			}
			for (Processor proc : freeProcList)
			{
				proc.setEnergy_consumed(proc.getActiveEnergy()+proc.getIdleEnergy()+proc.getSleepEnergy());
				
				energyTotal+= proc.getEnergy_consumed();
			}
			/* for(Processor p : freeProcList)
        {
        writer_taskProcWise.write("\n "+p.getId()+" "+p.getNoOfPriJobs()+" "+p.getNoOfBackJobs()+
        		" "+(p.getNoOfPriJobs()+p.getNoOfBackJobs()));
        }*/
			/*writer_tasks.write("\n"+fullBackupsExecuted +" "+partialBackupsExecuted +" "+fullBackupsCancelled+" "
					+ cancelledPrimariesFull +" "+  cancelledPrimariesPartial +" "+ fullPrimariesExecuted +" "+totalPrimaries+" " +noOfFaults);
*/
			if(deadlineMissed)
				writer_energy.write(total_no_tasksets++ + " "+Double.valueOf(twoDecimals.format(U_SUM))+" "
						+Double.valueOf(twoDecimals.format(0))+" 0"+" 1 "+pof_1+" "+pof_MOG+"\n");
				else
				writer_energy.write(total_no_tasksets++ + " "+Double.valueOf(twoDecimals.format(U_SUM))+" "
						+Double.valueOf(twoDecimals.format(energyTotal))	+" 1"+" 0 "+pof_1+" "+pof_MOG+"\n");
			/*writer_energy.write("\n"+total_no_tasksets++ + " "+Double.valueOf(twoDecimals.format(U_SUM))
			+" "+ Double.valueOf(twoDecimals.format(minfq))+" "+ Double.valueOf(twoDecimals.format(maxfq))+" "+
			" "+Double.valueOf(twoDecimals.format(energyTotal)));*/
			/*  for(Processor p : freeProcList)
        {
        	 writer_energy.write(" " +p.activeTime+" "+p.idleTime+" "+p.sleepTime);
        }*/
			System.out.println("  mogMULTI_6_12_18  fq    "+maxfq +"  tasksets  "+total_no_tasksets+" energy  "+energyTotal);

			/*if (deadlineMissed)
				break;*/

			/* if(total_no_tasksets>500)
    	   break;*/
		}

		// writer_allocation.close();
		writer_energy.close();
	//	writer_schedule.close();
		
		//writer_tasks.close();
	/*	writer_fault.close();
		writer_analysis.close();
	*/	//  writer_taskProcWise.close();
		System.out.println("finish mogMULTI_6_12_18 ");
	}

	
	public static double discreteFreq(double fq)
	{
		
		if(fq>0.9 && fq<=1)
	  		fq=1;
	  	if(fq>0.8 && fq<=0.9)
	  		fq=0.9;
	  	if(fq>0.7 && fq<=0.8)
	  		fq=0.8;
	  	if(fq>0.6 && fq<=0.7)
	  		fq=0.7;
	  	if(fq>0.5 && fq<=0.6)
	  		fq=0.6;
	  	if(fq>0.4 && fq<=0.5)
	  		fq=0.5;
	  	if(fq>0.3 && fq<=0.4)
	  		fq=0.4;
	  	if( fq<=0.3)
	  		fq=0.3;
	  	return fq;
	  	
	}
	public static void prioritize(ArrayList<ITask> taskset)
	{
		int priority =1;

		for(ITask t : taskset)
		{
			t.setPriority(priority++);

		}

		//		return taskset;

	}

}


