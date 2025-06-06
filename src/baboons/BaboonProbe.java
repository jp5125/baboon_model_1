package baboons;

import observer.Probe;
import sim.util.Bag;
import sweep.SimStateSweep;

public class BaboonProbe extends Probe
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
	
	
	public void probeMaleReproductiveOutput(SimStateSweep state)
	{
		Bag allgroups = state.sparseSpace.getAllObjects();
		
		for (Object obj : allGroups)
		{
			if(!(obj instanceof Group group)) continue;
		}
	}
	

}
