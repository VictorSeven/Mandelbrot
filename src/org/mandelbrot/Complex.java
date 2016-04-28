package org.mandelbrot;

//Esta clase es simplemente conceptual

public class Complex {
	
	public double x, y;
	public double r;
	
	public Complex(double a, double b)
	{
			x = a;
			y = b;
			
			r = Math.sqrt(x*x+y*y);
	}
	

}
