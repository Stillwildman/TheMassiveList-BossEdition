package com.vincent.massivelist;

public class Smileys
{
	private static final int[] iconIDs =
		{
			R.drawable.smiley_happy,
			R.drawable.smiley_love,
			R.drawable.smiley_cool,
			R.drawable.smiley_wink,
			R.drawable.smiley_sad,
			R.drawable.smiley_dead,
		};
	public static int happy = 0;
	public static int love = 1;
	public static int cool =2;
	public static int wink = 3;
	public static int sad = 4;
	public static int dead = 5;



	public static int getSmiley(int which)
	{
		return iconIDs[which];
	}
}