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
			male.fightingAbility = calculateFightingAbility(male.age);
			
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
			male.fightingAbility = calculateFightingAbility(male.age);
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
	
	// Age-fighting ability function based on Noe 1994
	public double calculateFightingAbility(int age) 
	{
	    /*
	    - Upon migrating, quickly rise in rank (to top or top 2 spots)
	    - fighting ability starts very high, drops over time as male ages
	    - As new young males join the group, males drop in dominance ranking
	    */
	    
		
		//placeholder function for fighting ability calculation, need to figure out implementation of this to be more biologically accurate
	    
	    double maxAbility = 1.0;    // Maximum fighting ability at age 0 (first migration)
	    double declineStartAge = 0; //After migrating, fighting ability starts declining 
	    double declineRate = 0.05;   // How quickly fighting ability declines after peak (5% per year currently, change to reflect model system)
	    
	    double ability = maxAbility - (declineRate * age); //FA calculation, ***need to more clearly define age in the model***
	    return Math.max(ability, 0.0); //prevents fighting ability from becoming negative
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
			if(!b.isMale() && b.cycleDay >= 27 && b.cycleDay <= 33) //If the Baboon object from the members bag has a state value of "male" = false, and is between cycle days 27 and 33, add to fertile array
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
		
		fertileFemales.addAll(sortedFemales); //Add all sorted females to the fertileFemales bag
		
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
			
			female.recordMating(consortMale); //record the mating event for this consortship
			
		}
		
		//Identify males eligible to form a coalition
		ArrayList<Baboon> coalitionaryMales = new ArrayList<>();
		
		for(Baboon male : sortedMales) //for each male in the sorted list of males by rank
		{
			if(male.hasCoalitionGene && !consortMales.contains(male)) //if the male has a the coalition gene and 
			{
				coalitionaryMales.add(male);
			}
		}
		
		//randomly pair males in coalitionaryMales into coalitions (add more detail in later model iteration for strategic pairing)
		 ArrayList<ArrayList<Baboon>> coalitions = formCoalitions(coalitionaryMales);
		
		
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
