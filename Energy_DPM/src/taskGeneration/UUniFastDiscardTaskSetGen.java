/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package taskGeneration;

import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author KIRAN
 */
public class UUniFastDiscardTaskSetGen {
    private final long MAX_PERIOD;
	private final Random random = new Random();
//	private final  ExponentialDistribution exp = new ExponentialDistribution(0.4);
	private final ITaskGenerator taskGenerator;
	private final int nbTask;
	private final double utilization;
	private final int deadlineModel;
	private long hyperperiod_factor;
	
	public UUniFastDiscardTaskSetGen(ITaskGenerator taskGen, int nbTask,
			 double utilization, int deadlineModel, long mAX_PERIOD) {

		if (utilization > 1) {
			throw new IllegalArgumentException(
					"Utilization must be less than or equal to 1.");
		}
		
		this.taskGenerator = taskGen;
		this.nbTask = nbTask;
		this.utilization = utilization;
		this.deadlineModel = deadlineModel;
		this.MAX_PERIOD = mAX_PERIOD;
		}
	
	
	public UUniFastDiscardTaskSetGen(ITaskGenerator taskGen, int nbTask,
			 double utilization, int deadlineModel, long mAX_PERIOD, long hyperperiod_factor) {

		if (utilization > 1) {
			throw new IllegalArgumentException(
					"Utilization must be less than or equal to 1.");
		}
		
		this.taskGenerator = taskGen;
		this.nbTask = nbTask;
		this.utilization = utilization;
		this.deadlineModel = deadlineModel;
		this.MAX_PERIOD = mAX_PERIOD;
		this.hyperperiod_factor= hyperperiod_factor;
		}
	
	
	public UUniFastDiscardTaskSetGen(ITaskGenerator taskGen, int nbTask,
			 double utilization, int deadlineModel, long mAX_PERIOD, long hyperperiod_factor, int m) {

	//	System.out.println("UUniFastDiscardTaskSetGen");
		
		this.taskGenerator = taskGen;
		this.nbTask = nbTask;
		this.utilization = utilization;
		this.deadlineModel = deadlineModel;
		this.MAX_PERIOD = mAX_PERIOD;
		this.hyperperiod_factor= hyperperiod_factor;
		}
	
	
        public ITask[] generate() {
		ITask[] taskset = new ITask[nbTask];
                double[] util;

			util = generateUtilizations();
			for (int i = 0; i < util.length; i++) {
				taskset[i] = taskGenerator.generate(util[i], deadlineModel,MAX_PERIOD);
		// for hyperperiod factor to implement DVS in decimal values
		//		taskset[i] = taskGenerator.generate(util[i], deadlineModel,MAX_PERIOD,hyperperiod_factor);
	                                 
			}
			// taskGenerator.finalizeTaskset(taskset, nbProc);
			return taskset;
        }
        // UNIPROCESSOR
        public ITask[] generate(long hyperperiod_factor, long MAX_PERIOD, long maxHyperPeriod) {
    		ITask[] taskset = new ITask[nbTask];
                    double[] util;
                    boolean duplicate=false;
            //        System.out.println(" UUniFastDiscardTaskSetGen generate");
    			util = generateUtilizations();
    			 long[] taskPeriods;
     			taskPeriods = generatePeriods(MAX_PERIOD, 100000);
     			for (int i = 0; i < taskPeriods.length; i++) {
     		//System.out.println(" period "+taskPeriods[i]);
     			}
     			
    			for (int i = 0; i < util.length; i++) {
    				//taskset[i] = taskGenerator.generate(util[i], deadlineModel,MAX_PERIOD);
    		// for hyperperiod factor to implement DVS in decimal values
    				taskset[i] = taskGenerator.generate(util[i], deadlineModel,MAX_PERIOD,hyperperiod_factor);
    				// for LCM<100000
    				//taskset[i] = taskGenerator.generateP(util[i], deadlineModel,taskPeriods[i],hyperperiod_factor);
           	        
    				duplicate=false;	
    				if (utilization>0.7 && i>0)
    				{
    					for(int j = i-1; j>=0;j--)
    					{
    						
    					//	System.out.println("j "+j+" i " +i);
    						if(taskset[i].getPeriod()==taskset[j].getPeriod())
    						{
    							System.out.println(" duplicate "+duplicate+"taskset[i].getPeriod() "+taskset[i].getPeriod()+
    									"  taskset[j].getPeriod()  "+ taskset[j].getPeriod());
    					
    							/*duplicate=true;
    							break;*/
    						}
    						
    							}
    				}
    				if(duplicate)
    				{	
    					i--;
    					continue;}
    				
    				//           System.out.println(" UUniFastDiscardTaskSetGen generate");           
    			}
    			// taskGenerator.finalizeTaskset(taskset, nbProc);
    			return taskset;
            }
        
