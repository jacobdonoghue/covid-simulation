import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.SwingUtilities;


public class CovidDriver extends DrawingGUI{
	
	// initiate module for mode -- allow them to begin input
	public CovidDriver(){
		System.out.print("CovidDriver initiated");
		Covid sim = new Covid(
			0.05, // death rate
			0.15,  // reactFactor
			0.25,  // distFactor
			0.85,  // propDistancing
			0.03,    // endDist
			0.5,  // contagionFactor
			"1", // trial
			true, // draw
			false); // onlyMetrics 
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			public synchronized void run() {
				new CovidDriver();
			}
		});
	}
	
}

