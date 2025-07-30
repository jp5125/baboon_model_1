package baboons;
import sim.engine.SimState;
import spaces.Spaces;
import sweep.SimStateSweep;
import sim.util.Bag;
import sim.engine.*;
import java.util.*;

public class Environment extends SimStateSweep implements Steppable
{
	//population variables
	public int n =10000; //number of baboons at simulation start
	public int groups = 100; //number of groups at simulation start
	public int minGroups = 20;
	public int minGroupSize = 12;
	public int maxGroupSize = 125; //***adjust group sizes to capture that only adults are in population, no juveniles. thus total group size is smaller than in wild***
	public int maxPopulation = 25000; 
	
	//reproduction variables
	public double mutationRate = 0.01; //rate of mutations in cooperative genotype
	public double migrationMortalityRate = 0.30; //30% chance a male will die when leaving their natal group
	
	//age variables
	public double averageAge; 
	
	//references for data collection
	public static Bag deadMales = new Bag();
	int malesWithGene = 0;
	int malesWithoutGene = 0;
	public Experimenter experimenter;
	
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
	
	public int getMaxPopulation(){
		return maxPopulation;
	}
	
	public void setMaxPopulation(int maxPopulation) {
		this.maxPopulation = maxPopulation;
	}
	
	public double getMigrationMortalityRate()
	{
		return migrationMortalityRate;
	}
	
	public void setMigrationMortalityRate(double rate)
	{
		this.migrationMortalityRate = rate;
	}
	
	//methods for environment
	public Environment(long seed, Class observer)
	{
		super(seed, observer);
		
	}
	
	
	// Method to make groups upon simulation initialization
	public void makeGroups()
	{
		//allow for variable group sizes upon initialization
		int totalAgentsAssigned = 0;
		int maxJuveniles = (int) (n * 0.2); //ensure that the simulation cannot be comprised of more than 20% juveniles upon initialization
		int totalJuvenilesAssigned = 0;
		
		for(int i = 0; i < groups; i++) //for each group
		{
			int x = random.nextInt(gridWidth); //give group random x coordinate
			int y = random.nextInt(gridHeight); //give group random y coordinate
			int groupSize = random.nextInt(maxGroupSize - minGroupSize + 1) + minGroupSize; //generates groups between minimum and maximum group size
			Bag g = new Bag(groupSize); //create bag called g with capacity equal to groupSize
			
			for(int j = 0; j < groupSize; j++) //add agents to groups
			{
				boolean isMale = (random.nextDouble() >= 0.714); //2.5:1 OSR
				int ageInDays;
				boolean isJuvenile = false;
				
				//decide if agent is going to be a juvenile or not based on if there are too many juveniles and a probability calculation
				if(totalJuvenilesAssigned < maxJuveniles && random.nextDouble() < 0.2)
				{
					isJuvenile = true; //set the agent's juvenile tag to true
					ageInDays = random.nextInt(3650 - 185) + 185; //initialize at an age between weaning (185 days after birth) and sexual maturity (10 y/o)
					totalJuvenilesAssigned++; //increase number of juveniles assigned
				}
				else //if the agent will be initialized as an adult
				{
					int ageInYears = random.nextInt(16) + 10; //ages 10-25 in years
					ageInDays = ageInYears * 365; //translate age in years upon initialization to age in days (timesteps)
				}
				
				Baboon b = new Baboon(this, isMale, x, y, ageInDays, isJuvenile); //create a new baboon for each baboon in the group
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
	
	//Utility method to get the current number of groups
	public int getTotalGroups()
	{
		//returns 0 when MASON inspects getTotalGroupsbefore initialization,  prevents nullPointerError
		if(sparseSpace == null)
		{
			return 0;
		}
		
		int groupCount = 0;
		for(Object obj : sparseSpace.getAllObjects())
		{
			if(obj instanceof Group)
			{
				groupCount++;
			}
		}
		return groupCount;
	}
	
	//Utility method for getting the current number of baboons in the simulation
	public int getTotalPopulation()
	{
		//returns 0 when MASON inspects getTotalPopulation before initialization, prevents nullPointerError
		if(sparseSpace == null)
		{
			return 0;
		}
		
		int count = 0;
		for(Object obj : sparseSpace.getAllObjects())
		{
			if(obj instanceof Group g)
			{
				count += g.members.numObjs;
			}
		}
		return count;
	}
	
	//utility method for printing model outputs to the console for the purpose of debugging BEFORE data collection
	public void printDebugSummary()
	{
		int totalBaboons = 0;
		int juvenileCount = 0;
		int adultMaleCount = 0;
		int adultFemaleCount = 0;
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
					
					if(b.isJuvenile)
					{
						juvenileCount++;
					}
					
					if(b.isMale() && !b.isJuvenile)
					{
						adultMaleCount++;
						totalFightingAbility += b.fightingAbility;
						
						if(b.hasCoalitionGene)
						{
							coalitionGeneCount++;
						}
					} 
					if(!b.isMale() && !b.isJuvenile)
					{
						adultFemaleCount++;
						
					}
				}
			}
		}
		
		int totalAdults = adultMaleCount + adultFemaleCount;
		double avgFightingAbility = adultMaleCount > 0 ? totalFightingAbility / adultMaleCount : 0.0;
		double coalitionGeneFreq = adultMaleCount > 0 ? (double) coalitionGeneCount / adultMaleCount : 0.0;
		
		System.out.printf(
				"[Step %d] Total: %d | Adults: %d | Juveniles: %d | Males: %d | Females: %d | Coalition Gene: %d (%.2f%%) | Avg FA: %.3f\n",
		        schedule.getSteps(), totalBaboons, totalAdults, juvenileCount, adultMaleCount, adultFemaleCount,
		        coalitionGeneCount, coalitionGeneFreq * 100, avgFightingAbility
		    );
		
		//used in test infant survival method
		//System.out.println(Baboon.getInfantSurvivalStats());
		//Baboon.resetInfantCounters();
	}
	
	public void start()
	{
		super.start();
		spaces = Spaces.SPARSE; //set the space
		make2DSpace(spaces, gridWidth, gridHeight);//make the space
		makeGroups(); //create the groups
		
		schedule.scheduleRepeating(this);
		if(observer != null)
		{
			observer.initialize(sparseSpace, spaces); // initialize the experimenter by calling initialize in the parent class
			experimenter = (Experimenter) observer;
			experimenter.resetVariables();
			
		}
		
	}
	
	
	public void step(SimState state)
	{
	
		if(schedule.getSteps() % 100 == 0)
		{
			printDebugSummary();
		}
		
	}

}
