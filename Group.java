package baboons;

import java.awt.Color;
import sim.engine.*;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Bag;
import sweep.GUIStateSweep;

public class Group implements Steppable
{
	int x; //x-location
	int y; //y-location
	Bag members = null; //bag of baboons who are members
	public Stoppable event; //so the group can be removed from the schedule
	Environment state;
	Bag fertileFemales = new Bag();
	Bag consortMales = new Bag();
	Bag coalitionMales = new Bag();
	

	
	public Group(Environment state, int x, int y, Bag members)
	{
		super();
		this.x = x;
		this.y = y;
		this.members = members;
		this.state = state;
		
		for(int i = 0; i<this.members.numObjs; i++)
		{
			Baboon b = (Baboon)this.members.objs[i];
			b.setGroup(this);
		}
	}
	
	
	//calculate the male dominance hierarchy based on age
	public void dominanceHierarchy()
	{
		/*
		 * new immigrant, young males become highest ranking
		 * 
		 * males slowly drop in rank as they age
		 * 
		 * start forming coalitions as they drop in rank
		 */
		
	}
	
	public void coalitionGame()
	{
		//logic for the game goes here
		/*
		 * 
		 * first - high ranking males pair with receptive females
		 * 
		 * second - non consort males try to form coalitions
		 * 
		 * third - coalitions challenge consort males, successful coalitions pick new consort
		 * 			**sneaker makes may attempt to steal reproductive female
		 * 
		 * 
		 * 
		 */
	}
	
	public void groupDisperse(Environment state)
	{
		if(members.numObjs < state.minGroupSize)
		{
			Baboon b = (Baboon)members.objs[0]; // does this need to be handled differently since groups in this model can be larger than 2 individuals?
			b.groupDisperse(state);
			die(state);
			System.out.print("Group dispersed");
		}
		
	}
	
	public boolean die(Environment state)
	{
		if(members == null || members.numObjs == 0)
		{
			state.sparseSpace.remove(this);
			event.stop();
			return true;
		}
		return false;
	}
	
	
	
	public void step(SimState state)
	{
		Environment eState = (Environment)state;
		if(die(eState))
			return;
		groupDisperse(eState);
		coalitionGame();
	}
}
