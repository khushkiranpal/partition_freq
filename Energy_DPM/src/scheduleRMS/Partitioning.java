/**
 * 
 */
package scheduleRMS;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import platform.Processor;
import taskGeneration.ITask;
import taskGeneration.SystemMetric;

/**
 * @author KHUSHKIRAN PAL
 *
 */
public class Partitioning { //initialization parameters//
	
	// MOGDAS FREQ SETTING 5-12-18
	// CLONING BACKUP 
	/*public  void allocatio_WFD_Mogdas (ArrayList<ITask> taskset,   ArrayList<Processor> freeProcList ,  String filename, double RMSMax_work) throws IOException
	{

	//	  Writer writer_allocation = new FileWriter(filename+"WFD_MOG.txt");
		 DecimalFormat twoDecimals = new DecimalFormat("#.##");  // upto 1 decimal points
		    DecimalFormat fourDecimals = new DecimalFormat("#.###");
		    SchedulabilityCheck schedule = new SchedulabilityCheck();
		    double fq=1;
		     
		 // SORT IN DECREASING ORDER OF UTILIZATION FOR WFD wcet according to frequency
			
			Comparator<ITask> c = new Comparator<ITask>() {
		    	 public int compare(ITask p1, ITask p2) {
		    		 int cmp;
			//	//System.out.println("t1 "+p1.getId()+"  u1 "+Double.valueOf(fourDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))));
			//	//System.out.println("t2 "+p2.getId()+"  u2 "+Double.valueOf(fourDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))));

		 double temp =  ( (Double.valueOf(twoDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))))
		-(Double.valueOf(twoDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))))); // backup????? wcet uti??
				//	//System.out.println("temp   "+temp);
					if(temp>0)
						cmp = 1;
					else
						cmp=-1;
					 if(temp==0)
							cmp=0;
					//	//System.out.println(" cmp  "+cmp);
						if (cmp==0)														
							cmp= (int)(p1.getId()-p2.getId());	
				//	//System.out.println(" cmp  "+cmp);
		    		 return cmp;
				}
			  };
			taskset.sort(c);
			
			for(ITask t : taskset)
	    	{
			System.out.println("task  "+t.getId()+" u "+ (Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))) );
	    	}
	   	  
			// WHILE ALL PROCESSORS HAVE SCHEDULABLE TASKSETS ON GIVEN FREQUENCIES 
		//	do{      
	     	
	     		for(Processor pMin : freeProcList)
	     		{
	         		
	     			pMin.taskset.clear();
	     			pMin.setWorkload(0);
	         	//	//System.out.println("processor   "+pMin.getId()+"   size  "+pMin.taskset.size()+"  w  "+pMin.getWorkload());
	     		}    		
	     	
	   	
	    	
		//ALLOCATION OF PRIMARIES/////RMS_max_work=1//////////////
	    	
	   		writer_allocation.write("\nPRIMARY ");
	   		writer_allocation.write("\nProc Workload u_task task WCET period ");
	   		freeProcList.get(0).opened=true;
	    	for(ITask t : taskset)
	    	{
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline()))),
	    				RMS_max_work=RMSMax_work, min_work=RMS_max_work;//0.7	;
	    		Processor minP=null, nextP=null;
	    		System.out.println(" u  "+u);
	    	
	    		for(Processor pMin : freeProcList)
	    		{
	    			
	    			if(min_work >(pMin.getWorkload() )&& pMin.opened)
	    			{
	    				min_work=Double.valueOf(twoDecimals.format(pMin.getWorkload()));
	    				minP = pMin;
	    				System.out.println("work   "+min_work+"  minP  "+minP.getId()+ "  pMin  "+pMin.getId());
	    		        
	    			}
	    			if(!pMin.opened)
	    			{
	    				nextP=pMin;
	    				System.out.println("breaking "+pMin.opened);
	    				break;
	    			}
	    			
	    		}
	    		if(minP== null || (minP.getWorkload()+u)>RMS_max_work)// open new bin
	    		{
	    			nextP.opened=true;
	    			minP=nextP;
	    			System.out.println("work   "+minP.getWorkload()+ "  nextP  "+nextP.getId());
	    		}
	    			
	    		
	    		
	    		t.setPrimary(true);
	    		t.setFrequency(t.getReliableFreq());//5-12-18 mogdas freq
	    		minP.taskset.add(t);
	    		minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		t.setP(minP);
	    		t.setPrimaryProcessor(minP);
	    		writer_allocation.write("\n"+minP.getId()+" "+minP.getWorkload()+" "+u+" "+t.getId()+" "
	    	    		+t.getWcet()+" "+t.getPeriod());	
	    		
	    	}
	    
	    	
	    	
	    	//ALLOCATION OF BACKUPS/////RMS_max_work=1//////////////
	    	writer_allocation.write("\nBACKUPS ");
	  		writer_allocation.write("\nProc Workload u_task task WCET period ");
	  	  	
	    	// SORT IN DECREASING ORDER OF UTILIZATION FOR MFWD wcet_original
			
	    			Comparator<ITask> c1 = new Comparator<ITask>() {
	    		    	 public int compare(ITask p1, ITask p2) {
	    		    		 int cmp;
	    			//	//System.out.println("t1 "+p1.getId()+"  u1 "+Double.valueOf(fourDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))));
	    			//	//System.out.println("t2 "+p2.getId()+"  u2 "+Double.valueOf(fourDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))));

	    		    		 double temp =  ( (Double.valueOf(twoDecimals.format(((double)p2.getWCET_orginal()/(double)p2.getDeadline()))))
	    							-(Double.valueOf(twoDecimals.format(((double)p1.getWCET_orginal()/(double)p1.getDeadline()))))); // backup????? wcet uti??
	    				//	//System.out.println("temp   "+temp);
	    					if(temp>0)
	    						cmp = 1;
	    					else
	    						cmp=-1;
	    					 if(temp==0)
	    							cmp=0;
	    					//	//System.out.println(" cmp  "+cmp);
	    						if (cmp==0)														
	    							cmp= (int)(p1.getId()-p2.getId());
	    				//	//System.out.println(" cmp  "+cmp);
	    		    		 return cmp;
	    				}
	    			  };
	    			taskset.sort(c1);
	    	
	    	for(ITask t : taskset)
	    	{
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWCET_orginal()/(double)t.getD()))),
	    				RMS_max_work=RMSMax_work,min_work=RMS_max_work;//0.7;
	    		ITask backup_task;
	    		Processor minP=null, nextP=null;
	    	//	System.out.println("t  "+t.getId()+" u backup  "+u);
	    		for(Processor pMin : freeProcList)
	    		{
	    			if (pMin == t.getP())   // IF PRIMARY PROCESSOR CONTAINS THE TASK, ALLOCATE BACKUP ON SOME OTHER PROCESSOR
	    				continue;
	    			
	    			if(min_work >(pMin.getWorkload() )&& pMin.opened)
	    			{
	    				min_work=Double.valueOf(twoDecimals.format(pMin.getWorkload()));
	    				minP = pMin;
	    			//	System.out.println("B work   "+min_work+"  minP  "+minP.getId()+ "  pMin  "+pMin.getId());
	    		        
	    			}
	    			if(!pMin.opened)
	    			{
	    				nextP=pMin;
	    			//	System.out.println("breaking "+pMin.opened);
	    				
	    				break;
	    			}
	    			
	    		}
	    		if(minP==null  || (minP.getWorkload()+u)>RMS_max_work)// open new bin
	    		{
	    			nextP.opened=true;
	    			minP=nextP;
	    		//	System.out.println("work   "+minP.getWorkload()+ "  nextP  "+nextP.getId());
	    			   
	    		}
	    			
	    		t.setBackupProcessor(minP);
	    		backup_task = t.cloneTask_MWFD_RMS_EEPS();
	    		backup_task.setPrimary(false);  //setup backup processor
	    		backup_task.setFrequency(t.getReliableFreq());//5-12-18 mogdas freq
	    		backup_task.setBackupProcessor(minP);
	    		backup_task.setPrimaryProcessor(t.getP());
	    		
	    		minP.taskset.add(backup_task);
	    		//18-12-18 problem =load   0.7228571428571428 LLB_N  0.720537650030755   slack   0.0
	    		  
	    		minP.setWorkload(minP.getWorkload()+u);
	    		//minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		backup_task.setP(minP);
	    		
	    		writer_allocation.write("\n"+minP.getId()+" "+minP.getWorkload()+" "+u+" "+t.getId()+" "
	    	    		+t.getWcet()+" "+t.getPeriod());	
	     	   
	    	}
	        
	    	for(Processor pMin : freeProcList)
	    	{
	    		
	    		writer_allocation.write("\nprocessor   "+pMin.getId()+"\t frequency   "+fq
	    				+ " schedulability "+schedule.worstCaseResp_TDA_RMS_multi(pMin.taskset)+"\n");
	    	    
	    	}
	 //   	 writer_allocation.write("\nProc workload TASK U WCET PERIOD freq IS_PRIMARY BACKUP_PR PRIMARY_PR\n");
			   
	    	for(Processor pMin : freeProcList)
	    	{
	       	//	writer_allocation.write("\n\nprocessor   "+pMin.getId()+"\t frequency   "+fq+"\n");
	    		for(ITask t : pMin.taskset)
	    			
	        	{
	    			
	    			writer_allocation.write(pMin.getId()+" "+pMin.getWorkload()+" "+t.getId()+" "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
	    			+" "+t.getWCET_orginal()+" "+t.getPeriod()+" "+t.getFrequency()+" "+
	    			" "+t.isPrimary()+	" "+t.getBackupProcessor().getId()+" "+t.getPrimaryProcessor().getId()+"\n");
	    			
//	    		System.out.println("task   "+t.getId()+"  u  "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
//	    			+"   primary  "+t.isPrimary()+"  Proc   "+t.getP().getId()+	"   backup p  "+t.getBackupProcessor().getId()+
//	    			"   primary  "+t.getPrimaryProcessor().getId());
//	       
	        	}
	    		}
	    //	writer_allocation.close();
	
	}*/
	
	
	////CLONING backup_task = t.cloneTask_MWFD_RMS_EEPS();/////////////////
				/////RMS_max_work=1//////////////

