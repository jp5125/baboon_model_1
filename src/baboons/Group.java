package baboons;

import java.awt.Color;
import sim.engine.*;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Bag;
import sweep.GUIStateSweep;
import java.util.Comparator;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Random;

public class Group implements Steppable
{
	int x; //x-location
	int y; //y-location
	Bag members = null; //bag of baboons who are members
	public Stoppable event; //so the group can be removed from the schedule
	Environment state;
	ArrayList<Baboon> fertileFemales = new ArrayList<>();
	ArrayList <Baboon> consortMales = new ArrayList<>();
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
	
	//initial dominance hierarchy implementation, unsure of hidden bugs due to redrawing from a bag so this method was re-implemented using an ArrayList below
	public void updateDominanceHierarchyBag() 
	{
		//First, we gather the male baboons from the set of all group members
		Bag males = new Bag(); //initialize an empty bag for males
		for(int i = 0; i < members.numObjs; i++) //for each agent in the group (object in the members bag)
		{
			Baboon b = (Baboon)members.objs[i]; //baboon b is a an object in the members bag cast as a baboon instance
			if (b.isMale()) //if the boolean state variable "Male" for b equals TRUE
			{
				males.add(b); //add that individual to the new bag "males"
			}
		}
		
		/*
		 * Next, we must determine fighting ability based on age, inspired by Noe 1992 and Noe 1994.
		 * Fighting ability is a signifcant determinant of dominance, and in male baboons fighting ability is mostly determined by age 
		 * age -> fighting ability -> dominance ranking
		*/
		
		for(int i = 0; i < males.numObjs; i++)
		{
			Baboon male = (Baboon)males.objs[i]; //***note for next week lab, is it redundant to recast this as a baboon as males should already contain objects of class baboon?***
			
			// Function to determine each males fighting ability based on age
			male.fightingAbility = male.calculateFightingAbility();
			
		}
		
		//From here, we can sort the males by fighting ability. This essentially creates the dominance hierarchy.
		males.sort(new Comparator()
				{
					public int compare(Object a, Object b)
					{
						Baboon maleA = (Baboon) a;
						Baboon maleB = (Baboon) b;
						return Double.compare(maleB.fightingAbility, maleA.fightingAbility);
					}
				});
		
		//Finally, we can update the dominance rank for each male
		for (int rank = 0; rank < males.numObjs; rank++) {
	        Baboon male = (Baboon) males.objs[rank];
	        male.dominanceRank = rank + 1; // Rank starts at 1 for highest-ranking male
	    } 
    }
	
	//Uses an ArrayList to keep track of the dominance hierarchy instead of a bag to avoid ordering errors with repeatedy drawing from a bag
	public void updateDominanceHierarchyArray()
	{
		//First, collect all male baboons from the members bag and put into ArrayList
		ArrayList<Baboon> males = new ArrayList<>();
		for(int i = 0; i < members.numObjs; i++)
		{
			Baboon b = (Baboon)members.objs[i];
			if(b.isMale())
			{
				males.add(b);
			}
		}
		
		//Next, calculate fighting ability based on age for each male
		for(Baboon male : males)
		{
			male.fightingAbility = male.calculateFightingAbility();
		}
		
		//Third, sort males by fighting ability, creating a dominance hierarchy
		Collections.sort(males, new Comparator<Baboon>()
		{
			public int compare(Baboon b1, Baboon b2)
			{
				return Double.compare(b2.fightingAbility, b1.fightingAbility);
			}
		});
		
		//Finally, assign dominance ranks based on sorted order
		for(int rank = 0; rank < males.size(); rank++)
		{
			Baboon male = males.get(rank);
			male.dominanceRank = rank + 1; //rank 1 should be highest dominance rank
		}
	}

