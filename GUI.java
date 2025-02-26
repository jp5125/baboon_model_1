package baboons;
import java.awt.Color;
import spaces.Spaces;
import sweep.SimStateSweep;
import sweep.GUIStateSweep;


public class GUI extends GUIStateSweep
{

	public GUI(SimStateSweep state, int gridWidth, int gridHeight, Color backdrop, Color agentDefaultColor,
            boolean agentPortrayal)
	{
		super(state, gridWidth, gridHeight, backdrop, agentDefaultColor, agentPortrayal);
	}
	
	
	
	
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		GUI.initialize(Environment.class, null, GUI.class, 400, 400, 
				Color.WHITE, Color.BLUE, true, Spaces.SPARSE);

	}

}