	public  void allocatio_WFD (ArrayList<ITask> taskset,   ArrayList<Processor> freeProcList ,  String filename, double RMSMax_work) throws IOException
	{
		  Writer writer_allocation = new FileWriter(filename+"WFD.txt");
		 DecimalFormat twoDecimals = new DecimalFormat("#.##");  // upto 1 decimal points
		    DecimalFormat fourDecimals = new DecimalFormat("#.###");
		    SchedulabilityCheck schedule = new SchedulabilityCheck();
		    double fq=1;
		     
		 // SORT IN DECREASING ORDER OF UTILIZATION FOR WFD wcet according to frequency
			
			Comparator<ITask> c = new Comparator<ITask>() {
		    	 public int compare(ITask p1, ITask p2) {
		    		 int cmp;
			//	//System.out.println("t1 "+p1.getId()+"  u1 "+Double.valueOf(fourDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))));
			//	//System.out.println("t2 "+p2.getId()+"  u2 "+Double.valueOf(fourDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))));

		 double temp =  ( (Double.valueOf(twoDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))))
		-(Double.valueOf(twoDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))))); // backup????? wcet uti??
				//	//System.out.println("temp   "+temp);
					if(temp>0)
						cmp = 1;
					else
						cmp=-1;
					 if(temp==0)
							cmp=0;
					//	//System.out.println(" cmp  "+cmp);
						if (cmp==0)														
							cmp= (int)(p1.getId()-p2.getId());	
				//	//System.out.println(" cmp  "+cmp);
		    		 return cmp;
				}
			  };
			taskset.sort(c);
			
			/*for(ITask t : taskset)
	    	{
			System.out.println("task  "+t.getId()+" u "+ (Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))) );
	    	}
	   	  */
			// WHILE ALL PROCESSORS HAVE SCHEDULABLE TASKSETS ON GIVEN FREQUENCIES 
		//	do{      
	     	
	     		for(Processor pMin : freeProcList)
	     		{
	         		
	     			pMin.taskset.clear();
	     			pMin.setWorkload(0);
	         	//	//System.out.println("processor   "+pMin.getId()+"   size  "+pMin.taskset.size()+"  w  "+pMin.getWorkload());
	     		}    		
	     	
	   	
	    	
		//ALLOCATION OF PRIMARIES/////RMS_max_work=1//////////////
	    	
	   		writer_allocation.write("\nPRIMARY WFD ");
	   		writer_allocation.write("\nProc Workload u_task task WCET period ");
	   		freeProcList.get(0).opened=true;
	    	for(ITask t : taskset)
	    	{
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline()))),
	    				RMS_max_work=RMSMax_work, min_work=RMS_max_work;//0.7	;
	    		Processor minP=null, nextP=null;
	    		//System.out.println(" u  "+u);
	    	
	    		for(Processor pMin : freeProcList)
	    		{
	    			
	    			if(min_work >(pMin.getWorkload() )&& pMin.opened)
	    			{
	    				min_work=Double.valueOf(twoDecimals.format(pMin.getWorkload()));
	    				minP = pMin;
	    			//	System.out.println("work   "+min_work+"  minP  "+minP.getId()+ "  pMin  "+pMin.getId());
	    		        
	    			}
	    			if(!pMin.opened)
	    			{
	    				nextP=pMin;
	    		//		System.out.println("breaking "+pMin.opened);
	    				break;
	    			}
	    			
	    		}
	    		if(minP==null  || (minP.getWorkload()+u)>RMS_max_work)// open new bin
	    		{
	    			nextP.opened=true;
	    			minP=nextP;
	    		//	System.out.println("work   "+minP.getWorkload()+ "  nextP  "+nextP.getId());
	    		}
	    			
	    		
	    		
	    		t.setPrimary(true);
	    		t.setFrequency(fq);
	    		minP.taskset.add(t);
	    		//18-12-18 problem =total uti   0.7228> LLB_N  0.7205   slack   0.0
	    		  
	    		minP.setWorkload(minP.getWorkload()+u);
	    	//	minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		t.setP(minP);
	    		t.setPrimaryProcessor(minP);
	    		writer_allocation.write("\n"+minP.getId()+" "+minP.getWorkload()+" "+u+" "+t.getId()+" "
	    	    		+t.getWcet()+" "+t.getPeriod());	
	    		
	    	}
	    
	    	/*for(Processor pMin : freeProcList)
    		{
	    		System.out.println("  pMin  "+pMin.getId()+"work   "+pMin.getWorkload());
		    	
    		}*/
	    	
	    	//ALLOCATION OF BACKUPS/////RMS_max_work=1//////////////
	    	writer_allocation.write("\nBACKUPS ");
	  		writer_allocation.write("\nProc Workload u_task task WCET period ");
	  	  	
	    	// SORT IN DECREASING ORDER OF UTILIZATION FOR MFWD wcet_original
			
	    			Comparator<ITask> c1 = new Comparator<ITask>() {
	    		    	 public int compare(ITask p1, ITask p2) {
	    		    		 int cmp;
	    			//	//System.out.println("t1 "+p1.getId()+"  u1 "+Double.valueOf(fourDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))));
	    			//	//System.out.println("t2 "+p2.getId()+"  u2 "+Double.valueOf(fourDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))));

	    		    		 double temp =  ( (Double.valueOf(twoDecimals.format(((double)p2.getWCET_orginal()/(double)p2.getDeadline()))))
	    							-(Double.valueOf(twoDecimals.format(((double)p1.getWCET_orginal()/(double)p1.getDeadline()))))); // backup????? wcet uti??
	    				//	//System.out.println("temp   "+temp);
	    					if(temp>0)
	    						cmp = 1;
	    					else
	    						cmp=-1;
	    					 if(temp==0)
	    							cmp=0;
	    					//	//System.out.println(" cmp  "+cmp);
	    						if (cmp==0)														
	    							cmp= (int)(p1.getId()-p2.getId());
	    				//	//System.out.println(" cmp  "+cmp);
	    		    		 return cmp;
	    				}
	    			  };
	    			taskset.sort(c1);
	    	
	    	for(ITask t : taskset)
	    	{
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWCET_orginal()/(double)t.getD()))),
	    				RMS_max_work=0.7,//RMSMax_work18-12-18
	    				min_work=RMS_max_work;//0.7;
	    		ITask backup_task;
	    		Processor minP=null, nextP=null;
	    	//	System.out.println("t  "+t.getId()+" u backup  "+u);
	    		for(Processor pMin : freeProcList)
	    		{
	    			if (pMin == t.getP())   // IF PRIMARY PROCESSOR CONTAINS THE TASK, ALLOCATE BACKUP ON SOME OTHER PROCESSOR
	    				continue;
	    			
	    			if(min_work >(pMin.getWorkload() )&& pMin.opened)
	    			{
	    				min_work=Double.valueOf(twoDecimals.format(pMin.getWorkload()));
	    				minP = pMin;
	    		//		System.out.println("B work   "+min_work+"  minP  "+minP.getId()+ "  pMin  "+pMin.getId());
	    		        
	    			}
	    			if(!pMin.opened)
	    			{
	    				nextP=pMin;
	    		//	System.out.println("breaking "+pMin.opened);
	    				
	    				break;
	    			}
	    			
	    		}
	    		if(minP==null  || (minP.getWorkload()+u)>RMS_max_work)// open new bin
	    		{
	    			nextP.opened=true;
	    			minP=nextP;
	    		//	System.out.println("work   "+minP.getWorkload()+ "  nextP  "+nextP.getId());
	    			   
	    		}
	    			
	    		t.setBackupProcessor(minP);
	    		backup_task = t.cloneTask_MWFD_RMS_EEPS();
	    		backup_task.setPrimary(false);  //setup backup processor
	    		backup_task.setFrequency(1);
	    		backup_task.setBackupProcessor(minP);
	    		backup_task.setPrimaryProcessor(t.getP());
	    		
	    		minP.taskset.add(backup_task);
	    		//18-12-18 problem =total uti   0.7228> LLB_N  0.7205   slack   0.0
	    		  
	    		minP.setWorkload(minP.getWorkload()+u);
	    	//	minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		backup_task.setP(minP);
	    		
	    		writer_allocation.write("\n"+minP.getId()+" "+minP.getWorkload()+" "+u+" "+t.getId()+" "
	    	    		+t.getWcet()+" "+t.getPeriod());	
	     	   
	    	}
	        
	    	for(Processor pMin : freeProcList)
	    	{
	    		
	    		writer_allocation.write("\nprocessor   "+pMin.getId()+"\t frequency   "+fq
	    				+ " schedulability "+schedule.worstCaseResp_TDA_RMS_multi(pMin.taskset)+"\n");
	    	    
	    	}
	    	 writer_allocation.write("\nProc workload TASK U WCET PERIOD freq IS_PRIMARY BACKUP_PR PRIMARY_PR\n");
			   
	    	for(Processor pMin : freeProcList)
	    	{
	       	writer_allocation.write("\n\nprocessor   "+pMin.getId()+"\t frequency   "+fq+"\n");
	    		for(ITask t : pMin.taskset)
	    			
	        	{
	    			
	    			writer_allocation.write(pMin.getId()+" "+pMin.getWorkload()+" "+t.getId()+" "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
	    			+" "+t.getWCET_orginal()+" "+t.getPeriod()+" "+t.getFrequency()+" "+
	    			" "+t.isPrimary()+	" "+t.getBackupProcessor().getId()+" "+t.getPrimaryProcessor().getId()+"\n");
	    			
//	    		System.out.println("task   "+t.getId()+"  u  "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
//	    			+"   primary  "+t.isPrimary()+"  Proc   "+t.getP().getId()+	"   backup p  "+t.getBackupProcessor().getId()+
//	    			"   primary  "+t.getPrimaryProcessor().getId());
//	       
	        	}
	    		}
	   	writer_allocation.close();
	}
	
	
	public void allocation_WFD_fixedThresh(ArrayList<ITask> taskset,   ArrayList<Processor> freeProcList ,  String filename, double threshold) throws IOException
	{

		  Writer writer_allocation = new FileWriter(filename+"WFD_threshold.txt");
		 DecimalFormat twoDecimals = new DecimalFormat("#.##");  // upto 1 decimal points
		    DecimalFormat fourDecimals = new DecimalFormat("#.###");
		    SchedulabilityCheck schedule = new SchedulabilityCheck();
		    double fq=1;
		     
		 // SORT IN DECREASING ORDER OF UTILIZATION FOR WFD wcet according to frequency
			
			Comparator<ITask> c = new Comparator<ITask>() {
		    	 public int compare(ITask p1, ITask p2) {
		    		 int cmp;
			//	//System.out.println("t1 "+p1.getId()+"  u1 "+Double.valueOf(fourDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))));
			//	//System.out.println("t2 "+p2.getId()+"  u2 "+Double.valueOf(fourDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))));

		 double temp =  ( (Double.valueOf(fourDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))))
		-(Double.valueOf(fourDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))))); // backup????? wcet uti??
				//	//System.out.println("temp   "+temp);
					if(temp>0)
						cmp = 1;
					else
						cmp=-1;
					 if(temp==0)
							cmp=0;
					//	//System.out.println(" cmp  "+cmp);
						if (cmp==0)														
							cmp= (int)(p1.getId()-p2.getId());	
				//	//System.out.println(" cmp  "+cmp);
		    		 return cmp;
				}
			  };
			taskset.sort(c);
			
			/*for(ITask t : taskset)
	    	{
			System.out.println("task  "+t.getId()+" u "+ (Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))) );
	    	}*/
	   	  
			// WHILE ALL PROCESSORS HAVE SCHEDULABLE TASKSETS ON GIVEN FREQUENCIES 
		//	do{      
	     	
	     		for(Processor pMin : freeProcList)
	     		{
	         		
	     			pMin.taskset.clear();
	     			pMin.setWorkload(0);
	         	//	//System.out.println("processor   "+pMin.getId()+"   size  "+pMin.taskset.size()+"  w  "+pMin.getWorkload());
	     		}    		
	     	
	   	
	    	
		//ALLOCATION OF PRIMARIES/////RMS_max_work=1//////////////
	    	
	   		writer_allocation.write("\nPRIMARY WFD_threshold " + threshold);
	   		writer_allocation.write("\nProc Workload u_task task WCET period ");
	   		freeProcList.get(0).opened=true;
	    	for(ITask t : taskset)
	    	{
	    		double u = Double.valueOf(fourDecimals.format(((double)t.getWcet()/(double)t.getDeadline()))),
	    				RMS_max_work=threshold, min_work=threshold;//0.7	;
	    		Processor minP=null, nextP=null;
	    		System.out.println(" u  "+u+"  t  "+ t.getId());
	    	
	    		for(Processor pMin : freeProcList)
	    		{
	    	System.out.println("OUT work   "+min_work+"  pMin  "+pMin.getId()+ "  WORKLOAD  "+pMin.getWorkload()+"  min_work   "+min_work);
	    		      
	    			if(min_work >(pMin.getWorkload() )&& pMin.opened)
	    			{
	    				min_work=Double.valueOf(fourDecimals.format(pMin.getWorkload()));
	    				minP = pMin;
	    				System.out.println("min_work   "+min_work+"  minP  "+minP.getId()+ "  WORKLOAD  "+pMin.getWorkload());
	    		        
	    			}
	    			if(!pMin.opened)
	    			{
	    				nextP=pMin;
	    			//	System.out.println("breaking "+pMin.opened);
	    				break;
	    			}
	    			
	    		}
	    		
	    		
	    		if(minP==null || (minP.getWorkload()+u)>(RMS_max_work) && minP.getWorkload()!=0)// open new bin
	    		{  // && minP.getWorkload()!=0    for threshold < task utilization 05/01/19
	    			
	    			nextP.opened=true;
	    			minP=nextP;
	    			System.out.println("work   "+minP.getWorkload()+ "  nextP  "+nextP.getId());
	    		}
	    			
	    		
	    		
	    		t.setPrimary(true);
	    		t.setFrequency(fq);
	    		minP.taskset.add(t);
	    		//18-12-18 problem =total uti   0.7228> LLB_N  0.7205   slack   0.0
	    		minP.setWorkload(minP.getWorkload()+u);
	    	//	minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		t.setP(minP);
	    		t.setPrimaryProcessor(minP);
	    		writer_allocation.write("\n"+minP.getId()+" "+minP.getWorkload()+" "+u+" "+t.getId()+" "
	    	    		+t.getWcet()+" "+t.getPeriod());	
	    		
	    	}
	    
	    	/*for(Processor pMin : freeProcList)
    		{
	    		System.out.println("  pMin  "+pMin.getId()+"work   "+pMin.getWorkload());
		    	
    		}*/
	    	
	    	//ALLOCATION OF BACKUPS/////RMS_max_work=1//////////////
	    	writer_allocation.write("\nBACKUPS ");
	  		writer_allocation.write("\nProc Workload u_task task WCET period ");
	  	  	
	    	// SORT IN DECREASING ORDER OF UTILIZATION FOR MFWD wcet_original
			
	    			Comparator<ITask> c1 = new Comparator<ITask>() {
	    		    	 public int compare(ITask p1, ITask p2) {
	    		    		 int cmp;
	    			//	//System.out.println("t1 "+p1.getId()+"  u1 "+Double.valueOf(fourDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))));
	    			//	//System.out.println("t2 "+p2.getId()+"  u2 "+Double.valueOf(fourDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))));

	    		    		 double temp =  ( (Double.valueOf(fourDecimals.format(((double)p2.getWCET_orginal()/(double)p2.getDeadline()))))
	    							-(Double.valueOf(fourDecimals.format(((double)p1.getWCET_orginal()/(double)p1.getDeadline()))))); // backup????? wcet uti??
	    				//	//System.out.println("temp   "+temp);
	    					if(temp>0)
	    						cmp = 1;
	    					else
	    						cmp=-1;
	    					 if(temp==0)
	    							cmp=0;
	    					//	//System.out.println(" cmp  "+cmp);
	    						if (cmp==0)														
	    							cmp= (int)(p1.getId()-p2.getId());
	    				//	//System.out.println(" cmp  "+cmp);
	    		    		 return cmp;
	    				}
	    			  };
	    			taskset.sort(c1);
	    	
	    	for(ITask t : taskset)
	    	{
	    		double u = Double.valueOf(fourDecimals.format(((double)t.getWCET_orginal()/(double)t.getD()))),
	    				RMS_max_work=0.7,min_work=0.7;//maximum 1 can be used;
	    		ITask backup_task;
	    		Processor minP=null, nextP=null;
	    	//	System.out.println("t  "+t.getId()+" u backup  "+u);
	    		for(Processor pMin : freeProcList)
	    		{
	    			
	    			if (pMin == t.getP())   // IF PRIMARY PROCESSOR CONTAINS THE TASK, ALLOCATE BACKUP ON SOME OTHER PROCESSOR
	    				continue;
	    		//	System.out.println("B work   "+pMin.getWorkload()+"  pMin  "+pMin.getId());
	    		       
	    			if(min_work >(pMin.getWorkload() )&& pMin.opened)
	    			{
	    				min_work=Double.valueOf(fourDecimals.format(pMin.getWorkload()));
	    				minP = pMin;
	    		//		System.out.println("B work   "+minP.getWorkload()+"  minP  "+minP.getId());
	    		        
	    			}
	    			if(!pMin.opened)
	    			{
	    				
	    				nextP=pMin;
	    			//	System.out.println("breaking "+pMin.opened);
	    				
	    				break;
	    			}
	    			
	    		}
	    		if( minP==null || (minP.getWorkload()+u)>RMS_max_work)// open new bin
	    		{
	    			nextP.opened=true;
	    			minP=nextP;
	    		//	System.out.println("work   "+minP.getWorkload()+ "  nextP alloted  "+nextP.getId());
	    			   
	    		}
	    			
	    		t.setBackupProcessor(minP);
	    		backup_task = t.cloneTask_MWFD_RMS_EEPS();
	    		backup_task.setPrimary(false);  //setup backup processor
	    		backup_task.setFrequency(1);
	    		backup_task.setBackupProcessor(minP);
	    		backup_task.setPrimaryProcessor(t.getP());
	    		
	    		minP.taskset.add(backup_task);
	    		//18-12-18 problem =total uti   0.7228> LLB_N  0.7205   slack   0.0
	    		minP.setWorkload(minP.getWorkload()+u);
	    	//	minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		backup_task.setP(minP);
	    		
	    		writer_allocation.write("\n"+minP.getId()+" "+minP.getWorkload()+" "+u+" "+t.getId()+" "
	    	    		+t.getWcet()+" "+t.getPeriod());	
	     	   
	    	}
	        
	    	for(Processor pMin : freeProcList)
	    	{
	    		
	    		writer_allocation.write("\nprocessor   "+pMin.getId()+"\t frequency   "+fq
	    				+ " schedulability "+schedule.worstCaseResp_TDA_RMS_multi(pMin.taskset)+"\n");
	    	    
	    	}
	   	 writer_allocation.write("\nProc workload TASK U WCET PERIOD freq IS_PRIMARY BACKUP_PR PRIMARY_PR\n");
			   
	    	for(Processor pMin : freeProcList)
	    	{
	       		writer_allocation.write("\n\nprocessor   "+pMin.getId()+"\t frequency   "+fq+"\n");
	    		for(ITask t : pMin.taskset)
	    			
	        	{
	    			
	    			writer_allocation.write(pMin.getId()+" "+pMin.getWorkload()+" "+t.getId()+" "+ Double.valueOf(fourDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
	    			+" "+t.getWCET_orginal()+" "+t.getPeriod()+" "+t.getFrequency()+" "+
	    			" "+t.isPrimary()+	" "+t.getBackupProcessor().getId()+" "+t.getPrimaryProcessor().getId()+"\n");
	    			
//	    		System.out.println("task   "+t.getId()+"  u  "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
//	    			+"   primary  "+t.isPrimary()+"  Proc   "+t.getP().getId()+	"   backup p  "+t.getBackupProcessor().getId()+
//	    			"   primary  "+t.getPrimaryProcessor().getId());
//	       
	        	}
	    		}
	    	writer_allocation.close();
	
	}
	
	// SETTING BACKUP FREQ  // 18-12-18 SET FREQ DURING SET_FREQ() PARAMETER SETTING  IN FREQ SETTING MAIN FUNCTION