	public void coalitionGame()
	{
		//Clear previous lists
		fertileFemales.clear(); 
		consortMales.clear();
		
		//Identify fertile females in the group
		ArrayList<Baboon> sortedFemales = new ArrayList<>(); //create an empty ArrayList to store fertile females in 
		for(Object obj : members) //for each baboon in the group's members bag
		{
			Baboon b = (Baboon) obj; //cast each object in the bag as type baboon and assign it as a baboon object "b"
			if(!b.isMale() && !b.isJuvenile && b.cycleDay >= 27 && b.cycleDay <= 33) //If the Baboon object from the members bag has a state value of "male" = false, isJuvenile = false, and is between cycle days 27 and 33, add to fertile array
			{
				sortedFemales.add(b);
			}
		}
		
		//Next, we sort fertile females by closeness to peak fertility (day 30 optimal)
		//Use a comparator to compare the absolute difference between each pair of females in the sortedFemales ArrayList
		sortedFemales.sort(new Comparator<Baboon>() 
		{
			@Override
			public int compare(Baboon female1, Baboon female2)
			{
				int diff1 = Math.abs(female1.cycleDay - 30);
				int diff2 = Math.abs(female2.cycleDay - 30);
				return Integer.compare(diff1, diff2);
			}
				
		});
		
		fertileFemales.addAll(sortedFemales); //Add all sorted females to the fertileFemales list
		
		//Identify male baboons in group
		ArrayList<Baboon> sortedMales = new ArrayList<>();
		for(Object obj : members)
		{
			Baboon b = (Baboon) obj;
			if(b.isMale())
			{
				sortedMales.add(b);
			}
		}
		
		//Sort males by dominance rank in asceending order
		sortedMales.sort(new Comparator<Baboon>()
		{
			@Override
			public int compare(Baboon m1, Baboon m2)
			{
				return Integer.compare(m1.dominanceRank, m2.dominanceRank);
			}
		});
		
		//Now we can assign initial consortships
		int maleIndex = 0;
		for(Baboon female : fertileFemales) //for each female in the fertileFemales ArrayList
		{
			if(maleIndex >= sortedMales.size()) //case where no more males are available to pair with 
			{
				break;
			}
			
			Baboon consortMale = sortedMales.get(maleIndex); 
			maleIndex++;
			
			consortMales.add(consortMale); //Add to consort males list
			female.currentConsortMale = consortMale; //Set the female's state variable for 'currentConsortmale' to the current 
			female.recordMating(consortMale); //record the mating event for the initial consortship
			
		}
		
		//Identify males eligible to form a coalition
		ArrayList<Baboon> coalitionaryMales = new ArrayList<>();
		
		for(Baboon male : sortedMales) //for each male in the sorted list of males by rank
		{
			if(male.hasCoalitionGene && !consortMales.contains(male)) //if the male has a the coalition gene and is not a consort
			{
				coalitionaryMales.add(male);
			}
		}
		
		//randomly pair males in coalitionaryMales into coalitions (add more detail in later model iteration for strategic pairing)
		 ArrayList<ArrayList<Baboon>> coalitions = formCoalitions(coalitionaryMales);
		
		//coalitions challenge consorts and then resolve the conflicts through the helper method
		 resolveCoalitionChallenges(coalitions);
		 
		//per-day mating record updating, ensures males who are consorts for multiple days in a row get more than 1 mating record for their consortship
		 for(Baboon female : fertileFemales)
		 {
			 Baboon consort = female.currentConsortMale;
			 if(consort != null)
			 {
				 female.recordMating(consort);
			 }
		 }
		
	}
	
	// Utility method to form coalitions(randomly)
	public ArrayList<ArrayList<Baboon>> formCoalitions(ArrayList<Baboon> eligibleMales) 
	{
	    ArrayList<ArrayList<Baboon>> coalitions = new ArrayList<>();

	    // Shuffle to make random pairs
	    Collections.shuffle(eligibleMales, new Random(state.random.nextLong()));

	    // Pair them
	    for (int i = 0; i < eligibleMales.size() - 1; i += 2) 
	    {
	        ArrayList<Baboon> pair = new ArrayList<>();
	        pair.add(eligibleMales.get(i));
	        pair.add(eligibleMales.get(i + 1));
	        coalitions.add(pair);
	    }

	    return coalitions;
	}
	
