import java.io.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.*;
import javax.swing.*;


public class Covid extends DrawingGUI {
	// constant parameters
	private static final int width = 1000, height = 800; // setup: window size
	private static final int totalNum = 2000;
	private static final int numInfected = 30;
	private static final int numHealthy = totalNum - numInfected;			// setup: how many blobs
	private static final int numToMove = (totalNum)/2;			// setup: how many blobs to animate each frame
	private static final int r = 2;
	private static final double safeDistance = r * 1.5;
	private static final int velocity = 5;
	private static final int recoveryFactor = 300;
	
	// values taken as parameters
	private boolean onlyMetrics;
	private boolean draw;
	private String trialNumber;
	private double deathRate;
	private double reactionFactor; // What % of population needs to be infected before distancing measures 
	private double distancingFactor; // what percent of velocity to operate at
	private double propDistancing;
	private double endDistancing;
	private double contagionFactor;
	
	// initializing variables for measurement
	private boolean complete = false;
	private int maxInfected = 0;
	private ArrayList<Integer> distBegin = new ArrayList<Integer>(); // records time that distancing is engaged/disengaged
	private ArrayList<Integer> distEnd = new ArrayList<Integer>();
	private boolean distEngaged = false;
	private BufferedImage result;						// the picture being painted
	// Arrays for healthy, exposed, recovered, and dead people
	private ArrayList<WanderingPerson> healthy;	
	private ArrayList<WanderingPerson> exposed;
	private ArrayList<WanderingPerson> recovered;
	private ArrayList<WanderingPerson> dead;
	
	
	// Arrays to keep statistics for amount of healthy, exposed, and recovered people at any given time
	private ArrayList<Integer> healthyStats = new ArrayList<Integer>();
	private ArrayList<Integer> sickStats = new ArrayList<Integer>();
	private ArrayList<Integer> recoveredStats = new ArrayList<Integer>();
	private ArrayList<Integer> deadStats = new ArrayList<Integer>();
	private int statClicker = 0;
	// takes parameters: deathRate, velocity, recoveryFactor, reactionFactor, distancingFactor, endDistancing
	
	public Covid( double deathrt,  double reactFactor, double distFactor, double propDistancing,
								double endDist, double contagionFact, String trial, boolean draw, boolean onlyMetrics) {
		super("Covid" + " trial " + trial, width, height);
		if (draw) result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		System.out.print("Simulation begun!");
		this.deathRate = deathrt;
		this.reactionFactor = reactFactor;
		this.distancingFactor = distFactor;
		this.propDistancing = propDistancing;
		this.endDistancing = endDist;
		this.trialNumber = trial;
		this.draw = draw;        
		this.contagionFactor = contagionFact;
		this.onlyMetrics = onlyMetrics;
		
		// create a bunch of healthy people
		healthy = new ArrayList<WanderingPerson>();
		
		for (int i = 0; i<numHealthy; i++) {
			int x = (int)(width*Math.random());
			int y = (int)(height*Math.random());
			//Create a person (just with location)
			healthy.add(new WanderingPerson(x, y, r, width, height, 0, velocity, recoveryFactor, contagionFactor));
		}
		
		// Create an infected person
		exposed = new ArrayList<WanderingPerson>();

		for (int i = 0; i<numInfected; i++) {
			int x = (int)(width*Math.random());
			int y = (int)(height*Math.random());
			//Create a person (just with location)
			exposed.add(new WanderingPerson(x, y, r, width, height, 1, velocity, recoveryFactor, contagionFactor));
		}
		// initialize recovered, dead arrays
		recovered = new ArrayList<WanderingPerson>();
		dead = new ArrayList<WanderingPerson>();
		
		startTimer();
	}
	
	
	/**
	 * DrawingGUI method, here just drawing all the people
	 */
	@Override
	public void draw(Graphics g) {
		if (draw) {
			g.drawImage(result,  0,  0, null);
			for (WanderingPerson h : healthy) {
				h.draw(g);
			}
			for (WanderingPerson e : exposed) {
				e.draw(g);
			}
			for (WanderingPerson r : recovered) {
				r.draw(g);
			}
		}
	}
	
	// Function to see if two people are close enough to infect one another 
	private boolean bump(WanderingPerson p1, WanderingPerson p2) {
		// returns true if difference in x and y are both close enough
		if (Math.abs(p1.getX() - p2.getX()) < safeDistance 
				&& Math.abs(p1.getY() - p2.getY()) < safeDistance) {
			return true;
		}
		else return false;
	}
	
