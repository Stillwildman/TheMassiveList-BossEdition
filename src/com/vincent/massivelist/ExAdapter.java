package com.vincent.massivelist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ExAdapter extends BaseExpandableListAdapter {
	
	private Context context;
	private LayoutInflater inflater;
	private List<Map<String, String>> listGroup;
	private List<List<Map<String, String>>> listChild;
	
	private Random ran;
	private int ranCount;
	private ArrayList<Integer> ranPosList;
	private ArrayList<String> ranColorList;
	private StringBuilder htmlSb;
	
	SmileysParser parser;
	
	private ArrayList<Integer> ranSmileyNumList;			//�o�̪� NumList �`���׳��� getGroupCount()�A
	//private ArrayList<Integer> ranUrlNumList;				//�̭����Ȭ� 0 ~ .size() or .length()
	//ImageLoader imageLoader;
	
	private List<String[]> smileyName;
	private List<String[]> imageName;
	
	private ArrayList<Integer> ranHtmlCountList;
	private ArrayList<String> ranHtmlColorList;
	private ArrayList<Integer> ranHtmlIconList;
	
	public ExAdapter(Context context, List<Map<String, String>> listGroup,List<List<Map<String, String>>> listChild)
	{
		this.context = context;
		this.listGroup = listGroup;
		this.listChild = listChild;
		
		SmileysParser.init(context);
		parser = SmileysParser.getInstance();
		//BitmapFactory.decodeResource(context.getResources(), R.drawable.coffee_icon);
		
		inflater = LayoutInflater.from(context);
		//imageLoader = new ImageLoader(context.getApplicationContext());
		
		smileyName = ((MainListActivity) context).getSmileyName();
		ranSmileyNumList = getRanSmileyNum();
		
		ranCount = (int) (getGroupCount() * 0.5);
		setRanColor();
		
		//ranUrlNumList = getRanUrlNum();
		
		imageName = ((MainListActivity) context).getImageName();  //��o�w�s�bcache����image�ɦW
		ranHtmlCountList = new ArrayList<Integer>();			//�o3�ӪF��A�O�ΨӱN �H��Color & �H��imageName �s��List�A
		ranHtmlColorList = new ArrayList<String>();				//�M��n�b�Y�� isDivisible ���a����ܥΪ�~
		ranHtmlIconList = new ArrayList<Integer>();
		setRanHtmlAtDivisible(5);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return listGroup.size();
	}
	
	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return listGroup.get(groupPosition);
	}
	
	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}
	
	@SuppressLint("InflateParams") @Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		ViewHolder holder;				//�o�N�O�� Efficient �� ViewHolder & convertView ���Q�ΡA�U�ظ`�ٸ귽���I
		
		if (convertView == null)
		{
			convertView = inflater.inflate(R.layout.ex_group, null);
			
			holder = new ViewHolder();
			holder.text1 = (TextView) convertView.findViewById(R.id.groupText1);
			holder.text2 = (TextView) convertView.findViewById(R.id.groupText2);
			holder.image = (ImageView) convertView.findViewById(R.id.Image1);
			holder.loadingImage = (ProgressBar) convertView.findViewById(R.id.loadingImage);
			
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		
		holder.image.setImageResource(getSmileyResByGroupPosition(groupPosition));
		
		if (holder.image.getDrawable() != null)
		{
			holder.loadingImage.setVisibility(View.GONE);
			holder.image.setVisibility(View.VISIBLE);
			/*												//�o�@�q�A�O�ΨӲ����H���ƶq��image�b isDivisible 2 ���a��
			holder.exGroupLinear.removeAllViews();
			if (isDivisible(groupPosition, 2))
			{
				int ranNum = ran.nextInt(9)+2;
				for (int i = 0; i < ranNum; i++)
				{
					ImageView iv = new ImageView(context);
					imageLoader.DisplayImage(url_array[ran.nextInt(url_array.length)], iv);
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
							(LayoutParams.MATCH_PARENT, 50, Gravity.CENTER);
					holder.exGroupLinear.addView(iv, params);
				}
			}
			*/
		}
		String groupText = (String) listGroup.get(groupPosition).get("groupSample");
		String groupNumber = (String) listGroup.get(groupPosition).get("groupNumber");
		holder.text1.setText(parser.addSmileySpans(groupText));
		
		holder.text2.setText(parser.addSmileySpans(groupNumber));
		
		holder.text1.setTextColor(Color.BLACK);							//���B�����аѷӤU����convertView!
		holder.text2.setTextColor(Color.BLACK);
		for (int i = 0; i < ranCount; i++)								//run ranCount �����j��A���ثe��Position�P�Q��X�Ӫ�Position�O�_�@��
		{
			if (groupPosition+1 == ranPosList.get(i))					// ranPosList �����ȬO�q 1 �}�l�AgroupPosition�O�q 0 �}�l�A�ҥH�n+1
			{
				holder.text1.setTextColor(Integer.parseInt(ranColorList.get(i)));
				holder.text2.setTextColor(Integer.parseInt(ranColorList.get(ranCount-(i+1))));		//�ϦV�q ranColorList �����X�ȨӡI
			}
		}
		
		convertView.setBackgroundColor(Color.WHITE);				//�C�� View ��o�̳��n����Color�]�^White�A�A�h�P�_if
		if (isDivisible(groupPosition, 100))						//���M�ھ�ViewHolder Reuse view���S�ʡA
			convertView.setBackgroundColor(Color.GRAY);				//�w�]��Gray��view�N�Ⲿ�X�h�F�A�٬O�|���W�Q���^�ӮM�Φb���諸��m�W�I
		/*
		if (!iconName.isEmpty())
		{
			for (int i = 0; i < ranHtmlCountList.size(); i++)
			{
				if (groupPosition+1 == ranHtmlCountList.get(i))
					holder.text1.setText(parser.addSmileySpans(htmlText(groupText, ranHtmlColorList.get(i), ranHtmlIconList.get(i))));
			}
		}
		*/
		((MainListActivity) context).showMemory();
		return convertView;
	}
	
	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return listChild.get(groupPosition).size();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return listChild.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}
	
	@SuppressLint("InflateParams") @Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.ex_child, null);
		
		TextView sampleText = (TextView) layout.findViewById(R.id.childText1);
		
		@SuppressWarnings("unchecked")
		String childText = ((Map<String, String>)getChild(groupPosition, childPosition)).get("childSample");
		sampleText.setText(childText);
		
		return layout;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}
	
	static class ViewHolder		//�ۦ�w�q�@��ViewHolder�A�̭���n�bconvetView���Ψ쪺�F��
	{
		TextView text1;
		TextView text2;
		ImageView image;
		ProgressBar loadingImage;
	}
	
	private boolean isDivisible(int position, int target)		//�Ω�P�_���w�� position �O�_�� target ������~(��_>��)
	{
		int total = getGroupCount() / target;
		
		for (int i = 1; i <= total; i++)
		{
			if (position+1 == target * i)
				return true;
		}
		return false;
	}
	
	private void setRanColor()							//���� ranCount �Ӫ��H�� Position �P Color�A�åB�U��J������ArrayList��~
	{
		ran = new Random();
		
		ranPosList = new ArrayList<Integer>();
		ranColorList = new ArrayList<String>();
		
		StringBuilder ranColorSb = new StringBuilder();		//�M�~�����n�� StringBuild or StringBuffer ���I 
		
		for (int i = 0; i < ranCount; i++)
		{
			int ranPos = ran.nextInt(getGroupCount())+1;
			int ranColor = 0xff000000 | ran.nextInt(0x00ffffff);	// Random �X  Color �N�X�A�S�r���A�u���Ʀr�A�u��6��A����8��A
																	//���T�w�O�X�i��A�ӥB���X���G���O�H "-" �}�Y�A
			ranColorSb.delete(0, 9);								//���I�O�A���M�٥i�H������ setTextColor �ӮM��?! Tell me why~~~~(��_>��)
			ranColorSb.append(String.valueOf(ranColor));
			
			ranPosList.add(ranPos);
			ranColorList.add(ranColorSb.toString());
			
			//Log.i("RanColor", "" + ranColor);
		}
		Log.i("RanCount", "" + ranCount);
	}
	
	public Spanned htmlText(String text, String ranColor, int ranIcon)
	{
		ran = new Random();
		
		int textLen = text.length() / 2;							//�N���쪺�r��A���@�b�� (���ե�!)
		//int ranColor = 0xff000000 | ran.nextInt(0x00ffffff);		//�����H�� Color �N�X~
		
		String text1 = text.substring(0, textLen);
		String text2 = text.substring(textLen);
		
		htmlSb = new StringBuilder();
		
		if (imageName.size() != 0)							//ranIcon = Random for IconList(ImageList)�A�`�ȬO 0~List.size();
		{													//�]�N�O ranHtmlIconList
			htmlSb.insert(0,"<b>").append(text1).append("</b>")
			.append(imageName.get(ranIcon)[0]).append("<font color=").append(ranColor)
			.append("><i>").append(text2).append("</i></font>");
		}
		return Html.fromHtml(htmlSb.toString());				//�H�W���� html �L���I�u���o�檺 Html.fromHtml() �~�� html ������~
	}
	
	private ArrayList<Integer> getRanSmileyNum()
	{
		ran = new Random();
		ArrayList<Integer> ranSmileyList = new ArrayList<Integer>();
		
		for (int i = 0; i < getGroupCount(); i++)
			ranSmileyList.add(ran.nextInt(smileyName.size()));
		
		return ranSmileyList;
	}
	
	private Integer getSmileyResByGroupPosition(int position)
	{
		String resStr = smileyName.get(ranSmileyNumList.get(position))[1];
		return Integer.parseInt(resStr);
	}
	
	/*
	private ArrayList<Integer> getRanUrlNum()
	{
		ArrayList<Integer> ranUrlList = new ArrayList<Integer>();
		for (int i = 0; i < getGroupCount(); i++)
		{
			ranUrlList.add(ran.nextInt(url_array.length));
		}
		return ranUrlList;
	}
	*/
	private void setRanHtmlAtDivisible(int position)
	{
		int total = getGroupCount() / position;
		for (int i = 1; i <= total; i++)
		{
			ranHtmlCountList.add(position * i);
		}
		ran = new Random();
		int ranColor;
		int ranIconNum;
		
		for (int i = 0; i < ranHtmlCountList.size(); i++)
		{
			ranColor = 0xff000000 | ran.nextInt(0x00ffffff);
			ranHtmlColorList.add(String.valueOf(ranColor));
			if (!imageName.isEmpty())
			{
				ranIconNum = ran.nextInt(imageName.size());
				ranHtmlIconList.add(ranIconNum);
			}
		}
	}
}
