package energy;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.math3.distribution.NormalDistribution;

import queue.ISortedJobQueue;
import taskGeneration.ITask;
import taskGeneration.Job;

public class ParameterSetting {
	 DecimalFormat twoDecimals = new DecimalFormat("#.##");
	
	 public void set_freq(ArrayList<ITask> taskset,double frequency) // USED BY MixedConstantBackFreq_fq___06_12_19
	{
		for (ITask t: taskset)
		{
			t.setFrequency(frequency);
		//	t.setWcet( Double.valueOf(twoDecimals.format((double)t.getWcet()/frequency)));

			t.setWcet( Double.valueOf(twoDecimals.format((double)t.getWCET_orginal()/frequency)));
			//System.out.println("wcet  "+t.getWcet());
			t.setBCET(t.getBest_CET()/frequency);
		//	System.out.println("bcet   "+t.getBCET());

			t.setACET(t.getAverage_CET()/frequency);
		//	System.out.println("acet   "+t.getACET()+"  avg acet  "  +t.getAverage_CET());
			/*t.setWCET_orginal(t.getC()*1000);
			t.setPeriod(t.getT()*1000);
			t.setDeadline(t.getD()*1000);
			*/
			
			//t.setBCET(t.getBest_CET()*1000);
			//t.setACET(t.getAverage_CET()*1000);
		}
	}
	 
	 
	 public void set_freq_MixedAlloc(ArrayList<ITask> taskset,double frequency)
		{
			for (ITask t: taskset)
			{
				if (t.isPrimary())  //only primary copies 
				{
				t.setFrequency(frequency);
			//	t.setWcet( Double.valueOf(twoDecimals.format((double)t.getWcet()/frequency)));
				}
				else
					t.setFrequency(1);
				t.setWcet( Double.valueOf(twoDecimals.format((double)t.getWCET_orginal()/t.getFrequency())));
		//	System.out.println("frequency    "+frequency+"   task  "+t.getId()+"   wcet  "+t.getWcet());
				t.setBCET(t.getBest_CET()/t.getFrequency());
			//	System.out.println("bcet   "+t.getBCET());

				t.setACET(t.getAverage_CET()/t.getFrequency());
			
				
			}
		}
	 
	 
	 public void set_freq_JOB(Job t, double frequency)
		{
		
			//	frequency = ((double)t.getRemainingTime()/(double)t.getPeriod());
			//	t.setFrequency(frequency);
			//	t.setWcet( Double.valueOf(twoDecimals.format((double)t.getWcet()/frequency)));
			//	System.out.println("set_freq_JOB t "+t.getTaskId()+" freq   "+t.getFrequency());
		// 		System.out.println(" t "+t.getTaskId()+"before wcet  "+t.getRemainingTime());
		 		t.setRemainingTime((long)((double)t.getRomainingTimeCost()/frequency));
		//		System.out.println("after wcet  "+t.getRemainingTime());
				t.setBCET(t.getBest_CET()/frequency);
			//	System.out.println("bcet   "+t.getBCET());

				t.setACET(t.getAverage_CET()/frequency);
			//	System.out.println("acet   "+t.getACET()+"  avg acet  "  +t.getAverage_CET());
				
		}
	 
	
	 public double set_System_Freq(ISortedJobQueue activeJobQ,ArrayList <Long> freq_set_tasks , double  CRITICAL_freq,double sys_freq )
		{
		 double sys_freq_temp=0;
		 Job j;
		 Iterator<Job> itra = activeJobQ.iterator();
			while (itra.hasNext())
			{
				j= itra.next();
				//System.out.println(" t "+itra+" f "+itra.next().getFrequency());
				if(!freq_set_tasks.contains(j.getTaskId()))
				{
					freq_set_tasks.add(j.getTaskId());
					sys_freq_temp+=j.getFrequency(); 
				}
				}
			if(sys_freq_temp<CRITICAL_freq)
				sys_freq=CRITICAL_freq;
			else if(sys_freq_temp>1)
				sys_freq=1;
			else
				sys_freq= sys_freq_temp;
		return sys_freq;
	 
		 
		}
	 
	 
	
