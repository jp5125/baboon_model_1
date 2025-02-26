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

	
	public Group(Environment state, int x, int y, Bag Members)
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
}
