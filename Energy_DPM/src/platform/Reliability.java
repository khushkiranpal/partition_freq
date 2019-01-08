package platform;

import java.util.ArrayList;

import taskGeneration.ITask;
import taskGeneration.SystemMetric;

public class Reliability {
	
	public double reliability_task (long wcet_org,double fMin, double freq, int d)
	{
		double lamda_0 = 0.000001 , exponent, rel, lamda_f;
		exponent = (d*(1-freq))/(1-fMin);
		lamda_f = lamda_0*Math.pow(10, exponent);// .000001 *10^(d(1-f)/(1-crit_freq))
		rel= Math.exp(-1*lamda_f*(wcet_org/freq));//e^(-lamda_f*c_i/f)
	//	System.out.println(" wcet "+wcet_org+" (wcet_org/freq) "+(wcet_org/freq) +" fMin "+fMin+" freq "+freq +" d "+d+"reliability_task  "+rel);
		return rel;
	}
	
	
	public double reliabilitySystem(ArrayList<ITask> taskset)
	{
		double sys_rel=1;
		
		for(ITask t : taskset)
		{
			sys_rel = sys_rel*t.getReliability();
		//	System.out.println("reliabilitySystem "+sys_rel);
		}
	return sys_rel;
	}
	
	
	//5-12-18
	public void setRelib_Mogdhaddas(ArrayList<ITask> taskset, double fMin, int d)
	{
		double reli_freq =0, freq=1, pof_freq;
	//	System.out.println("setRelib_Mogdhaddas  ");
		for(ITask t : taskset)
		{
			freq=1;
			do {
		//		System.out.println("t "+ t.getId()+" freq "+freq);
			double R_f = reliability_task(t.getWCET_orginal(), fMin,  freq,  d); //
			reli_freq= 1-Math.pow(1-R_f, 2);//1-(1-r(s))^2 eq-10 moghadas
			pof_freq = 1-reli_freq;
			freq-=0.1;
			}while(Math.sqrt(t.getPof_1())>pof_freq 
					&& freq>(Math.max(fMin, t.getWCET_orginal()/t.getPeriod())));//FREQ> MAX(FMIN,U)
		//	System.out.println("freq  "+freq+"  pof_freq  "+pof_freq +"  t.getPof_1()  "+t.getPof_1());
			freq+=0.2;// freq-0.1- due to freq-=0.1;. NEXT freq-0.1 DUE TO CONDITION	t.getPof_1())>pof_freq FAILURE	
		//	System.out.println("pof_freq  "+pof_freq +"  t.getPof_1()  "+t.getPof_1());
			if(freq>fMin)
			t.setReliableFreq(freq);
			else
			t.setReliableFreq(fMin);
		//	System.out.println("t "+ t.getId()+" freq "+freq+ " getReliableFreq "+t.getReliableFreq());
		}
		
	}
	public void setRelib_Mixed(ArrayList<ITask> taskset, ArrayList<Processor> freeProcList, double fMin,  int d)
	{
		double reli_freq =0;
		for(ITask t : taskset)
		{
			double freq =0,freq_B=0;
			Processor p ;
			p=t.getP();
			for (ITask t_prim : p.taskset)
			{
				if(t.getId()==t_prim.getId())
					freq=t_prim.getFrequency();
		/*		System.out.println("proc  "+p.getId()+"  t_prim "+t_prim.getId()
				+"  t  "+t.getId()+" freq  "+freq+ " t_prim.getFrequency() "+t_prim.getFrequency());
		*/	}
			double R_primary = reliability_task(t.getWCET_orginal(), fMin,  freq,  d); //
			p=t.getBackupProcessor();
			for (ITask t_back : p.taskset)
			{
				if(t.getId()==t_back.getId())
					freq_B=t_back.getFrequency();
		/*		System.out.println("proc  "+p.getId()+"  t_back "+t_back.getId()
				+"  t  "+t.getId()+" freq_B  "+freq_B+" t_back.getFrequency() "+t_back.getFrequency());
		*/	}
			double R_backup = reliability_task(t.getWCET_orginal(), fMin,  freq_B,  d); //
			
			reli_freq= 1-((1-R_primary)*(1-R_backup));//1-((1-r(s))*(1-r(s))) eq 9 haque 6.6
		//	System.out.println("reli_freq  "+reli_freq);
			t.setReliability(reli_freq);
					
		}
		
	}
	
	public void setRelib_original(ArrayList<ITask> taskset, double fMin, double freq, int d)
	{
		double reli_freq =0;
	//	System.out.println("setRelib_original ");
		for(ITask t : taskset)
		{
			
			double R_f = reliability_task(t.getWCET_orginal(), fMin,  1,  d); //
			reli_freq= R_f;// original reliability//////////1-r(s) eq-9 moghadas
			t.setReliability(reli_freq);
			t.setPof_1(1-t.getReliability());//5-12-18
	//		System.out.println("t "+t.getId()+" getReliability  "+t.getReliability()+"  getPof_1  "+t.getPof_1());
		}
		
	}
	
	//5-12-18 POF TASK LEVEL in hyperperiod
	public double POF_task (ITask task, long hyperperiod)
	{
		double pofTAsk=1;
		long noOfInstances = hyperperiod/task.getPeriod();
	     for(int i=0; i<noOfInstances; i++)
	     {
		pofTAsk*= task.getReliability();
	//	System.out.println("pofTAsk "+pofTAsk +" i "+i +" task "+task.getId()+"  Period "+task.getPeriod());
	     }
	 //    System.out.println("task.getReliability()  "+task.getReliability() +" noOfInstances  "+noOfInstances+"  pofTAsk  "+pofTAsk+"     1-pofTAsk  "+(1-pofTAsk ));
		return (pofTAsk); // eq 7 haque On reliability management 5.1
		
	}
	
	// 5-12-18 POF system
	public double POFSystem(ArrayList<ITask> taskset)
	{
		double sys_POF=1;
		long hyper =SystemMetric.hyperPeriod(taskset);
		for(ITask t : taskset)
		{
			sys_POF = sys_POF*POF_task(t,hyper);
	//		System.out.println("sys_POF  "+sys_POF +" t "+t.getId()+ " hyper "+hyper);
		}
	return (1-sys_POF);// eq 7 haque On reliability management 5.1
	}
	
	
}
