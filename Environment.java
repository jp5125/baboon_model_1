package baboons;
import sim.engine.SimState;
import spaces.Spaces;
import sweep.SimStateSweep;

public class Environment extends SimStateSweep
{
	public Environment(long seed, Class observer)
	{
		super(seed, observer);
	}
	
	public void start()
	{
		super.start();
		spaces = Spaces.SPARSE; //set the space
		make2DSpace(spaces, gridWidth, gridHeight);//make the space
		
	}

}