        //for multiprocessor
        public ITask[] generate(long hyperperiod_factor, double Utotal, long MAX_PERIOD, long maxHyperPeriod) {
    		ITask[] taskset = new ITask[nbTask];
    		double  total_U=0.0;int size=0;
    		 boolean duplicate=false;
                    double[] util;
             //       System.out.println(" UUniFastDiscardTaskSetGen generate");
    			util = generateUtilizations(Utotal);
    		//////GENERATE PERIODS
    			long[] taskPeriods;
    			taskPeriods = generatePeriods(MAX_PERIOD, 100000);
    			for (int i = 0; i < taskPeriods.length; i++) {
    			//	System.out.println(" period "+taskPeriods[i]);
    			}
    			// GENERATE TASKSET
    			for (int i = 0; i < util.length; i++) {
    				//taskset[i] = taskGenerator.generate(util[i], deadlineModel,MAX_PERIOD);
    		// for hyperperiod factor to implement DVS in decimal values
    			//	taskset[i] = taskGenerator.generate(util[i], deadlineModel,MAX_PERIOD,hyperperiod_factor);
    				taskset[i] = taskGenerator.generateP(util[i], deadlineModel,taskPeriods[i],hyperperiod_factor);
    				total_U+=(double)taskset[i].getWcet()/(double)taskset[i].getPeriod();
    				size=i;
    						
    			//	System.out.println("i "+i+"  total_U  "+total_U+" size "+size);
    				if(total_U>Utotal)
    					break;
    				/*duplicate=false;	
    				if (utilization>0.7 && i>0)
    				{
    					for(int j = i-1; j>=0;j--)
    					{
    						
    					//	System.out.println("j "+j+" i " +i);
    						if(taskset[i].getPeriod()==taskset[j].getPeriod())
    						{
    							System.out.println(" duplicate "+duplicate+"taskset[i].getPeriod() "+taskset[i].getPeriod()+
    									"  taskset[j].getPeriod()  "+ taskset[j].getPeriod());
    					
    							duplicate=true;
    							break;
    						}
    						
    							}
    				}
    				if(duplicate)
    				{	
    					i--;
    					continue;}
    				
    				//           System.out.println(" UUniFastDiscardTaskSetGen generate");           
    			
    				//     System.out.println(" UUniFastDiscardTaskSetGen generate");           
    			*/}
    			// taskGenerator.finalizeTaskset(taskset, nbProc);
    			taskset= Arrays.copyOf(taskset,size);
    			return taskset;
            }
        
