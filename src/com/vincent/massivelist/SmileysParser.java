package com.vincent.massivelist;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

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
	private final Pattern pattern;
	private final HashMap<String, Integer> smileyMap;

	private SmileysParser(Context context)
	{
		this.context = context;
		this.smileyTexts = context.getResources().getStringArray(R.array.smileys_array);
		this.smileyMap = buildSmileyToRes();
		this.pattern = buildPattern();
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

	public CharSequence addSmileySpans(CharSequence text)
	{
		SpannableStringBuilder builder = new SpannableStringBuilder(text);

		Matcher matcher = pattern.matcher(text);
		
		while (matcher.find())
		{
			int resId = smileyMap.get(matcher.group());
			builder.setSpan(new ImageSpan(context, resId), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return builder;
	}
}