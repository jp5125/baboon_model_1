package baboons;
import sim.engine.SimState;
import spaces.Spaces;
import sweep.SimStateSweep;
import sim.util.Bag;

public class Environment extends SimStateSweep
{
	//population variables
	public int n =10000; //number of baboons at simulation start
	public int groups = 100; //number of groups at simulation start
	public int minGroupSize = 12;
	public int maxGroupSize = 120;
	
	//reproduction variables
	public double mutationRate = 0.01; //rate of mutations in cooperative genotype
	
	//age variables
	public double averageAge; 
	
	//getters and setters
	
	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public double getMutationRate() {
		return mutationRate;
	}

	public void setMutationRate(double mutationRate) {
		this.mutationRate = mutationRate;
	}

	public double getAverageAge() {
		return averageAge;
	}

	public void setAverageAge(double averageAge) {
		this.averageAge = averageAge;
	}
	
	public int getGroups() {
		return groups;
	}

	public void setGroups(int groups) {
		this.groups = groups;
	}

	//methods for environment
	
	public Environment(long seed, Class observer)
	{
		super(seed, observer);
	}
	
	
	// ** This method needs to be reworked so groups start with different numbers of agents **
	// ** Groups should have rougly similar OSRs (2 females for every male on average) **
	public void makeGroups()
	{
		int m = n/groups; //number of agents in each group at genesis
		for(int i = 0; i < groups; i++) //for each group
		{
			int x = random.nextInt(gridWidth); //give group random x coordinate
			int y = random.nextInt(gridHeight); //give group random y coordinate
			Bag g = new Bag(m); //create bag called g which an initial capacity equal to m
			
			for(int j = 0; j < m; j++) //add agents to groups
			{
				Baboon b;
				int age =random.nextInt(); //need to set this so it is in baboons life span
				b = new Baboon(this, true, age, x, y);
				b.event = schedule.scheduleRepeating(1,0,b);
				g.add(b);
			}
			
			Group group = new Group(this, x,y,g);
			group.event = schedule.scheduleRepeating(1.0, 1, group, scheduleTimeInterval);
			sparseSpace.setObjectLocation(group,  x, y);
		}
	}
	
	public void start()
	{
		super.start();
		spaces = Spaces.SPARSE; //set the space
		make2DSpace(spaces, gridWidth, gridHeight);//make the space
		makeGroups(); //create the groups
		
	}

}