        private long[] generatePeriods(long maxperiod, long hyper) {
    		long[] periods = new long[nbTask];
    		long[] temp_periods = new long[10];
    		long LCM;
    		Random random = new Random();
    		long period=0;
    		int loop=0;

    		do {   			 
    			// IF NUMBER OF tasks are less than 10
    			if (nbTask<=10) {
					for (int i = 0; i < nbTask; i++) {
						period = nextInt(10, (int) maxperiod);

						periods[i] = period;
						//	System.out.println(" nbTask "+nbTask+"  period  "+period+ "  periods[i] "+periods[i]);
					} 
				}
    			else// IF NUMBER OF tasks are greater than 10
    			{
    				do {
						// first generate 10 periods with lcm <100000
						for (int i = 0; i < 10; i++) {
							period = nextInt(10, (int) maxperiod);

							temp_periods[i] = period;
						//	System.out.println(" nbTask "+nbTask+"  period  "+period+ "  temp_periods[i] "+temp_periods[i]);
						}
						LCM = SystemMetric.lcm(period, temp_periods);
					} while (LCM>hyper || LCM<0);
    				
    				// THAN WITH THE HELP OF UPPER GENERATED PERIODS ,
    				// TAKE ONE NUMBER AT A TIME AND FIND ITS FACTORS FOR FURTHER GENERATION PF PERIODS
    				
    				for(int i=0; i<temp_periods.length; i++)
    				{
    					periods[i] = temp_periods[i];
    				//	System.out.println("periods[i] " + periods[i]+" size "+ periods.length);
    				}
    				int count = 0,i=1, counter=10;
    				do
    				{
    					i=1;
    					int Number = (int)periods[count] ;
    					while( i <= Number && counter<nbTask) {// FACTOR OF NUMBER
    						if(Number % i == 0 && i>10 && i<100) {
    						//	System.out.println("Number  "+Number +"  i "+i +"  counter "+counter);
    							// ADD NEW PERIOD = i, 10<i<100
    							periods[counter]=i;
    							counter++;
    						}
    						i++;
    					}
    					count++;  // next number2 value in period array
    				//	System.out.println("count  "+count+" counter "+counter);
    					
    				}while (counter<nbTask);
    				
    				
    			}
    			period= periods[nbTask-1];
				LCM= SystemMetric.lcm(period,periods);
    			//System.out.println("  LCM "+LCM );
    			
    		}	while(LCM>hyper || LCM<0);
    		
    		return periods;

    		
        }
        
        protected int nextInt(int from, int to) {
    		return from + random.nextInt(to - from + 1);
                    }
        
        // uniprocessor
        private double[] generateUtilizations() {
		double[] util = new double
				[nbTask];
		double nextSumU;
		boolean discard;
		double temp = 0;
		do {
			double sumU = utilization;
			discard = false;
			for (int i = 0; i < nbTask - 1; i++) {
			//random uti
					nextSumU = sumU	* Math.pow(random.nextDouble(), (double) 1/ (nbTask - (i + 1)));
			//exponentional uti
		//		nextSumU = sumU	* Math.pow(exp.sample(), (double) 1/ (nbTask - (i + 1)));
				util[i] = sumU - nextSumU;
				temp+=util[i];
		//		System.out.println("util[i]   "+util[i] + "  temp  "+(temp));
			
				sumU = nextSumU;
				if (util[i] > 1) {
					discard = true;
				}
			}
			util[nbTask - 1] = sumU;
			if (util[nbTask - 1] > 1) {
				discard = true;
			}
		} while (discard || !utilizationIsValid(util));
		return util;
	}
        
        
	private boolean utilizationIsValid(double[] util) {
		double sum = 0;

		for (double u : util) {
			sum += u;
		}
		return (sum <= 1) ? true : false;
	}
	
	// multiprocessor
	  private double[] generateUtilizations(double Utotal) {
			double[] util = new double
					[nbTask];
			double nextSumU;
			boolean discard;
			double temp = 0;
			do {
				temp=0;
				double sumU = utilization;
				discard = false;
				for (int i = 0; i < nbTask - 1; i++) {
				//random uti
						nextSumU = sumU	* Math.pow(random.nextDouble(), (double) 1/ (nbTask - (i + 1)));
				//exponentional uti
			//		nextSumU = sumU	* Math.pow(exp.sample(), (double) 1/ (nbTask - (i + 1)));
					util[i] = sumU - nextSumU;
					/*if(util[i]>0.4)
					{
						i--;
						System.out.println("util[i] "+util[i] +" i "+i );
						continue;
					}*/
					temp+=util[i];
					
					sumU = nextSumU;
					if (util[i] > 0.3) {//19-12-18util[i] > 0.5 for RMS task_u must be less than 0.7
						discard = true;
					}
				//	System.out.println("discard  "+discard +"i  "+i+"  util[i]   "+util[i] + "  temp  "+(temp));
					
					
				}
				util[nbTask - 1] = sumU;
				if (util[nbTask - 1] > 1) {
					discard = true;
				}
			} while (discard || !utilizationIsValid(util,Utotal));
			return util;
		}
	        
	        
		private boolean utilizationIsValid(double[] util, double Utotal) {
			double sum = 0;

			for (double u : util) {
				sum += u;
			}
			return (sum <= Utotal) ? true : false;
		}
}
