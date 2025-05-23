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

	public Experimenter(String fileName, String folderName, SimStateSweep state, ParameterSweeper sweeper,
			String precision, String[] headers) {
		super(fileName, folderName, state, sweeper, precision, headers);
	}
	
	public void numberOfCoalitionMales(Environment state) {
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
	            if (baboon.isMale()) 
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

	    double time = state.schedule.getTime();
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
	
	public void step(SimState state)
	{
		super.step(state);
		if(step %this.state.dataSamplingInterval == 0)
		{
			numberOfCoalitionMales((Environment) state);
		}
	}

}
