package com.vincent.massivelist;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainListActivity extends Activity
{
	private ExpandableListView exList;
	private ExAdapter exAdapter;
	
	private EditText textInput;
	
	private EditText numberInput;
	
	private List<Map<String, String>> listGroup;
	private List<List<Map<String, String>>> listChild;
	
	private String defualtNum = "500";
	private String inputNum = "";
	private String input = "";
	private StringBuilder sb;
	
	ImageLoader imageLoader;
	
	private LinearLayout iconsLayout;
	private LinearLayout smileyIconLayout;
	private ImageButton showIconBtn;
	private boolean iconShown;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_list_layout);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.setTitle(getResources().getString(R.string.app_name) + "_v" + getResources().getString(R.string.Version));
		
		exList = (ExpandableListView) findViewById(R.id.sampleExList);
		
		final String testingText = getResources().getString(R.string.TestingText);
		new createAsyncList().execute(defualtNum, testingText);
		
		textInput = (EditText) findViewById(R.id.textInput);
		textInput.setFocusable(true);
		textInput.setFocusableInTouchMode(true);
		textInput.setOnKeyListener(goKey);
		
		numberInput = (EditText) findViewById(R.id.numberInput);
		numberInput.setFocusable(true);
		numberInput.setFocusableInTouchMode(true);
		
		numberInput.setOnEditorActionListener(new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				String number = numberInput.getText().toString();
				if (input.isEmpty())
				{
					if (numberInput.getText().toString().isEmpty())
						new createAsyncList().execute(defualtNum, testingText);
					else {
						inputNum = String.valueOf(number.toString());
						new createAsyncList().execute(inputNum, testingText);
					}
				} else
				{
					if (numberInput.getText().toString().isEmpty())
						new createAsyncList().execute(defualtNum, input);
					else {
						inputNum = String.valueOf(number.toString());
						new createAsyncList().execute(inputNum, input);
					}
				}
				return false;
			}
		});
		collapseOther();
		showMemory();
		
		imageLoader = new ImageLoader(getApplicationContext());
		
		iconsLayout = (LinearLayout) findViewById(R.id.iconsLayout);
		smileyIconLayout = (LinearLayout) findViewById(R.id.smileysIconLayout);
		showIconBtn = (ImageButton) findViewById(R.id.showIconBtn);
	}
	
	class createAsyncList extends AsyncTask<String, Integer, Void>
	{
		private int count;
		private String text;
		
		private Dialog dialog;
		private TextView loadingText;
		private String[] urlList;
    	//InputMethodManager input;
		
		@SuppressLint("InflateParams") @Override
		protected void onPreExecute()
		{
			//input = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			listGroup = new ArrayList<Map<String, String>>();
			listChild = new ArrayList<List<Map<String, String>>>();
			
			LayoutInflater inflater = getLayoutInflater();
			View view = inflater.inflate(R.layout.dialog_layout, null);
			dialog = new Dialog(MainListActivity.this);
			loadingText = (TextView) view.findViewById(R.id.loadingText);
			dialog.setContentView(view);
    		dialog.setTitle("Generating List...");
    		dialog.setCanceledOnTouchOutside(false);
    		
    		Window dialogWindow = dialog.getWindow();
    		WindowManager.LayoutParams windowParams = dialogWindow.getAttributes();
    		windowParams.alpha = 0.9f;
    		
    		dialog.show();
		}
		
		@Override
		protected Void doInBackground(String... params)
		{
			GetStringByUrl urlString = new GetStringByUrl(getString(R.string.ImageUrl));	//從Testing server上取得Image URL的清單
			String[] urlTempString = urlString.getString().split(",");		//URL清單中的每個檔名，是以","做區隔，所以在這裡將他分割為Array
			StringBuilder urlSb;
			ArrayList<String> urlTempList = new ArrayList<String>();
			for (String s: urlTempString)
			{
				urlSb = new StringBuilder(s);
				urlSb.insert(0, "http://60.199.201.66/");		//上面獲得的檔名為 images/(fileName)，因此在前面再加上Server的路徑~
				urlTempList.add(urlSb.toString());
			}
			urlTempString = new String[urlTempList.size()];		//將 ArrayList 轉為 String[]
			urlList = urlTempList.toArray(urlTempString);
			
			count = Integer.parseInt(params[0]);
			text = params[1];

			/*--------------從這裡--------------------------一直到------------------------ */

			ArrayList<Integer> ranNumList = new ArrayList<Integer>();		//用來儲存 ranNum
			ArrayList<Integer> ranMultiList = new ArrayList<Integer>();		//用來儲存 ranMulti
			
			int ranCount = (int) (count * 0.1);								//新增一個int，值為總行數(count)的10分之1
			Random ran = new Random();
			StringBuilder sb;

			for (int i = 0; i < ranCount; i++)			//產生"ranCount"個的亂數
			{
				int ranNum = ran.nextInt(count)+1;			//亂數範圍為1~count
				int ranMulti = ran.nextInt(16)+5;			//產生5~20的亂數，用來決定sb要加幾倍
				Log.i("ranNumber",""+ranNum);
				Log.i("ranMultiple",""+ranMulti);

				ranNumList.add(ranNum);
				ranMultiList.add(ranMulti);
			}

			for (int i = 1; i <= count; i++)
			{
				publishProgress(Integer.valueOf(i));
				
				Map<String, String> listGroupItem = new HashMap<String, String>();

				listGroupItem.put("groupSample", text);		//正常put進固定的內容
				listGroupItem.put("groupNumber", " "+i);

				for (int j = 0; j < ranCount; j++)						//從這裡開始run "ranCount" 次的迴圈
				{
					if (i == ranNumList.get(j))							//如果該次的 i 等於ranNumList其中一個數字的話...
					{													//由於 i 是從 1 開始去run，所以一定是從ranNumList中最小的值開始抓到
						sb = new StringBuilder();			
						Log.i("ranNumberList",""+ranNumList.get(j));	//把該次比對到的值Log出來，從最小到最大...
																		//所以在這裡也順便給 ranNumList 給做了排序...
																		//意外發現 Bubble Sort 之外的另一個排序法阿！
						for (int k = 0; k < ranMultiList.get(j); k++)
						{
							sb.append("This is the Chosen One! ");		//看該次的ranMultiList的值是多少，就run幾次
						}
						listGroupItem.put("groupSample", sb.toString());	//把該次的內容put進hashMap裡，覆蓋原本put的值
					}
				}
				listGroup.add(listGroupItem);

				/*----------到這裡，來給在既有的Group數量下，產生一定比例的亂數(挑出10分之1個)，
				 * 然後被選中的那幾個，再依ranMultiList中的值，給與指定倍數的成長！-----------I'm fucking Brilliant!-----*/

				List<Map<String, String>> listChildItems = new ArrayList<Map<String, String>>();
				Map<String, String> listChildItem = new HashMap<String, String>();

				listChildItem.put("childSample", ""+i);
				listChildItems.add(listChildItem);
				listChild.add(listChildItems);
			}
			ThreadLogUtils.logThread();
			return null;
		}
		protected void onPostExecute(Void result)
		{
			exAdapter = new ExAdapter(MainListActivity.this, listGroup, listChild, urlList);
			//exList.setIndicatorBounds(0,100);
			exList.setAdapter(exAdapter);
			dialog.dismiss();
	    	//input.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		}
		protected void onProgressUpdate(Integer...status)
		{
			double percent = ((double)status[0] / (double)count) * 10000;
			percent = Math.floor(percent + 0.5) / 100;
			loadingText.setText(String.valueOf(status[0]) + "/" + String.valueOf(count) + "\n" + "\n" + percent + "%");
		}
	}
	
	public void showMemory()
	{
		TextView memTip = (TextView) findViewById(R.id.memTip);
		
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		//int freeMemory = (int) (Runtime.getRuntime().freeMemory() / 1024);
		long availableMem = mi.availMem;
		//String freeMem = String.valueOf(freeMemory) + "MB"; 
		String avaMem = String.valueOf(Formatter.formatFileSize(this, availableMem));
		
		memTip.setText("Memory: " + avaMem);
	}
	
	public void collapseOther()
	{
		exList.setOnGroupExpandListener(new OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int groupPosition) {
				// TODO Auto-generated method stub
				int totalCount = exList.getExpandableListAdapter().getGroupCount();
				for (int i = 0; i < totalCount; i++)
				{
					if (i != groupPosition)
						exList.collapseGroup(i);		//除了展開的那個位置，其餘位置通通collapse!
				}
			}
		});
	}
	
	public void sendClick(View view)
	{
		input = textInput.getText().toString();
		
		if (!input.isEmpty())										//判斷 textInput 不是空的話，就顯示來自使用者的input
		{
			if (inputNum.isEmpty())
				new createAsyncList().execute(defualtNum, input);
			else
				new createAsyncList().execute(inputNum, input);
		} else
		{
			if (inputNum.isEmpty())									//否則就顯示預設的 TestingText
				new createAsyncList().execute(defualtNum, getResources().getString(R.string.TestingText));
			else
				new createAsyncList().execute(inputNum, getResources().getString(R.string.TestingText));
		}
		textInput.setText("");			
	}
	
	OnKeyListener goKey = new OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
				
    			InputMethodManager input = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    			
    			if (input.isActive()) {
    				sendClick(v);
    				//input.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    			}
    			return true;
    		}
			return false;
		}
    };
    
    private int getPixels(int dipValue)
    {
    	Resources res = getResources();
    	int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, res.getDisplayMetrics());
    	Log.i("Dip to Pixels~~", "" + dipValue + " to " + px);
    	return px;
    }
    
    @SuppressWarnings("deprecation")
	public void createIconBtn()
    {
    	WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    	int screenWidth = wm.getDefaultDisplay().getWidth();
    	
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getPixels(40), getPixels(40), Gravity.CENTER);
    	
    	iconsLayout.removeAllViews();
    	Drawable iconDraw;
    	
    	String SDPath = Environment.getExternalStorageDirectory().getPath();
    	String cacheDir = getResources().getString(R.string.cache_dirname);
    	StringBuilder imagePathSb = new StringBuilder();
    	String imagePath = imagePathSb.append(SDPath).append("/").append(cacheDir).append("/").toString();
    	
    	LinearLayout iconLayout = new LinearLayout(this);
		iconLayout.setLayoutParams(new LinearLayout.LayoutParams
				(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    	
    	int btnWidthSum = 0;
    	boolean isFirstCreate = true;
    	
    	for (String[] imgName: getImageName())
    	{
    		iconDraw = Drawable.createFromPath(imagePath + imgName[1]);
    		
    		ImageButton imgBtn = new ImageButton(this);
    		imgBtn.setImageDrawable(iconDraw);
    		imgBtn.setScaleType(ScaleType.CENTER_CROP);
    		imgBtn.setLayoutParams(params);
    		imgBtn.setTag(imgName[0]);
    		
    		btnWidthSum += imgBtn.getLayoutParams().width;
    		Log.i("BtnWidth!", imgBtn.getLayoutParams().width + " of " + btnWidthSum);
    		
    		if (isFirstCreate)
    		{
    			iconLayout.addView(imgBtn);
    			iconsLayout.addView(iconLayout);
    			isFirstCreate = false;
    		}
    		else if (btnWidthSum <= screenWidth)
    		{
    			iconLayout.addView(imgBtn);
    		}
    		else if (btnWidthSum > screenWidth)
    		{
    			iconLayout = new LinearLayout(this);
    			iconLayout.setLayoutParams(new LinearLayout.LayoutParams
    					(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    			iconLayout.addView(imgBtn);
    			iconsLayout.addView(iconLayout);
    			btnWidthSum = 0;
    			//shortMessage("OOPS~~~~~~~~");
    		}
    		imgBtn.setOnClickListener(btnClick);
    	}
    }
    OnClickListener btnClick = new OnClickListener()
    {
    	public void onClick(View v) {
    		setSmileyText(v.getTag().toString());
    	}
    };
	
    @SuppressWarnings("deprecation")
	public void createIcons()
    {
    	WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    	int screenWidth = wm.getDefaultDisplay().getWidth();
    	
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getPixels(40), getPixels(40), Gravity.CENTER);
    	LinearLayout smileyLayout = (LinearLayout) findViewById(R.id.smileysIconLayout);
    	
    	R.drawable drawableRes = new R.drawable();
    	
    	Field[] drawables = R.drawable.class.getFields();
    	
    	for (Field f: drawables)
    	{
    		try
    		{
    			ImageButton imgBtn = new ImageButton(this);
    			imgBtn.setImageResource(f.getInt(drawableRes));
    			imgBtn.setScaleType(ScaleType.CENTER_CROP);
    			imgBtn.setLayoutParams(params);
    			imgBtn.setTag(f.getName());
    			Log.i("ImageBtn FieldName", f.getName());
    			smileyLayout.addView(imgBtn);
    			smileyLayout.setVisibility(View.VISIBLE);
    		}
    		catch (Exception e) {
    			e.printStackTrace();
    			Log.e("Drawable Not Found!!", e.getMessage().toString());
    		}
    	}
    }
    
    public void setSmileyText(String smileyText)
    {
    	SmileysParser.init(this);							//每次 setSmileyText 的時候，都讓 SmileysParser 重新 init 一次，
    	SmileysParser parser = SmileysParser.getInstance();	//以用來更新存放images檔名的 HashMap
    	
    	String oriText = textInput.getText().toString();
    	int index = Math.max(textInput.getSelectionStart(), 0);
    	Log.i("Text Index", "" + index);
    	
    	sb = new StringBuilder(oriText);
    	sb.insert(index, smileyText);
    	
    	textInput.setText(parser.addSmileySpans(sb.toString()));
    	textInput.setSelection(index + smileyText.length());
    }
    
    public HashMap<String, String> getImageMap()		//由於 SmileyParser 是吃 HashMap 來分析資料，所以這裡也把 imageNames 做成HashMap！
    {
    	HashMap<String, String> imgNameItem = new HashMap<String, String>(getImageName().size());
    	String imgName;
    	String imgFullName;
    	for (String[] img: getImageName())			//用 for each 呼叫 getImageName()，run完後就有一個HashMap啦~~
    	{
    		imgName = img[0];
    		imgFullName = img[1];
    		imgNameItem.put(imgName, imgFullName);
    	}
    	return imgNameItem;
    }
    
    public List<String[]> getImageName()			//將已儲存的Image檔案名稱放進一個 List<String[]> 裡
    {
    	File cacheDir;
    	try
    	{			/*---------使用 File 之前，都要先做以下的判斷阿!!! 若沒SD，就用內部的cache資料夾；--------*/
    		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
    			cacheDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.cache_dirname));
    		else
    			cacheDir = getCacheDir();
    		if (!cacheDir.exists())
    			cacheDir.mkdirs();
    				/*-------若自訂的 cache 資料夾不存在，就新增出來！----*/
    		
    		File[] imgCount = cacheDir.listFiles();		// List 出 cacheDir 裡的所有檔案

    		List<String[]> imageNameList = new ArrayList<String[]>();

    		String imgFullName;
    		StringBuilder imgNameSb;
    		String imgName;

    		for (File img: imgCount)
    		{
    			imgFullName = img.getName();	//完整檔案名稱，包含副檔名
    			imgNameSb = new StringBuilder(imgFullName).insert(imgFullName.lastIndexOf("%2F")+3, "/")
    					.insert(imgFullName.lastIndexOf(".")+1, "/");	//修改Image的檔名~
    			imgName = imgNameSb.substring(imgNameSb.lastIndexOf("%2F")+3, imgNameSb.lastIndexOf("."))
    					.replace("%2B", "+");	//新的 image name，含 /../ 不含副檔名

    			Log.i("imgName", imgName);
    			Log.i("imgFullName", imgFullName);

    			imageNameList.add(createListName(imgName, imgFullName)); //將 已修改過的檔名[0] & 完整檔名[1] add 進 imageNameList 中！
    		}
    		return imageNameList;
    	}
    	catch (Exception e) {
    			e.printStackTrace();
    			Log.e("FileNotFound!", e.toString());
    	}
    	return null;
    }
    private String[] createListName(String imgName, String imgFullName)		//產生 String[] 的一個小東東~
    {
    	String[] listName = {imgName, imgFullName};
    	return listName;
    }
    
    public void showIconClick(View view)
    {
    	if (!iconShown)
    	{
    		showIcons();
    		iconShown = true;
    	} else
    	{
    		hideIcons();
    		iconShown = false;
    	}
    }
    private void showIcons()
    {
    	iconsLayout.setVisibility(View.VISIBLE);
    	smileyIconLayout.setVisibility(View.VISIBLE);
    	showIconBtn.setImageResource(android.R.drawable.ic_menu_more);
    	createIconBtn();
    }
    private void hideIcons()
    {
    	iconsLayout.setVisibility(View.GONE);
    	smileyIconLayout.setVisibility(View.GONE);
    	showIconBtn.setImageResource(android.R.drawable.ic_menu_add);
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK || event.getAction() == KeyEvent.KEYCODE_BACK)
		{
			if (iconShown)
			{
				hideIcons();
				iconShown = false;
			} else
				android.os.Process.killProcess(android.os.Process.myPid());
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
    
	public void shortMessage(String msg)
	{
		Toast.makeText(MainListActivity.this, msg, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_options, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_clear:
			imageLoader.clearCache();
			SmileysParser.init(this);
			
			if (iconShown) {
				hideIcons();
				iconShown = false;
			}
			shortMessage("Image Cache Cleared");
			break;
		
		case R.id.menu_test:
			createIcons();
			break;
		}
		return true;
	}
}
