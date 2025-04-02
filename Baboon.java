package baboons;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Bag;

public class Baboon implements Steppable
{
	
	int age; //current age of the agent
	boolean male; //sex of agent
	public Group group; //the group that the agent is currently a member of
	int maxAge; //maximum agent age
	int x; //x-axis location of agent
	int y; //y-axis location of agent
	
	
	//variables used for calculations
	Environment state;
	public Stoppable event;
	
	
	public Baboon(Environment state, boolean male, int age, int x, int y)
	{
		this.state = state;
		this.male = male;
		this.age = age;
		this.x = x;
		this.y = y;
		
	}
	
	public void die(Environment state)
	{
		event.stop();
		group.members.remove(this);
	}
			
	
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}


	public boolean isMale() {
		return male;
	}

	public void setMale(boolean male) {
		this.male = male;
	}


	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}
	
	//finds group nearest to calling group if one exists
	public Group findGroupNearest(Environment state, final int x, final int y, final int mode)
	{
		if(state.sparseSpace.getAllObjects().numObjs < 2)
			return null;
		
		Bag groups;
		int i = 1;//starting search radius
		groups = state.sparseSpace.getMooreNeighbors(x, y, i, mode, false);
		Group g = null;
		while(groups.numObjs == 0 || g == null)
		{
			i++;
			groups = state.sparseSpace.getMooreNeighbors(x, y, i, mode,false);
			if(groups.numObjs > 0)
			{
				groups.shuffle(state.random);
				for(int j = 0; j < groups.numObjs; j++)
				{
					Group o = (Group)groups.objs[j];
					if(o.members.numObjs > 0)
					{
						g = o;
						break;
					}
				}
			}
		}
		
		return g;
	}

	
	//Members of a group disperse to other groups within the agent dispersal radius if group is too small
	public void groupDisperse(Environment state)
	{
		Bag members = group.members;
		if(members.numObjs < state.minGroupSize)
		{
			Group g = findGroupNearest(state,group.x, group.y, state.sparseSpace.TOROIDAL);
			if(g == null) return; 
			for(int i = 0; i < members.numObjs; i++)
			{
				Baboon b = (Baboon)members.objs[i];
				b.x = g.x;
				b.y = g.y;
				b.setGroup(g);
				g.members.add(b);
			}
			members.clear();
		}
		
	}

	
	//need cycling and reproduction method logic
	public void cycleUpdate()
	{
		/*
		 * update cycle day each time step
		 * 
		 * check if fertile, if fertile pair with male
		 * 
		 * update copulation matrix
		 * 
		 */
	}
	
	public void reproduce()
	{
		/*
		 * Upon exiting cycle, calculate probability female becomes pregnant
		 * 
		 * if female becomes pregnant, determine probabilistically which consort male becomes father
		 * 
		 * female gestates, gives birth, restarts cycling
		 */
	}
	
	//dispersal method for newborn males
	public void maleImmgration()
	{
		//newborn male immigration method?
	}
	
	//male strategy genotype
	public void maleStrategy()
	{
		/*
		 * all males can be sneakers, while some males have coalition strategy gene and others do not. 
		 * 
		 * Coalition formation is haploid, males pass their gene to male offspring with a 1% mutation rate
		 * 
		 * males must have coalition formation gene in order to solicit/participate in a coalition
		 */
	}


	@Override
	public void step(SimState state)
	{
		if(age >= maxAge)
		{
			die(this.state);
		}
		
		//need logic for reproduction
		
		age++;
		
	}

}
