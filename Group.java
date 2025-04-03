package baboons;

import java.awt.Color;
import sim.engine.*;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Bag;
import sweep.GUIStateSweep;
import java.util.Comparator;
import java.util.Collections;
import java.util.ArrayList;

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
			Baboon male = (Baboon)males.objs[i];
			
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
			male.dominanceRank = rank + 1;
		}
	}
	
	// Age-fighting ability function based on the figure
	public double calculateFightingAbility(int age) 
	{
	    /*
	    This function should follow the developmental trajectory from the figure:
	    
	    - Rapidly increases to peak after migration (e.g., between ages 8–10).
	    - Peaks around early adulthood (e.g., 10–12 years).
	    - Slowly declines afterward, reducing fighting ability and rank.
	    
	    Example simplified formula:
	    - Peak fighting ability at age ~10-12, then linear/exponential decline.
	    - You can refine the exact equation later based on biological data.
	    */
	    
	    double peakAge = 11.0;      // Age at peak fighting ability
	    double maxAbility = 1.0;    // Maximum fighting ability (normalized)
	    double declineRate = 0.1;   // How quickly fighting ability declines after peak
	    
	    if (age <= peakAge) {
	        // Younger males increase rapidly in strength
	        return (age / peakAge) * maxAbility;
	    } else {
	        // Older males decline steadily after the peak
	        double ability = maxAbility - ((age - peakAge) * declineRate);
	        return Math.max(ability, 0); // fighting ability cannot be negative
	    }
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
			Baboon b = (Baboon)members.objs[0]; // ***does this need to be handled differently since groups in this model can be larger than 2 individuals?***
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
