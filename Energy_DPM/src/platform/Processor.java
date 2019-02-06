/**
 * 
 */
package platform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import queue.ISortedJobQueue;
import queue.SortedJobQueuePeriod;
import taskGeneration.ITask;
import taskGeneration.Job;

/**
 * @author KIRAN
 *
 */
/**
 * @author KIRAN
 *
 */
/**
 * @author KHUSHKIRAN PAL
 *
 */
public class Processor {
	private  long id;
	private boolean busy= false;
	private static long count=0;
	private long idleStartTime ;
	private long idleEndTime;
	private long idleSlotLength;
	public long idleTime=0;
	public long sleepTime=0;
	public long activeTime=0;
	private long endTimeCurrentJob;
	
	private Job currentJob;
	private long nextActivationTime ;
	private long timeToNextArrival;
	private long noOfPriJobs=0;
	private long noOfBackJobs=0;
	private long  totalJobsExecByProc =0;
	public int noOfActiveBackups=0;//for dpm freq setting to 1 //MixedAllocation_DPM_primaryfreq
	// PROCESSOR STATE  ACTIVE  1, 	IDLE   -1, 	SLEEP   0
     private ProcessorState proc_state;
     
     private double energy_consumed=0;
     private double idleEnergy=0;
     private double sleepEnergy=0;
     private double activeEnergy=0;
     private double frequency=1;
     private double workload=0;	
     public boolean opened = false; //WFD PARTITIONING// BIN OPENED OR CLOSED
     
	private ArrayList<Job> jobsExeOnProc = new ArrayList<Job>();
	private ArrayList<Long> tasks= new ArrayList<Long>() ;
	
	private ArrayList<Long> jobs= new ArrayList<Long>();
	private PriorityQueue<Long> startBusyTime = new PriorityQueue<Long>();
	private PriorityQueue<Long> endBusyTime = new PriorityQueue<Long>();
	public ISortedJobQueue pendingJobs = new SortedJobQueuePeriod(); // dynamic jobqueue 

	
	public ArrayList<ITask> taskset = new ArrayList<ITask>();
	public ISortedJobQueue primaryJobQueue = new SortedJobQueuePeriod(); // contains the primary activated jobs
	public ISortedJobQueue readyQueue = new SortedJobQueuePeriod();
	public ArrayList <Long> freq_set_tasks = new ArrayList<Long>();
	
	 private final Comparator<Job> comparator = new Comparator<Job>() {
    	 public int compare(Job j1, Job j2) {
			int cmp =  (int) (j1.getPromotionTime()-j2.getPromotionTime());
			if(cmp==0)
				 cmp =  (int) (j1.getPeriod()-j2.getPeriod());
			if(cmp==0)
				 cmp = 	(int) (j1.getDeadline()-j2.getDeadline());
			if(cmp==0)
			    cmp =  (int) (j1.getAbsoluteDeadline()-j2.getAbsoluteDeadline());
			if(cmp==0)
				cmp= (int) (j2.getRemainingTime() - j1.getRemainingTime());// due to least  laxity of job having larger execution time 
			if (cmp==0)														// if D=9, R1=2, R2 = 5, so R2 must have higher priority having laxity
				cmp= (int)(j1.getTaskId()-j2.getTaskId());					// larger 9-5=4 than 9-2=7.
			return cmp;
		}
	  };
	  public ISortedJobQueue backupJobQueue = new SortedJobQueuePeriod(comparator);// contains the secondary activated jobs
	
	  
	  public ArrayList<Integer> fault = new ArrayList<Integer>();
	/**
	 * 
	 */
	public Processor() {
		this.id = ++count;
		this.busy = false;
		this.idleSlotLength=0;
	//	this.jobsExeOnProc = null;
		
		
	}

	/**
	 * @param id
	 * @param busy
	 */
	public Processor(int id, boolean busy) {
		this.id = id;
		this.busy = busy;
		//this.jobsExeOnProc = null;
	}
	
	
	
	

	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	/**
	 * @return the noOfPriJobs
	 */
	public long getNoOfPriJobs() {
		return noOfPriJobs;
	}

	/**
	 * @param noOfPriJobs the noOfPriJobs to set
	 */
	public void setNoOfPriJobs(long noOfPriJobs) {
		this.noOfPriJobs = noOfPriJobs;
	}

	/**
	 * @return the noOfBackJobs
	 */
	public long getNoOfBackJobs() {
		return noOfBackJobs;
	}

	/**
	 * @param noOfBackJobs the noOfBackJobs to set
	 */
	public void setNoOfBackJobs(long noOfBackJobs) {
		this.noOfBackJobs = noOfBackJobs;
	}