	public void setBCET(ArrayList<ITask> taskset, double ratio)
	{
		for (ITask t: taskset)
		{
		//	t.setBCET( Double.valueOf(twoDecimals.format(t.getC()*ratio)));
			t.setBCET( Double.valueOf(twoDecimals.format(t.getWCET_orginal()*ratio)));
		//	System.out.println("bcet   "+t.getBCET());
			
			t.setBest_CET(t.getBCET());
		//	t.setBCET(t.getBest_CET()*1000);
		//	t.setBCET(t.getBest_CET());

		}
	}
	
	public void setACET(ArrayList<ITask> taskset)
	{
		double mean , variance , standardDev, acet;
	//	NormalDistribution normal = 	new NormalDistribution();
		for (ITask t: taskset)
		{
			mean = (t.getWCET_orginal()+t.getBCET())/2;
			variance = (t.getWCET_orginal()-t.getBCET())/6;
			standardDev = Math.sqrt(variance);
			NormalDistribution normal = 	new NormalDistribution(mean, standardDev);
			acet = normal.sample();
		//	System.out.println("task  "+t.getId()+"  acet calculated   "+acet);
			
			if (acet<1)
				acet=1;
		//	System.out.println("acet changed   "+acet);
			
			t.setACET( Double.valueOf(twoDecimals.format(acet)));
			
			t.setAverage_CET(t.getACET());
		//	t.setACET(t.getAverage_CET());
		}
		
	}
	
	public void setACET_TEMP(ArrayList<ITask> taskset)
	{
		double acet;
		for (ITask t: taskset)
		{
			
			acet = t.getWcet()*0.75;
		//	System.out.println("task  "+t.getId()+"  acet calculated   "+acet);
			
			if (acet<1)
				acet=1;
		//	System.out.println("acet changed   "+acet);
			
			t.setACET( Double.valueOf(twoDecimals.format(acet)));
			
			t.setAverage_CET(t.getACET());
		//	t.setACET(t.getAverage_CET());
		}
		
	}
	
	public void setParameterDouble(ArrayList<ITask> taskset)
	{
	
		for (ITask t: taskset)
		{
			t.setWcet(t.getWcet()*1000);
	//		System.out.println(" 1000 wcet  "+t.getWcet());
			t.setWCET_orginal(t.getC()*1000);
			t.setPeriod(t.getT()*1000);
			t.setDeadline(t.getD()*1000);
			t.setACET(t.getACET()*1000);
			t.setBCET(t.getBCET()*1000);
			t.setBest_CET(t.getBest_CET()*1000);
			t.setAverage_CET(t.getAverage_CET()*1000);
		}
	}
	
