package baboons;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Bag;
import java.util.HashMap;

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
	
	//Reproduction instance variables(for females only)
	int cycleDay = 1; //tracks current day of 33-day reproductive cycle
	int gestationRemaining = 0; //counts down the gestation period (300 days) if pregnant
	HashMap<Baboon, Integer> matingHistory; //Records mating events during the fertile period
	
	//Dominance instance variables for males
	double fightingAbility; 
	int dominanceRank;
	
	
	public Baboon(Environment state, boolean male, int age, int x, int y)
	{
		this.state = state;
		this.male = male;
		this.age = age;
		this.x = x;
		this.y = y;
		
		if(!male)
		{
			cycleDay = 1; //start at day 1 of the cycle... ***change this to start at random point in the cycle***
			gestationRemaining = 0; //not pregnant initially
			matingHistory = new HashMap<>(); //Initialize an empty mating history
		}
	}
	
	//Method for handling agent death, remove the baboon from the schedule and its group
	public void die(Environment state)
	{
		event.stop();
		group.members.remove(this);
	}
			
	
	//getters and setters for age, sex, and group
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
	
	
	// --- Reproduction Methods for Female Baboons ---

	
	//Updates the reproductive cycle of female agents each time step
	//First updates the cycle day, then records mating events in the fertile window, then determines if the pregnancy occurs at the end of the fertile period.
	//Manages gestation.
	
	public void cycleUpdate()
	{
		// Ensures method only applies to females
		if(male) return;
		
		// If currently pregnant, decrement gestation
		if(gestationRemaining > 0)
		{
			gestationRemaining--;
			if(gestationRemaining == 0) //When gestation is complete, give birth
			{
				giveBirth();
				cycleDay = 1;
				matingHistory.clear();
			}
			return;
			
		}
		
		//if agent is not male and not pregnant, update the cycle day
		cycleDay++;
		
		//If in the fertile period (days 27-33), mating events are recorded in the matingHistory data structure to use for paternity calculations
		if (cycleDay >= 27 && cycleDay <= 33)
		{
			//need to add code here for updating the paternityCalculation hashMap based on coalition game outcomes
		}
		
		if(cycleDay > 33)
		{
			reproduce();
			cycleDay = 1; //Reset the cycle regardless of outcome
			matingHistory.clear();
			
		}
		
	}
	
	//Determines if the female becomes pregnant and selects the father of offspring
	public void reproduce()
	{
		//ensures the method is for females only
		if(male) return; 
		
		//If no mating events have been recorded, no pregnancy can occur
		//extremely unlikely edge case but should be accounted for so it does not break the simulation
		if(matingHistory == null || matingHistory.isEmpty())
		{
			return;
		}
		
		//If the agent is female and does have a history of copulations with a male in the previous cycle
		//Determine baseline probability of becoming pregnant at the end of the cycle
		double pregnancyProbability = 0.5; // ***adjust this to reflect biological system***
		if(state.random.nextDouble() < pregnancyProbability)
		{
			//pregnancy occurs
			int totalMatingCount = 0; //counter for calculating total number of recorded reproductive events
			for(Integer count : matingHistory.values())
			{
				totalMatingCount += count;
			}
			
			if(totalMatingCount == 0) //guard against divide by zero error
			{
				return;
			}
			
			//Use weighted random selection algorithm to determine which male will sire offspring based on number of copulations
			//currently, male that sires offspring is selected based on mating frequency
			// *** consider weighting paternity determination by frequency of copulations AS WELL AS timeframe when copulations occured
			int randomChoice = state.random.nextInt(totalMatingCount) + 1; //choose a random number between 1 : totalMateCount inclusive
			int cumulative = 0; 
			Baboon father = null;
			for(Baboon male : matingHistory.keySet()) //for each male in the hashMap
			{
				cumulative += matingHistory.get(male); //add the number of times they mated to the cumulative counter one by one
				if(randomChoice <= cumulative) //if the value of the random number selected is less than or equal to cumulative at that stage of the for loop
				{
					father = male; //that male becomes the father
					break;
				}
			}
			
			//Begin the gestation period
			gestationRemaining = 300;
			
		}
		else
		{
			return; // if agent does not become pregnant, exit the reproduce method
		}
	}
	
	
	//Handles agent birthing event, creates a new baboon agent
	public void giveBirth()
	{
		//Calculate the probability that a newborn will be female (OSR in baboons is between 2-3 adult females for every male)
		//To abstract away high male mortality rate in the simulation, we will just use a 2.5:1 sex ratio at birth of females to males
		double femaleProbability = 2.5 / (2.5 + 1.0); // ~0.714
		boolean newbornIsMale = (state.random.nextDouble() >= femaleProbability);
		
		Baboon newborn = new Baboon(state, newbornIsMale, 0, group.x, group.y);
		if(newbornIsMale)
		{
			//For newborn males, trigger migration event
			newborn.maleImmigration();
			
		}
		else
		{
			//for newborn females, add them to the current group
			group.members.add(newborn);
		}
		
		// Schedule the newborn for stepping in the simulation
		newborn.event = state.schedule.scheduleRepeating(newborn);
	}
	
	
	/// --- Methods for Males ---
	
	//dispersal method for newborn males
	public void maleImmigration()
	{
		//Use findGroupNearest method to find a new group
		Group newGroup = findGroupNearest(state, x, y, state.sparseSpace.TOROIDAL);
		
		//If a new group is found, proceed with migration
		if(newGroup != null)
		{
			//remove the baboon from its current group
			group.members.remove(this);
			
			//Update the baboon's spatial coordinate to match the new group's location
			this.x = newGroup.x;
			this.y = newGroup.y;
			
			//Set the baboons group reference to the new group
			setGroup(newGroup);
			
			//Add the baboon to the new group's member list
			newGroup.members.add(this);
		}
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
	
    /* --- The Step Method ---
     In each simulation step, baboons update their state:
	 - Agents die if too old
     - Females update their reproductive cycle.
     - New agents are born
     - Newborn males immigrate
     - Newborn females begin cycling
     - **Male dominance is updated at the group level**
    */
     


	@Override
	public void step(SimState state)
	{
		if(age >= maxAge)
		{
			die(this.state);
			return;
		}
		
		//update female reproduction
		if(!male)
		{
			cycleUpdate();
		}
		
		
		age++;
		
	}

}
