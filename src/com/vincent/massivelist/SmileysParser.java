package com.vincent.massivelist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;

public class SmileysParser
{
	private static SmileysParser instance;
	
	public static SmileysParser getInstance()
	{
		return instance;
	}
	public static void init(Context context)
	{
		instance = new SmileysParser(context);
	}
	private final Context context;
	private final String[] smileyTexts;
	private final Pattern smileyMapPattern;
	private final HashMap<String, Integer> smileyMap;
	
	private final HashMap<String, String> imgMap;
	private final List<String[]> imageNames;
	private final Pattern imgMapPattern;
	
	private SmileysParser(Context context)
	{
		this.context = context;
		this.smileyTexts = context.getResources().getStringArray(R.array.smileys_array);
		this.smileyMap = buildSmileyToRes();
		this.smileyMapPattern = buildPattern();
		
		this.imgMap = getImgMap();
		this.imageNames = getImgNames();
		this.imgMapPattern = imgMapPattern();
	}

	public static final int[] SMILEY_RES_IDS =
	{
		Smileys.getSmiley(Smileys.happy),
		Smileys.getSmiley(Smileys.love),
		Smileys.getSmiley(Smileys.cool),
		Smileys.getSmiley(Smileys.wink),
		Smileys.getSmiley(Smileys.sad),
		Smileys.getSmiley(Smileys.dead)
	};
		

	private HashMap<String, Integer> buildSmileyToRes()
	{
		if (SMILEY_RES_IDS.length != smileyTexts.length)
		{
			throw new IllegalStateException("Smiley resource ID/text mismatch");
		}

		HashMap<String, Integer> smileyToRes = new HashMap<String, Integer>(smileyTexts.length);
		
		for (int i = 0; i < smileyTexts.length; i++)
		{
			smileyToRes.put(smileyTexts[i], SMILEY_RES_IDS[i]);
		}
		return smileyToRes;
	}
	
	private HashMap<String, String> getImgMap()
	{
		return ((MainListActivity) context).getImageMap();
	}
	private List<String[]> getImgNames()
	{
		return ((MainListActivity) context).getImageName();
	}

	private Pattern buildPattern()
	{
		StringBuilder patternString = new StringBuilder(smileyTexts.length * 3);
		patternString.append('(');
		
		for (String s : smileyTexts)
		{
			patternString.append(Pattern.quote(s));
			patternString.append('|');
		}
		patternString.replace(patternString.length() - 1, patternString.length(), ")");
		
		return Pattern.compile(patternString.toString());
	}
	
	private Pattern imgMapPattern()
	{
		if (!imageNames.isEmpty())
		{
			StringBuilder patternString = new StringBuilder(imageNames.size() * 3);
			patternString.append('(');
			
			for (String[] imgNames : imageNames)
			{
				patternString.append(Pattern.quote(imgNames[0]));
				patternString.append('|');
				Log.i("PatternImgMap", imgNames[0]);
			}
			patternString.replace(patternString.length() - 1, patternString.length(), ")");
			
			return Pattern.compile(patternString.toString());
		}
		return Pattern.compile("Image Files Empty!");
	}

	public CharSequence addSmileySpans(CharSequence text)
	{
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		
		String SDPath = Environment.getExternalStorageDirectory().getPath();
		String cacheDir = context.getResources().getString(R.string.cache_dirname);
		
		Matcher smileyMatcher = smileyMapPattern.matcher(text);
		
		while (smileyMatcher.find())
		{
			int resId = smileyMap.get(smileyMatcher.group());
			Drawable resDraw = context.getResources().getDrawable(resId);
			resDraw.setBounds(0, 0, 50, 50);
			
			ImageSpan imageSpan = new ImageSpan(resDraw, ImageSpan.ALIGN_BOTTOM); 
			builder.setSpan(imageSpan, smileyMatcher.start(), smileyMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		if (!imgMapPattern.toString().equals("Image Files Empty!"))
		{
			Matcher imgMatcher = imgMapPattern.matcher(text);

			while (imgMatcher.find())
			{
				String imgFileName = imgMap.get(imgMatcher.group());
				//Log.d("imgMap~~~~~", imgFileName);

				Drawable resDraw = Drawable.createFromPath(SDPath + "/" + cacheDir + "/" + imgFileName);
				resDraw.setBounds(0, 0, 50, 50);

				//Bitmap bitImg = compressImage(drawableToBitmap(resDraw));

				ImageSpan imageSpan = new ImageSpan(resDraw, ImageSpan.ALIGN_BOTTOM); 
				builder.setSpan(imageSpan, imgMatcher.start(), imgMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		} else
		{
			String textString = text.toString();
			if (textString.contains("/"))
			{
				Log.i("EmptyPattern~~", textString + "\n" + textString.indexOf("/") + " " + textString.lastIndexOf("/"));
				Drawable waitDraw = context.getResources().getDrawable(R.drawable.wait01);
				waitDraw.setBounds(0, 0, 50, 50);
				ImageSpan imageSpan = new ImageSpan(waitDraw, ImageSpan.ALIGN_BOTTOM);
				builder.setSpan(imageSpan, textString.indexOf("/"), textString.lastIndexOf("/")+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return builder;
	}
	
	private static Bitmap drawableToBitmap(Drawable draw)
	{
		int width = draw.getIntrinsicWidth();
		int height = draw.getIntrinsicHeight();
		
		Bitmap bitImg = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitImg);
		
		draw.setBounds(0, 0, width, height);
		draw.draw(canvas);
		
		return bitImg;
	}
	
	private static Bitmap compressImage(Bitmap image)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 90, baos);
		
		int options = 80;
		
		while (baos.toByteArray().length / 1024 > 100)
		{
			baos.reset();
			image.compress(Bitmap.CompressFormat.PNG, options, baos);
			options -= 10;
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Bitmap bitmap = BitmapFactory.decodeStream(bais, null, null);
		
		return bitmap;
	}
}