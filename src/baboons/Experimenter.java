package baboons;

import observer.Observer;
import sim.engine.SimState;
import sim.util.Bag;
import sweep.CustomData;
import sweep.ParameterSweeper;
import sweep.SimStateSweep;
import java.util.*;

public class Experimenter extends Observer
{
	
	//fields for tracking Coalition gene Carriers (CC)
		public double ccPrime = 0;
		public double ccPostPrime = 0;
		public double ccSenescent = 0;
		public double ccN = 0; //number of dead coalition gene males
		
		//fields for tracking Non-Carriers of coalition gene (NG)
		public double ncPrime = 0;
		public double ncPostPrime = 0;
		public double ncSenescent = 0;
		public double ncN = 0; //number of dead non-coalition gene males
		
		double time = state.schedule.getTime();
		

	public Experimenter(String fileName, String folderName, SimStateSweep state, ParameterSweeper sweeper,
			String precision, String[] headers) 
	{
		super(fileName, folderName, state, sweeper, precision, headers);
	}
	
	public void resetVariables()
	{
		ccPrime = 0;
		ccPostPrime = 0;
		ccSenescent = 0;
		ccN = 0; 
		
		ncPrime = 0;
		ncPostPrime = 0;
		ncSenescent = 0;
		ncN = 0;
		
	}
	
	public void numberOfCoalitionMales(Environment state) 
	{
	    Bag groups = state.sparseSpace.getAllObjects();
	    int malesWithGene = 0;
	    int totalMales = 0;

	    for (int g = 0; g < groups.numObjs; g++) 
	    {
	        Group group = (Group) groups.objs[g];
	        Bag members = group.members;

	        for (int i = 0; i < members.numObjs; i++) 
	        {
	            Baboon baboon = (Baboon) members.objs[i];
	            if (baboon.isMale() && !baboon.isJuvenile) 
	            {
	                totalMales++;
	                if (baboon.hasCoalitionGene) 
	                {
	                    malesWithGene++;
	                } 
	            }
	        }
	    }
	    
	    int malesWithoutGene = totalMales - malesWithGene;

	    long interval = 1000;

	    // Update count chart (Chart 0)
	    //plots number of males with coalition gene
	    this.upDateTimeChart(0, time, malesWithGene, true, interval);
	    

	    // Update percentage chart (Chart 1)
	    if (totalMales > 0) 
	    {
	    	//percentage of males with the coalition gene
	        double pctWithGene = (malesWithGene * 100.0) / totalMales;
	        double pctWithoutGene = 100.0 - pctWithGene;
	        
	        //plot this percentage
	        this.upDateTimeChart(1, time, pctWithGene, true, interval);
	        
	    }
	}
	
	public void maleOffspringByStrategy(Baboon male)
	{
		if(!male.isMale() || male.isJuvenile) return;
		
		if(male.hasCoalitionGene)
		{
			ccPrime += male.primeOffspring;
			ccPostPrime += male.postPrimeOffspring;
			ccSenescent += male.senescentOffspring;
			ccN++;
		}
		else
		{
			ncPrime += male.primeOffspring;
			ncPostPrime += male.postPrimeOffspring;
			ncSenescent += male.senescentOffspring;
			ncN++;
		}
	}
	
	public void step(SimState state)
	{
		super.step(state);
		if(step % this.state.dataSamplingInterval == 0)
		{
			numberOfCoalitionMales((Environment) state);
			
			 double[] barValues = new double[] {
			            ccPrime,
			            ccPostPrime,
			            ccSenescent,
			            ncPrime,
			            ncPostPrime,
			            ncSenescent
			        };

			        this.upDateHistogramChart(1, barValues, step);
		}
	}

}