	// Function to scale velocity of all people 
	private void beginDistancing() {
		distEngaged = true;
		// distance for propDistancing out of healthy
		for (WanderingPerson h : healthy) {
			if (Math.random() < propDistancing) h.setV(velocity * distancingFactor);
		}
		// distance for propDistancing out of healthy
		for (WanderingPerson e : exposed) {
			if (Math.random() < propDistancing) e.setV(velocity * distancingFactor);
		}
		// distance for propDistancing out of healthy
		for (WanderingPerson r : recovered) {
			if (Math.random() < propDistancing) r.setV(velocity * distancingFactor);
			
		}
		System.out.println("DISTANCING ENGAGED TRIAL " + trialNumber);
		distBegin.add(healthyStats.size());
	}
	
	private void stopDistancing() {
		System.out.println("DISTANCING De-ENGAGED " + trialNumber);
		distEngaged = false;
		// return velocity to normal to show end of distancing
		for (WanderingPerson h : healthy) h.setV(velocity);
		for (WanderingPerson e : exposed) e.setV(velocity);
		for (WanderingPerson r : recovered) r.setV(velocity);	
		distEnd.add(healthyStats.size()); 
	}
	// Function to let driver know if simulation is complete
	public boolean isComplete() {
		if (complete) return true;
		return false;
	}
	/**
	 * DrawingGUI method, here moving some of the blobs
	 */
	@Override
	public void handleTimer() {
		if (complete) return;
		// move healthy people
		int numHealthyToMove = (int) numToMove * healthy.size() / totalNum;
		if (healthy.size() > 0) {
			for (int b = 0; b < numHealthyToMove; b++) {
				// Pick a random blob and ask it to move
				WanderingPerson person = healthy.get((int)(Math.random()*healthy.size()));
				person.step();
			}
		}
		// move exposed people
		int numExposedToMove = (int) numToMove * exposed.size() / totalNum;
		if (exposed.size() > 0) {
			for (int b = 0; b < numExposedToMove; b++) {
				// Pick a random blob and ask it to move
				WanderingPerson person = exposed.get((int)(Math.random()*exposed.size()));
				person.step();
			}
		}
		
		// move recovered people 
		int numRecoveredToMove = (int) numToMove * recovered.size() / totalNum;
		if (recovered.size() > 0) {
			for (int b = 0; b < numRecoveredToMove; b++) {
				// Pick a random blob and ask it to move
				WanderingPerson person = recovered.get((int)(Math.random()*recovered.size()));
				person.step();
				if (b > recovered.size()) break;
			}
		}
		
		// create arrays for newly infected, newly recovered, and newly dead
		ArrayList<WanderingPerson> newlyInfected = new ArrayList<WanderingPerson>();
		ArrayList<WanderingPerson> newlyRecovered = new ArrayList<WanderingPerson>();
		ArrayList<WanderingPerson> newlyDead = new ArrayList<WanderingPerson>();
		
		// check if any exposed blobs are close enough to pass the disease
		for (WanderingPerson p : exposed) {
			//iterate through healthy 
			for (WanderingPerson h: healthy) {
				// if healthy close enough to infect and is not immune
				if ( !h.isImmune() && bump(p, h)) {
					// check if person catches disease
					if (h.catchesDisease(p.getContagion())) {
						h.infect();
						newlyInfected.add(h);
					}
				}
			}
			p.click();
			
			// deal with recovery
			if (p.isImmune()) {
				newlyRecovered.add(p);
			}
			// deal with death
			if (p.getDeathFactor() > 1 - deathRate && p.getClick() >= 25) {
				newlyDead.add(p);
			}
			
		}
	
		// put all newly infected in exposed list
		for (WanderingPerson i : newlyInfected) {
			exposed.add(i);
			healthy.remove(i);
		}
		// if (exposed.size() > maxInfected) maxInfected = exposed.size();
		// put all newly recovered in recovered list
		for (WanderingPerson r : newlyRecovered) {
			recovered.add(r);
			exposed.remove(r);
		}
		// put all newly dead in dead list
		for (WanderingPerson d : newlyDead) {
			dead.add(d);
			exposed.remove(d);
		}
		
		// begin distancing when reactionFactor proportion of the population is infected
		if (!distEngaged && (exposed.size() >= (reactionFactor * totalNum))) beginDistancing();
		
		// end distancing when endDistancing proportion of the population is infected
		else if (distEngaged && (exposed.size() <= (endDistancing * totalNum))) stopDistancing();
		
		// take stats every 15 clicks 
		if (statClicker % 15 == 0) {
			healthyStats.add(healthy.size());
			sickStats.add(exposed.size());
			recoveredStats.add(recovered.size());
			deadStats.add(dead.size()); 
			System.out.print("Time: "); System.out.println(statClicker/15);
			
		}
		statClicker += 1;
		// write statistics to output file when infection is over
		if ((exposed.size() <= 5 || healthyStats.size() > 400) && !complete) {
			try {
				if (!onlyMetrics) { 
					// initialize new file and write to it
					BufferedWriter output = new BufferedWriter(new FileWriter("inputs/CovidSimulation" + trialNumber + ".txt", true));
					System.out.println("Writing output to file " + "inputs/CovidStats" + trialNumber + ".txt");
					// First out value of parameters
					output.write("%Initial Infected: " + numInfected + "\n");
					output.write("%Velocity: " + velocity + "\n");
					output.write("%Death Rate: " + deathRate + "\n");
					output.write("%Recovery Factor: " + recoveryFactor + "\n");
					output.write("%Reaction Factor: " + reactionFactor + "\n");
					output.write("%Distancing Factor: " + distancingFactor + "\n");
					output.write("%Safe Distance: " + safeDistance + "\n");
					output.write("distancingTime" + trialNumber + " = " + distBegin + distEnd + ";" + "\n");
					
					// Then write out results
					output.write("Healthy" + trialNumber + " = " + healthyStats + ";" + "\n");
					output.write("Sick" + trialNumber + " = "  + sickStats + ";" + "\n");
					output.write("Recovered" + trialNumber + " = "  + recoveredStats + ";" + "\n"); 
					output.write("Dead" + trialNumber + " = "  + deadStats + ";" + "\n");
					output.write("covidAnalyzer(Healthy1, Sick1, Recovered1, Dead1, distancingTime1);" + "\n");
	
						
					System.out.println("COVID Simulation Complete");
					System.out.println(healthyStats);
					output.close();	
					}
				else {
					// only write out metrics of evaluation: maxInfected, totalInfected, totalDead, totalTime
					
					// calculate maxInfected and write to file
					for (int i: sickStats) {
						if (i > maxInfected) maxInfected = i;
					}
					
					/*  Code to write to file -- only used in project setting/analyzing results
					 * // DISTANCING VARIATION OUTPUT
					  BufferedWriter output1 = new BufferedWriter(new FileWriter("inputs/maxInfected" + trialNumber + ".txt", true));
					  System.out.println("writing to inputs/maxInfected" + trialNumber + ".txt");
					  output1.write("["+ endDistancing + "," + maxInfected + "],");
					  output1.close();
					  
					  // calculate totalInfected and write to file 
					  int totalInfected = deadStats.get(deadStats.size() - 1) + recoveredStats.get(recoveredStats.size() - 1) + 
							  	sickStats.get(sickStats.size() - 1); 
					  BufferedWriter output2 = new BufferedWriter(new FileWriter("inputs/totalInfected" + trialNumber + ".txt",
					  true)); System.out.println("writing to inputs/totalInfected" + trialNumber + ".txt"); 
					  output2.write("["+ endDistancing + "," + totalInfected + "],");
					  output2.close();
					  
					// store/write out time that distancing is begun and ended
					  
					  BufferedWriter output3 = new BufferedWriter(new FileWriter("inputs/distBegin" + trialNumber + ".txt", true));
					  System.out.println("writing to inputs/distBegin" + trialNumber + ".txt");
					  output3.write("["+ endDistancing + "," + distBegin.size()+ "],");
					  output3.close();
					  
					  
					 BufferedWriter output4 = new BufferedWriter(new FileWriter("inputs/distEnd" + trialNumber + ".txt", true));
					 System.out.println("writing to inputs/distEnd" + trialNumber + ".txt");
			         output4.write("["+ endDistancing + "," + distEnd.size()+ "],");
					 output4.close(); */ 
					 
					 
					/*
					 * BufferedWriter output5 = new BufferedWriter(new FileWriter("inputs/simTime" +
					 * trialNumber + ".txt", true)); System.out.println("writing to inputs/simTime"
					 * + trialNumber + ".txt"); output5.write("["+ endDistancing + "," +
					 * healthyStats.size() + "],"); output5.close();
					 */
					 
					 if (!distBegin.isEmpty()  && !distEnd.isEmpty()) {
						 int timeDistancing = distEnd.get(0) - distBegin.get(0);
						 BufferedWriter output6 = new BufferedWriter(new FileWriter("inputs/timeDistancing" + trialNumber + ".txt", true));
						 System.out.println("writing to inputs/timeDistancing" + trialNumber + ".txt");
				         output6.write("["+ endDistancing + "," + timeDistancing + "],");
						 output6.close();
					 }
					 else if (!distEnd.isEmpty()) {
						 int timeDistancing = healthyStats.size() - distBegin.get(0);
						 BufferedWriter output6 = new BufferedWriter(new FileWriter("inputs/timeDistancing" + trialNumber + ".txt", true));
						 System.out.println("writing to inputs/timeDistancing" + trialNumber + ".txt");
				         output6.write("["+ endDistancing + "," + timeDistancing + "],");
						 output6.close();
					 }
					 
					/*
					 * // CONTAGIONFACTOR VARIATION OUTPUT BufferedWriter output1 = new
					 * BufferedWriter(new FileWriter("inputs/maxInfected" + trialNumber + ".txt",
					 * true)); System.out.println("writing to inputs/maxInfected" + trialNumber +
					 * ".txt"); output1.write("[" + contagionFactor+ "," + maxInfected + "],");
					 * output1.close();
					 * 
					 * // calculate totalInfected and write to file int totalInfected =
					 * deadStats.get(deadStats.size() - 1) +
					 * recoveredStats.get(recoveredStats.size() - 1) +
					 * sickStats.get(sickStats.size() - 1); BufferedWriter output2 = new
					 * BufferedWriter(new FileWriter("inputs/totalInfected" + trialNumber + ".txt",
					 * true)); System.out.println("writing to inputs/totalInfected" + trialNumber +
					 * ".txt"); output2.write("[" + contagionFactor+ "," + totalInfected + "],");
					 * output2.close();
					 * 
					 * // calculate totalDead and write to file int totalDead =
					 * deadStats.get(deadStats.size() - 1); BufferedWriter output3 = new
					 * BufferedWriter(new FileWriter("inputs/totalDead" + trialNumber + ".txt",
					 * true)); System.out.println("writing to inputs/totalDead" + trialNumber +
					 * ".txt"); output3.write("[" + contagionFactor + "," + totalDead + "],");
					 * output3.close();
					 * 
					 * // calculate time until simulation ends int totalTime = deadStats.size();
					 * BufferedWriter output4 = new BufferedWriter(new FileWriter("inputs/totalTime"
					 * + trialNumber + ".txt", true));
					 * System.out.println("writing to inputs/totalTime" + trialNumber + ".txt");
					 * output4.write("[" + contagionFactor+ "," + totalTime + "],");
					 * output4.close();
					 */
					  
					 
					/*
					 * // CONTROL OUTPUT BufferedWriter output1 = new BufferedWriter(new
					 * FileWriter("inputs/maxInfected" + trialNumber + ".txt", true));
					 * System.out.println("writing to inputs/maxInfected" + trialNumber + ".txt");
					 * output1.write( maxInfected + ","); output1.close();
					 * 
					 * // calculate totalInfected and write to file int totalInfected =
					 * deadStats.get(deadStats.size() - 1) +
					 * recoveredStats.get(recoveredStats.size() - 1) +
					 * sickStats.get(sickStats.size() - 1); BufferedWriter output2 = new
					 * BufferedWriter(new FileWriter("inputs/totalInfected" + trialNumber + ".txt",
					 * true)); System.out.println("writing to inputs/totalInfected" + trialNumber +
					 * ".txt"); output2.write(totalInfected + ","); output2.close();
					 * 
					 * // calculate totalDead and write to file int totalDead =
					 * deadStats.get(deadStats.size() - 1); BufferedWriter output3 = new
					 * BufferedWriter(new FileWriter("inputs/totalDead" + trialNumber + ".txt",
					 * true)); System.out.println("writing to inputs/totalDead" + trialNumber +
					 * ".txt"); output3.write(totalDead + ","); output3.close();
					 * 
					 * // calculate time until simulation ends int totalTime = deadStats.size();
					 * BufferedWriter output4 = new BufferedWriter(new FileWriter("inputs/totalTime"
					 * + trialNumber + ".txt", true));
					 * System.out.println("writing to inputs/totalTime" + trialNumber + ".txt");
					 * output4.write(totalTime + ","); output4.close();
					 */
				}
				// make sure only write to file once
				complete = true;
			}
			catch(IOException ex) {
				System.out.println("IOException caught");
			} 
			
			
		}
		
		// Now update the people's locations
		repaint();
	}
	
	public void main(String[] args) {
		while (!complete) {
			SwingUtilities.invokeLater(new Runnable(){
				public synchronized void run() {
					new Covid(deathRate, reactionFactor, distancingFactor, propDistancing, endDistancing, contagionFactor, trialNumber, draw, onlyMetrics);
				}
			});
		}
	}
	
}
