package hkust.comp3111h.ballcraft.server;

import java.io.Serializable;

public class Vector2f implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7481119251470929286L;
	
	public float x;
	public float y;
	
	public Vector2f(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Vector2f(Vector2f v)
	{
		this.x = v.x;
		this.y = v.y;
	}
	
	public float length()
	{
		return (float) Math.sqrt(x * x + y * y);
	}
	
	public void add(Vector2f v)
	{
		x += v.x;
		y += v.y;
	}
	
	public float distanceSqured(Vector2f v)
	{
		return (x - v.x) * (x - v.x) + (y - v.y)*(y - v.y);
	}

	public void scalarMul(float rate)
	{
		x *= rate;
		y *= rate;
	}
	

}