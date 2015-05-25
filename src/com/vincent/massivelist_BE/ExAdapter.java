package com.vincent.massivelist_BE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.vincent.massivelist_BE.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
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
	private Drawable waitIcon;
	private HashMap<String, Bitmap> imgMap;
	private StringBuilder urlSb;
	
	private String[] url_array;
	private ArrayList<Integer> ranUrlNumList;
	ImageLoader imageLoader;
	FileCache fileCache;
	//GetWebImg webImg;
	
	private List<String[]> iconName;
	private ArrayList<Integer> ranHtmlCountList;
	private ArrayList<String> ranHtmlColorList;
	private ArrayList<Integer> ranHtmlIconList;
	
	public ExAdapter(Context context, List<Map<String, String>> listGroup,List<List<Map<String, String>>> listChild, String[] urlList)
	{
		this.context = context;
		this.listGroup = listGroup;
		this.listChild = listChild;
		
		waitIcon = context.getResources().getDrawable(R.drawable.wait01);
		waitIcon.setBounds(0, 0, 50, 50);
		
		inflater = LayoutInflater.from(context);
		imageLoader = new ImageLoader(context.getApplicationContext());
		fileCache = new FileCache(context);
		//webImg = new GetWebImg(context);
		imgMap = new HashMap<String, Bitmap>();
		
		ranCount = (int) (getGroupCount() * 0.5);
		setRanColor();
		
		SmileysParser.init(context);
		parser = SmileysParser.getInstance();
		
		this.url_array = urlList;
		ranUrlNumList = new ArrayList<Integer>();
		getRanArrNum();
		
		iconName = ((MainListActivity) context).getImageName();  //��o�w�s�bcache����image�ɦW
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
		
		String groupText = (String) listGroup.get(groupPosition).get("groupSample");
		String groupNumber = (String) listGroup.get(groupPosition).get("groupNumber");
		
		try
		{
			//imageLoader.DisplayImage(url_array[ranUrlNumList.get(groupPosition)], holder.image);

			/*	//(The "GetWebImg" way, from PTT)
				if (webImg.IsCache(url_array[ranUrlNumList.get(groupPosition)]) == false)
					webImg.LoadUrlPic(url_array[ranUrlNumList.get(groupPosition)], handler);
				else if (webImg.IsDownLoadFine(url_array[ranUrlNumList.get(groupPosition)]) == true)
				{
					holder.image.setImageBitmap(webImg.getImg(url_array[ranUrlNumList.get(groupPosition)]));
					holder.loadingImage.setVisibility(View.GONE);
					holder.image.setVisibility(View.VISIBLE);
				} else {}
			 */
			//holder.image.setImageBitmap(Icon);
		}
		catch (OutOfMemoryError e) {
			e.printStackTrace();
			//Icon.recycle();
			Log.e("OOM Oops!", e.getMessage().toString());
			Log.e("Memory","Out!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Oops!", e.getMessage().toString());
		} finally {
			notifyDataSetChanged();
		}
		
		if (groupText.contains("http://") || groupText.contains("https://"))
		{
			List<String> imgUrlList = getImgUrlString(groupText);
			
			for (String imgUrl: imgUrlList)
			{
				if (!imgUrl.equals("Unkonw Image URL!"))
				{
					//File imgFile = fileCache.getFile(imgUrl);
					Log.d("GetImageURL!!", imgUrl);
					
					if (imgMap.containsKey(imgUrl)) {
						try {
							holder.text1.setText(parser.addIconSpans(groupText, imgMap));
							Log.i("ExistsFileViewed", imgUrl.substring(imgUrl.lastIndexOf("/")));
						} catch (Exception e) {
							//Log.e("ImageFileFielded", e.getMessage().toString());
							holder.text1.setText(parser.addWaitSpans(groupText, imgUrl.substring(imgUrl.lastIndexOf("."))));
							((MainListActivity) context).shortMessage("Slow Down Please!");
						}
					}
					else {
						try {
							holder.text1.setText(groupText);
							//holder.text1.setText(parser.addWaitSpans(groupText, imgUrl.substring(imgUrl.lastIndexOf("."))));
							downloadBitmapByUrl(imgUrl);
							Log.i("ImageFile", "OH YEAH~~~~~~~~~~");
						} catch (Exception e) {
							e.printStackTrace();
							Log.e("ImageFile", "NO!!!!! What happed~~~");
						}
					}
				} else
					((MainListActivity) context).shortMessage("Unknow URL!!");
			}
		} else
			holder.text1.setText(parser.addIconSpans(groupText, null));
		
		holder.text2.setText(groupNumber);

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
	/*
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler()		//�i�DBaseAdapter��Ƥw�g��s�F (�� GetWebImg �Ϊ� Handler)
	{
		@Override
		public void handleMessage(Message msg)
		{
			Log.d("Handler", "notifyDataSetChanged");
			notifyDataSetChanged();
			super.handleMessage(msg);
		}
	};
	 */
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
		
		if (iconName.size() != 0)							//ranIcon = Random for IconList(ImageList)�A�`�ȬO 0~List.size();
		{													//�]�N�O ranHtmlIconList
			htmlSb.insert(0,"<b>").append(text1).append("</b>")
			.append(iconName.get(ranIcon)[0]).append("<font color=").append(ranColor)
			.append("><i>").append(text2).append("</i></font>");
		}
		return Html.fromHtml(htmlSb.toString());				//�H�W���� html �L���I�u���o�檺 Html.fromHtml() �~�� html ������~
	}
	
	private void getRanArrNum()
	{
		for (int i = 0; i < getGroupCount(); i++)
		{
			ranUrlNumList.add(ran.nextInt(url_array.length));
		}
	}
	
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
			if (!iconName.isEmpty())
			{
				ranIconNum = ran.nextInt(iconName.size());
				ranHtmlIconList.add(ranIconNum);
			}
		}
	}
	
	private List<String> getImgUrlString(String text)	//�NgroupText��L�ӡA�ǥ�����r�M�U�ذj��ӧ�䤤��URLs�إߨ�List��
	{
		String[] urlArr = text.split("http");
		List<String> urlList = new ArrayList<String>();
		
		for (int i = 1; i < urlArr.length; i++)
		{
			if (text.contains(".png"))
			{
				String url = urlArr[i].substring(0, urlArr[i].lastIndexOf(".png")+4);
				urlSb = new StringBuilder(url);
				urlSb.insert(0, "http");
				urlList.add(urlSb.toString());
			}
			else if (text.contains(".jpg"))
			{
				String url = urlArr[i].substring(0, urlArr[i].lastIndexOf(".jpg")+4);
				urlSb = new StringBuilder(url);
				urlSb.insert(0, "http");
				urlList.add(urlSb.toString());
			}
			else if (text.contains(".gif"))
			{
				String url = urlArr[i].substring(0, urlArr[i].lastIndexOf(".gif")+4);
				urlSb = new StringBuilder(url);
				urlSb.insert(0, "http");
				urlList.add(urlSb.toString());
			}
			else if (text.contains(".bmp"))
			{
				String url = urlArr[i].substring(0, urlArr[i].lastIndexOf(".bmp")+4);
				urlSb = new StringBuilder(url);
				urlSb.insert(0, "http");
				urlList.add(urlSb.toString());
			}
			else
				urlList.add("Unknow Image URL!");
		}
		return urlList;
	}
	
	public void downloadBitmapByUrl(final String urlString)
	{
		try
		{
			((MainListActivity) context).LoadingShow();
			new Thread(new Runnable() {
				@Override
				public void run() {
					Log.d("BitmapDownload", "Downloading~~~~");
					imageLoader.getBitmap(urlString, false);	//���� ImageLoader �� getBitmap ����k�A�]��false��ܤ��ΥL��decodeFile()
					handler.obtainMessage(0, urlString).sendToTarget();	//�U������N URL �e�h�� handler ��~ 
				}
			}).start();
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.e("urlMAP~~~~~", "Didn't get the Position!");
		}
	}
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case 0:
				String urlString = null;
				if (msg.obj instanceof String)
					urlString = (String) msg.obj;
				
				Log.d("DownloadedURL!", urlString.substring(urlString.lastIndexOf("/")));
				
				String imgPathName = ((MainListActivity) context).getImagePathByName(urlString);
				try {
					Bitmap imgBitmap = MainListActivity.getDecodedBitmap(imgPathName, 80, 80);
					imgMap.put(urlString, imgBitmap);		//�N�U���n��Decode���᪺Bitmap��J�s���� ImageMap ���I(key�Y��URL~)
				} catch(Exception e) {
					Log.e("ImageBitmap", "OH!!!!!NO~~~~~~~~~");			//�p�G�Ƥӧ֡A�W���� getDecodedBitmap() �����u�@�٨Ӥ��Χ���...
					((MainListActivity)context).shortMessage("OH!!!!! NO~~~~~");	//�M��A�S View �쨺�@�q����...���N OH!!! NO~~ �F��
				}
				((MainListActivity) context).LoadingHide();
				SmileysParser.init(context);
				parser = SmileysParser.getInstance();
				notifyDataSetChanged();
				
				break;
			}
		}
	};
	
	public HashMap<String, Bitmap> getNewImageMap()	//�o�̬O�� setIconText() �Ϊ�
	{
		return imgMap;
	}
}
