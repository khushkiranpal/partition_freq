package scheduleRMS;

import java.io.FileNotFoundException;
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
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;

import energy.ParameterSetting;
import energy.SysClockFreq;
import platform.Energy;
import platform.Fault;
import platform.Processor;
import platform.ProcessorState;
import queue.ISortedJobQueue;
import queue.ISortedQueue;
import queue.SortedJobQueuePeriod;
import queue.SortedQueuePeriod;
import taskGeneration.FileTaskReaderTxt;
import taskGeneration.ITask;
import taskGeneration.IdleSlot;
import taskGeneration.Instance;
import taskGeneration.Job;
import taskGeneration.SystemMetric;

/**
 * @author KHUSHKIRAN PAL
 *	IMPLEMENTATION OF HAQUE (NO BENEFIT OF BACKUP CANCELLING BY DELAYING THE LOWER PRIORITY BACKUPS
 * ONLY STATIC IMPLEMETATION)
 */
public class RMS_NoPowerManag{

	/* public static final   double  CRITICAL_freq= 0.50;//0.42;   //
		 public static final long hyperperiod_factor=1;
		 public static final   double  CRITICAL_TIME=  1.5*10;//hyperperiod_factor;///1500;  //
		 public static final int d=0;
		 private double freq=1;
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
	//public void schedule(String IP_filename, long hyperperiod_factor, int d,double CRITICAL_TIME,double CRITICAL_freq) throws IOException
	/**
	 * @param inputfilename
	 * @param outputFolder
	 * @param inputFolder
	 * @param hyperperiod_factor
	 * @param d
	 * @param CRITICAL_TIME
	 * @param CRITICAL_freq
	 * @param faultFromFile
	 * @param bcetRatio
	 * @throws IOException
	 */
	public void schedule(String inputfilename,String outputFolder,String inputFolder,
			long hyperperiod_factor, int d,double CRITICAL_TIME,double CRITICAL_freq,double min_freq,
			boolean faultFromFile,double bcetRatio, long hyper, int n_proc) throws IOException
	{
		System.out.println("STARTING NPM");
		FileTaskReaderTxt reader = new FileTaskReaderTxt(inputFolder+inputfilename); // read taskset from file
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
		Calendar cal = Calendar.getInstance();
		String date = dateFormat.format(cal.getTime());
		String filename= outputFolder+"primaryNPM"+"_"+inputfilename+"_"+date+".txt";
		String filename2= outputFolder+"energyNPM"+"_"+inputfilename+"_"+date+".txt";
	//	Writer writer_primary = new FileWriter(filename);
		Writer writer_energy = new FileWriter(filename2);

		DecimalFormat twoDecimals = new DecimalFormat("#.##");  // upto 1 decimal points
		Energy energyConsumed = new Energy();
		Job[] current= new Job[2];  // FOR SAVING THE NEWLY INTIAlIZED JOB  FROM JOBQUEUE SO THAT IT 
		// IS VISIBLE OUTSIDE THE BLOCK

		ITask task;
		ITask[] set = null;
		double U_SUM;
		// final   long  CRITICAL_TIME= 4;

		// IDLE SLOTS QUEUE
		IdleSlot slot = new IdleSlot(); // idle slot
		List <IdleSlot> slots = new ArrayList<IdleSlot>();
		int total_no_tasksets=1;
		//    writer2.write("TASKSET UTILIZATION SYS_FREQ FREQ P_ACTIVE P_IDLE P_SLEEP S_ACTIVE S_IDLE S_SLEEP PRIMARY_ENERGY SPARE_ENERGY NPM TOTAL(S+P) \n");
		writer_energy.write("NPMTASKSET UTILIZATION PRIMARY_ENERGY success\n");
		SysClockFreq frequency = new SysClockFreq();

		while ((set = reader.nextTaskset()) != null)
		{
			
			boolean primaryBusy=false;
			double energy_consumed=0;
			boolean deadlineMissed = false;
			Job lastExecutedJob= null;
			ProcessorState proc_state = null;
			int response_zero=0;
			int id = 0;  // idle slot id 
			long time=0 ;
		
			long timeToNextArrival=0;
			long endTime = 0; // endtime of job
		
			long idle = 0;  // idle time counter for processor idle slots
			SchedulabilityCheck schedule = new SchedulabilityCheck();

			Processor primary = new Processor();
			primary.setBusy(false);
			primary.setProc_state(proc_state.IDLE);


			ISortedQueue queue = new SortedQueuePeriod ();
			queue.addTasks(set);
			ArrayList<ITask> taskset = new ArrayList<ITask>();
			ArrayList<Job> completedJobs = new ArrayList<Job>();
			taskset = queue.getSortedSet();
			U_SUM= (SystemMetric.utilisation(taskset));
			//	total_no_tasks=total_no_tasks+ tasks.size();
			prioritize(taskset);
				/////////////// HYPER PERIOD////////////
			hyper=SystemMetric.hyperPeriod(taskset);
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

			ps.setBCET(taskset, bcetRatio);
			ps.setACET(taskset);

			double  fq = 1;

		//	fq=Math.max(set_fq, CRITICAL_freq);

			//	System.out.println("frequency   " +fq);
			ps.set_freq(taskset,Double.valueOf(twoDecimals.format(fq)));

			boolean schedulability = schedule.worstCaseResp_TDA_RMS(taskset);
				  System.out.println("   schedulability   "+schedulability);
				  if(!schedulability)
				  {
					//  writer_primary.write("\nschedulability "+schedulability);

					  break;
				  }
			ps.setResponseTime(taskset);    
		
			long temp=0;
			ISortedJobQueue activeJobQ = new SortedJobQueuePeriod(); // dynamic jobqueue 
			Job j; //job
			TreeSet<Long> activationTimes = new TreeSet<Long>();
			//	TreeSet<Long> promotionTimes = new TreeSet<Long>();
			ArrayList <Long> promotionTimes = new ArrayList<Long>();

			long nextActivationTime=0;

			long executedTime=0;

			// ACTIVATE ALL TASKS AT TIME 0 INITIALLY IN QUEUE  

			for(ITask t : taskset)  // activate all tasks at time 0
			{
				temp=0;
				j =  t.activateRMS_energy_ExecTime(time);  ////	remainingTime =  (long)ACET;  ////////////////
				//totalPrimaries++;
				j.setPriority(t.getPriority());

				activeJobQ.addJob(j);  //////ADD TO PRIMARY QUEUE
				j.setCompletionSuccess(false);
				while (temp<=hyper*hyperperiod_factor)
				{
					temp+=t.getPeriod();
					activationTimes.add(temp);
					
				}

			}

			//	System.out.println("activationTimes  "+activationTimes.size()+"  promotionTimes  "+promotionTimes.size());
			promotionTimes.sort(new Comparator <Long>() {
				@Override
				public int compare(Long t1, Long t2) {
					if(t1!=t2)
						return (int) (t1-t2);
					else 
						return 0;

				}
			});

			
	//		writer_primary.write("\nTASKID av_ACET BCET ACET WCET_or WCET RESP PROM DEADLINE\n");

			for (ITask t: taskset)
			{
				if(t.getResponseTime()==0)
					response_zero++;

			/*	writer_primary.write("\n"+t.getId()+" "+t.getAverage_CET()+" "
						+t.getBCET()+" "+t.getACET()+" "+t.getWCET_orginal()+" " +t.getWcet()+" "+t.getResponseTime()+
						" "+t.getSlack()+" "+t.getDeadline());
		*/		/* System.out.println("t.getId"+t.getId()+" getACET  "+t.getACET()+" getWCET_orginal "+t.getWCET_orginal()+"  getWcet " +t.getWcet()+" getResponseTime "+t.getResponseTime()+
						  "  prom "+t.getSlack()+" getDeadline  "+t.getDeadline());
				 */	  }

			/*if(response_zero>0)
				writer_primary.write(" \nResponse time zero "+response_zero);

			writer_primary.write("\nSCHEDULE\nTASKID  JOBID  ARRIVAL  ACET WCET prom DEADLINE  isPreempted STARTTIME ENDTIME\n");// EASSfullBackupsExecuted partialBackupsExecuted fullBackupsCancelled"
*/
			nextActivationTime=  activationTimes.pollFirst();

			while(time<hyper*hyperperiod_factor)
			{

				if( (long)time== (long)nextActivationTime) // AFTER 0 TIME JOB ACTIVAIONS
				{

					if (!activationTimes.isEmpty())
						nextActivationTime=  activationTimes.pollFirst();

					for (ITask t : taskset) 
					{

						Job n = null;
						long activationTime;
						activationTime = t.getNextActivation(time-1);  //GET ACTIVATION TIME
						//		System.out.println("  activationTime  "+activationTime);
						long temp1= (long) activationTime, temp2 =(long) time;
						if (temp1==temp2)
							n= t.activateRMS_energy_ExecTime(time); ///	remainingTime =  (long)ACET;  ////////////////

						if (n!=null)
						{
						//	totalPrimaries++;
							n.setCompletionSuccess(false);
							activeJobQ.addJob(n);  // add NEW job ///////////ADD TO PRIMARY QUEUE

						}
					}

				} 
				//////////////////PREEMPTION in PRIMARY////////////////////////

				if(time>0 && !activeJobQ.isEmpty() && time==activeJobQ.first().getActivationDate() && current[0]!=null )
				{
					// System.out.println("activeJobQ.first().getActivationDate()  "+activeJobQ.first().getActivationDate());

					if (activeJobQ.first().getPeriod()<current[0].getPeriod())
					{
						// System.out.println("preemption  ");

						primaryBusy=false;
					//	writer_primary.write("\t"+time+"\t preempted\n");
						executedTime = time - current[0].getStartTime();
						// System.out.println("time   "+time+"  executedTime  "+executedTime);
						current[0].setRemainingTime(current[0].getRemainingTime()-executedTime);
						current[0].isPreempted=true;
						if (current[0].getRemainingTime()>0)
							activeJobQ.addJob(current[0]);
						// System.out.println("preempted job  "+current[0].getTaskId()+" remaining time "+current[0].getRemainingTime()+ "   wcet "+
						//			current[0].getRomainingTimeCost());
					}
				}


				// SELECT JOB FROM QUEUE ONLY if processor is free///////
				if ((primaryBusy == false ) )
				{

					j = activeJobQ.pollFirst(); // get the job at the top of queue

					// QUEUE MAY BE EMPTY , SO CHECK IF IT IS  NOT NULL
					if (j!=null && j.isCompletionSuccess()==false)      // if job in queue is null 
					{
						primary.setProc_state(proc_state.ACTIVE);
						//	System.out.println("time   "+time+"   active   "+primary.getActiveTime());
						//  IDLE SLOTS RECORD
						if (idle!=0)
						{
							energy_consumed+= energyConsumed.energy_IDLE(idle);
							
					//		writer_primary.write("  endtime  "+time+"\n");
							slot.setLength(idle);  // IF PROCESSOR IS IDLE FROM LONF TIME, RECORD LENGTH OF IDLESLOT
							IdleSlot cloneSlot = (IdleSlot) slot.cloneSlot(); // CLONE THE SLOT
							slots.add(cloneSlot); // ADD THE SLOT TO LIST OR QUEUE
						}
						//RE- INITIALIZE IDLE VARIABLE FOR IDLE SLOTS
						idle =0;   // if job on the queue is not null, initialize  processor idle VARIABLE to 0

						current[0]=j;  // TO MAKE IT VISIBLE OUTSIDE BLOCK
						//		System.out.println("current[0]  "+current[0].getTaskId()+" start time "+(long)time);

				/*		writer_primary.write(j.getTaskId()+"\t  "+j.getJobId()+"\t"+j.getActivationDate()+"\t"+j.getACET()+
								"\t"+j.getRemainingTime()+"\t"+j.getPromotionTime()+"\t"+j.getAbsoluteDeadline()+"\t"+j.isPreempted+"\t\t"+time+"\t");
*/

						j.setStartTime(time);  // other wise start time is one less than current time 
						// BCOZ START TIME IS EQUAL TO END OF LAST EXECUTED JOB

						endTime =  (time+j.getRemainingTime());
						//	System.out.println("current[0]  "+current[0].getTaskId()+"   endTime  "+(long)endTime);
						primaryBusy = true;   //set  processor busy
						lastExecutedJob = j;    
					}
					else  // if no job in jobqueue
					{

						
						primary.setProc_state(proc_state.IDLE);
						if (idle==0)  // if starting of idle slot
						{
					//	writer_primary.write("\nIDLE SLOT");
							slot.setId(id++); // SET ID OF SLOT
							slot.setStartTime(time);// START TIME OF SLOT
							current[0] = null;
						//	writer_primary.write("\tstart time\t"+time+"\t");
						}

						idle++; // IDLE SLOT LENGTH 

						slot.setEndTime(idle + slot.getStartTime()); // SET END TIME OF SLOT
					} //end else IDLE SLOTS

				}

				// CHECK DEADLINE MISS IN PRIMARY
				Iterator<Job> it = activeJobQ.iterator();
				while (it.hasNext()) //CHECK FOR ALL ACTIVE JOBS
				{
					Job j1 = it.next();
					if (j1.getAbsoluteDeadline()<time) // IF TIME IS MORE THAN THE DEADLINE, ITS A MISSING DEADLINE
					{
						System.out.println("deadline missed  task id "+j1.getTaskId()+"job id " + j1.getJobId()+"  deadline time  "+j1.getAbsoluteDeadline()+"  time "+time);
					/*	writer_energy.write("\ndeadline     missed  task id "+j1.getTaskId()+" job id "+j1.getJobId()+
								"  deadline time  "+j1.getAbsoluteDeadline()+"  time "+time+" ");
*/
						deadlineMissed= true;

/*
						writer_energy.close();

						System.exit(0);*/
					}
				}

				// IF NOW TIME IS EQUAL TO ENDTIME OF JOB

				//	double temp1 = Double.valueOf(twoDecimals.format(time)), temp2= Double.valueOf(twoDecimals.format(endTime-1));
				if ((long)time==(long)endTime-1 && lastExecutedJob.isCompletionSuccess()==false ) // if current time == endtime 
				{

					//  			System.out.println("                time  "+time+"  end time "+ (endTime-1));
					//	Job k =  executedList.get(noOfJobsExec-1);// get last executed job added to list or job at the top of executed list
					primaryBusy = false;  // set processor free

					// CHECK DEADLINE MISS of current job
					if (time+1>lastExecutedJob.getAbsoluteDeadline())
					{
						System.out.println("current  deadline missed  spare task id "+lastExecutedJob.getTaskId()+"job id " + lastExecutedJob.getJobId()+"  deadline time  "+lastExecutedJob.getAbsoluteDeadline()+"  time "+time);
						System.out.println("  comp  "+lastExecutedJob.isCompletionSuccess()+"  faulty  "+lastExecutedJob.isFaulty()+"   prom  "+lastExecutedJob.getPromotionTime());
						writer_energy.write("\ndeadline     missed   sparetask id "+lastExecutedJob.getTaskId()+" job id "+lastExecutedJob.getJobId()+
								"  deadline time  "+lastExecutedJob.getAbsoluteDeadline()+"  time "+time+" ");

			/*			writer_primary.write("\ndeadline    missed spare task id "+lastExecutedJob.getTaskId()+" job id "+lastExecutedJob.getJobId()+
								"  deadline time  "+lastExecutedJob.getAbsoluteDeadline()+"  time "+time+"  ");
*/
				//		writer_primary.close();
						deadlineMissed= true;
						/*writer_energy.close();
						System.exit(0);*/
					}
					lastExecutedJob.setEndTime(endTime);  // set endtime of job
					lastExecutedJob.setCompletionSuccess(true);//-------------------
			//	writer_primary.write(endTime+" endtime \n");
				}

				if (primary.getProc_state()==proc_state.ACTIVE)
					{primary.activeTime++;}
				
				
				if (primary.getProc_state()==proc_state.IDLE)
				{primary.idleTime++;}
				
				time=time+1;
				if (deadlineMissed)
					break;
			}
			System.out.println("primary  active time "+primary.getActiveTime()+"  sleep "+primary.getSleepTime()+"  idle  "+primary.getIdleTime());


			double primaryEnergy;
			primaryEnergy = energyConsumed.energyActive(primary.activeTime, fq)+energy_consumed ;
			// FOR TOTAL ENERGY COMPARISON NPM*2 ON TWO PROCESSORS
			if(deadlineMissed)
				writer_energy.write(total_no_tasksets++ + " "+Double.valueOf(twoDecimals.format(U_SUM))+" "
						+" "+Double.valueOf(twoDecimals.format(primaryEnergy))+
						" 0"+"\n");
				else
				writer_energy.write(total_no_tasksets++ + " "+Double.valueOf(twoDecimals.format(U_SUM))+" "
							+" "+Double.valueOf(twoDecimals.format(primaryEnergy))+
							" 1"+"\n");
			
			System.out.println("NPM    fq    "+fq +"    tasksets  "+total_no_tasksets+"  energy  "+ Double.valueOf(twoDecimals.format(primaryEnergy)));

		}

		//writer_primary.close();
		writer_energy.close();

		System.out.println("success NPM");
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


