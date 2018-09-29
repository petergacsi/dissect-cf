package hu.uszeged.inf.iot.simulator.pliant;

/**
 * This function helps for decision making
 * @author dombijd
 *
 */
public class Kappa {

	public Kappa()
	{
		lambda = 1.0;
		nu = 0.5;
	}
	
	public Kappa(double lambda, double nu)
	{
		this.lambda = lambda;
		this.nu = nu;
	}
	
	public Double getAt(Double x)
	{		
		return 1.0 / (1.0 + Math.pow(( (nu/(1.0-nu)) * ((1.0-x) / x)), lambda) );
	}
	
	private double lambda;
	private double nu;
	
}
