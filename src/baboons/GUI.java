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
	
	
	//test
	
	public static void main(String[] args) 
	{
		String[] titles = {"Coalition Gene Frequency",
				"Coalition Gene Male Percentage"}; //A string array, where every entry is the title of a chart
		String[] x = {"Time Steps", 
				"Time Steps"}; // A string array, where every entry is the x-axis title
		String[] y = {"Number of Males with Coalition Gene", 
				"Percentage of Males with Coalition Gene"}; //A string array, where every entry is the y-axis title
		
		GUI.initializeArrayTimeSeriesChart(2, titles, x, y); //creates as many charts as indicated by the first number
		GUI.initializeHistogramChart("Male Reproductive Success: Gene x LifeStage", "Category", "Avg Offspring", 6);
		
		
		// TODO Auto-generated method stub
		GUI.initialize(Environment.class, Experimenter.class, GUI.class, 400, 400, 
				Color.WHITE, Color.BLUE, true, Spaces.SPARSE);

	}

}
