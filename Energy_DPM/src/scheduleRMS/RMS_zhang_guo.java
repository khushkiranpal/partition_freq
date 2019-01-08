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
public class RMS_zhang_guo{

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
			long hyperperiod_factor, int d,double CRITICAL_TIME,double CRITICAL_freq,double  min_freq,
			boolean faultFromFile,double bcetRatio, long hyper, int n_proc) throws IOException
	{
		System.out.println("STARTING zhang_guo");
		FileTaskReaderTxt reader = new FileTaskReaderTxt(inputFolder+inputfilename); // read taskset from file
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
		Calendar cal = Calendar.getInstance();
		String date = dateFormat.format(cal.getTime());
		String filename= outputFolder+"primaryzhang_guo"+"_"+inputfilename+"_"+date+".txt";
		String filename2= outputFolder+"energyzhang_guo"+"_"+inputfilename+"_"+date+".txt";
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
		writer_energy.write("zhang_guoTASKSET UTILIZATION PRIMARY_ENERGY SUCCESS\n");
		//	SysClockFreq frequency = new SysClockFreq();

		while ((set = reader.nextTaskset()) != null)
		{

			boolean primaryBusy=false;

			boolean deadlineMissed = false;
			Job lastExecutedJob= null;
			ProcessorState proc_state = null;
			int response_zero=0;
			int id = 0;  // idle slot id 
			long time=0 ;
			boolean critical=false;
			long timeToNextArrival=0;
			long endTime = 0; // endtime of job

			long idle = 0;  // idle time counter for processor idle slots
			SchedulabilityCheck schedule = new SchedulabilityCheck();

			Processor primary = new Processor();
			primary.setBusy(false);
			primary.setProc_state(proc_state.SLEEP);


			ISortedQueue queue = new SortedQueuePeriod ();
			queue.addTasks(set);
			ArrayList<ITask> taskset = new ArrayList<ITask>();
			ArrayList<Job> completedJobs = new ArrayList<Job>();
			ArrayList <Long> freq_set_tasks = new ArrayList<Long>();

			taskset = queue.getSortedSet();
			U_SUM= (SystemMetric.utilisation(taskset));
			//	total_no_tasks=total_no_tasks+ tasks.size();
			prioritize(taskset);
			/////////////// HYPER PERIOD////////////
			hyper= 100000;//SystemMetric.hyperPeriod(taskset);
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

			///////////set FREQUENCY OF TASK SET//////////


			double fq=1, LLB_N=1, energy_consumed;
			LLB_N = taskset.size()*(Math.pow(2, ((double)1/(double)(taskset.size())))-1);
			//	System.out.println("llb "+LLB_N +"  "+ (double)Math.pow(2, ((double)1/(double)(taskset.size()))));

			for (ITask t: taskset)
			{
				fq = ((double)t.getWcet()/(double)t.getPeriod());
				t.setFrequency(fq/LLB_N);
			}


			ps.setResponseTime(taskset);    

			long temp=0;
			ISortedJobQueue activeJobQ = new SortedJobQueuePeriod(); // dynamic jobqueue 
			Job j; //job
			TreeSet<Long> activationTimes = new TreeSet<Long>();
			//	TreeSet<Long> promotionTimes = new TreeSet<Long>();


			long nextActivationTime=0;

			long executedTime=0;

			// ACTIVATE ALL TASKS AT TIME 0 INITIALLY IN QUEUE  

			for(ITask t : taskset)  // activate all tasks at time 0
			{
				temp=0;
				j =  t.activateRMS_energy_ExecTime(time);  ////	remainingTime =  (long)ACET;  ////////////////
				//totalPrimaries++;
				j.setPriority(t.getPriority());
				//	System.out.println("t "+j.getTaskId()+" j "+j.getJobId()+" f "+j.getFrequency());
				activeJobQ.addJob(j);  //////ADD TO PRIMARY QUEUE
				j.setCompletionSuccess(false);
				while (temp<=hyper*hyperperiod_factor)
				{
					temp+=t.getPeriod();
					activationTimes.add(temp);

				}

			}

			//	System.out.println("activationTimes  "+activationTimes.size()+"  promotionTimes  "+promotionTimes.size());

			//		writer_primary.write("freq  "+set_fq); 
		//	writer_primary.write("\nTASKID LLB_N U_SUM freq  WCET DEADLINE u_i\n");

			for (ITask t: taskset)
			{
				if(t.getResponseTime()==0)
					response_zero++;

			/*	writer_primary.write("\n"+t.getId()+" "+LLB_N+" "+U_SUM+
						" "+t.getFrequency()+" "+ t.getWcet()+" "+t.getDeadline()+" "+((double)t.getWcet()/(double)t.getPeriod()));
			*/	/* System.out.println("t.getId"+t.getId()+" getACET  "+t.getACET()+" getWCET_orginal "+t.getWCET_orginal()+"  getWcet " +t.getWcet()+" getResponseTime "+t.getResponseTime()+
						  "  prom "+t.getSlack()+" getDeadline  "+t.getDeadline());
				 */	  }

		/*	if(response_zero>0)
				writer_primary.write(" \nResponse time zero "+response_zero);

			writer_primary.write("\nSCHEDULE\nTASKID  JOBID task_freq proc_freq ARRIVAL WCET_or WCET DEADLINE  isPreempted STARTTIME ENDTIME executed remain_or energy\n");// EASSfullBackupsExecuted partialBackupsExecuted fullBackupsCancelled"
*/
			nextActivationTime=  activationTimes.pollFirst();


			//	fq= ps.set_System_Freq(activeJobQ, freq_set_tasks, CRITICAL_freq, fq);
			double sys_freq_temp=0;
			Iterator<Job> itra = activeJobQ.iterator();
			while (itra.hasNext())
			{
				Job j_temp = itra.next();
				if(!freq_set_tasks.contains(j_temp.getTaskId()))
				{
					freq_set_tasks.add(j_temp.getTaskId());
					sys_freq_temp+=j_temp.getFrequency(); 
					//	System.out.println("j_temp "+j_temp.getTaskId()+" sys_freq_temp "+sys_freq_temp);
				}

			}
			if(sys_freq_temp<CRITICAL_freq)
				primary.setFrequency(CRITICAL_freq);
			else if(sys_freq_temp>1)
				primary.setFrequency(1);
			else
				primary.setFrequency(sys_freq_temp);

			itra = activeJobQ.iterator();
			while (itra.hasNext())
			{
				Job j_temp = itra.next();
				j_temp.setExec_frequency(Double.valueOf(twoDecimals.format(primary.getFrequency())));
				
				//	System.out.println("0 while t "+j_temp.getTaskId()+" f "+j_temp.getFrequency());
			}

			////////////start scheduling//////////////////////////////
			while(time<hyper*hyperperiod_factor)
			{

			/*	if(time>500 && time<1000)
				//	System.out.println("time "+time+" f "+primary.getFrequency());
			*/	if( (long)time== (long)nextActivationTime) // AFTER 0 TIME JOB ACTIVAIONS
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
						//	System.out.println("time "+time +" new n"+n.getTaskId());
						}
					}
					
					
					//////////////////PREEMPTION in PRIMARY////////////////////////

					if(time>0 && !activeJobQ.isEmpty() && time==activeJobQ.first().getActivationDate() && current[0]!=null && !current[0].isCompletionSuccess())
					{
					
						if (activeJobQ.first().getPeriod()<current[0].getPeriod())
						{
							// System.out.println("preemption  ");

							
							executedTime = time - current[0].getStartTime();
					/*		if (time>500 && time<1000) {
								 System.out.println("t "+activeJobQ.first().getTaskId()+" activeJobQ.first() ariival  "+activeJobQ.first().getActivationDate());

								System.out.println("t " + current[0].getTaskId() + " j " + current[0].getJobId());
								System.out.println("time  preemption   " + time + " start " + current[0].getStartTime()
										+ "  executedTime  " + executedTime);
								System.out.println("current[0].getRemainingTime() "+current[0].getRemainingTime()+
										" primary.getFrequency() "+primary.getFrequency()+" exec f"+current[0].getExec_frequency());
							}*/
							long remain_time=(long) Math.floor( (current[0].getRemainingTime()-executedTime)*Double.valueOf(twoDecimals.format(current[0].getExec_frequency())));
							
//							if (time>500 && time<1000)
//							System.out.println("remain_time "+remain_time+" f "+primary.getFrequency());
//							
							
												
						//	System.out.println("remaining    "+current[0].getRemainingTime());
						
							if (remain_time>0) {
								primaryBusy = false;
								energy_consumed = energyConsumed.energyActive(executedTime,
										current[0].getExec_frequency());
								primary.setEnergy_consumed(energy_consumed);
								current[0].setRomainingTimeCost(remain_time);
								current[0].isPreempted = true;
						/*		writer_primary.write("\t" + time + "\t " + executedTime + "\t " + remain_time + "\t "
										+ energy_consumed + "\t preempted\n");
				*/				if (current[0].getRemainingTime() > 0)
									activeJobQ.addJob(current[0]);
								// System.out.println("preempted job  "+current[0].getTaskId()+" remaining time "+current[0].getRemainingTime()+ "   wcet "+
								//			current[0].getRomainingTimeCost());
							}
						}
					}
				
					
					
					/////////REVISING FREQUENCY////////////
					/*if(critical)
					{
						freq_set_tasks.clear();
						critical=false;
					}*/
					
					itra = activeJobQ.iterator();
					
					while (itra.hasNext())
					{
						Job j_temp = itra.next();
					//	System.out.println(" time "+time+" activeJobQ "+activeJobQ.size()+" t "+j_temp.getTaskId()+" j "+j_temp.getJobId());
						if(!freq_set_tasks.contains(j_temp.getTaskId()) )
						{
							if( freq_set_tasks.isEmpty())
							{
								primary.setFrequency(j_temp.getFrequency());
									//	+((double)j_temp.getRomainingTimeCost()/(double)j_temp.getPeriod()));//+min_freq);
								freq_set_tasks.add(j_temp.getTaskId());
							}
							else
							{
								
								freq_set_tasks.add(j_temp.getTaskId());
								primary.setFrequency(primary.getFrequency()+j_temp.getFrequency());

							}
							
						/*	if (time>500 && time<1000)
							System.out.println("time "+time+" n while t "+j_temp.getTaskId()+" f "+j_temp.getFrequency()
							+" proc f "+primary.getFrequency());
*/
						}
					}

					if(primary.getFrequency()<CRITICAL_freq)
					{
						primary.setFrequency(CRITICAL_freq);
						critical = true;
					}
					else if(primary.getFrequency()>1)
						primary.setFrequency(1);
					
					itra = activeJobQ.iterator();
					while (itra.hasNext())
					{
						Job j_temp = itra.next();
						j_temp.setExec_frequency(primary.getFrequency());
						
						//	System.out.println("0 while t "+j_temp.getTaskId()+" f "+j_temp.getFrequency());
					}
					
					// when low priority jobs came and change the frequency of currently 
					//running job
					if(current[0]!=null && !current[0].isCompletionSuccess() 
							&& primaryBusy)
					{
						
						executedTime = time - current[0].getStartTime();

					/*	if (time>500 && time<1000) {
							 System.out.println("t "+activeJobQ.first().getTaskId()+" activeJobQ.first() ariival  "+activeJobQ.first().getActivationDate());

							System.out.println("t " + current[0].getTaskId() + " j " + current[0].getJobId());
							System.out.println("time  freq changed   " + time + " start " + current[0].getStartTime()
									+ "  executedTime  " + executedTime);
							System.out.println("current[0].getRemainingTime() "+current[0].getRemainingTime()+
									" primary.getFrequency() "+primary.getFrequency()+" exec f"+current[0].getExec_frequency());
						}*/
						
						long remain_time=(long) Math.floor( (current[0].getRemainingTime()-executedTime)*Double.valueOf(twoDecimals.format(current[0].getExec_frequency())));
						
					/*	if (time>2000)
						System.out.println("remain_time "+remain_time+" f "+primary.getFrequency());
					*/	
						
						if (remain_time>0) {
							energy_consumed= energyConsumed.energyActive(executedTime, 
									current[0].getExec_frequency());
							primary.setEnergy_consumed(energy_consumed);
							
							current[0].setRomainingTimeCost(remain_time);
							primaryBusy = false;
				/*			writer_primary.write("\t" + time + "\t " + executedTime + "\t " + remain_time + "\t "
									+ energy_consumed + "\t freqChanged\n");
				*/			current[0].setExec_frequency(Double.valueOf(twoDecimals.format(primary.getFrequency())));
							if (current[0].getRemainingTime() > 0)
								activeJobQ.addJob(current[0]);
							//	System.out.println("remaining    "+current[0].getRemainingTime());
						}

						
					}
					/*itra = activeJobQ.iterator();
					while (itra.hasNext())
					{
						Job j_temp = itra.next();

						System.out.println("n while t "+j_temp.getTaskId()+" f "+j_temp.getFrequency()
						+" proc f "+primary.getFrequency());
						System.out.println("setting freq activeJobQ "+activeJobQ.size()+" t "+j_temp.getTaskId()+" j "+j_temp.getJobId());

						ps.set_freq_JOB(j_temp, primary.getFrequency());
					}
*/


				} 



			

				// SELECT JOB FROM QUEUE ONLY if processor is free///////
				if ((primaryBusy == false ) )
				{

					j = activeJobQ.pollFirst(); // get the job at the top of queue

					// QUEUE MAY BE EMPTY , SO CHECK IF IT IS  NOT NULL
					if (j!=null && j.isCompletionSuccess()==false)      // if job in queue is null 
					{
						ps.set_freq_JOB(j, Double.valueOf(twoDecimals.format(j.getExec_frequency())));
					
						/*if (time>500 && time<1000)
						{
						System.out.println("time   "+time+"   active   "+primary.getActiveTime());
						System.out.println("j "+j.getTaskId()+" "+j.getJobId()+" j freq  "+j.getFrequency()+
						" proc f  "+primary.getFrequency()	);
					}*/
						//  IDLE SLOTS RECORD
						if (idle!=0)
						{
							if(idle>CRITICAL_TIME && primary.getProc_state()==proc_state.SLEEP)
								energy_consumed= energyConsumed.energySLEEP(idle);
							else
								energy_consumed= energyConsumed.energy_IDLE(idle);

							
							primary.setEnergy_consumed(energy_consumed);
				//			writer_primary.write("  idle_endtime  "+time+"\t\t"+energy_consumed+"\n");
							slot.setLength(idle);  // IF PROCESSOR IS IDLE FROM LONF TIME, RECORD LENGTH OF IDLESLOT
							IdleSlot cloneSlot = (IdleSlot) slot.cloneSlot(); // CLONE THE SLOT
							slots.add(cloneSlot); // ADD THE SLOT TO LIST OR QUEUE
						}

						//RE- INITIALIZE IDLE VARIABLE FOR IDLE SLOTS
						idle =0;   // if job on the queue is not null, initialize  processor idle VARIABLE to 0
						primary.setProc_state(proc_state.ACTIVE);
						current[0]=j;  // TO MAKE IT VISIBLE OUTSIDE BLOCK
						//		System.out.println("current[0]  "+current[0].getTaskId()+" start time "+(long)time);
					/*	writer_primary.write("\nSCHEDULE\nTASKID  JOBID task_freq proc_freq ARRIVAL WCET_or WCET DEADLINE "
								+ " isPreempted STARTTIME ENDTIME executed remain_or energy\n");// EASSfullBackupsExecuted partialBackupsExecuted fullBackupsCancelled"
*/
		/*				writer_primary.write(j.getTaskId()+"\t  "+j.getJobId()+"\t"+Double.valueOf(twoDecimals.format(j.getFrequency()))+"\t"+
						Double.valueOf(twoDecimals.format(primary.getFrequency()))+"\t"+j.getActivationDate()+
								"\t"+j.getRomainingTimeCost()+"\t"+j.getRemainingTime()+"\t"+j.getAbsoluteDeadline()+"\t"+j.isPreempted+"\t\t"+time+"\t");
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

						timeToNextArrival= nextActivationTime-lastExecutedJob.getEndTime(); 
						//	System.out.println("nextActivationTime  "+nextActivationTime+"  lastExecutedJob.getEndTime   "+lastExecutedJob.getEndTime());
						//	System.out.println("time   "+time+"timeToNextArrival   "+timeToNextArrival);

						if (timeToNextArrival<CRITICAL_TIME)
						{
							primary.setFrequency(min_freq);
							primary.setProc_state(proc_state.IDLE);
							primary.idleTime++;  ///-------------------
							if(!freq_set_tasks.isEmpty())
								freq_set_tasks.clear();
						}
						else
						{
							primary.setFrequency(0);
							primary.setProc_state(proc_state.SLEEP);
							primary.sleepTime++;//-------------------
							if(!freq_set_tasks.isEmpty())
								freq_set_tasks.clear();
						}

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
				/*		writer_energy.write("\ndeadline     missed  task id "+j1.getTaskId()+" job id "+j1.getJobId()+
								"  deadline time  "+j1.getAbsoluteDeadline()+"  time "+time+" ");
		*/		/*		writer_primary.write("\ndeadline     missed  task id "+j1.getTaskId()+" job id "+j1.getJobId()+
								"  deadline time  "+j1.getAbsoluteDeadline()+"  time "+time+" ");
*/
						deadlineMissed= true;

					//	writer_primary.close();
					/*	writer_energy.close();

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
					/*	writer_energy.write("\ndeadline     missed   sparetask id "+lastExecutedJob.getTaskId()+" job id "+lastExecutedJob.getJobId()+
								"  deadline time  "+lastExecutedJob.getAbsoluteDeadline()+"  time "+time+" ");
*/
				/*		writer_primary.write("\ndeadline    missed spare task id "+lastExecutedJob.getTaskId()+" job id "+lastExecutedJob.getJobId()+
								"  deadline time  "+lastExecutedJob.getAbsoluteDeadline()+"  time "+time+"  ");

						writer_primary.close();*/
						deadlineMissed= true;
					/*	writer_energy.close();
						System.exit(0);*/
					}
					lastExecutedJob.setEndTime(endTime);  // set endtime of job
					lastExecutedJob.setCompletionSuccess(true);//-------------------
					executedTime=endTime-lastExecutedJob.getStartTime();
					energy_consumed= energyConsumed.energyActive(executedTime, lastExecutedJob.getExec_frequency());
					primary.setEnergy_consumed(energy_consumed);
		//			System.out.println("t "+lastExecutedJob.getTaskId()+"executedTime "+executedTime);
					//writer_primary.write(endTime+"\t "+energy_consumed
			//		writer_primary.write("\t"+endTime+"\t "+executedTime+"\t "+0+"\t "+energy_consumed
								
			//		+"\t"+" endtime \n");

				}

				if (primary.getProc_state()==proc_state.ACTIVE)
					primary.activeTime++;
				time=time+1;
				if (deadlineMissed)
					break;
			}
			System.out.println("primary  active time "+primary.getActiveTime()+"  sleep "+primary.getSleepTime()+"  idle  "+primary.getIdleTime());


			// FOR TOTAL ENERGY COMPARISON NPM*2 ON TWO PROCESSORS
			
			if(deadlineMissed)
			writer_energy.write(total_no_tasksets++ + " "+Double.valueOf(twoDecimals.format(U_SUM))+" "
					+" "+Double.valueOf(twoDecimals.format(primary.getEnergy_consumed()))+
					" 0 "+"\n");
			else
			writer_energy.write(total_no_tasksets++ + " "+Double.valueOf(twoDecimals.format(U_SUM))+" "
						+" "+Double.valueOf(twoDecimals.format(primary.getEnergy_consumed()))+
						" 1 "+"\n");

			System.out.println("rms zhang_guo  fq    "+"    tasksets  "+total_no_tasksets+"  energy  "+ Double.valueOf(twoDecimals.format(primary.getEnergy_consumed())));

		}
//		writer_primary.close();
		writer_energy.close();

		System.out.println("success zhang_guo");
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


