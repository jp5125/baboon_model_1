package baboons;

import java.awt.Color;
import sim.engine.*;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Bag;
import sim.util.*;
import sweep.GUIStateSweep;
import java.util.Comparator;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Random;
import java.util.*;

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
		this.state = state;
		this.x = x;
		this.y = y;
		this.members = members;
		
		for(int i = 0; i<this.members.numObjs; i++)
		{
			Baboon b = (Baboon)this.members.objs[i];
			b.setGroup(this);
		}
	}
	
	public Group(Environment state) //specifically for group.fission()
	{
		super();
		this.state = state;
		this.members = new Bag();
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
		//Collect all male baboons from the members bag and put into ArrayList
		ArrayList<Baboon> males = new ArrayList<>();
		for(int i = 0; i < members.numObjs; i++)
		{
			Baboon b = (Baboon)members.objs[i];
			if(b.isMale() && !b.isJuvenile )
			{
				males.add(b);
			}
		}
		
		//reset all ranks for adult males in the group
		for(int i = 0; i < males.size(); i++)
		{
			Baboon b = males.get(i);
			b.dominanceRank = -1;
		}
		
		//Next, calculate fighting ability based on age for each male
		for(Baboon male : males)
		{
			male.fightingAbility = male.calculateFightingAbilityLogistic();
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
			if(b.isMale() && !b.isJuvenile)
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
		/*System.out.println("=== Initial Consort Assignments ===");
		for (Baboon male : consortMales) {
		    System.out.println("Consort Male | Rank: " + male.dominanceRank +
		                       " | FA: " + male.fightingAbility +
		                       " | LifeStage: " + male.getLifeStage() +
		                       " | hasCoalitionGene: " + male.hasCoalitionGene);
		}*/
		
		//Identify males eligible to form a coalition
		ArrayList<Baboon> coalitionaryMales = new ArrayList<>();
		
		for(Baboon male : sortedMales) //for each male in the sorted list of males by rank
		{
			if(male.hasCoalitionGene && !consortMales.contains(male) && (male.getLifeStage() == LifeStage.POST_PRIME || male.getLifeStage() == LifeStage.SENESCENT)) //if the male has a the coalition gene and is not a consort
			{
				coalitionaryMales.add(male);
			}
		}
		
		//randomly pair males in coalitionaryMales into coalitions (add more detail in later model iteration for strategic pairing)
		 ArrayList<ArrayList<Baboon>> coalitions = formCoalitions(coalitionaryMales);
		 
		 /*System.out.println("=== Coalition Participants ===");
		 for (Baboon male : coalitionaryMales) {
		     System.out.println("Coalition Male | Rank: " + male.dominanceRank +
		                        " | FA: " + male.fightingAbility +
		                        " | LifeStage: " + male.getLifeStage() +
		                        " | hasCoalitionGene: " + male.hasCoalitionGene);
		 }*/
		
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
					mortalWoundConsort(currentConsort);
					mortalWoundCoalition(male1);
					mortalWoundCoalition(male2);
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
	
	public void mortalWoundConsort(Baboon male)
	{
		double probMortalWound = 0.00;
		
		if(state.random.nextDouble() < probMortalWound)
		{
			male.die(state);
		}
	}
	
	public void mortalWoundCoalition(Baboon male)
	{
		double probMortalWound = 0.05;
		
		if(state.random.nextDouble() < probMortalWound)
		{
			male.die(state);
		}
	}
	
	//group dispersion utility method
	public void groupDisperse(Environment state) 
	{
	    if (members.numObjs < state.minGroupSize) 
	    {
	        Group newGroup = state.findGroupNearest(this.x, this.y, state.sparseSpace.TOROIDAL);
	        if (newGroup == null || newGroup.members.numObjs >= state.maxGroupSize) 
	        	return;

	        for (int i = 0; i < members.numObjs; i++) 
	        {
	            Baboon b = (Baboon) members.objs[i];
	            b.x = newGroup.x;
	            b.y = newGroup.y;
	            b.setGroup(newGroup);
	            newGroup.members.add(b);
	            newGroup.updateDominanceHierarchyArray();
	        }

	        members.clear();
	        state.sparseSpace.remove(this);
	        event.stop();
	        System.out.println("Group dispersed from (" + x + ", " + y + ") to (" + newGroup.x + ", " + newGroup.y + ")");
	    }
	}
	
	//method for when group reaches maximum size
	
	  public void fission(Environment state)
	 
	{
		double now = state.schedule.getTime(); //current time step for scheduling new groups
		  
		if(members.numObjs >= state.maxGroupSize) //if a group becomes the size of, or supasses, the maximum group size
		{
			
			// first, create new group and place nearby
			Group newGroup = new Group(state); //generate a new empty group
			Int2D currentLocation = state.sparseSpace.getObjectLocation(this); //the new group's location is the same as the group that runs the group.fission()
			int dx = state.random.nextInt(5) - 2; //-2 to +2 for new x-axis value
			int dy = state.random.nextInt(5) - 2; //same for y-axis
			Int2D newLocation = new Int2D(
					Math.min(state.gridWidth - 1, Math.max(0, currentLocation.x + dx)),
					Math.min(state.gridHeight - 1, Math.max(0, currentLocation.y + dy))
					); //determines new location for the new group
			state.sparseSpace.setObjectLocation(newGroup, newLocation); //then sets new groups new location
			newGroup.x = newLocation.x;
			newGroup.y = newLocation.y;
			newGroup.event = state.schedule.scheduleRepeating(now + state.scheduleTimeInterval, 1, newGroup, state.scheduleTimeInterval); //adds the new group to the schedule
		
				
			/*
			 * We want fission to happen based on matrilineal splitting, so related females will move to new group or stay in old
			 * group together. Males stay/leave randomly, except cooperating males will join same group. 
			 * We use a HashMap to collect matrilines, where matrilineID is the key and a bag of female objects containing
			 * the memebers of the matriline is the value. Adult Males are stored in their own bag and don't need to be sorted based
			 * on matriline, however all juveniles regardless of sex migrate with their mothers matriline (hence why males have a matriline ID).
			 */
		
			HashMap<String, Bag> matrilines = new HashMap<>();
			Bag adultMales = new Bag();
		
			for(int i = 0; i < members.numObjs; i++) //iterate across all group members
			{
				Baboon b = (Baboon) members.objs[i];
			
				if (!b.isMale() || (b.isMale() && b.isJuvenile)) // Female or juvenile male
				{
					Bag groupBag = matrilines.get(b.matrilineID); 
					if (groupBag == null) 
					{
						groupBag = new Bag();
						matrilines.put(b.matrilineID, groupBag);
					}
					groupBag.add(b);
				}
				else 
				{
					adultMales.add(b);
				}
			
			}
			//Once female sorting is complete, we want to separate matrilines into the new and old group
			ArrayList<String> matrilineKeys = new ArrayList<>(matrilines.keySet()); //create an arrayList of matrilineIDs
			Bag matrilineKeyBag = new Bag(matrilineKeys);
			matrilineKeyBag.shuffle(state.random);
		
			for(int i = 0; i < matrilineKeyBag.size(); i++) //for each matriline in the arrayList
			{
				String matrilineID = (String) matrilineKeyBag.objs[i];
				Bag groupBag = matrilines.get(matrilineID);
				Group targetGroup;
			
				if (i % 2 == 0) 
				{
					targetGroup = this; // assign to the original group
				} 
				else 
				{
					targetGroup = newGroup; // assign to the newly created group
				}

				for (int j = 0; j < groupBag.numObjs; j++)
				{
					Baboon b = (Baboon) groupBag.objs[j];
					this.members.remove(b);
					targetGroup.members.add(b);
					b.setGroup(targetGroup);
					b.x = targetGroup.x;
					b.y = targetGroup.y;
					
					if(b.event == null)
					{
						b.event = state.schedule.scheduleRepeating(now + state.scheduleTimeInterval, 0, b, state.scheduleTimeInterval);
					}
				}
				//otherwise the female remains in the old group
			}
		
			//now we need to randomly assign males to either stay in the old group or join the new group
			Bag coalitionMales = new Bag();
			Bag nonCoalitionMales = new Bag();
		
			for(int i = 0; i < adultMales.numObjs; i++) //for each object in the males bag
			{
				Baboon m = (Baboon) adultMales.objs[i]; //cast this object as type Baboon
				if(m.hasCoalitionGene)
				{
					coalitionMales.add(m);
				}
				else
				{
					nonCoalitionMales.add(m);
				}
			}
		
			//shuffle and move half from each bag of males
			coalitionMales.shuffle(state.random);
			nonCoalitionMales.shuffle(state.random);
			int coalitionSplit = coalitionMales.numObjs / 2;
			int nonCoalitionSplit = nonCoalitionMales.numObjs / 2;
	    
			for(int i = 0; i < coalitionMales.numObjs; i++)
			{
				Baboon m = (Baboon) coalitionMales.objs[i];
				if(i < coalitionSplit)
				{
					members.remove(m);
					m.setGroup(newGroup);
					newGroup.members.add(m);
					
					m.x = newGroup.x;
					m.y = newGroup.y;
					
					if(m.event == null)
					{
						m.event = state.schedule.scheduleRepeating(now + state.scheduleTimeInterval, 0, m, state.scheduleTimeInterval);
					}
				}
			}
	    
			for(int i = 0; i < nonCoalitionMales.numObjs; i++)
			{
				Baboon m = (Baboon) nonCoalitionMales.objs[i]; 
				if(i < nonCoalitionSplit)
				{
					members.remove(m);
					m.setGroup(newGroup);
					newGroup.members.add(m);
					
					m.x = newGroup.x;
					m.y = newGroup.y;
					
					if(m.event == null)
					{
						m.event = state.schedule.scheduleRepeating(now + state.scheduleTimeInterval,0,m, state.scheduleTimeInterval);
					}
				}
			}
			//update dominance hierarchies after group shuffling
			this.updateDominanceHierarchyArray();
			newGroup.updateDominanceHierarchyArray();
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
		fission(eState);
		updateDominanceHierarchyArray();
		coalitionGame();
	}
}
