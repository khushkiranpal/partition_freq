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
public class RMS_DPM_FEASIBLE_FREQ_OLD{

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
		System.out.println("STARTING RMS_DPM_FEASIBLE_FREQ");
		FileTaskReaderTxt reader = new FileTaskReaderTxt(inputFolder+inputfilename); // read taskset from file
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
		Calendar cal = Calendar.getInstance();
		String date = dateFormat.format(cal.getTime());
		String filename= outputFolder+"primaryRMS_DPM_FEASIBLE_FREQ"+"_"+inputfilename+"_"+date+".txt";
		String filename2= outputFolder+"energyRMS_DPM_FEASIBLE_FREQ"+"_"+inputfilename+"_"+date+".txt";
	Writer writer_primary = new FileWriter(filename);
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
		writer_energy.write("RMS_DPM_FEASIBLE_FREQTASKSET UTILIZATION FQ PRIMARY_ENERGY SUCCESS fail\n");
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
			hyper= SystemMetric.hyperPeriod(taskset);
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
				t.setFrequency(Double.valueOf(twoDecimals.format(fq/LLB_N)));
			}


			ps.setResponseTime(taskset);    

			long temp=0;
			ISortedJobQueue activeJobQ = new SortedJobQueuePeriod(); // dynamic jobqueue 
			Job j; //job
			TreeSet<Long> activationTimes = new TreeSet<Long>();
			//	TreeSet<Long> promotionTimes = new TreeSet<Long>();


			long nextActivationTime=0;

			long executedTime=0;
		/////SETTING FEASIBLE FREQUENCY/////////////10-12-18
			boolean getFeasibleFreq = true;
			if(getFeasibleFreq)
			{
			writer_primary.write("\nTASKID LLB_N U_SUM freq  WCET DEADLINE u_i RESP f\n");

			
				ArrayList<ITask> tasksetTemp = new ArrayList<ITask>();
				Iterator<ITask> itrTask = taskset.iterator();
				while (itrTask.hasNext())
				{

					ITask tempTask = itrTask.next();
					tasksetTemp.add(tempTask.cloneTask_MWFD_RMS_EEPS());

				}
				double f=0.3;
				while(f<=1)
				{
					Iterator<ITask> itrTaskTemp = tasksetTemp.iterator();
					while (itrTaskTemp.hasNext())
					{

						ITask tempTask = itrTaskTemp.next();
						tempTask.setWcet(tempTask.getWCET_orginal()/f);
						//		System.out.println("tempTask "+tempTask.getWcet());

					}
					
					ps.setResponseTimeForMWFD(tasksetTemp);
					tasksetTemp.sort(new Comparator<ITask>() {
						public int compare(ITask p1, ITask p2) {
							int cmp;
							cmp= (int)(p1.getPeriod()-p2.getPeriod());
							return cmp;
						}
					});
					for(ITask tempT : tasksetTemp)
					{
					//	System.out.println(" t "+ tempT.getId());
						Iterator<ITask> itr_tt = taskset.iterator();
						while(itr_tt.hasNext())
						{
							ITask tt = itr_tt.next();
						//	System.out.println(" TT "+ tt.getId());
							if(tt.getId()==tempT.getId())
							{
							/*	System.out.println("t.getResponseTime()  "+tempT.getResponseTime()  +
										"  tt.getResponseTime()  "+ tt.getResponseTime()
										+" tt.getFeasibleFreq() "+tt.getFeasibleFreq());*/
								if(tempT.getResponseTime()>0 && tt.getFeasibleFreq()==0)
								{
								//	System.out.println(" feasi f "+tt.getFeasibleFreq());
											tt.setFeasibleFreq(f);
								//	System.out.println(" feasi f "+tt.getFeasibleFreq());
								}
								break;
							}
						}
						writer_primary.write("\n"+tempT.getId()+" "+LLB_N+" "+U_SUM+
								" "+tempT.getFrequency()+" "+ tempT.getWcet()+" "+tempT.getDeadline()
								+" "+((double)tempT.getWcet()/(double)tempT.getPeriod())+" "+f+" "+
								tempT.getResponseTime());
					/*	System.out.println("\n"+tempT.getId()+" "+LLB_N+" "+U_SUM+
								" "+tempT.getFrequency()+" "+ tempT.getWcet()+" "+tempT.getDeadline()
								+" "+((double)tempT.getWcet()/(double)tempT.getPeriod())+" "+f+" "+
								tempT.getResponseTime());*/
					writer_primary.write("\t"+ps.calculateMixedResponseTime(tasksetTemp, tempT.getId()));
					}		
					f=f+0.05;
				}
				
				
				///////14-12-18///if response time exceeds f=1
				Iterator<ITask> itr_temp = taskset.iterator();
				while(itr_temp.hasNext())
				{
					ITask tt = itr_temp.next();
					if(tt.getFeasibleFreq()==0)
						tt.setFeasibleFreq(1);
					writer_primary.write("\n"+tt.getId()+" "+LLB_N+" "+U_SUM+
							" "+tt.getFrequency()+" "+ tt.getWcet()+" "+tt.getDeadline()
							+" "+((double)tt.getWcet()/(double)tt.getPeriod())+" "+f+" "+
							tt.getResponseTime());
				}
			
			}
		/////SETTING FEASIBLE FREQUENCY/////////////END 10-12-18
			
			
			// ACTIVATE ALL TASKS AT TIME 0 INITIALLY IN QUEUE  

			for(ITask t : taskset)  // activate all tasks at time 0
			{
				temp=0;
				j =  t.activateRMS_energy_ExecTime(time);  ////	remainingTime =  (long)ACET;  ////////////////
				//totalPrimaries++;
				j.setPriority(t.getPriority());
				j.setFeasibleFreq(t.getFeasibleFreq());
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

				//////	writer_primary.write("freq  "+set_fq); 
			writer_primary.write("\nTASKID LLB_N U_SUM freq  WCET DEADLINE u_i RESP\n");

			for (ITask t: taskset)
			{
				if(t.getResponseTime()==0)
					response_zero++;

				writer_primary.write("\n"+t.getId()+" "+LLB_N+" "+U_SUM+
						" "+t.getFrequency()+" "+ t.getWcet()+" "+t.getDeadline()+
						" "+((double)t.getWcet()/(double)t.getPeriod())+" "+t.getResponseTime()+" "+t.getFeasibleFreq());
				/* System.out.println("t.getId"+t.getId()+" getACET  "+t.getACET()+" getWCET_orginal "+t.getWCET_orginal()+"  getWcet " +t.getWcet()+" getResponseTime "+t.getResponseTime()+
						  "  prom "+t.getSlack()+" getDeadline  "+t.getDeadline());
				 */	 
			}

			if(response_zero>0)
				writer_primary.write(" \nResponse time zero "+response_zero);

			writer_primary.write("\nSCHEDULE\nTASKID  JOBID task_freq proc_freq ARRIVAL WCET_or WCET DEADLINE  isPreempted STARTTIME ENDTIME executed remain_or energy\n");// EASSfullBackupsExecuted partialBackupsExecuted fullBackupsCancelled"

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
				primary.setFrequency(Double.valueOf(twoDecimals.format(discreteFreq(sys_freq_temp))));//15-12-18

			
			
			
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
							n.setFeasibleFreq(t.getFeasibleFreq());
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
								writer_primary.write(time + "\t" + executedTime + "\t" + remain_time + "\t"
										+ energy_consumed + "\t preempted\n");
								if (current[0].getRemainingTime() > 0)
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
					boolean waking = false;//10-12-18
					while (itra.hasNext())
					{
						Job j_temp = itra.next();
						Iterator itrFreq = freq_set_tasks.iterator();
						/*while(itrFreq.hasNext())
						{
							System.out.println("time  "+time +" t "+itrFreq.next());
						}*/
						
					//	System.out.println(" time "+time+" activeJobQ "+activeJobQ.size()+" t "+j_temp.getTaskId()+" j "+j_temp.getJobId());
						if(!freq_set_tasks.contains(j_temp.getTaskId()) )
						{
							if( freq_set_tasks.isEmpty())
							{
								waking = true;
							//	System.out.println("initialize  waking   "+waking);

								primary.setFrequency(Double.valueOf(twoDecimals.format
										(j_temp.getFrequency())));
									//	+((double)j_temp.getRomainingTimeCost()/(double)j_temp.getPeriod()));//+min_freq);
								freq_set_tasks.add(j_temp.getTaskId());
							}
							else
							{
								freq_set_tasks.add(j_temp.getTaskId());
								primary.setFrequency(Double.valueOf(twoDecimals.format(primary.getFrequency()+j_temp.getFrequency())));

					/*			System.out.println("time  "+time+"   t  "+j_temp.getTaskId()+
										"  f  "+j_temp.getFrequency()+" proc f "+primary.getFrequency());
					*//*	//////////FEASIBLE FREQ SETTING ///////////////FOR HEAVY TASKS//////////10-12-18
								if(primary.getFrequency()<j_temp.getFeasibleFreq())
								{
									primary.setFrequency(j_temp.getFeasibleFreq());
									
										System.out.println("time  "+time+"   t  "+j_temp.getTaskId()+
												"  f  "+j_temp.getFrequency()+" proc f "+primary.getFrequency());
										for(ITask temptask: taskset)
										{
											if(temptask.getPeriod()<j_temp.getPeriod())
											{
							//					System.out.println("temptask.getPeriod() "+temptask.getPeriod() +"  j_temp.getPeriod()  "+j_temp.getPeriod());
												if(!freq_set_tasks.contains(temptask.getId()))
												{
													//14-12-18
													if(primary.getFrequency()>temptask.getFeasibleFreq())//14-12-18
													freq_set_tasks.add(temptask.getId());
						//							System.out.println("freq_set_tasks size "+freq_set_tasks.size());
												}
											}
										}
								}
								//////////FEASIBLE FREQ SETTING ///////////////FOR HEAVY TASKS//////////
								*/
							}
							
							
						/*	if (time>500 && time<1000)
							System.out.println("time "+time+" n while t "+j_temp.getTaskId()+" f "+j_temp.getFrequency()
							+" proc f "+primary.getFrequency());
*/
						}
					}
					
					//14-12-18
					itra = activeJobQ.iterator();
					Job j_tempf=null;
					double max_feasibleFreq=0;
					while (itra.hasNext())
					{
						Job j_tempmax = itra.next();
						
						// get max feasible freq in active queue
						if(j_tempmax.getFeasibleFreq()>max_feasibleFreq)
						{
							j_tempf= j_tempmax;
							max_feasibleFreq= j_tempmax.getFeasibleFreq();
							/*if(time>14600)
								System.out.println("time  "+time+"   max_feasibleFreq  "+max_feasibleFreq+"   t  "+j_tempf.getTaskId()+
										"  f  "+j_tempf.getFrequency()+" proc f "+primary.getFrequency());
							*/
						}
						
					}
					//15-12-18
					primary.setFrequency(discreteFreq(primary.getFrequency()));//15-12-18
					
					//////////FEASIBLE FREQ SETTING ///////////////FOR HEAVY TASKS//////////10-12-18
					if(primary.getFrequency()<j_tempf.getFeasibleFreq())
					{
						primary.setFrequency(j_tempf.getFeasibleFreq());
							
						/*	if(time>14600)
							System.out.println("time  "+time+"   t  "+j_tempf.getTaskId()+
									"  f  "+j_tempf.getFrequency()+" proc f "+primary.getFrequency());
						*/	for(ITask temptask: taskset)
							{
								if(temptask.getPeriod()<j_tempf.getPeriod())
								{
				//					System.out.println("temptask.getPeriod() "+temptask.getPeriod() +"  j_temp.getPeriod()  "+j_temp.getPeriod());
									if(!freq_set_tasks.contains(temptask.getId()))
									{
										//14-12-18
										if(primary.getFrequency()>temptask.getFeasibleFreq())//14-12-18
										freq_set_tasks.add(temptask.getId());
			//							System.out.println("freq_set_tasks size "+freq_set_tasks.size());
									}
								}
							}
					}
					
					////end//////FEASIBLE FREQ SETTING ///////////////FOR HEAVY TASKS//////////

					if(primary.getFrequency()<CRITICAL_freq)
					{
						primary.setFrequency(CRITICAL_freq);
						critical = true;
					}
					else if(primary.getFrequency()>1)
						primary.setFrequency(1);
					
			//////////FEASIBLE FREQ SETTING ///////////////FOR HEAVY TASKS////////
					//12-12-18 CORRECTION //IF LOW PRIORITY TASK ARRIVES AT FIRST, CHECK ITS FEASIBLE FREQ
			//		System.out.println("waking   "+waking);
				/*	if(waking)
					{
						Iterator<Job> itrtemp = activeJobQ.iterator();
						Job j_temp1;
						while(itrtemp.hasNext())
						{
							j_temp1= itrtemp.next();
							//get job with max feasible freq in queue
							
							
							
							if(primary.getFrequency()<j_temp1.getFeasibleFreq())
							{
								primary.setFrequency(j_temp1.getFeasibleFreq());

								System.out.println("waking time  "+time+"   t  "+j_temp1.getTaskId()+
										"  f  "+j_temp1.getFrequency()+" proc f "+primary.getFrequency());
								for(ITask temptask: taskset)
								{
									if(temptask.getPeriod()<j_temp1.getPeriod())
									{
						//		System.out.println("temptask.getPeriod() "+temptask.getPeriod() +"  j_temp.getPeriod()  "+j_temp1.getPeriod());
										if(!freq_set_tasks.contains(temptask.getId()))
										{
											if(primary.getFrequency()>temptask.getFeasibleFreq())//14-12-18
											freq_set_tasks.add(temptask.getId());
					//						System.out.println("waking  freq_set_tasks size "+freq_set_tasks.size());

									}
									}
								}
							}

						}


					}*/
		//////////FEASIBLE FREQ SETTING ///////////////FOR HEAVY TASKS//////////
											
					//10-12-18
				//	System.out.println("primary.getFrequency()  "+primary.getFrequency());
					
					//10-12-18 wakeup freq
			/*		if(waking)
					{
						/////////FIND RESPONE TIME OF LOWEST FREQ TASK AT WAKEUP TIME////
						Job minPriJob=null;
						Iterator<Job> itrB = activeJobQ.iterator();
						while (itrB.hasNext())
						{
							long MinPriorityTask =0;
							Job j_tempb = itrB.next();
							if(j_tempb.getPeriod()>MinPriorityTask)
							{
								minPriJob=j_tempb;
								MinPriorityTask= j_tempb.getPeriod();
								//10-12-18			System.out.println("j_tempb  "+j_tempb.getTaskId()+" per "+j_tempb.getPeriod()+"  MinPriorityTask  "+MinPriorityTask);
							}

						}

						ArrayList<ITask> tasksetTemp = new ArrayList<ITask>();
						Iterator<ITask> itrTask = taskset.iterator();
						while (itrTask.hasNext())
						{

							ITask tempTask = itrTask.next();
							tasksetTemp.add(tempTask.cloneTask_MWFD_RMS_EEPS());

						}
						Iterator<ITask> itrTaskTemp = tasksetTemp.iterator();
						while (itrTaskTemp.hasNext())
						{

							ITask tempTask = itrTaskTemp.next();
							tempTask.setWcet(tempTask.getWCET_orginal()/primary.getFrequency());
							//10-12-18			System.out.println("tempTask "+tempTask.getWcet());

						}
						double responseTime = ps.calculateMixedResponseTime(tasksetTemp, minPriJob.getTaskId())
								,freqNew=1;
						long instances = time/minPriJob.getPeriod();
						//10-12-18			System.out.println("instances  "+instances + " time  "+time+" period  "+minPriJob.getPeriod());
						responseTime+=(instances*minPriJob.getPeriod()); 
								System.out.println("responseTime  "+responseTime +"  t "+minPriJob.getTaskId()+
										" dead "+minPriJob.getDeadline() +" abs dead "+ minPriJob.getAbsoluteDeadline());
						if(responseTime>minPriJob.getAbsoluteDeadline())
						{
							//10-12-18			System.out.println("responseTime  "+responseTime);
							freqNew=Double.valueOf(twoDecimals.format(U_SUM/LLB_N));
							//10-12-18			System.out.println("freqNew "+freqNew);
							primary.setFrequency(Double.valueOf(twoDecimals.format(freqNew)));
						}

						/////////END FIND RESPONE TIME OF LOWEST FREQ TASK AT WAKEUP TIME////
												
					}
					
					*/
					
					
					itra = activeJobQ.iterator();
					while (itra.hasNext())
					{
						Job j__temp = itra.next();
						j__temp.setExec_frequency(Double.valueOf(twoDecimals.format(primary.getFrequency())));
						
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
							writer_primary.write("\t" + time + "\t" + executedTime + "\t" + remain_time + "\t"
									+ energy_consumed + "\t freqChanged\n");
							current[0].setExec_frequency(Double.valueOf(twoDecimals.format(primary.getFrequency())));
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
					writer_primary.write("  idle_endtime  "+time+"\t\t"+energy_consumed+"\n");
							slot.setLength(idle);  // IF PROCESSOR IS IDLE FROM LONF TIME, RECORD LENGTH OF IDLESLOT
							IdleSlot cloneSlot = (IdleSlot) slot.cloneSlot(); // CLONE THE SLOT
							slots.add(cloneSlot); // ADD THE SLOT TO LIST OR QUEUE
						}

						//RE- INITIALIZE IDLE VARIABLE FOR IDLE SLOTS
						idle =0;   // if job on the queue is not null, initialize  processor idle VARIABLE to 0
						primary.setProc_state(proc_state.ACTIVE);
						current[0]=j;  // TO MAKE IT VISIBLE OUTSIDE BLOCK
						//		System.out.println("current[0]  "+current[0].getTaskId()+" start time "+(long)time);
					
				writer_primary.write(j.getTaskId()+"\t"+j.getJobId()+"\t"+Double.valueOf(twoDecimals.format(j.getFrequency()))+"\t"+
						Double.valueOf(twoDecimals.format(primary.getFrequency()))+"\t"+j.getActivationDate()+
								"\t"+j.getRomainingTimeCost()+"\t"+j.getRemainingTime()+"\t"+j.getAbsoluteDeadline()+"\t"+j.isPreempted+"\t\t"+time+"\t");


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
						writer_primary.write("\nIDLE SLOT");
							slot.setId(id++); // SET ID OF SLOT
							slot.setStartTime(time);// START TIME OF SLOT
							current[0] = null;
						writer_primary.write("\tstart time\t"+time+"\t");
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
						deadlineMissed= true;
						System.out.println("deadline missed  task id "+j1.getTaskId()+"job id " + j1.getJobId()+
								" activation  "+j1.getActivationDate()+
								"  deadline time  "+j1.getAbsoluteDeadline()+"  time "+time);
						/*		writer_energy.write("\ndeadline     missed  task id "+j1.getTaskId()+" job id "+j1.getJobId()+
								"  deadline time  "+j1.getAbsoluteDeadline()+"  time "+time+" ");
						 */	
					/*	writer_primary.write("\ndeadline     missed  task id "+j1.getTaskId()+" job id "+j1.getJobId()+
								 "  deadline time  "+j1.getAbsoluteDeadline()+"  time "+time+" ");

						 writer_primary.close();
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
						deadlineMissed= true;
						System.out.println("current  deadline missed  spare task id "+lastExecutedJob.getTaskId()+"job id " + lastExecutedJob.getJobId()+"  deadline time  "+lastExecutedJob.getAbsoluteDeadline()+"  time "+time);
						System.out.println("  comp  "+lastExecutedJob.isCompletionSuccess()+"  faulty  "+lastExecutedJob.isFaulty()+"   prom  "+lastExecutedJob.getPromotionTime());
					/*	writer_energy.write("\ndeadline     missed   sparetask id "+lastExecutedJob.getTaskId()+" job id "+lastExecutedJob.getJobId()+
								"  deadline time  "+lastExecutedJob.getAbsoluteDeadline()+"  time "+time+" ");
*/
						/*writer_primary.write("\ndeadline    missed spare task id "+lastExecutedJob.getTaskId()+" job id "+lastExecutedJob.getJobId()+
								"  deadline time  "+lastExecutedJob.getAbsoluteDeadline()+"  time "+time+"  ");

						writer_primary.close();
					
						writer_energy.close();
						System.exit(0);*/
					}
					lastExecutedJob.setEndTime(endTime);  // set endtime of job
					lastExecutedJob.setCompletionSuccess(true);//-------------------
					executedTime=endTime-lastExecutedJob.getStartTime();
					energy_consumed= energyConsumed.energyActive(executedTime, lastExecutedJob.getExec_frequency());
					primary.setEnergy_consumed(energy_consumed);
		//			System.out.println("t "+lastExecutedJob.getTaskId()+"executedTime "+executedTime);
					///////writer_primary.write(endTime+"\t "+energy_consumed
				writer_primary.write(endTime+"\t "+executedTime+"\t "+0+"\t "+energy_consumed
						+"\t"+" endtime \n");

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
						+Double.valueOf(twoDecimals.format(fq	))+" "+Double.valueOf(twoDecimals.format(0))
						+" 0"+" 1"+"\n");
			else
				writer_energy.write(total_no_tasksets++ + " "+Double.valueOf(twoDecimals.format(U_SUM))+" "
						+Double.valueOf(twoDecimals.format(fq	))+" "+Double.valueOf(twoDecimals.format(primary.getEnergy_consumed()))
							+" 1"+" 0"+"\n");
			System.out.println("RMS_DPM_FEASIBLE_FREQ  fq    "+"    tasksets  "+total_no_tasksets+"  energy  "+ Double.valueOf(twoDecimals.format(primary.getEnergy_consumed())));

		}

			writer_primary.close();
			writer_energy.close();

		System.out.println("success RMS_DPM_FEASIBLE_FREQ");
	}

	public static double discreteFreq(double fq)
	{
		
		if(fq>0.95 && fq<=1)
	  		fq=1;
	  	if(fq>0.9 && fq<=0.95)
	  		fq=0.95;
	  	if(fq>0.85 && fq<=0.9)
	  		fq=0.9;
	  	if(fq>0.8 && fq<=0.85)
	  		fq=0.85;
	  	if(fq>0.75 && fq<=0.8)
	  		fq=0.8;
	  	if(fq>0.7 && fq<=0.75)
	  		fq=0.75;
	  	if(fq>0.65 && fq<=0.7)
	  		fq=0.7;
	  	if(fq>0.6 && fq<=.65)
	  		fq=.65;
	  	if(fq>0.55 && fq<=0.6)
	  		fq=0.6;
	  	if(fq>0.5&& fq<=0.55)
	  		fq=0.55;
	  	if(fq>0.45 && fq<=0.5)
	  		fq=0.5;
	  	if(fq>0.4 && fq<=0.45)
	  		fq=0.45;
	  	if(fq>0.35 && fq<=0.4)
	  		fq=0.4;
	  	if(fq>0.3 && fq<=0.35)
	  		fq=0.35;
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