	/**
	 * @param taskset
	 */
	
	
	public void setResponseTimeOVERLOADING(ArrayList<ITask> taskset)
	{
		taskset.sort(new Comparator<ITask>() {
			public int compare(ITask p1, ITask p2) {
				int cmp;
				cmp= (int)(p1.getPeriod()-p2.getPeriod());
				return cmp;
			}
		});	
		
		double load=0, interference=0;
		for(ITask t:taskset)
      {
	//	System.out.println("task i "+t.getId()+" wcet  "+t.getWCET_orginal());
            double w=t.getWCET_orginal(),w1=w-1;
            while(w != w1)
            {
                w1 = w;
                w =t.getWCET_orginal();
                for(int i=0; taskset.get(i) != t; i++)
                {

                	if (taskset.get(i).getPeriod()==t.getPeriod())
                		load =1;// ((1-(double)((double)(taskset.get(i).getPeriod()-1)/(double)t.getPeriod()))+
                				//((double)taskset.get(i).getWCET_orginal()/(double)taskset.get(i).getPeriod()));                     		
                	else
                		load = ((1-(double)((double)taskset.get(i).getPeriod()/(double)t.getPeriod()))+
                				((double)taskset.get(i).getWCET_orginal()/(double)taskset.get(i).getPeriod()));         	


                	if(load>1)
                		load=1;
                	interference= Math.ceil(load*(double)taskset.get(i).getWCET_orginal());

                	/*load  =(Math.ceil((1-(double)((double)taskset.get(i).getPeriod()/(double)t.getPeriod()))*
                    		taskset.get(i).getWCET_orginal()));*/
                /*	System.out.println("tj  "+taskset.get(i).getPeriod()+"   ti  "+t.getPeriod()+
                			"   cj   "+taskset.get(i).getWCET_orginal());
                	System.out.println("tj/ti    "+((double)((double)taskset.get(i).getPeriod()/(double)t.getPeriod()))+
                			"   1-tj/ti  "+(1-((double)((double)taskset.get(i).getPeriod()/(double)t.getPeriod())))
                			+             		" load  "+(Math.ceil((1-(double)((double)taskset.get(i).getPeriod()/(double)t.getPeriod()))*
                					taskset.get(i).getWCET_orginal())));
                	System.out.println("load   "+load+"  interference  "+interference);*/
                	w += (int) (Math.ceil((double) w1/(double)taskset.get(i).getPeriod())*interference);

                	//	w += (int) (Math.ceil((double) w1/taskset.get(i).getPeriod())*taskset.get(i).getWCET_orginal());
            //    	System.out.println("task j "+taskset.get(i).getId()+"response time  "+w);
                }
            }
            if( w > t.getDeadline())
             t.setResponseTime(0);
            else
            	t.setResponseTime(w);
  //    System.out.println("task i "+t.getId()+ "  response time  "+w);
        }
	/*	for (ITask t : taskset)
		{
			System.out.println("task i "+t.getId()+" wcet  "+t.getWcet()+"  response  "+t.getResponseTime());
			

		}*/
	}
	/**
	 * @param taskset
	 */
	public void setResponseTime(ArrayList<ITask> taskset)
	{
		
		taskset.sort(new Comparator<ITask>() {
			public int compare(ITask p1, ITask p2) {
				int cmp;
				cmp= (int)(p1.getPeriod()-p2.getPeriod());
				return cmp;
			}
		});	
		
		
		for(ITask t:taskset)
      {
	//	System.out.println("task i "+t.getId()+" wcet  "+t.getWcet());
            double w=t.getWCET_orginal(),w1=w-1;
            while(w != w1)
            {
                w1 = w;
                w =t.getWCET_orginal();
                for(int i=0; taskset.get(i) != t; i++)
                {
                	w += (int) (Math.ceil((double) w1/(double)taskset.get(i).getPeriod())
                			*(double)taskset.get(i).getWCET_orginal());
          //     	 System.out.println("task j "+taskset.get(i).getId()+"  wcet  "+(double)taskset.get(i).getWCET_orginal()+"   response time  "+w);
                }
            }
            if( w > t.getDeadline())
             t.setResponseTime(0);
            else
            	t.setResponseTime(w);
   //    System.out.println("response time  "+w);
        }
	/*	for (ITask t : taskset)
		{
			System.out.println("task i "+t.getId()+" wcet  "+t.getWcet()+"  response  "+t.getResponseTime());
			

		}*/
	}
	public void setResponseTime_Moghdass(ArrayList<ITask> taskset)
	{
		taskset.sort(new Comparator<ITask>() {
			public int compare(ITask p1, ITask p2) {
				int cmp;
				cmp= (int)(p1.getPeriod()-p2.getPeriod());
				return cmp;
			}
		});	
		
		for(ITask t:taskset)
      {
	//	System.out.println("task i "+t.getId()+" wcet  "+t.getWCET_orginal());
            double w=t.getWcet(),w1=w-1;
            while(w != w1)
            {
                w1 = w;
                w =t.getWcet();
                for(int i=0; taskset.get(i) != t; i++)
                {
                	w += (int) (Math.ceil((double) w1/(double)taskset.get(i).getPeriod())
                			*(double)taskset.get(i).getWcet());
          //     	 System.out.println("task j "+taskset.get(i).getId()+"  wcet  "+(double)taskset.get(i).getWCET_orginal()+"   response time  "+w);
                }
            }
            if( w > t.getDeadline())
             t.setResponseTime(0);
            else
            	t.setResponseTime(w);
   //      System.out.println("response time  "+w);
        }
	/*	for (ITask t : taskset)
		{
			System.out.println("task i "+t.getId()+" wcet  "+t.getWcet()+"  response  "+t.getResponseTime());
			

		}*/
	}
	