	/**
	 * @return the totalJobsExecByProc
	 */
	public long getTotalJobsExecByProc() {
		return totalJobsExecByProc;
	}

	/**
	 * @param totalJobsExecByProc the totalJobsExecByProc to set
	 */
	public void setTotalJobsExecByProc(long totalJobsExecByProc) {
		this.totalJobsExecByProc = totalJobsExecByProc;
	}

	/**
	 * @return the timeToNextArrival
	 */
	public long getTimeToNextArrival() {
		return timeToNextArrival;
	}

	/**
	 * @param timeToNextArrival the timeToNextArrival to set
	 */
	public void setTimeToNextArrival(long timeToNextArrival) {
		this.timeToNextArrival = timeToNextArrival;
	}

	/**
	 * @return the fault
	 */
	public ArrayList<Integer> getFault() {
		return fault;
	}

	/**
	 * @param fault the fault to set
	 */
	public void setFault(ArrayList<Integer> fault) {
		this.fault = fault;
	}

	/**
	 * @return the nextActivationTime
	 */
	public long getNextActivationTime() {
		return nextActivationTime;
	}

	/**
	 * @param nextActivationTime the nextActivationTime to set
	 */
	public void setNextActivationTime(long time) {
		 ArrayList<ITask> primaryTaskset = new ArrayList<ITask>();
		 ArrayList<ITask> backupTaskset = new ArrayList<ITask>();
		 long minActiv= Long.MAX_VALUE,temp;
		 long minProm= Long.MAX_VALUE;
		 for(ITask t : taskset)
		 {
			 if(t.isPrimary())
				 primaryTaskset.add(t);
			 else
				 backupTaskset.add(t);
		 }
		 for(ITask t : primaryTaskset)
		 {
		     temp = ((long) Math.floor(((double)time/(double)t.getPeriod()))+1)*t.getPeriod();
			 if(temp<minActiv)
				 minActiv=temp;
			// System.out.println("primaryTaskset    task  "+t.getId()+"  minActiv "+minActiv+"  temp  "+temp);
		 }
		 for(ITask t : backupTaskset)
		 {
			 if (time<t.getSlack())
				 temp = (long) (((long) Math.floor(((double)time/(double)t.getPeriod())))*t.getPeriod()+t.getSlack());
			 else
				 temp = (long) (((long) Math.floor(((double)time/(double)t.getPeriod()))+1)*t.getPeriod()+t.getSlack());
			 if(temp<minProm)
				 minProm=temp;
		//	 System.out.println("backupTaskset   task  "+t.getId()+"   minProm  "+minProm +"  temp  "+temp);
		 }
		 
		 this.nextActivationTime = Math.min(minProm, minActiv);
	//	 System.out.println("  next Activation/promotion  Time of processor  "+this.nextActivationTime);
				 
		
	}

	/**
	 * @return the endTimeCurrentJob
	 */
	public long getEndTimeCurrentJob() {
		return endTimeCurrentJob;
	}

	/**
	 * @param endTimeCurrentJob the endTimeCurrentJob to set
	 */
	public void setEndTimeCurrentJob(long endTimeCurrentJob) {
		this.endTimeCurrentJob = endTimeCurrentJob;
	}

	/**
	 * @return the taskset
	 */
	public ArrayList<ITask> getTaskset() {
		return taskset;
	}

	/**
	 * @param taskset the taskset to set
	 */
	public void setTaskset(ArrayList<ITask> taskset) {
		this.taskset = taskset;
	}

	/**
	 * @return the primaryJobQueue
	 */
	public ISortedJobQueue getPendingJobs() {
		return primaryJobQueue;
	}

	/**
	 * @param primaryJobQueue the primaryJobQueue to set
	 */
	public void setPendingJobs(ISortedJobQueue pendingJobs) {
		this.primaryJobQueue = pendingJobs;
	}

	
	
	/**
	 * @return the currentJob
	 */
	public Job getCurrentJob() {
		return currentJob;
	}

	/**
	 * @param currentJob the currentJob to set
	 */
	public void setCurrentJob(Job currentJob) {
		this.currentJob = currentJob;
	}

	/**
	 * @return the workload
	 */
	public double getWorkload() {
		return workload;
	}

	/**
	 * @param workload the workload to set
	 */
	public void setWorkload(double workload) {
		this.workload = workload;
	}

	
	/**
	 * @return the idleTime
	 */
	public long getIdleTime() {
		return idleTime;
	}

	/**
	 * @param idleTime the idleTime to set
	 */
	public void setIdleTime(long idleTime) {
		this.idleTime = idleTime;
	}

	/**
	 * @return the sleepTime
	 */
	public long getSleepTime() {
		return sleepTime;
	}

