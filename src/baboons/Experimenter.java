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
	public static final int CHART_COUNTS = 0;
	public static final int CHART_PERCENTAGES = 1;
	
	public Experimenter(String fileName, String folderName, SimStateSweep state, ParameterSweeper sweeper,
			String precision, String[] headers) {
		super(fileName, folderName, state, sweeper, precision, headers);
	}
	
	public void numberOfCoalitionMales(Environment state) {
	    Bag groups = state.sparseSpace.getAllObjects();
	    int malesWithGene = 0;
	    int malesWithoutGene = 0;
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
	                else 
	                {
	                    malesWithoutGene++;
	                }
	            }
	        }
	    }

	    double time = state.schedule.getTime();
	    long interval = 1000;

	    // Update count chart (Chart 0)
	    //plots number of males with coalition gene
	    this.upDateTimeChart(CHART_COUNTS, time, malesWithGene, true, interval);

	    // Update percentage chart (Chart 1)
	    if (totalMales > 0) 
	    {
	    	//percentage of males with the coalition gene
	        double pctWithGene = (malesWithGene * 100.0) / totalMales;
	        
	        //plot this percentage
	        this.upDateTimeChart(CHART_PERCENTAGES, time, pctWithGene, true, interval);
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