	// USED IN OVERLOADING TO REVISE PROMOTION TIME
	public double calculateResponseTime(ArrayList<ITask> taskset, long taskId)
	{
		
		
		double w = 0, w1;
		for(ITask t:taskset)
		{
			
			//	System.out.println("task i "+t.getId()+" wcet  "+t.getWCET_orginal());
			 w=t.getWCET_orginal();
			 w1=w-1;
			while(w != w1)
			{
				w1 = w;
				w =t.getWCET_orginal();
				for(int i=0; taskset.get(i) != t; i++)
				{
					w += (int) (Math.ceil((double) w1/(double)taskset.get(i).getPeriod())*(double)taskset.get(i).getWCET_orginal());
					//     	 System.out.println("task j "+taskset.get(i).getId()+"  wcet  "+(double)taskset.get(i).getWCET_orginal()+"   response time  "+w);
				}
			}
	//		 System.out.println("response time  "+w);
		/*	if( w > t.getDeadline())
				t.setResponseTime(0);
			else
				t.setResponseTime(w);*/
			if(t.getId()==taskId)
				{
				//return w;
				break;
				}
			
			   
		}
		/*	for (ITask t : taskset)
		{
			System.out.println("task i "+t.getId()+" wcet  "+t.getWcet()+"  response  "+t.getResponseTime());


		}*/
		return w;
	}
	
