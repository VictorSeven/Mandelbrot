
package org.mandelbrot; //Las clases se agrupan en paquetes. Este programa solo tiene este

import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

import org.newdawn.slick.geom.Rectangle;

public class Main extends BasicGame
{
	//Constructor del juego mediante la clase BasicGame. 
	//Este es código puesto por defecto.
	public Main(String gamename)
	{
		super(gamename); 
	}
	
	Input input; //Controla el ratón
	float mx1, my1; //Para almacerna el lugar donde hemos hecho click
	boolean clicked; //Para comprobar si hicimos click
	Rectangle selec; //Rectángulo que almacena nuestra selección de zoom.
	Rectangle[] p = new Rectangle[800*600]; //Usados para dibujar los píxeles
	Color[] c = new Color[800*600]; //Usados para seleccionar el color de cada pixel
	int colorIndex = 0; //Para ir dandole valores a c[i]
	double deltaX, deltaY;  //Diferencia de valor entre píxel y píxel
	double initialX, initialY; //Punto donde se comienza a dibujar
	

	//Inicializa las variables a su estado inicial y calcula el fractal
	@Override
	public void init(GameContainer gc) throws SlickException 
	{

		selec = new Rectangle(0,0,0,0); //Inicamos rectángulo de selección
		clicked = false; 
		deltaX = 0.00375;
		deltaY = 0.005; //Valores iniciales para el fractal, correspondientes a deltaX = 3/800, deltaY = 3/600
		initialX = -2; //Posición desde la que empezar a dibujar en X
		initialY = 1.5; //Posición desde la que empezar a dibujar en Y
		calculateMandelbrot(initialX, initialY, deltaX, deltaY, gc);
		
	}

	@Override
	public void update(GameContainer gc, int i) throws SlickException 
	{
		input = gc.getInput(); //Comprobamos estado del ratón
		
		if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON))
		{
			if (clicked) 
			{
				//Si ya hemos hecho click, es que tenemos una esquina cogida
				clicked = false;
				colorIndex=0; //Limpiar array de colores
				initialX = initialX + mx1*deltaX; 
				initialY = initialY - my1*deltaY; //Vemos el punto en el que vamos a comenzar, según nuestra escala
				//Recalculamos los deltas según el el tamaño de nuestro rectángulo
				deltaX = (deltaX*Math.abs(mx1-input.getMouseX()))/gc.getWidth();
				deltaY = (deltaY*Math.abs(my1-input.getMouseY()))/gc.getHeight(); 
				//Recalculamos fractal
				calculateMandelbrot(initialX, initialY, deltaX, deltaY, gc);
				System.out.println(Double.toString(deltaX) + " " + Double.toString(deltaY));

			}
			else
			{
				//No hemos pulsado aún, así que hacemos clicked true
				//y almacenamos la posición del click
				clicked = true;
				mx1 = input.getMouseX();
				my1 = input.getMouseY();
			}
		}
		else if (input.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON))
		{
			if (clicked)
			{
				//Si hemos pulsado, el derecho cancela la acción
				clicked = false;
			}
			else
			{
				//Valores iniciales de nuevo. Permite salir del zoom.
				deltaX = 0.00375;
				deltaY = 0.005;
				initialX = -2;
				initialY = 1.5;
				colorIndex=0; //Limpiar array de colores
				calculateMandelbrot(initialX, initialY, deltaX, deltaY, gc);
			}
		}
		
		if (clicked)
		{
			//Actualizamos el rectángulo para poder ir dibujándolo en la pantalla.
			selec.setBounds(mx1, my1, (-mx1+ input.getMouseX()), (-my1+input.getMouseY()));	
		}
	}
	
	

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException
	{
		int i;
		//Dibujamos todos los rectángulos con su color correspondiente
		for (i=0; i<p.length; i++)
		{
			//if (c[i]!=Color.white){
				g.setColor(c[i]);
				g.draw(p[i]);
			//}
		
		}
		
		//Si se ha pulsado, redibujamos rectángulo
		if (clicked)
		{
			g.setColor(Color.red);
			g.draw(selec);
		}
	}

	//Método por defecto; inicializa el programa
	public static void main(String[] args)
	{
		try
		{
			AppGameContainer appgc;
			appgc = new AppGameContainer(new Main("Mandelbrot Set"));
			appgc.setDisplayMode(800, 600, false);
			appgc.start();
		}
		catch (SlickException ex)
		{
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	
	private void mandelbrotSeriesConvergence(Complex z0, int iterations, double acot)
	{
		Complex z;
		int i;
		
		z = z0;
		i=0;
		//Calculamos la serie mientras no diverja
		while  (i < iterations && !Double.isNaN(z.r))
		{
			z = new Complex(z.x*z.x - z.y*z.y+ z0.x, z.x*z.y*2+z0.y);
			i++;
		}
		
		//Comprobamos si la serie converge o no
		if (z.r < acot)
		{
			//Si es convergente, lo dibujamos de blanco
			//(Pertenece al conjunto)
			c[colorIndex]=Color.white;
			colorIndex++;
		}
		else
		{
			//Si diverge, vemos cuánto ha tardado el valor del número
			//a ser NaN. La multiplicación por 5 es simplemente para lograr
			//un color bonito
			c[colorIndex] = new Color(i*5);
			colorIndex++;
		}
		
		return;
	}
	
	private void calculateMandelbrot(double initX, double initY, double factorX, double factorY, GameContainer gc)
	{
		Complex z0;
		int i, j, t;
		t=0;

		//Realizamos un bucle por cada uno de los píxeles de la pantalla
		for (i =0; i < gc.getWidth(); i++)
		{
			for (j=0; j < gc.getHeight(); j++)
			{
				//Vamos viendo el valor de los complejos según hemos reescalado
				z0 = new Complex(initX+i*factorX, initY-j*factorY);
				
				//Comprobamos la convergencia
				mandelbrotSeriesConvergence(z0, 1000, 1e10); //)
				p[t] = new Rectangle((float)i, (float)j, 1,1);
				
				t++;
			}
			
		}
		//System.out.println("Completed");
	}
	

	
}
	
	

