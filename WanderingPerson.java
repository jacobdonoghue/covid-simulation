import java.awt.Color;
import java.awt.Graphics;

/**
 * Class to create an animated blob 
 * @author Jacob Donoghue
 * 
 */
public class WanderingPerson{
	protected double x, y;				// position
	protected double dx=0, dy=0;		// velocity, defaults to none
	protected double r=5;				// radius
	protected double dr=0;				// growth step (size and sign), defaults to none
	public double stepCounter = 0; // counts steps since the last change in direction
	public double stepsBtwChanges = 10 + (int)(10 * Math.random()); // number of steps between change in direction
	public int width;
	public int height;
	protected double infected = 0; // infection status 0 = uninfected, 1 = infected
	protected Color color = new Color(0, 0, 0);
	protected boolean isDistancing = false;
	protected double deathFactor = Math.random();
	protected double contagionFactor;
	public int sickTimer = 0;
	public int recoveryTime;
	protected boolean immune = false;
	public int velocity;
	
	/**
	 * @param x		initial x coordinate
	 * @param y		initial y coordinate
	 */
	public WanderingPerson (double x, double y, int radius, int w, int h, int status, int velocity, int recoveryFactor, double contagionF) {
		this.x = x;
		this.y = y;
		this.r = radius;
		this.width = w;
		this.height = h;
		this.infected = status;
		this.velocity = velocity;
		this.contagionFactor = contagionF;
		if (infected == 1) {
			color = new Color(255, 0, 0);
		}
		recoveryTime = (int) (recoveryFactor * Math.random());
			
	}
	
	public double getX() {
		return x;
	}
	
	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
	
	public double getR() {
		return r;
	}

	public void setR(double r) {
		this.r = r;
	}
	
	public void setV(double v) {
		this.velocity = (int)v;
	}
	

	/**
	 * Sets the velocity.
	 * @param dx	new dx
	 * @param dy	new dy
	 */
	public void setVelocity(double dx, double dy) {
		this.dx = dx;
		this.dy = dy;
	}
		
	public boolean isImmune() {
		return immune;
	}
	/**
	 * Tests whether the point is inside the blob.
	 * @param x2
	 * @param y2
	 * @return		is (x2,y2) inside the blob?
	 */
	public boolean contains(double x2, double y2) {
		double dx = x-x2;
		double dy = y-y2;
		return dx*dx + dy*dy <= r*r;
	}
	
	
	// given contagionFactor c, returns true if WanderingPerson will catch disease
	public boolean catchesDisease(double c) {
		if (Math.random() <= c) {;
			return true;
		}
		else return false;
	}
	
	// infects person
	public void infect() {
		infected = 1;
		color = new Color(255, 0, 0); 
	}
	
	// clicks recovery time counter
	public void click() {
		sickTimer += 1;
		if (sickTimer == recoveryTime) {
			immune = true;
			color = new Color(0, 0, 255);
		}
	}
	
	public int getClick() {
		return sickTimer;
	}
	
	public double getContagion() {
		return contagionFactor;
	}
	
	public double getDeathFactor() {
		return deathFactor;
	}
	public void step() {
		// Choose a new step between -1 and +1 in each of x and y 
		if (stepCounter == stepsBtwChanges || stepCounter == 0) { // if first step or at stepBtwChanges limit, initiate direction change
			dx = velocity * (Math.random() - 0.5);
			dy = velocity * (Math.random() - 0.5);
			stepCounter = 0;
			stepsBtwChanges = 10 + (int)(10 * Math.random());
		
			}
		// move blob, change direction if headed off screen
		if (x <= 3) { dx = Math.abs(dx); stepCounter = stepsBtwChanges - 2;}
		if (x >= width - 3) { dx = -1 * Math.abs(dx); stepCounter = stepsBtwChanges - 2;}
		x = x + dx;
		
		if (y + dy <= 3){dy = 5;  stepCounter = stepsBtwChanges - 2;}
		if (y + dy >= height - 3){ dy = -5; stepCounter = stepsBtwChanges - 2;}
		y += dy;
	
		stepCounter += 1; // increment stepCounter after stepping
		
	}
	
	/**
	 * Draws the blob on the graphics.
	 * @param g
	 */
	public void draw(Graphics g) {
		g.setColor(color);
		g.fillOval((int)(x-r), (int)(y-r), (int)(2*r), (int)(2*r));
	}
}
