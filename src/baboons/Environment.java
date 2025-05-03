package baboons;
import sim.engine.SimState;
import spaces.Spaces;
import sweep.SimStateSweep;
import sim.util.Bag;
import sim.engine.*;

public class Environment extends SimStateSweep implements Steppable
{
	//population variables
	public int n =10000; //number of baboons at simulation start
	public int groups = 100; //number of groups at simulation start
	public int minGroupSize = 12;
	public int maxGroupSize = 120; //***adjust group sizes to capture that only adults are in population, no juveniles. thus total group size is smaller than in wild***
	
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
		//allow for variable group sizes upon initialization
		int totalAgentsAssigned = 0;
		
		for(int i = 0; i < groups; i++) //for each group
		{
			int x = random.nextInt(gridWidth); //give group random x coordinate
			int y = random.nextInt(gridHeight); //give group random y coordinate
			int groupSize = random.nextInt(maxGroupSize - minGroupSize + 1) + minGroupSize; //generates groups between minimum and maximum group size
			
			if(totalAgentsAssigned + groupSize > n || i == groups - 1) //if there are not enough agents to assign to the group, or there are n-1 groups already
			{
				groupSize = n - totalAgentsAssigned; //assign leftover agents to the last group
			}
			
			Bag g = new Bag(groupSize); //create bag called g with capacity equal to groupSize
			
			for(int j = 0; j < groupSize; j++) //add agents to groups
			{
				boolean isMale = (random.nextDouble() >= 0.714); //2.5:1 OSR
				int ageInYears = random.nextInt(16) + 10; //ages 10-25 in years
				int ageInDays = ageInYears * 365; //translate age in years upon initialization to age in days (timesteps)
				Baboon b = new Baboon(this, isMale, x, y, ageInDays); //create a new baboon for each baboon in the group
				
				g.add(b);
			}
			
			totalAgentsAssigned += groupSize; //add the number of agents in the newly formed group to our count of totalAgentsAssigned
			
			Group group = new Group(this, x,y,g);
			group.event = schedule.scheduleRepeating(1.0, 1, group, scheduleTimeInterval);
			sparseSpace.setObjectLocation(group, x, y);
			
			for(int k = 0; k < g.numObjs; k++) //Assigns group reference to each baboon
			{
				Baboon b = (Baboon) g.objs[k];
				b.setGroup(group);
				b.event = schedule.scheduleRepeating(1,0,b);
			}
			
			System.out.printf("Group %d initialized at (%d, %d) with %d members.\n", i + 1, x, y, groupSize); //log each groups starting size at startup
			
			if(totalAgentsAssigned >= n) break; //if all agents are assigned to groups, exit loop
		}
		
	}
	
	//utility method for finding nearest group in simulation. Used by Group.groupDisperse() and Baboon.maleImmigration()
	public Group findGroupNearest(int x, int y, int mode)
	{
		if(sparseSpace.getAllObjects().numObjs < 2)
			return null;
		
		Bag groups; //create an empty bag of other groups in the simulation
		int radius = 1; //starting search radius
		Group nearestGroup = null; // stores the nearest group upon completion of the method
		
		while(nearestGroup == null) //loop until a nearest group is found
		{
			groups = sparseSpace.getMooreNeighbors(x, y, radius, mode, false); //all groups that are within the moores neighborhood are added to the bag of neighbors
			groups.shuffle(random); //randomly shuffles the order of the bag
			
			for(Object obj : groups) //this logic draws the first group from the bag of neighbors, sets it to nearestGroup, and breaks
			{
				Group g = (Group) obj; 
				if(g.members != null && g.members.numObjs > 0)
				{
					nearestGroup = g;
					break;
				}
			}
			radius++; //increase the radius if no neighbors in the previous radius' moores neighborhood
			if(radius > Math.max(gridWidth, gridHeight)) //stops search from expanding past the size of the environment
			{
				break;
			}
		}
		return nearestGroup;
	}
	
	//utility method for printing model outputs to the console for the purpose of debugging BEFORE data collection
	public void printDebugSummary()
	{
		int totalBaboons = 0;
		int maleCount = 0;
		int femaleCount = 0;
		int coalitionGeneCount = 0;
		double totalFightingAbility = 0.0;
		
		for(Object obj : sparseSpace.getAllObjects())
		{
			if(obj instanceof Group group)
			{
				for(int i = 0; i < group.members.numObjs; i++)
				{
					Baboon b = (Baboon) group.members.objs[i];
					totalBaboons++;
					
					if(b.isMale())
					{
						maleCount++;
						totalFightingAbility += b.fightingAbility;
						
						if(b.hasCoalitionGene)
						{
							coalitionGeneCount++;
						}
					} 
					else
					{
						femaleCount++;
						
					}
				}
			}
		}
		double avgFightingAbility = maleCount > 0 ? totalFightingAbility / maleCount : 0.0;
		double coalitionGeneFreq = maleCount > 0 ? (double) coalitionGeneCount / maleCount : 0.0;
		
		System.out.printf(
				"[Step %d] Total: %d | Males: %d | Females: %d | Coalition Gene: %d (%.2f%%) | Avg FA: %.3f\n",
		        schedule.getSteps(), totalBaboons, maleCount, femaleCount,
		        coalitionGeneCount, coalitionGeneFreq * 100, avgFightingAbility
		    );	
	}
	
	public void start()
	{
		super.start();
		spaces = Spaces.SPARSE; //set the space
		make2DSpace(spaces, gridWidth, gridHeight);//make the space
		makeGroups(); //create the groups
		
		schedule.scheduleRepeating(this);
		
	}
	
	
	public void step(SimState state)
	{
		
		if(schedule.getSteps() % 100 == 0)
		{
			printDebugSummary();
		}
	}

}
