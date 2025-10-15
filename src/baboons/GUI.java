package baboons;
import java.awt.Color;
import spaces.Spaces;
import sweep.SimStateSweep;
import sweep.GUIStateSweep;
import sim.util.media.*;
import sim.util.media.chart.*;
import sim.util.gui.*;

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
		
		
		 String[] labels = {
			    "ccPrime", "ccPostPrime", "ccSenescent",
			    "ncPrime", "ncPostPrime", "ncSenescent"
			};
		String chartTitleBar = "Cumulative Male Reproductive Success by Genotype & LifeStage";
		String xLabelBar = "Genotype x Life Stage";
		String yLabelBar = "Cumulative Offspring";
		
		//GUI.initializeBarChart(chartTitleBar, xLabelBar, yLabelBar, labels);
		
		
		// TODO Auto-generated method stub
		GUI.initialize(Environment.class, Experimenter.class, GUI.class, 400, 400, 
				Color.WHITE, Color.BLUE, true, Spaces.SPARSE);

	}

}
