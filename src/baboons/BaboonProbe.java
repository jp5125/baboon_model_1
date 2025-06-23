package baboons;

import observer.Probe;
import sim.util.Bag;
import sweep.SimStateSweep;
import sim.engine.*;

public class BaboonProbe extends Probe implements Steppable
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
	
	//access to environment variables
	Environment env = (Environment) state;
	Bag deadMales = env.deadMales;
	
	//resets probe counts, only run during simulation initialization in the environment
	public void reset() 
	{
		ccPrime = ccPostPrime = ccSenescent = ccN = 0;
		ncPrime = ncPostPrime = ncSenescent = ncN = 0;
	}
	
	public void probeMaleReproductiveOutput(SimStateSweep state)
	{
		for(Object obj: Environment.deadMales)
		{
			if(!(obj instanceof Baboon male)) continue;
			
			if(!male.isMale() || male.isJuvenile) continue;
			
			if(male.hasCoalitionGene)
			{
				ccPrime += male.primeOffspring;
				ccPostPrime += male.postPrimeOffspring;
				ccSenescent += male.senescentOffspring;
				ccN += 1;
			}
			else
			{
				ncPrime += male.primeOffspring;
				ncPostPrime += male.postPrimeOffspring;
				ncSenescent += male.senescentOffspring;
				ncN += 1;
			}
		}
		deadMales.clear();
	}
	
	@Override
	public void step(SimState state)
	{
		Environment env = (Environment) state;
		Bag deadMales = env.deadMales;
		
		for(Object obj : deadMales)
		{
			if(!(obj instanceof Baboon b)) continue;
			
			if(!b.isMale() || b.isJuvenile) continue; 
			
			double offspring = 
		}
	}
	

}