	// USED IN OVERLOADING TO REVISE PROMOTION TIME
		public double calculateMixedResponseTime(ArrayList<ITask> taskset, long taskId)
		{
		/*	taskset.sort(new Comparator<ITask>() {
				public int compare(ITask p1, ITask p2) {
					int cmp;
					cmp= (int)(p1.getPeriod()-p2.getPeriod());
					return cmp;
				}
			});	*/
			
			
			double w = 0, w1;
			for(ITask t:taskset)
			{
				//10-12-18
		//			System.out.println("task i "+t.getId()+" wcet  "+t.getWcet());
				 w=t.getWcet();
				 w1=w-1;
				while(w != w1)
				{
					w1 = w;
					w =t.getWcet();
					for(int i=0; taskset.get(i) != t; i++)
					{
						w += (int) (Math.ceil((double) w1/(double)taskset.get(i).getPeriod())*(double)taskset.get(i).getWcet());
		//				    	 System.out.println("task j "+taskset.get(i).getId()+"  wcet  "+(double)taskset.get(i).getWCET_orginal()+"   response time  "+w);
					
						//10-12-18
						if(w>t.getDeadline())
					{
			//			System.out.println(" for breaking response time  "+w+" dead "+ t.getDeadline());
						break;
					}
					}
					
					if(w>t.getDeadline())
					{
						//10-12-18
			//			System.out.println(" wbhile breaking response time  "+w+" dead "+ t.getDeadline());
						break;
					}
				}
		//		 System.out.println("response time  "+w+" dead "+ t.getDeadline());
			/*	if( w > t.getDeadline())
					t.setResponseTime(0);
				else
					t.setResponseTime(w);*/
				if(t.getId()==taskId)
					{
					//return w;
					break;
					}
				
				   
			}
			/*	for (ITask t : taskset)
			{
				System.out.println("task i "+t.getId()+" wcet  "+t.getWcet()+"  response  "+t.getResponseTime());


			}*/
			// System.out.println("response time  "+w);
			return w;
		}
	
	
	public void setResponseTimeMixedOVERLOADING(ArrayList<ITask> taskset, int d, double u)
	{
		/*taskset.sort(new Comparator<ITask>() {
			public int compare(ITask p1, ITask p2) {
				int cmp;
				cmp= (int)(p1.getPeriod()-p2.getPeriod());
				return cmp;
			}
		});*/	
		
		double interference;
		double load=0, old_load=0;
		for(ITask t:taskset)
		{
			//	System.out.println(" task i "+t.getId());
			double w=t.getWcet(),w1=w-1;
			while(w != w1)
			{
				w1 = w;
				w =t.getWcet();
				for(int i=0; taskset.get(i) != t; i++)
				{
					if (!taskset.get(i).isPrimary())// IF BACKUPP TASK, TAKE LESS WORKLOAD
					{
						if (taskset.get(i).getPeriod()==t.getPeriod())
							load =1;
						// ((1-(double)((double)(taskset.get(i).getPeriod()-1)/(double)t.getPeriod()))+
						//((double)taskset.get(i).getWCET_orginal()/(double)taskset.get(i).getPeriod()));                     		
						else
							load = ((1-(double)((double)taskset.get(i).getPeriod()/(double)t.getPeriod()))+
									((double)taskset.get(i).getWcet()/(double)taskset.get(i).getPeriod()));         	
						//load = ((1-(double)((double)taskset.get(i).getPeriod()/(double)t.getPeriod()))); 

						if(load>1)
							load=1;

						interference = Math.ceil(load*(double)taskset.get(i).getWcet());

						// old load /////////////////////
						  old_load = (Math.ceil((1-(double)((double)taskset.get(i).getPeriod()/  
                			   (double)t.getPeriod()))*  (double) taskset.get(i).getWcet()));
						   	
						/*  System.out.println("u "+(double)taskset.get(i).getWcet()/(double)taskset.get(i).getPeriod()
								  +"  new  "+load
								  +" old " +(1-(double)((double)taskset.get(i).getPeriod()/  
			                			   (double)t.getPeriod())));*/
                	 
						      }
					else
					{

						interference = taskset.get(i).getWcet();
				   //  System.out.println("task j "+taskset.get(i).getId()+" wcet  "+taskset.get(i).getWcet()+"  primary  "+taskset.get(i).isPrimary()+"  load  "+load);
					}
					
				/*	if(t.getId()==3)
					{
					System.out.println("task j "+taskset.get(i).getId()+" wcet  "+taskset.get(i).getWcet()+
               			   "  primary  "+taskset.get(i).isPrimary()+"  load  "+load+"  old_load  "+old_load+ "   interference   "+interference );
					         System.out.println("tj  "+taskset.get(i).getPeriod()+"   ti  "+t.getPeriod()+
                    		"   cj   "+taskset.get(i).getWcet()+" u "+(double)taskset.get(i).getWcet()/(double)taskset.get(i).getPeriod());
                    System.out.println("tj/ti    "+((double)((double)taskset.get(i).getPeriod()/(double)t.getPeriod())+
                    		"   1-tj/ti  "+(1-((double)((double)taskset.get(i).getPeriod()/(double)t.getPeriod()))))
                    +		" load  "+(Math.ceil((1-(double)((double)taskset.get(i).getPeriod()/(double)t.getPeriod()))*
                           		taskset.get(i).getWcet())));
                
						//System.out.println(" load "+load+" interference   "+interference+ "  w "+w +"  w1 "+w1+ " t "+t.getId());
					}*/
                    
					w += (int) (Math.ceil((double) w1/(double)taskset.get(i).getPeriod())*(interference));

					//	w += (int) (Math.ceil((double) w1/taskset.get(i).getPeriod())*taskset.get(i).getWCET_orginal());
				//	    	 System.out.println("task j "+taskset.get(i).getId()+"response time  "+w);
				}
			}
			if( w > t.getDeadline())
				t.setResponseTime(0);
			else
				t.setResponseTime(w);
			//      System.out.println("response time  "+w);
		}
	/*		for (ITask t : taskset)
		{
			System.out.println("task i "+t.getId()+" wcet  "+t.getWcet()+"  response  "+t.getResponseTime());
		}*/
	}
	
	
	public void setResponseTimeMixed(ArrayList<ITask> taskset)
	{
		
		taskset.sort(new Comparator<ITask>() {//11-12-18
			public int compare(ITask p1, ITask p2) {
				int cmp;
				cmp= (int)(p1.getPeriod()-p2.getPeriod());
				return cmp;
			}
		});	
		
		for(ITask t:taskset)
	      {
			//System.out.println("task i "+t.getId()+" wcet  "+t.getWcet()+"  getWCET_orginal "+t.getWCET_orginal());
	            double w=t.getWcet(),w1=w-1;
	            while(w != w1)
	            {
	                w1 = w;
	                w =t.getWcet();
	                for(int i=0; taskset.get(i) != t; i++)
	                {
	                	
	                	
	                	w += (int) (Math.ceil((double) w1/(double)taskset.get(i).getPeriod())
	                			*(double)taskset.get(i).getWcet());
	          //     	 System.out.println("task j "+taskset.get(i).getId()+"  wcet  "+(double)taskset.get(i).getWCET_orginal()+"   response time  "+w);
	                }
	            }
	        
	            
	            if( w > t.getDeadline())
	             t.setResponseTime(0);
	            else
	            	t.setResponseTime(w);
	    //   System.out.println("response time  "+w+" d "+t.getDeadline());
	        }
		/*	for (ITask t : taskset)
			{
				System.out.println("task i "+t.getId()+" wcet  "+t.getWcet()+"  response  "+t.getResponseTime());
				

			}*/
		}
	