/*	public  void allocation_M_WFD_MOG (ArrayList<ITask> taskset,   ArrayList<Processor> freeProcList ,  String filename) throws IOException
	{

	//	  Writer writer_allocation = new FileWriter(filename+"M_WFD_MOG.txt");
		 DecimalFormat twoDecimals = new DecimalFormat("#.##");  // upto 1 decimal points
		    DecimalFormat fourDecimals = new DecimalFormat("#.###");
		    SchedulabilityCheck schedule = new SchedulabilityCheck();
		    double fq=1;
		     
		 // SORT IN DECREASING ORDER OF UTILIZATION FOR MFWD wcet according to frequency
			
			Comparator<ITask> c = new Comparator<ITask>() {
		    	 public int compare(ITask p1, ITask p2) {
		    		 int cmp;
			//	//System.out.println("t1 "+p1.getId()+"  u1 "+Double.valueOf(fourDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))));
			//	//System.out.println("t2 "+p2.getId()+"  u2 "+Double.valueOf(fourDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))));

		    		 double temp =  ( (Double.valueOf(twoDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))))
							-(Double.valueOf(twoDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))))); // backup????? wcet uti??
				//	//System.out.println("temp   "+temp);
					if(temp>0)
						cmp = 1;
					else
						cmp=-1;
					 if(temp==0)
							cmp=0;
					//	//System.out.println(" cmp  "+cmp);
						if (cmp==0)														
							cmp= (int)(p1.getId()-p2.getId());	
				//	//System.out.println(" cmp  "+cmp);
		    		 return cmp;
				}
			  };
			taskset.sort(c);
			
			for(ITask t : taskset)
	    	{
			//	//System.out.println("task  "+t.getId()+" u "+ (Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))) );
	    	}
	   	  
			// WHILE ALL PROCESSORS HAVE SCHEDULABLE TASKSETS ON GIVEN FREQUENCIES 
		//	do{      
	     	
	     		for(Processor pMin : freeProcList)
	     		{
	         		
	     			pMin.taskset.clear();
	     			pMin.setWorkload(0);
	         	//	//System.out.println("processor   "+pMin.getId()+"   size  "+pMin.taskset.size()+"  w  "+pMin.getWorkload());
	     		}    		
	     	
	   	
	    	
		//ALLOCATION OF PRIMARIES
	    	
	   		writer_allocation.write("\nPRIMARY ");
	   		writer_allocation.write("\nProc Workload u_task task WCET period ");
		   	
	    	for(ITask t : taskset)
	    	{
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline()))),
	    				work=1;
	    		Processor minP=null;
	    	//	//System.out.println(" u  "+u);
	    		
	    		for(Processor pMin : freeProcList)
	    		{
	    			if(work >pMin.getWorkload())
	    			{
	    				work=Double.valueOf(twoDecimals.format(pMin.getWorkload()));
	    				minP = pMin;
	    			}
	    		//	//System.out.println("work   "+work+"  minP  "+minP.getId()+ "  pMin  "+pMin.getId());
	        		
	    		}
	    		t.setPrimary(true);
	    		t.setFrequency(t.getReliableFreq());//5-12-18 mogdas freq
	    	//	t.setFrequency(fq);
	    		minP.taskset.add(t);
	    		//18-12-18 problem =total uti   0.7228> LLB_N  0.7205   slack   0.0
	    		minP.setWorkload(minP.getWorkload()+u);
	    	//	minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		t.setP(minP);
	    		t.setPrimaryProcessor(minP);
	    		writer_allocation.write("\n"+minP.getId()+" "+minP.getWorkload()+" "+u+" "+t.getId()+" "
	    	    		+t.getWcet()+" "+t.getPeriod());	
	    	}
	    
	    	
	    	
	    	//ALLOCATION OF BACKUPS
	    	writer_allocation.write("\nBACKUPS ");
	  		writer_allocation.write("\nProc Workload u_task task WCET period ");
	  	    
	    	// SORT IN DECREASING ORDER OF UTILIZATION FOR MFWD wcet_original
			
	    			Comparator<ITask> c1 = new Comparator<ITask>() {
	    		    	 public int compare(ITask p1, ITask p2) {
	    		    		 int cmp;
	    			//	//System.out.println("t1 "+p1.getId()+"  u1 "+Double.valueOf(fourDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))));
	    			//	//System.out.println("t2 "+p2.getId()+"  u2 "+Double.valueOf(fourDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))));

	    		    		 double temp =  ( (Double.valueOf(twoDecimals.format(((double)p2.getWCET_orginal()/(double)p2.getDeadline()))))
	    							-(Double.valueOf(twoDecimals.format(((double)p1.getWCET_orginal()/(double)p1.getDeadline()))))); // backup????? wcet uti??
	    				//	//System.out.println("temp   "+temp);
	    					if(temp>0)
	    						cmp = 1;
	    					else
	    						cmp=-1;
	    					 if(temp==0)
	    							cmp=0;
	    					//	//System.out.println(" cmp  "+cmp);
	    						if (cmp==0)														
	    							cmp= (int)(p1.getId()-p2.getId());
	    				//	//System.out.println(" cmp  "+cmp);
	    		    		 return cmp;
	    				}
	    			  };
	    			taskset.sort(c1);
	    	
	    	for(ITask t : taskset)
	    	{
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWCET_orginal()/(double)t.getD()))),
	    				work=1;
	    		ITask backup_task;
	    		Processor minP=null;
	  //  		//System.out.println("t  "+t.getId()+" u backup  "+u);
	    		for(Processor pMin : freeProcList)
	    		{
	    			if (pMin == t.getP())   // IF PRIMARY PROCESSOR CONTAINS THE TASK, ALLOCATE BACKUP ON SOME OTHER PROCESSOR
	    				continue;
	    			if(work >pMin.getWorkload())
	    			{
	    				work=Double.valueOf(twoDecimals.format(pMin.getWorkload()));
	    				minP = pMin;
	    			}
	    //			//System.out.println("work   "+work+"  minP  "+minP.getId()+ "  pMin  "+pMin.getId());
	        		
	    		}
	    		t.setBackupProcessor(minP);
	    		backup_task = t.cloneTask_MWFD_RMS_EEPS();
	    		backup_task.setPrimary(false);  //setup backup processor
	    		backup_task.setFrequency(t.getReliableFreq());//5-12-18 mogdas freq
	    		
	    		//backup_task.setFrequency(1);
	    		backup_task.setBackupProcessor(minP);
	    		backup_task.setPrimaryProcessor(t.getP());
	    		
	    		minP.taskset.add(backup_task);
	    		//18-12-18 problem =total uti   0.7228> LLB_N  0.7205   slack   0.0
	    		minP.setWorkload(minP.getWorkload()+u);
	    		//minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		backup_task.setP(minP);
	    		
	    		writer_allocation.write("\n"+minP.getId()+" "+minP.getWorkload()+" "+u+" "+t.getId()+" "
	    	    		+t.getWcet()+" "+t.getPeriod());	   
	    	}
	        
	    	for(Processor pMin : freeProcList)
	    	{
	    		
	    		writer_allocation.write("\nprocessor   "+pMin.getId()+"\t frequency   "+fq
	    				+ " schedulability "+schedule.worstCaseResp_TDA_RMS_multi(pMin.taskset)+"\n");
	    	    
	    	}
	    //	  writer_allocation.write("\nProc workload TASK U WCET PERIOD freq IS_PRIMARY BACKUP_PR PRIMARY_PR\n");
			  
	    	for(Processor pMin : freeProcList)
	    	{
	       	//	writer_allocation.write("\n\nprocessor   "+pMin.getId()+"\t frequency   "+fq+"\n");
	    		for(ITask t : pMin.taskset)
	    			
	        	{
	    			
	    			writer_allocation.write(pMin.getId()+" "+pMin.getWorkload()+" "+t.getId()+" "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
	    			+" "+t.getWCET_orginal()+" "+t.getPeriod()+" "+t.getFrequency()+" "+
	    			" "+t.isPrimary()+	" "+t.getBackupProcessor().getId()+" "+t.getPrimaryProcessor().getId()+"\n");
	    			
//	    		System.out.println("task   "+t.getId()+"  u  "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
//	    			+"   primary  "+t.isPrimary()+"  Proc   "+t.getP().getId()+	"   backup p  "+t.getBackupProcessor().getId()+
//	    			"   primary  "+t.getPrimaryProcessor().getId());
//	       
	        	}
	    		}
	  //  	writer_allocation.close();
	
	}
	*/
	public  void allocation_M_WFD (ArrayList<ITask> taskset,   ArrayList<Processor> freeProcList ,  String filename) throws IOException
	{
		 Writer writer_allocation = new FileWriter(filename+"M_WFD.txt");
		 DecimalFormat twoDecimals = new DecimalFormat("#.##");  // upto 1 decimal points
		    DecimalFormat fourDecimals = new DecimalFormat("#.###");
		    SchedulabilityCheck schedule = new SchedulabilityCheck();
		    double fq=1;
		     
		 // SORT IN DECREASING ORDER OF UTILIZATION FOR MFWD wcet according to frequency
			
			Comparator<ITask> c = new Comparator<ITask>() {
		    	 public int compare(ITask p1, ITask p2) {
		    		 int cmp;
			//	//System.out.println("t1 "+p1.getId()+"  u1 "+Double.valueOf(fourDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))));
			//	//System.out.println("t2 "+p2.getId()+"  u2 "+Double.valueOf(fourDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))));

		    		 double temp =  ( (Double.valueOf(twoDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))))
							-(Double.valueOf(twoDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))))); // backup????? wcet uti??
				//	//System.out.println("temp   "+temp);
					if(temp>0)
						cmp = 1;
					else
						cmp=-1;
					 if(temp==0)
							cmp=0;
					//	//System.out.println(" cmp  "+cmp);
						if (cmp==0)														
							cmp= (int)(p1.getId()-p2.getId());	
				//	//System.out.println(" cmp  "+cmp);
		    		 return cmp;
				}
			  };
			taskset.sort(c);
			
			for(ITask t : taskset)
	    	{
			//	//System.out.println("task  "+t.getId()+" u "+ (Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))) );
	    	}
	   	  
			// WHILE ALL PROCESSORS HAVE SCHEDULABLE TASKSETS ON GIVEN FREQUENCIES 
		//	do{      
	     	
	     		for(Processor pMin : freeProcList)
	     		{
	         		
	     			pMin.taskset.clear();
	     			pMin.setWorkload(0);
	         	//	//System.out.println("processor   "+pMin.getId()+"   size  "+pMin.taskset.size()+"  w  "+pMin.getWorkload());
	     		}    		
	     	
	   	
	    	
		//ALLOCATION OF PRIMARIES
	    	
	   		writer_allocation.write("\nPRIMARY M_WFD");
	   		writer_allocation.write("\nProc Workload u_task task WCET period ");
		   	
	    	for(ITask t : taskset)
	    	{
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline()))),
	    				work=0.7, rmsUB=0.7;
	    		Processor minP=null;
	    	//	//System.out.println(" u  "+u);
	    		
	    		for(Processor pMin : freeProcList)
	    		{
	    			if ( (pMin.getWorkload()+u>rmsUB) )   // IF PRIMARY PROCESSOR CONTAINS THE TASK, ALLOCATE BACKUP ON SOME OTHER PROCESSOR
	    				continue;
	    			
	    			if(work >pMin.getWorkload()  )
	    			{
	    				work=Double.valueOf(twoDecimals.format(pMin.getWorkload()));
	    				minP = pMin;
	    			}
	    		//	//System.out.println("work   "+work+"  minP  "+minP.getId()+ "  pMin  "+pMin.getId());
	        		
	    		}
	    		
	    		t.setPrimary(true);
	    		t.setFrequency(fq);
	    		minP.taskset.add(t);
	    		//18-12-18 problem =total uti   0.7228> LLB_N  0.7205   slack   0.0
	    		minP.setWorkload(minP.getWorkload()+u);
	    	//	minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		t.setP(minP);
	    		t.setPrimaryProcessor(minP);
	    		writer_allocation.write("\n"+minP.getId()+" "+minP.getWorkload()+" "+u+" "+t.getId()+" "
	    	    		+t.getWcet()+" "+t.getPeriod());	
	    	}
	    
	    	
	    	
	    	//ALLOCATION OF BACKUPS
	    	writer_allocation.write("\nBACKUPS ");
	  		writer_allocation.write("\nProc Workload u_task task WCET period ");
	  	    
	    	// SORT IN DECREASING ORDER OF UTILIZATION FOR MFWD wcet_original
			
	    			Comparator<ITask> c1 = new Comparator<ITask>() {
	    		    	 public int compare(ITask p1, ITask p2) {
	    		    		 int cmp;
	    			//	//System.out.println("t1 "+p1.getId()+"  u1 "+Double.valueOf(fourDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))));
	    			//	//System.out.println("t2 "+p2.getId()+"  u2 "+Double.valueOf(fourDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))));

	    		    		 double temp =  ( (Double.valueOf(twoDecimals.format(((double)p2.getWCET_orginal()/(double)p2.getDeadline()))))
	    							-(Double.valueOf(twoDecimals.format(((double)p1.getWCET_orginal()/(double)p1.getDeadline()))))); // backup????? wcet uti??
	    				//	//System.out.println("temp   "+temp);
	    					if(temp>0)
	    						cmp = 1;
	    					else
	    						cmp=-1;
	    					 if(temp==0)
	    							cmp=0;
	    					//	//System.out.println(" cmp  "+cmp);
	    						if (cmp==0)														
	    							cmp= (int)(p1.getId()-p2.getId());
	    				//	//System.out.println(" cmp  "+cmp);
	    		    		 return cmp;
	    				}
	    			  };
	    			taskset.sort(c1);
	    	
	    	for(ITask t : taskset)
	    	{
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWCET_orginal()/(double)t.getD()))),
	    				work=0.7, rmsUB=0.7;
	    		ITask backup_task;
	    		Processor minP=null;
	// System.out.println("t  "+t.getId()+" u backup  "+u);
	    		for(Processor pMin : freeProcList)
	    		{
	    			//19-12-18 ||  (pMin.getWorkload()+u>rmsUB) if U(Pr)=0.68 case
	    			if (pMin == t.getP()  ||  (pMin.getWorkload()+u>rmsUB) )   // IF PRIMARY PROCESSOR CONTAINS THE TASK, ALLOCATE BACKUP ON SOME OTHER PROCESSOR
	    				continue;
	    			if(work >pMin.getWorkload())
	    			{
	    				work=Double.valueOf(twoDecimals.format(pMin.getWorkload()));
	    				minP = pMin;
	    			}
	// System.out.println("work   "+work+"  pMin.getWorkload()  "+pMin.getWorkload()+ "  pMin  "+pMin.getId()+ " w "+ pMin.getWorkload());
	        		
	    		}
	    		
	    		
	    		
	    		t.setBackupProcessor(minP);
	    		backup_task = t.cloneTask_MWFD_RMS_EEPS();
	    		backup_task.setPrimary(false);  //setup backup processor
	    		backup_task.setFrequency(1);
	    		backup_task.setBackupProcessor(minP);
	    		backup_task.setPrimaryProcessor(t.getP());
	    		
	    		minP.taskset.add(backup_task);
	    		//18-12-18 problem =total uti   0.7228> LLB_N  0.7205   slack   0.0
	    		minP.setWorkload(minP.getWorkload()+u);
	    	//	minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		backup_task.setP(minP);
	    		
	    		writer_allocation.write("\n"+minP.getId()+" "+minP.getWorkload()+" "+u+" "+t.getId()+" "
	    	    		+t.getWcet()+" "+t.getPeriod());	   
	    	}
	        
	    	for(Processor pMin : freeProcList)
	    	{
	    		
	    		writer_allocation.write("\nprocessor   "+pMin.getId()+"\t frequency   "+fq
	    				+ " schedulability "+schedule.worstCaseResp_TDA_RMS_multi(pMin.taskset)+"\n");
	    	    
	    	}
	   	  writer_allocation.write("\nProc workload TASK U WCET PERIOD freq IS_PRIMARY BACKUP_PR PRIMARY_PR\n");
			  
	    	for(Processor pMin : freeProcList)
	    	{
	       		writer_allocation.write("\n\nprocessor   "+pMin.getId()+"\t frequency   "+fq+"\n");
	    		for(ITask t : pMin.taskset)
	    			
	        	{
	    			
	    			writer_allocation.write(pMin.getId()+" "+pMin.getWorkload()+" "+t.getId()+" "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
	    			+" "+t.getWCET_orginal()+" "+t.getPeriod()+" "+t.getFrequency()+" "+
	    			" "+t.isPrimary()+	" "+t.getBackupProcessor().getId()+" "+t.getPrimaryProcessor().getId()+"\n");
	    			
//	    		System.out.println("task   "+t.getId()+"  u  "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
//	    			+"   primary  "+t.isPrimary()+"  Proc   "+t.getP().getId()+	"   backup p  "+t.getBackupProcessor().getId()+
//	    			"   primary  "+t.getPrimaryProcessor().getId());
//	       
	        	}
	    		}
	    	writer_allocation.close();
	}
	
	/**
	 * @param taskset
	 * @param freeProcList
	 * @param filename
	 * @param threshold
	 * @throws IOException
	 */
	public  void alloc_Prioritywise_threshold (ArrayList<ITask> taskset,   ArrayList<Processor> freeProcList , 
			String filename,double threshold) throws IOException
	{

		 Writer writer_allocation = new FileWriter(filename+"Prioritywise_threshold.txt");
		 DecimalFormat twoDecimals = new DecimalFormat("#.##");  // upto 1 decimal points
		
		    SchedulabilityCheck schedule = new SchedulabilityCheck();
		    double fq=1;
		    int currentProcId=1;
		   
		    
		      
		 // SORT IN increasing ORDER OF priority
		    Comparator<ITask> c = new Comparator<ITask>() {
		    	 public int compare(ITask p1, ITask p2) {
		    		 int cmp;
		    		 cmp= (int)(p1.getPeriod()-p2.getPeriod());	

					if (cmp==0)														
						cmp= (int)(p1.getId()-p2.getId());	
		    		 return cmp;
				}
			  };
			taskset.sort(c);
			
			/*for(ITask t : taskset)
	    	{
			System.out.println("task  "+t.getId()+" p "+t.getPriority()+" u "+ (Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))) );
	    	}
	   	  */
			// WHILE ALL PROCESSORS HAVE SCHEDULABLE TASKSETS ON GIVEN FREQUENCIES 
		//	do{      
	     	
	     		for(Processor pMin : freeProcList)
	     		{
	         		
	     			pMin.taskset.clear();
	     			pMin.setWorkload(0);
	         	//	//System.out.println("processor   "+pMin.getId()+"   size  "+pMin.taskset.size()+"  w  "+pMin.getWorkload());
	     		}    		
	     	
	   	
	    	
		//ALLOCATION OF PRIMARIES
	    	
	   		writer_allocation.write("\nPRIMARY Prioritywise_threshold");
	   		writer_allocation.write("\nProc Workload u_task task WCET period ");
	   		freeProcList.get(0).opened=true;
	    	for(ITask t : taskset)
	    	{
	    		
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline()))),
	    				RMS_max_work=threshold;// , min_work= RMS_max_work*(threshold/100);
	    		Processor minP=null,nextP = null;
	    	//	System.out.println(" u  "+u+" currentProcId  "+ currentProcId);
	    		for(Processor pMin : freeProcList)
	    		{
	    			//System.out.println("threshold   "+threshold+"  pMin  "+pMin.getId()+ "  pMin getWorkload "+pMin.getWorkload());
		    		 
	    			if(currentProcId>pMin.getId())
	    				continue;
	    			if((RMS_max_work) >(pMin.getWorkload() )&& pMin.opened)
	    			{
	    				//min_work=Double.valueOf(twoDecimals.format(pMin.getWorkload()));
	    				minP = pMin;
	    		//	System.out.println("work   "+RMS_max_work+"  minP  "+minP.getId()+ "  pMin getWorkload "+pMin.getWorkload());
	    		        
	    			}
	    			if(!pMin.opened)
	    			{
	    				nextP=pMin;
	    			//	System.out.println("breaking "+pMin.opened);
	    				break;
	    			}
	    			
	    		}
	    		if(minP==null || (minP.getWorkload()+u)>(RMS_max_work))// open new bin
	    		{
	    			nextP.opened=true;
	    			currentProcId++;
	    			minP=nextP;
	    	//		System.out.println("work   "+minP.getWorkload()+ "  nextP  "+nextP.getId()+" currentProcId "+currentProcId);
	    		}
	    			
	    	
	    		
	    	/*	System.out.println("  minP  "+minP.getId()+" t "+t.getId()+ 
	    				" wcet "+t.getWcet()+"  load  "+minP.getWorkload());*/
	    		t.setPrimary(true);
	    		t.setFrequency(fq);
	    		minP.taskset.add(t);
	    		//18-12-18 problem =total uti   0.7228> LLB_N  0.7205   slack   0.0
	    		minP.setWorkload(minP.getWorkload()+u);
	    	//	minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		t.setP(minP);
	    		t.setPrimaryProcessor(minP);
	    		writer_allocation.write("\n"+minP.getId()+" "+minP.getWorkload()+" "+u+" "+t.getId()+" "
	    	    		+t.getWcet()+" "+t.getPeriod());	
	    		   	/*System.out.println(" after minP  "+minP.getId()+" t "+t.getId()+ 
	    				" wcet "+t.getWcet()+" work load  "+minP.getWorkload());*/
	    	}
	    
	    	/*// for processors having no load, in case of lesser U and large Proc
	    	Iterator<Processor> itrP =  freeProcList.iterator();
	    	while (itrP.hasNext())
	    	
  		{
	    		if(itrP.next().getWorkload()==0)
	    			itrP.remove();
  		}
	    	
	    	System.out.println("  revised freeProcList size "+freeProcList.size());
	    	writer_allocation.write("  \nrevised freeProcList size "+freeProcList.size());
	    */
	    	 double max_U= (SystemMetric.utilisation(taskset)/currentProcId)*2;
			//   System.out.println("max_U  "+max_U+  "  currentProcId "+currentProcId);
	    	//ALLOCATION OF BACKUPS
	    	writer_allocation.write("\nBACKUPS ");
	    	writer_allocation.write("\nProc Workload u_task task WCET period ");
		   	
	    	 // SORT IN increasing ORDER OF priority
		    Comparator<ITask> c1 = new Comparator<ITask>() {
		    	 public int compare(ITask p1, ITask p2) {
		    		 int cmp;
		    		 cmp= (int)(p2.getPeriod()-p1.getPeriod());	

					if (cmp==0)														
						cmp= (int)(p1.getId()-p2.getId());	
		    		 return cmp;
				}
			  };
			taskset.sort(c1);
	    	
	    			/*freeProcList.sort(new Comparator<Processor>() {
	   		    	 public int compare(Processor p1, Processor p2) {
	   		    		 int cmp;
	   		    		cmp = (int) (p2.getId()-p1.getId());	
	   		    	
	   		    		 return cmp;
	   				}
	   			  });*/
	   	    		
	    			
	    		/*	for(Processor ptemp : freeProcList)
	    	    	{
	    	    	System.out.println("  p  "+ptemp.getId());
	    	    	}
	    			*/
	    			
	    			for(ITask t : taskset)
	    			{
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWCET_orginal()/(double)t.getD()))), 
	    				RMS_max_work=0.7,min_work=0.7;//0.7;
	    		ITask backup_task;
	    		Processor minP=null, nextP=null;
	    	//	System.out.println("t  "+t.getId()+" period "+t.getPeriod()+" u backup  "+u);
	    		for(Processor pMin : freeProcList)
	    		{
	    			
	    			if (pMin == t.getP())   // IF PRIMARY PROCESSOR CONTAINS THE TASK, ALLOCATE BACKUP ON SOME OTHER PROCESSOR
	    				continue;
	    	//		System.out.println("B work   "+pMin.getWorkload()+"  pMin  "+pMin.getId());
	    			
	    			
	    			
	    			
	    			// fill bin upto max load=RMS_max_work =max_U// 
	    			// balanced load on each processor
	    			//max_U >(pMin.getWorkload() +u)
	    			//or
	    			//fill bin upto max load=RMS_max_work =1
	    			//RMS_max_work >(pMin.getWorkload() +u
	    			// starting processors are fully occupied with last processors lightly loaded
	    			if(RMS_max_work >(pMin.getWorkload() +u) && pMin.opened)
	    			{
	    			//	min_work=Double.valueOf(twoDecimals.format(pMin.getWorkload()));
	    				minP = pMin;
	    		//		System.out.println("B work   "+minP.getWorkload()+"  minP  "+minP.getId());
	    		        break;
	    			}
	    			if(!pMin.opened)
	    			{
	    				
	    				nextP=pMin;
	    		//		System.out.println("breaking "+pMin.opened);
	    				
	    				break;
	    			}
	    			
	    		}
	    		
	    		// fill bin upto max load=RMS_max_work =max_U// 
    			// balanced load on each processor
	    		//(minP.getWorkload()+u)>max_U
    			//or
    			//fill bin upto max load=RMS_max_work =1
	    		//(minP.getWorkload()+u)>RMS_max_work
    			// starting processors are fully occupied with last processors lightly loaded
    		
	    		if( minP==null || (minP.getWorkload()+u)>RMS_max_work)// open new bin
	    		{
	    			nextP.opened=true;
	    			minP=nextP;
	    		//	System.out.println("work   "+minP.getWorkload()+ "  nextP alloted  "+nextP.getId());
	    			
	    		}
	    			
	    		t.setBackupProcessor(minP);
	    		backup_task = t.cloneTask_MWFD_RMS_EEPS();
	    		backup_task.setPrimary(false);  //setup backup processor
	    		backup_task.setFrequency(1);
	    		backup_task.setBackupProcessor(minP);
	    		backup_task.setPrimaryProcessor(t.getP());
	    		
	    		minP.taskset.add(backup_task);
	    		//18-12-18 problem =total uti   0.7228> LLB_N  0.7205   slack   0.0
	    		minP.setWorkload(minP.getWorkload()+u);
	    	//	minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		backup_task.setP(minP);
	    		
	    		writer_allocation.write("\n"+minP.getId()+" "+minP.getWorkload()+" "+u+" "+t.getId()+" "
	    	    		+t.getWcet()+" "+t.getPeriod());	
	    			   
	    	}
	        
	    	for(Processor pMin : freeProcList)
	    	{
	    		
	    		writer_allocation.write("\nprocessor   "+pMin.getId()+"\t frequency   "+fq
	    				+ " schedulability "+schedule.worstCaseResp_TDA_RMS_multi(pMin.taskset)+"\n");
	    	    
	    	}
	    	
	     writer_allocation.write("\nProc workload TASK U WCET PERIOD freq IS_PRIMARY BACKUP_PR PRIMARY_PR\n");
	    				
	    	for(Processor pMin : freeProcList)
	    	{
	       		//writer_allocation.write("\n\nprocessor   "+pMin.getId()+"\t frequency   "+fq+"\n");
	    		for(ITask t : pMin.taskset)
	    			
	        	{
	    			
	    			writer_allocation.write(pMin.getId()+" "+pMin.getWorkload()+" "+t.getId()+" "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
	    			+" "+t.getWCET_orginal()+" "+t.getPeriod()+" "+t.getFrequency()+" "+
	    			" "+t.isPrimary()+	" "+t.getBackupProcessor().getId()+" "+t.getPrimaryProcessor().getId()+"\n");
	    				
//	    		System.out.println("task   "+t.getId()+"  u  "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
//	    			+"   primary  "+t.isPrimary()+"  Proc   "+t.getP().getId()+	"   backup p  "+t.getBackupProcessor().getId()+
//	    			"   primary  "+t.getPrimaryProcessor().getId());
//	       
	        	}
	    		}
	 	writer_allocation.close();
	
	}
	
	/*public  void allocation_Prioritywise (ArrayList<ITask> taskset,   ArrayList<Processor> freeProcList ,  String filename) throws IOException
	{

		  Writer writer_allocation = new FileWriter(filename+"Prioritywise .txt");
		 DecimalFormat twoDecimals = new DecimalFormat("#.##");  // upto 1 decimal points
		    DecimalFormat fourDecimals = new DecimalFormat("#.###");
		    SchedulabilityCheck schedule = new SchedulabilityCheck();
		    double fq=1;
		    double max_U= SystemMetric.utilisation(taskset)/freeProcList.size();
		    System.out.println("max_U  "+max_U);
		    
		    writer_allocation.write("Proc TASK U WCET PERIOD IS_PRIMARY BACKUP_PR PRIMARY_PR");
		     
		 // SORT IN increasing ORDER OF priority
		    Comparator<ITask> c = new Comparator<ITask>() {
		    	 public int compare(ITask p1, ITask p2) {
		    		 int cmp;
		    		 cmp= (int)(p1.getPriority()-p2.getPriority());	

					if (cmp==0)														
						cmp= (int)(p1.getId()-p2.getId());	
		    		 return cmp;
				}
			  };
			taskset.sort(c);
			
			for(ITask t : taskset)
	    	{
			System.out.println("task  "+t.getId()+" p "+t.getPriority()+" u "+ (Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))) );
	    	}
	   	  
			// WHILE ALL PROCESSORS HAVE SCHEDULABLE TASKSETS ON GIVEN FREQUENCIES 
		//	do{      
	     	
	     		for(Processor pMin : freeProcList)
	     		{
	         		
	     			pMin.taskset.clear();
	     			pMin.setWorkload(0);
	         	//	//System.out.println("processor   "+pMin.getId()+"   size  "+pMin.taskset.size()+"  w  "+pMin.getWorkload());
	     		}    		
	     	
	   	
	    	
		//ALLOCATION OF PRIMARIES
	    	
	   		writer_allocation.write("\nPRIMARY ");
	    	
	    	for(ITask t : taskset)
	    	{
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline()))),
	    				work=1;
	    		Processor minP=null;
	    	System.out.println(" u  "+u);
	    		
	    	for(Processor pMin : freeProcList)
	    	{

	    		//System.out.println("  pMin  "+pMin.getId()+ "  load  "+pMin.getWorkload());
	    		if(pMin.getWorkload()<max_U && u<max_U)
	    		{
	    			if((pMin.getWorkload()+u) >(max_U+(.10*max_U)) )//max_U+(.10*max_U) affects partitioning
	    			{
	    				System.out.println("  minP  "+pMin.getId()+" t "+t.getId()+ 
	    						" u "+u+"  load  "+pMin.getWorkload()+
	    						" max_U+(.05*max_U) "+(max_U+(.05*max_U)));
	    				continue;
	    			}
	    			else
	    			{
	    				minP = pMin;

	    				break;
	    			}
	    		}
	    	}
	    		
	    		if(u>max_U || minP==null)
	    		{
	    			for(Processor pMin : freeProcList)
	    			{

	    				if(work >pMin.getWorkload())
	    				{
	    					work=Double.valueOf(twoDecimals.format(pMin.getWorkload()));
	    					minP = pMin;
	    				//	System.out.println("  pMin  "+pMin.getId()+ "  load  "+pMin.getWorkload());


	    				}
	    			}
	    		}
	    		
	    		System.out.println("  minP  "+minP.getId()+" t "+t.getId()+ 
	    				" wcet "+t.getWcet()+"  load  "+minP.getWorkload());
	    		t.setPrimary(true);
	    		t.setFrequency(fq);
	    		minP.taskset.add(t);
	    		minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		t.setP(minP);
	    		t.setPrimaryProcessor(minP);
	         	writer_allocation.write("\n"+minP.getId()+" "+t.getId()+" "+u+" "+t.getWcet()+" "+t.getPeriod());
	         	System.out.println(" after minP  "+minP.getId()+" t "+t.getId()+ 
	    				" wcet "+t.getWcet()+" work load  "+minP.getWorkload());
	    	}
	    
	    	// for processors having no load, in case of lesser U and large Proc
	    	Iterator<Processor> itrP =  freeProcList.iterator();
	    	while (itrP.hasNext())
	    	
  		{
	    		if(itrP.next().getWorkload()==0)
	    			itrP.remove();
  		}
	    	
	    	System.out.println("  freeProcList size "+freeProcList.size());
	    	//ALLOCATION OF BACKUPS
	    	writer_allocation.write("\nBACKUPS ");
	    	// SORT IN DECREASING ORDER OF UTILIZATION FOR MFWD wcet_original
			
	    			Comparator<ITask> c1 = new Comparator<ITask>() {
	    		    	 public int compare(ITask p1, ITask p2) {
	    		    		 int cmp;
	    			//	//System.out.println("t1 "+p1.getId()+"  u1 "+Double.valueOf(fourDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))));
	    			//	//System.out.println("t2 "+p2.getId()+"  u2 "+Double.valueOf(fourDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))));

	    		    		 double temp =  ( (Double.valueOf(twoDecimals.format(((double)p2.getWCET_orginal()/(double)p2.getDeadline()))))
	    							-(Double.valueOf(twoDecimals.format(((double)p1.getWCET_orginal()/(double)p1.getDeadline()))))); // backup????? wcet uti??
	    				//	//System.out.println("temp   "+temp);
	    					if(temp>0)
	    						cmp = 1;
	    					else
	    						cmp=-1;
	    					
	    					 if(temp==0)
	    							cmp=0;
	    					//	//System.out.println(" cmp  "+cmp);
	    						if (cmp==0)														
	    							cmp= (int)(p1.getId()-p2.getId());
	    				//	//System.out.println(" cmp  "+cmp);
	    		    		 return cmp;
	    				}
	    			  };
	    			taskset.sort(c1);
	    	
	    			for(ITask t : taskset)
	    			{
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWCET_orginal()/(double)t.getD()))), 
	    				work=freeProcList.size()/2;
	    		ITask backup_task;
	    		Processor minP=null;
	  //  		//System.out.println("t  "+t.getId()+" u backup  "+u);
	    		for(Processor pMin : freeProcList)
	    		{
	    			if (pMin == t.getP())   // IF PRIMARY PROCESSOR CONTAINS THE TASK, ALLOCATE BACKUP ON SOME OTHER PROCESSOR
	    				continue;
	    			if(work >pMin.getWorkload())
	    			{
	    				work=Double.valueOf(twoDecimals.format(pMin.getWorkload()));
	    				minP = pMin;
	    			}
	    //			//System.out.println("work   "+work+"  minP  "+minP.getId()+ "  pMin  "+pMin.getId());
	        		
	    		}
	    		t.setBackupProcessor(minP);
	    		backup_task = t.cloneTask_MWFD_RMS_EEPS();
	    		backup_task.setPrimary(false);  //setup backup processor
	    		backup_task.setFrequency(1);
	    		backup_task.setBackupProcessor(minP);
	    		backup_task.setPrimaryProcessor(t.getP());
	    		
	    		minP.taskset.add(backup_task);
	    		minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		backup_task.setP(minP);
	    		
	   		writer_allocation.write("\n"+minP.getId()+" "+t.getId()+" "+u+" "+t.getWcet()+" "+t.getPeriod());
	     	   
	    	}
	        
	    	for(Processor pMin : freeProcList)
	    	{
	    		
	    		writer_allocation.write("\nprocessor   "+pMin.getId()+"\t frequency   "+fq
	    				+ " schedulability "+schedule.worstCaseResp_TDA_RMS_multi(pMin.taskset)+"\n");
	    	    
	    	}
	    	
	    	for(Processor pMin : freeProcList)
	    	{
	       		writer_allocation.write("\n\nprocessor   "+pMin.getId()+"\t frequency   "+fq+"\n");
	    		for(ITask t : pMin.taskset)
	    			
	        	{
	    			
	    			writer_allocation.write(pMin.getId()+" "+t.getId()+" "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
	    			+" "+t.getWCET_orginal()+" "+t.getPeriod()+" "+t.getFrequency()+" "+
	    			" "+t.isPrimary()+	" "+t.getBackupProcessor().getId()+" "+t.getPrimaryProcessor().getId()+"\n");
	    			
//	    		System.out.println("task   "+t.getId()+"  u  "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
//	    			+"   primary  "+t.isPrimary()+"  Proc   "+t.getP().getId()+	"   backup p  "+t.getBackupProcessor().getId()+
//	    			"   primary  "+t.getPrimaryProcessor().getId());
//	       
	        	}
	    		}
	    	writer_allocation.close();
	
	}
	*/
	
	public  void allocation_Prioritywise (ArrayList<ITask> taskset,   ArrayList<Processor> freeProcList ,  String filename) throws IOException
	{
		  Writer writer_allocation = new FileWriter(filename+"Prioritywise .txt");
		 DecimalFormat twoDecimals = new DecimalFormat("#.##");  // upto 1 decimal points
		    DecimalFormat fourDecimals = new DecimalFormat("#.###");
		    SchedulabilityCheck schedule = new SchedulabilityCheck();
		    double fq=1;
		    double max_U= SystemMetric.utilisation(taskset)/freeProcList.size();
		//    System.out.println("max_U  "+max_U);
		    
		    writer_allocation.write("Proc TASK U WCET PERIOD IS_PRIMARY BACKUP_PR PRIMARY_PR");
		     
		 // SORT IN increasing ORDER OF priority
		    Comparator<ITask> c = new Comparator<ITask>() {
		    	 public int compare(ITask p1, ITask p2) {
		    		 int cmp;
		    		 cmp= (int)(p1.getPriority()-p2.getPriority());	

					if (cmp==0)														
						cmp= (int)(p1.getId()-p2.getId());	
		    		 return cmp;
				}
			  };
			taskset.sort(c);
			
		/*	for(ITask t : taskset)
	    	{
			System.out.println("task  "+t.getId()+" p "+t.getPriority()+" u "+ (Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))) );
	    	}
	   	 */ 
			// WHILE ALL PROCESSORS HAVE SCHEDULABLE TASKSETS ON GIVEN FREQUENCIES 
		//	do{      
	     	
	     		for(Processor pMin : freeProcList)
	     		{
	         		
	     			pMin.taskset.clear();
	     			pMin.setWorkload(0);
	         	//	//System.out.println("processor   "+pMin.getId()+"   size  "+pMin.taskset.size()+"  w  "+pMin.getWorkload());
	     		}    		
	     	
	   	
	    	
		//ALLOCATION OF PRIMARIES
	    	
	   		writer_allocation.write("\nPRIMARY Prioritywise");
	    	
	    	for(ITask t : taskset)
	    	{
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline()))),
	    				work=0.7;
	    		Processor minP=null;
	    	//System.out.println(" u  "+u);
	    		
	    	for(Processor pMin : freeProcList)
	    	{

	    		//System.out.println("  pMin  "+pMin.getId()+ "  load  "+pMin.getWorkload());
	    		if(pMin.getWorkload()<max_U && u<max_U)
	    		{
	    			if((pMin.getWorkload()+u) >(max_U+(.10*max_U)) )//max_U+(.10*max_U) affects partitioning
	    			{
	    		/*		System.out.println("  minP  "+pMin.getId()+" t "+t.getId()+ 
	    						" u "+u+"  load  "+pMin.getWorkload()+
	    						" max_U+(.05*max_U) "+(max_U+(.05*max_U)));
	    		*/		continue;
	    			}
	    			else
	    			{
	    				minP = pMin;

	    				break;
	    			}
	    		}
	    	}
	    		
	    		if(u>max_U || minP==null)
	    		{
	    			for(Processor pMin : freeProcList)
	    			{

	    				if(work >pMin.getWorkload())
	    				{
	    					work=Double.valueOf(twoDecimals.format(pMin.getWorkload()));
	    					minP = pMin;
	    				//	System.out.println("  pMin  "+pMin.getId()+ "  load  "+pMin.getWorkload());


	    				}
	    			}
	    		}
	    		
	    	/*	System.out.println("  minP  "+minP.getId()+" t "+t.getId()+ 
	    				" wcet "+t.getWcet()+"  load  "+minP.getWorkload());
	    */		t.setPrimary(true);
	    		t.setFrequency(fq);
	    		minP.taskset.add(t);
	    		minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		t.setP(minP);
	    		t.setPrimaryProcessor(minP);
	       	writer_allocation.write("\n"+minP.getId()+" "+t.getId()+" "+u+" "+t.getWcet()+" "+t.getPeriod());
	         	/*System.out.println(" after minP  "+minP.getId()+" t "+t.getId()+ 
	    				" wcet "+t.getWcet()+" work load  "+minP.getWorkload());*/
	    	}
	    
	    	// for processors having no load, in case of lesser U and large Proc
	    	Iterator<Processor> itrP =  freeProcList.iterator();
	    	while (itrP.hasNext())
	    	
    		{
	    		if(itrP.next().getWorkload()==0)
	    			itrP.remove();
    		}
	    	
	  //  	System.out.println("  freeProcList size "+freeProcList.size());
	    	//ALLOCATION OF BACKUPS
	    	writer_allocation.write("\nBACKUPS ");
	    	// SORT IN DECREASING ORDER OF UTILIZATION FOR MFWD wcet_original
			
	    			Comparator<ITask> c1 = new Comparator<ITask>() {
	    		    	 public int compare(ITask p1, ITask p2) {
	    		    		 int cmp;
	    			//	//System.out.println("t1 "+p1.getId()+"  u1 "+Double.valueOf(fourDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))));
	    			//	//System.out.println("t2 "+p2.getId()+"  u2 "+Double.valueOf(fourDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))));

	    		    		 double temp =  ( (Double.valueOf(twoDecimals.format(((double)p2.getWCET_orginal()/(double)p2.getDeadline()))))
	    							-(Double.valueOf(twoDecimals.format(((double)p1.getWCET_orginal()/(double)p1.getDeadline()))))); // backup????? wcet uti??
	    				//	//System.out.println("temp   "+temp);
	    					if(temp>0)
	    						cmp = 1;
	    					else
	    						cmp=-1;
	    					
	    					 if(temp==0)
	    							cmp=0;
	    					//	//System.out.println(" cmp  "+cmp);
	    						if (cmp==0)														
	    							cmp= (int)(p1.getId()-p2.getId());
	    				//	//System.out.println(" cmp  "+cmp);
	    		    		 return cmp;
	    				}
	    			  };
	    			taskset.sort(c1);
	    	
	    			for(ITask t : taskset)
	    			{
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWCET_orginal()/(double)t.getD()))), 
	    				work=0.7,rmsUB=0.7;//18-12-18//freeProcList.size()/2;
	    		ITask backup_task;
	    		Processor minP=null;
	  //  		//System.out.println("t  "+t.getId()+" u backup  "+u);
	    		for(Processor pMin : freeProcList)
	    		{
	    			//19-12-18 ||  (pMin.getWorkload()+u>rmsUB) if U(Pr)=0.68 case
	    			if (pMin == t.getP()  ||  (pMin.getWorkload()+u>rmsUB) )   // IF PRIMARY PROCESSOR CONTAINS THE TASK, ALLOCATE BACKUP ON SOME OTHER PROCESSOR
	    				continue;
	    			if(work >pMin.getWorkload())
	    			{
	    				work=Double.valueOf(twoDecimals.format(pMin.getWorkload()));
	    				minP = pMin;
	    			}
	    //			//System.out.println("work   "+work+"  minP  "+minP.getId()+ "  pMin  "+pMin.getId());
	        		
	    		}
	    		t.setBackupProcessor(minP);
	    		backup_task = t.cloneTask_MWFD_RMS_EEPS();
	    		backup_task.setPrimary(false);  //setup backup processor
	    		backup_task.setFrequency(1);
	    		backup_task.setBackupProcessor(minP);
	    		backup_task.setPrimaryProcessor(t.getP());
	    		
	    		minP.taskset.add(backup_task);
	    		minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		backup_task.setP(minP);
	    		
	   		writer_allocation.write("\n"+minP.getId()+" "+t.getId()+" "+u+" "+t.getWcet()+" "+t.getPeriod());
	     	   
	    	}
	        
	    	for(Processor pMin : freeProcList)
	    	{
	    		
	    		writer_allocation.write("\nprocessor   "+pMin.getId()+"\t frequency   "+fq
	    				+ " schedulability "+schedule.worstCaseResp_TDA_RMS_multi(pMin.taskset)+"\n");
	    	    
	    	}
	    	
	    	for(Processor pMin : freeProcList)
	    	{
	      		writer_allocation.write("\n\nprocessor   "+pMin.getId()+"\t frequency   "+fq+"\n");
	    		for(ITask t : pMin.taskset)
	    			
	        	{
	    			
	    			writer_allocation.write(pMin.getId()+" "+t.getId()+" "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
	    			+" "+t.getWCET_orginal()+" "+t.getPeriod()+" "+t.getFrequency()+" "+
	    			" "+t.isPrimary()+	" "+t.getBackupProcessor().getId()+" "+t.getPrimaryProcessor().getId()+"\n");
	    			
//	    		System.out.println("task   "+t.getId()+"  u  "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
//	    			+"   primary  "+t.isPrimary()+"  Proc   "+t.getP().getId()+	"   backup p  "+t.getBackupProcessor().getId()+
//	    			"   primary  "+t.getPrimaryProcessor().getId());
//	       
	        	}
	    		}
	   	writer_allocation.close();
	}
	
	
	
	
	/**
	 * @param taskset
	 * @param freeProcList
	 * @param filename
	 * @throws IOException
	 */
	/*public  void allocation_Priority (ArrayList<ITask> taskset,   ArrayList<Processor> freeProcList, String filename ) throws IOException
	{
		 Writer writer_allocation = new FileWriter(filename+"Priority.txt");
		 DecimalFormat twoDecimals = new DecimalFormat("#.##");  // upto 1 decimal points
		    DecimalFormat fourDecimals = new DecimalFormat("#.###");
		    SchedulabilityCheck schedule = new SchedulabilityCheck();
		    double fq=1;
		    writer_allocation.write("Proc TASK U WCET PERIOD IS_PRIMARY BACKUP_PR PRIMARY_PR");
			
		 // SORT IN INCREASING ORDER OF PRIORITY 
			
			Comparator<ITask> c = new Comparator<ITask>() {
		    	 public int compare(ITask p1, ITask p2) {
		    		 int cmp;
		    		 cmp= (int)(p1.getPriority()-p2.getPriority());	

					if (cmp==0)														
						cmp= (int)(p1.getId()-p2.getId());	
		    		 return cmp;
				}
			  };
			taskset.sort(c);
			
			for(ITask t : taskset)
	    	{
			System.out.println("task  "+t.getId()+  " priority "+t.getPriority()+" u "+ (Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))) );
	    	}
	   	  
			   
	     	
	     		for(Processor pMin : freeProcList)
	     		{
	         		
	     			pMin.taskset.clear();
	     			pMin.setWorkload(0);
	         	//	//System.out.println("processor   "+pMin.getId()+"   size  "+pMin.taskset.size()+"  w  "+pMin.getWorkload());
	     		}    		
	     	
	   	
	    	
		//ALLOCATION OF PRIMARIES
	    	
	  		writer_allocation.write("\nPRIMARY ");
	    	int taskCount=1, procCount=1, round=1;
	    	for(ITask t : taskset)
	    	{
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline()))), work=1;
	    		Processor minP=null;
	    	//	//System.out.println(" u  "+u);
	    		
	    		if(Math.floorMod(round, 2)!=0)
	    		{
	    			procCount= Math.floorMod(taskCount, freeProcList.size())-1;
	    		
	    		if(procCount<0)
	    		procCount=freeProcList.size()-1;
	    		}
	    		else
	    		{
	    			procCount= (Math.floorMod(taskCount, freeProcList.size())-freeProcList.size())*(-1);
		    		
		    		if(procCount==freeProcList.size())
		    		procCount=0;
	    		}
	    		System.out.println("  minP  "+freeProcList.get(procCount).getId()+" taskCount "+taskCount +" freeProcList.size() "+freeProcList.size()+" procCount "+procCount);
	    		
	    		if(Math.floorMod(taskCount, freeProcList.size())==0)
	    		round++;
	    		
	    		taskCount++;
	    		minP= freeProcList.get(procCount);
	    		t.setPrimary(true);
	    		t.setFrequency(fq);
	    		minP.taskset.add(t);
	    		minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		t.setP(minP);
	    		t.setPrimaryProcessor(minP);
	         	System.out.println(" p "+minP.getId()+" t "+t.getId()+" u "+u+"  wcet "+t.getWcet()+" p "+t.getPeriod());
	       	writer_allocation.write("\n"+minP.getId()+" "+t.getId()+" "+u+" "+t.getWcet()+" "+t.getPeriod());
	            
	    	}
	    
	    	
	    	
	    	//ALLOCATION OF BACKUPS
	   	writer_allocation.write("\nBACKUPS ");
	    	// SORT IN DECREASING ORDER OF UTILIZATION FOR MFWD wcet_original
			
	    	taskCount=1;
	    	round=1;
	    	for(ITask t : taskset)
	    	{
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWCET_orginal()/(double)t.getD()))), work=1;
	    		ITask backup_task;
	    		Processor minP=null;
	    		
	    		System.out.println("t  "+t.getId()+" u backup  "+u);
	    		for(Processor pMin : freeProcList)
	    		{
	    			if (pMin == t.getP())   // IF PRIMARY PROCESSOR CONTAINS THE TASK, ALLOCATE BACKUP ON SOME OTHER PROCESSOR
	    				continue;
	    			if(work >pMin.getWorkload())
	    			{
	    				work=Double.valueOf(twoDecimals.format(pMin.getWorkload()));
	    				minP = pMin;
	    			}
	    //			//System.out.println("work   "+work+"  minP  "+minP.getId()+ "  pMin  "+pMin.getId());
	        		
	    		}
	    		
	    		
	    		if(Math.floorMod(round, 2)==0)
	    		{
	    			procCount= Math.floorMod(taskCount, freeProcList.size())-1;
	    		
	    		if(procCount<0)
	    		procCount=freeProcList.size()-1;
	    		}
	    		else
	    		{
	    			procCount= (Math.floorMod(taskCount, freeProcList.size())-freeProcList.size())*(-1);
		    		
		    		if(procCount==freeProcList.size())
		    		procCount=0;
	    		}
	    		
	    		procCount= (Math.floorMod(taskCount, freeProcList.size())-freeProcList.size())*(-1);
	    		
	    		if(procCount==freeProcList.size())
	    		procCount=0;
	    	
	    		System.out.println(" backup minP  "+freeProcList.get(procCount).getId()+" taskCount "+taskCount +" freeProcList.size() "+freeProcList.size()+" procCount "+procCount);
	    	
	    		if(Math.floorMod(taskCount, freeProcList.size())==0)
	    		round++;
	    		taskCount++;
	    		minP= freeProcList.get(procCount);
	    		t.setBackupProcessor(minP);
	    		backup_task = t.cloneTask_MWFD_RMS_EEPS();
	    		backup_task.setPrimary(false);  //setup backup processor
	    		backup_task.setFrequency(1);
	    		backup_task.setBackupProcessor(minP);
	    		backup_task.setPrimaryProcessor(t.getP());
	    		
	    		minP.taskset.add(backup_task);
	    		minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		backup_task.setP(minP);
	    	 	System.out.println(" back p "+minP.getId()+" t "+t.getId()+" u "+u+"  wcet "+t.getWcet()+" p "+t.getPeriod());
	        		
	   writer_allocation.write("\n"+minP.getId()+" "+t.getId()+" "+u+" "+t.getWcet()+" "+t.getPeriod());
	     	   
	    	}
	    	

	    	for(Processor pMin : freeProcList)
	    	{
	       		writer_allocation.write("\n\nprocessor   "+pMin.getId()+"\t frequency   "+fq+"\n");
	    		for(ITask t : pMin.taskset)
	    			
	        	{
	    			writer_allocation.write(pMin.getId()+" "+t.getId()+" "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
	    			+" "+t.getWCET_orginal()+" "+t.getPeriod()+" "+t.getFrequency()+" "+
	    			" "+t.isPrimary()+	" "+t.getBackupProcessor().getId()+" "+t.getPrimaryProcessor().getId()+"\n");
	    			
//	    		System.out.println("task   "+t.getId()+"  u  "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
//	    			+"   primary  "+t.isPrimary()+"  Proc   "+t.getP().getId()+	"   backup p  "+t.getBackupProcessor().getId()+
//	    			"   primary  "+t.getPrimaryProcessor().getId());
//	       
	        	}
	    		}
	    	writer_allocation.close();
	    	
	       
	    	
			
	    	///END ALLOCATION
		    
	}*/
	
	/*public  void allocation_Back_Prio (ArrayList<ITask> taskset,   ArrayList<Processor> freeProcList , String filename) throws IOException
	{
		 Writer writer_allocation = new FileWriter(filename+"_Back_Prio.txt");
		 DecimalFormat twoDecimals = new DecimalFormat("#.##");  // upto 1 decimal points
		    DecimalFormat fourDecimals = new DecimalFormat("#.###");
		    SchedulabilityCheck schedule = new SchedulabilityCheck();
		    double fq=1;
		       writer_allocation.write("Proc TASK U WCET PERIOD IS_PRIMARY BACKUP_PR PRIMARY_PR");
			
		 // SORT IN DECREASING ORDER OF UTILIZATION FOR MFWD wcet according to frequency
			
			Comparator<ITask> c = new Comparator<ITask>() {
		    	 public int compare(ITask p1, ITask p2) {
		    		 int cmp;
		    	//	 cmp= (int)(p1.getPriority()-p2.getPriority());	
		    		 double temp =  ( (Double.valueOf(twoDecimals.format(((double)p2.getWcet()/(double)p2.getDeadline()))))
							-(Double.valueOf(twoDecimals.format(((double)p1.getWcet()/(double)p1.getDeadline()))))); // backup????? wcet uti??
			//	System.out.println("temp   "+temp);
					if(temp>0)
						cmp = 1;
					else
						cmp=-1;
				 if(temp==0)
						cmp=0;
				//	//System.out.println(" cmp  "+cmp);
					if (cmp==0)														
						cmp= (int)(p1.getId()-p2.getId());	
		    		 return cmp;
				}
			  };
			taskset.sort(c);
			System.out.println("SORT IN DECREASING ORDER OF UTILIZATION " );
			for(ITask t : taskset)
	    	{
			System.out.println("task  "+t.getId()+  " priority "+t.getPriority()+" u "+ (Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))) );
	    	}
	   	  
			// WHILE ALL PROCESSORS HAVE SCHEDULABLE TASKSETS ON GIVEN FREQUENCIES 
		//	do{      
	     	
	     		for(Processor pMin : freeProcList)
	     		{
	         		
	     			pMin.taskset.clear();
	     			pMin.setWorkload(0);
	         	//	//System.out.println("processor   "+pMin.getId()+"   size  "+pMin.taskset.size()+"  w  "+pMin.getWorkload());
	     		}    		
	     	
	   	
	    	
		//ALLOCATION OF PRIMARIES
	    	
	  		writer_allocation.write("\nPRIMARY ");
	    	System.out.println("ALLOCATION OF PRIMARIES");
	    	for(ITask t : taskset)
	    	{
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline()))), work=1;
	    		Processor minP=null;
	    	//	//System.out.println(" u  "+u);
	    		
	    		for(Processor pMin : freeProcList)
	    		{
	    			if(work >pMin.getWorkload())
	    			{
	    				work=Double.valueOf(twoDecimals.format(pMin.getWorkload()));
	    				minP = pMin;
	    			}
	    		//	//System.out.println("work   "+work+"  minP  "+minP.getId()+ "  pMin  "+pMin.getId());
	        		
	    		}
	    		
	    		
	    		t.setPrimary(true);
	    		t.setFrequency(fq);
	    		minP.taskset.add(t);
	    		minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		t.setP(minP);
	    		t.setPrimaryProcessor(minP);
	         	System.out.println(" p "+minP.getId()+" t "+t.getId()+" u "+u+"  wcet "+t.getWcet()+" p "+t.getPeriod());
	     	writer_allocation.write("\n"+minP.getId()+" "+t.getId()+" "+u+" "+t.getWcet()+" "+t.getPeriod());
	            
	    	}
	    
	    	
	    	
	    	//ALLOCATION OF BACKUPS
	   	writer_allocation.write("\nBACKUPS ");
	    	// SORT IN INCREASING ORDER OF PRIORITY
	    	Comparator<ITask> cBackup = new Comparator<ITask>() {
		    	 public int compare(ITask p1, ITask p2) {
		    		 int cmp;
		    		 cmp= (int)(p1.getPriority()-p2.getPriority());	

					if (cmp==0)														
						cmp= (int)(p1.getId()-p2.getId());	
		    		 return cmp;
				}
			  };
			  
			taskset.sort(cBackup);
			for(ITask t : taskset)
	    	{
				System.out.println(" sorted backup t "+t.getId());
	    	}
	    	
	    	for(ITask t : taskset)
	    	{
	    		boolean alloc = false;
	    		double u = Double.valueOf(twoDecimals.format(((double)t.getWCET_orginal()/(double)t.getD()))), work=1;
	    		ITask backup_task;
	    		Processor minP=null;
	    		freeProcList.sort(new Comparator<Processor>() {
		    	 public int compare(Processor p1, Processor p2) {
		    		 int cmp;
		    		 double temp = (double)(p1.getWorkload()-p2.getWorkload());	
		    		if(temp>0)
		    			cmp=1;
		    		else
		    			cmp=-1;
		    		if(temp==0)
		    			cmp=0;
		    		
		    	//	 System.out.println(" cmp "+cmp+ " p1 "+p1.getWorkload()+ " p2 "+p2.getWorkload());
		    		  double sum1=0, sum2=0;
		    		 
		    		 for(ITask tsum : p1.taskset)
		    		 {
		    			 sum1+=tsum.getPriority();
		    		 }
		    		 for(ITask tsum : p2.taskset)
		    		 {
		    			 sum2+=tsum.getPriority();
		    		 }
		    	//	 System.out.println(" cmp "+cmp+ " sum1 "+sum1+ " sum2 "+sum2);
				    	
					if (cmp==0)														
						cmp= (int)(sum1-sum2);	
		    		 return cmp;
				}
			  });
	    		
	    		for(Processor pr : freeProcList)
		    	{
					System.out.println(" sorted backup proc  "+pr.getId()+" workload "+pr.getWorkload());
		    	}
	    		
	    		System.out.println("allocate t  "+t.getId()+" u backup  "+u);
	    		
	    		for(Processor pMin : freeProcList)
	    		{
	    			pMin.taskset.sort(new Comparator<ITask>() {
		    	 public int compare(ITask p1, ITask p2) {
		    		 int cmp;
		    		 cmp= (int)(p1.getPriority()-p2.getPriority());
		    		 return cmp;
					}
				  });	
	    		}
	    		
	    		for(Processor pMin : freeProcList)
	    		{
	    			System.out.println(" alloc started p "+pMin.getId());
	    			if(t.getP()==pMin)
	    				continue;
	    			else
	    			{
	    				for(ITask t_j : pMin.taskset)
	    				{
	    					if(t_j.getPriority()>t.getPriority())
	    					{
	    					
	    						minP=pMin;
	    						alloc=true;
	    						System.out.println("t_j.getPriority() "+t_j.getPriority()
	    						+"  minP "+minP.getId()+"  alloc "+ alloc);
	    					}
	    					else
	    						break;
	    				}
	    				if(alloc)
	    					break;
	    					
	    			}
	    			
	    		}
	    		//allocate the minimum load processor
	    		if(!alloc)
	    		{
	    			for(Processor pMin : freeProcList)
		    		{
		    			if(t.getP()==pMin)
		    				continue;
		    			else
		    			{
		    				minP=pMin;
		    				System.out.println("allocate the minimum load processor "+minP.getId());
		    			}
		    			
		    			}
	    		}
	    			
	    		
	    		t.setBackupProcessor(minP);
	    		backup_task = t.cloneTask_MWFD_RMS_EEPS();
	    		backup_task.setPrimary(false);  //setup backup processor
	    		backup_task.setFrequency(1);
	    		backup_task.setBackupProcessor(minP);
	    		backup_task.setPrimaryProcessor(t.getP());
	    		
	    		minP.taskset.add(backup_task);
	    		minP.setWorkload(Double.valueOf(twoDecimals.format(minP.getWorkload()+u)));
	    		backup_task.setP(minP);
	    	 	System.out.println(" back p "+minP.getId()+" t "+t.getId()+" u "+u+"  wcet "+t.getWcet()+" p "+t.getPeriod());
	        		
	   writer_allocation.write("\n"+minP.getId()+" "+t.getId()+" "+u+" "+t.getWcet()+" "+t.getPeriod());
	     	   
	    	}
	    	

	    	for(Processor pMin : freeProcList)
	    	{
	       		writer_allocation.write("\n\nprocessor   "+pMin.getId()+"\t frequency   "+fq+"\n");
	    		for(ITask t : pMin.taskset)
	    			
	        	{
	    			writer_allocation.write(pMin.getId()+" "+t.getId()+" "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
	    			+" "+t.getWCET_orginal()+" "+t.getPeriod()+" "+t.getFrequency()+" "+
	    			" "+t.isPrimary()+	" "+t.getBackupProcessor().getId()+" "+t.getPrimaryProcessor().getId()+"\n");
	    			
//	    		System.out.println("task   "+t.getId()+"  u  "+ Double.valueOf(twoDecimals.format(((double)t.getWcet()/(double)t.getDeadline())))
//	    			+"   primary  "+t.isPrimary()+"  Proc   "+t.getP().getId()+	"   backup p  "+t.getBackupProcessor().getId()+
//	    			"   primary  "+t.getPrimaryProcessor().getId());
//	       
	        	}
	    		}
	    	writer_allocation.close();
	    	
	       
	    	
			
	    	///END ALLOCATION
		    
	}*/

}
