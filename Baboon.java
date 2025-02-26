package baboons;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;

public class Baboon implements Steppable
{
	
	int age; //current age of the agent
	boolean male; //sex of agent
	public Group group; //the group that the agent is currently a member of
	
	
	//variables used for calculations
	Environment state;
	public Stoppable event;
	
	
	public Baboon(Environment state, boolean male, int age)
	{
		this.state = state;
		this.male = male;
		this.age = age;
		
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




	@Override
	public void step(SimState state)
	{
		
	}

}