	public void setResponseTimeForMWFD(ArrayList<ITask> taskset)
	{
		
		taskset.sort(new Comparator<ITask>() {//11-12-18
			public int compare(ITask p1, ITask p2) {
				int cmp;
				cmp= (int)(p1.getPeriod()-p2.getPeriod());
				return cmp;
			}
		});	
		
		for(ITask t:taskset)
	      {
			//System.out.println("task i "+t.getId()+" wcet  "+t.getWcet()+"  getWCET_orginal "+t.getWCET_orginal());
	            double w=t.getWcet(),w1=w-1;
	            while(w != w1)
	            {
	                w1 = w;
	                w =t.getWcet();
	                for(int i=0; taskset.get(i) != t; i++)
	                {
	                	
	                	
	                	w += (int) (Math.ceil((double) w1/(double)taskset.get(i).getPeriod())
	                			*(double)taskset.get(i).getWcet());
	          //     	 System.out.println("task j "+taskset.get(i).getId()+"  wcet  "+(double)taskset.get(i).getWCET_orginal()+"   response time  "+w);
	                }
	            }
	          //FEASIBLE FREQ14-12-18
	            
	            	for(int i=0; taskset.get(i) != t; i++)
	                {
	            		w+=taskset.get(i).getWcet();
	                }
	            	//FEASIBLE FREQ
	            
	            if( w > t.getDeadline())
	             t.setResponseTime(0);
	            else
	            	t.setResponseTime(w);
	    //   System.out.println("response time  "+w+" d "+t.getDeadline());
	        }
		/*	for (ITask t : taskset)
			{
				System.out.println("task i "+t.getId()+" wcet  "+t.getWcet()+"  response  "+t.getResponseTime());
				

			}*/
		}
	
	
	public void setPromotionTime(ArrayList<ITask> taskset)
	{
		for (ITask t : taskset)
		{
			if(t.getResponseTime()==0)
			{
				t.setSlack(0);
				
				  System.out.println("task   "+t.getId()+" wcet  "+t.getWCET_orginal()+" res time "+t.getResponseTime()+"  deadline  "+t.getDeadline()+"  promotion   "+t.getSlack()+"  primary "+t.isPrimary());
					
			}
			else
			t.setSlack(t.getDeadline()-t.getResponseTime());
	//   System.out.println("task   "+t.getId()+" wcet  "+t.getWCET_orginal()+" res time "+t.getResponseTime()+"  deadline  "+t.getDeadline()+"  promotion   "+t.getSlack());
		}
		
	}
	
	
		}
	