	/**
	 * @param sleepTime the sleepTime to set
	 */
	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	/**
	 * @return the activeTime
	 */
	public long getActiveTime() {
		return activeTime;
	}

	/**
	 * @param activeTime the activeTime to set
	 */
	public void setActiveTime(long activeTime) {
		this.activeTime = activeTime;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the busy
	 */
	public boolean isBusy() {
		return busy;
	}
	/**
	 * @param busy the busy to set
	 */
	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	
	/**
	 * @return the jobsExeOnProc
	 */
	public ArrayList<Job> getjobsExeOnProc() {
		return jobsExeOnProc;
	}
	
	public void addJob(Job job)
	{
		jobsExeOnProc.add(job);
	}
	
	/**add the end time of job currently executing
	 * @param time
	 */
	public void addStartBusyTime(long time)
	{
		startBusyTime.add(time);
	}

	/** add the end time of job currently executing
	 * @param time
	 */
	public void addEndBusyTime(long time)
	{
		endBusyTime.add(time);
	}

	/** start times of jobs executed on the given processor
	 * @return the startBusyTime
	 */
	public PriorityQueue<Long> getStartBusyTime() {
		return startBusyTime;
	}

	/**end times of jobs executed on the given processor
	 * @return the endBusyTime
	 */
	public PriorityQueue<Long> getEndBusyTime() {
		return endBusyTime;
	}
	/**
	 * @return the tasks
	 */
	public ArrayList<Long> getTasks() {
		return tasks;
	}

	/**
	 * @param tasks the tasks to set
	 */
	public void setTasks(ArrayList<Long> tasks) {
		this.tasks = tasks;
	}

	
	/**add task no.
	 * @param task
	 */
	public void addTasks(Long task) {
		 tasks.add(task);
	}
	
	/**add task no.
	 * @param task
	 */
	public void addJobs(Long job) {
		 jobs.add(job);
	}
	
	/**
	 * @return the jobs
	 */
	public ArrayList<Long> getJobs() {
		return jobs;
	}

	/**
	 * @param jobs the jobs to set
	 */
	public void setJobs(ArrayList<Long> jobs) {
		this.jobs = jobs;
	}

	public Processor clone()
	{
		return this.clone();
	}
	
	/**
	 * @return the idleStartTime
	 */
	public long getIdleStartTime() {
		return idleStartTime;
	}

	/**
	 * @param idleStartTime the idleStartTime to set
	 */
	public void setIdleStartTime(long idleStartTime) {
		this.idleStartTime = idleStartTime;
	}

	/**
	 * @return the idleEndTime
	 */
	public long getIdleEndTime() {
		return idleEndTime;
	}

	/**
	 * @param idleEndTime the idleEndTime to set
	 */
	public void setIdleEndTime(long idleEndTime) {
		this.idleEndTime = idleEndTime;
	}

	/**
	 * @return the idleSlotLength
	 */
	public long getIdleSlotLength() {
		return idleSlotLength;
	}

	/**
	 * @param idleSlotLength the idleSlotLength to set
	 */
	public void setIdleSlotLength(long idleSlotLength) {
		this.idleSlotLength = idleSlotLength;
	}

	/**
	 * @return the energy_consumed
	 */
	public double getEnergy_consumed() {
		return energy_consumed;
	}

	/**
	 * @param energy_consumed the energy_consumed to set
	 */
	public void setEnergy_consumed(double energy_consumed) {
		this.energy_consumed += energy_consumed;
	}

	/**
	 * @return the idleEnergy
	 */
	public double getIdleEnergy() {
		return idleEnergy;
	}

	/**
	 * @param idleEnergy the idleEnergy to set
	 */
	public void setIdleEnergy(double idleEnergy) {
		this.idleEnergy += idleEnergy;
	}

	/**
	 * @return the sleepEnergy
	 */
	public double getSleepEnergy() {
		return sleepEnergy;
	}

	/**
	 * @param sleepEnergy the sleepEnergy to set
	 */
	public void setSleepEnergy(double sleepEnergy) {
		this.sleepEnergy += sleepEnergy;
	}

	/**
	 * @return the activeEnergy
	 */
	public double getActiveEnergy() {
		return activeEnergy;
	}

	/**
	 * @param activeEnergy the activeEnergy to set
	 */
	public void setActiveEnergy(double activeEnergy) {
		this.activeEnergy += activeEnergy;
	//	System.out.println(" id "+ this.getId()+" active   "+activeEnergy );
	}

	/**
	 * @return the proc_state
	 */
	public ProcessorState getProc_state() {
		return proc_state;
	}

	/**
	 * @param proc_state the proc_state to set
	 */
	public void setProc_state(ProcessorState proc_state) {
		this.proc_state = proc_state;
	}

	
	
	
}