	//utility method for coalition challenges in the coalition game
	public void resolveCoalitionChallenges(ArrayList<ArrayList<Baboon>> coalitions)
	{
		for(ArrayList<Baboon> coalition : coalitions) //for each coalitionary pairing in the coalitions arraylist
		{
			if(fertileFemales.isEmpty()) continue; //if there are no fertile females, skip this pairing (and subsequent pairings)
			Baboon targetFemale = fertileFemales.get(state.random.nextInt(fertileFemales.size())); //randomly select a target female for coalition challenge
			Baboon currentConsort = targetFemale.currentConsortMale; //identify targetFemale's consort male and save as currentConsort
			
			//Coalition members
			Baboon male1 = coalition.get(0);
			Baboon male2 = coalition.get(1);
			
			
			if(currentConsort == null) //handles cases where female has no consort but coalitions have already paired
			{
				Baboon newConsort; 
				if(state.random.nextBoolean()) //if this returns true, male1 becomes the new consort
				{
					newConsort = male1;
				}
				else //otherwise, male2 becomes the new consort, should be 50-50 prob.
				{
					newConsort = male2;
				}
				
				
				targetFemale.currentConsortMale = newConsort; //assign the females current consort as the newly decided consort
				consortMales.add(newConsort); //add the new consort male to the list of consort males
				targetFemale.recordMating(newConsort); //record mating event between new consort male and fertile female
			}
			else //handles all normal cases where the fertile female has a consort and the coalition challenges him
			{
				
				//challenge occurs
				boolean coalitionWins = state.random.nextBoolean(); // 50% chance coalition wins against consort male (base case, more complex strategies later)
				if(coalitionWins)
				{
					//Randomly select new consort from coalition members (same logic as above)
					Baboon newConsort; 
					if(state.random.nextBoolean())
					{
						newConsort = male1;
					}
					else
					{
						newConsort = male2;
					}
					targetFemale.currentConsortMale = newConsort; //assign the new consort as the targetFemale's currentConsortMale
					
					//Update consort male list
					consortMales.remove(currentConsort); //remove the old consort male from list of current consort males
					consortMales.add(newConsort);
					
					targetFemale.recordMating(newConsort); //record the mating event with the coalitionary male
					
					//Apply costs to every male involved
					applyFightingCost(currentConsort);
					applyFightingCost(male1);
					applyFightingCost(male2);
				}
			}
		}
	}
	
	//utility method for cost to fighting
	public void applyFightingCost(Baboon male)
	{
		double cost = 0.01; //1% of fighting ability decreased after a fight
		male.fightingAbility = Math.max(0, male.fightingAbility - cost); 
	}
	
	//group dispersion utility method
	public void groupDisperse(Environment state) 
	{
	    if (members.numObjs < state.minGroupSize) 
	    {
	        Group newGroup = state.findGroupNearest(this.x, this.y, state.sparseSpace.TOROIDAL);
	        if (newGroup == null) return;

	        for (int i = 0; i < members.numObjs; i++) 
	        {
	            Baboon b = (Baboon) members.objs[i];
	            b.x = newGroup.x;
	            b.y = newGroup.y;
	            b.setGroup(newGroup);
	            newGroup.members.add(b);
	        }

	        members.clear();
	        state.sparseSpace.remove(this);
	        event.stop();
	        System.out.println("Group dispersed from (" + x + ", " + y + ") to (" + newGroup.x + ", " + newGroup.y + ")");
	    }
	}
	
	//remove group from simulation
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
		updateDominanceHierarchyArray();
		coalitionGame();
	}
}
