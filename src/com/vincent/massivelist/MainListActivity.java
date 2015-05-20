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
		
		numberInput.setOnEditorActionListener(new OnEditorActionListener() {	//��ťEditText�A�u���b���UEnter or �����������A�~�|Ĳ�o�ƥ�
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
		//private String[] urlList;
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
			/*
			GetStringByUrl urlString = new GetStringByUrl(getString(R.string.ImageUrl));	//�qTesting server�W���oImage URL���M��
			String[] urlTempString = urlString.getString().split(",");		//URL�M�椤���C���ɦW�A�O�H","���Ϲj�A�ҥH�b�o�̱N�L���ά�Array
			StringBuilder urlSb;
			ArrayList<String> urlTempList = new ArrayList<String>();
			for (String s: urlTempString)
			{
				urlSb = new StringBuilder(s);
				urlSb.insert(0, "http://60.199.201.66/");		//�W����o���ɦW�� images/(fileName)�A�]���b�e���A�[�WServer�����|~
				urlTempList.add(urlSb.toString());
			}
			urlTempString = new String[urlTempList.size()];		//�N ArrayList �ର String[]
			urlList = urlTempList.toArray(urlTempString);
			*/
			count = Integer.parseInt(params[0]);
			text = params[1];

			/*--------------�q�o��--------------------------�@����------------------------ */

			ArrayList<Integer> ranNumList = new ArrayList<Integer>();		//�Ψ��x�s ranNum
			ArrayList<Integer> ranMultiList = new ArrayList<Integer>();		//�Ψ��x�s ranMulti
			
			int ranCount = (int) (count * 0.1);								//�s�W�@��int�A�Ȭ��`���(count)��10����1
			Random ran = new Random();
			StringBuilder sb;

			for (int i = 0; i < ranCount; i++)			//����"ranCount"�Ӫ��ü�
			{
				int ranNum = ran.nextInt(count)+1;			//�üƽd��1~count
				int ranMulti = ran.nextInt(16)+5;			//����5~20���üơA�ΨӨM�wsb�n�[�X��
				Log.i("ranNumber",""+ranNum);
				Log.i("ranMultiple",""+ranMulti);

				ranNumList.add(ranNum);
				ranMultiList.add(ranMulti);
			}

			for (int i = 1; i <= count; i++)
			{
				publishProgress(Integer.valueOf(i));
				
				Map<String, String> listGroupItem = new HashMap<String, String>();

				listGroupItem.put("groupSample", text);		//���`put�i�T�w�����e
				listGroupItem.put("groupNumber", " "+i);

				for (int j = 0; j < ranCount; j++)						//�q�o�̶}�lrun "ranCount" �����j��
				{
					if (i == ranNumList.get(j))							//�p�G�Ӧ��� i ����ranNumList�䤤�@�ӼƦr����...
					{													//�ѩ� i �O�q 1 �}�l�hrun�A�ҥH�@�w�O�qranNumList���̤p���ȶ}�l���
						sb = new StringBuilder();			
						//Log.i("ranNumberList",""+ranNumList.get(j));	//��Ӧ����쪺��Log�X�ӡA�q�̤p��̤j...
																		//�ҥH�b�o�̤]���K�� ranNumList �����F�Ƨ�...
																		//�N�~�o�{ Bubble Sort ���~���t�@�ӱƧǪk���I
						for (int k = 0; k < ranMultiList.get(j); k++)
						{
							sb.append("This is the Chosen One! ").append(getSmileyName().get(ran.nextInt(getSmileyName().size()))[0]);
							//�ݸӦ���ranMultiList���ȬO�h�֡A�Nrun�X��
						}
						listGroupItem.put("groupSample", sb.toString());	//��Ӧ������eput�ihashMap�̡A�л\�쥻put����
					}
				}
				listGroup.add(listGroupItem);

				/*----------��o�̡A�ӵ��b�J����Group�ƶq�U�A���ͤ@�w��Ҫ��ü�(�D�X10����1��)�A
				 * �M��Q�襤�����X�ӡA�A��ranMultiList�����ȡA���P���w���ƪ������I-----------I'm fucking Brilliant!-----*/

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
			exAdapter = new ExAdapter(MainListActivity.this, listGroup, listChild);
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
						exList.collapseGroup(i);		//���F�i�}�����Ӧ�m�A��l��m�q�qcollapse!
				}
			}
		});
	}
	
	public void sendClick(View view)
	{
		input = textInput.getText().toString();
		
		if (!input.isEmpty())										//�P�_ textInput ���O�Ū��ܡA�N��ܨӦۨϥΪ̪�input
		{
			if (inputNum.isEmpty())
				new createAsyncList().execute(defualtNum, input);
			else
				new createAsyncList().execute(inputNum, input);
		} else
		{
			if (inputNum.isEmpty())									//�_�h�N��ܹw�]�� TestingText
				new createAsyncList().execute(defualtNum, getResources().getString(R.string.TestingText));
			else
				new createAsyncList().execute(inputNum, getResources().getString(R.string.TestingText));
		}
		textInput.setText("");			
	}
	
	OnKeyListener goKey = new OnKeyListener() {						//��ť�n����L�W���ʧ@�I
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {	//�p�G���U Enter or ���� ������...
				
    			InputMethodManager input = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    			
    			if (input.isActive()) {							//�|Ĳ�o sendClick() �o�� Function
    				sendClick(v);
    				//input.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    			}
    			return true;
    		}
			return false;
		}
    };
    
    private int getPixels(int dipValue)			//�ۦ�w�q�@�� Dip To Pixels ���\��I
    {
    	Resources res = getResources();
    	int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, res.getDisplayMetrics());
    	Log.i("Dip to Pixels~~", "" + dipValue + " to " + px);
    	return px;
    }
    
    @SuppressWarnings("deprecation")
	public void createImageBtn()			//�q image_cache ���A�ʺA�إ� ImageButton
    {
    	WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);	//��o WindowManager ���A��
    	int screenWidth = wm.getDefaultDisplay().getWidth();		//���o�ثe�ù����e��(Pixels)
    	
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getPixels(40), getPixels(40), Gravity.CENTER);
    												//�Y������J�Ʀr���ܡA���|�ODip�A�]���n�� getPixels() �N����ഫ��Pixels�A
    	iconsLayout.removeAllViews();				//�b�p�⪫��b�ù������Ŷ����Y�ɡA�~�|�ܷǽT��~~
    	Drawable iconDraw;
    	
    	String SDPath = Environment.getExternalStorageDirectory().getPath();
    	String cacheDir = getResources().getString(R.string.cache_dirname);
    	StringBuilder imagePathSb = new StringBuilder();
    	String imagePath = imagePathSb.append(SDPath).append("/").append(cacheDir).append("/").toString();
    	
    	LinearLayout iconLayout = new LinearLayout(this);		//���F new ImageButon ���~�A�]�n new LinearLayout ��I
		iconLayout.setLayoutParams(new LinearLayout.LayoutParams
				(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    	
    	int btnWidthSum = 0;						// ���w�q�@�ӪŪ� int�A�ݷ|�n�ΨӲ֥[ImageButton���e��
    	boolean isFirstCreate = true;
    	
    	for (String[] imgName: getImageName())		//�ھ� getImageName() ����o��size��run�j��
    	{
    		iconDraw = Drawable.createFromPath(imagePath + imgName[1]);
    		
    		ImageButton imgBtn = new ImageButton(this);
    		imgBtn.setImageDrawable(iconDraw);
    		imgBtn.setScaleType(ScaleType.CENTER_CROP);
    		imgBtn.setLayoutParams(params);
    		imgBtn.setTag(imgName[0]);			// setTag() �ڥ��u���n�Ϊ�!!!
    		
    		btnWidthSum += imgBtn.getLayoutParams().width;	//�ǥ� .getLayoutParams().width ��o imgBtn ���e�סA�M��[�� btnWidthSum ���I
    		Log.i("BtnWidth!", imgBtn.getLayoutParams().width + " of " + btnWidthSum);
    		
    		if (isFirstCreate)				//�ѩ�ä��O�C�鳣�n�[�J new Layout�A�ҥH�n���P�_����~
    		{
    			iconLayout.addView(imgBtn);
    			iconsLayout.addView(iconLayout);	//�p�G�O�Ĥ@�� (isFirstCreate)�A�N���L���󪺥[�J�@�� Layout
    			isFirstCreate = false;
    		}
    		else if (btnWidthSum <= screenWidth)	//�p�G imgBtn �Ҳ֥[���e�סA�٨S�j��ù��e�ת��ܡA�N�~��b add �b�쥻��Layout��
    		{
    			iconLayout.addView(imgBtn);
    		}
    		else if (btnWidthSum > screenWidth)		//�Ϥ��p�G imgBtn �Ҳ֥[���e�פw�j��ù��F�A�N�A new �@��Layout�X��~
    		{
    			iconLayout = new LinearLayout(this);
    			iconLayout.setLayoutParams(new LinearLayout.LayoutParams
    					(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    			iconLayout.addView(imgBtn);
    			iconsLayout.addView(iconLayout);
    			btnWidthSum = 0;					//�M��M��A�n�O�o�� btnWidthSum �k�s�H���s�p���I
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
	
	public void createIconsBtn()		//��Resources����Drawable�A�ӰʺA�إ�ImageButton
    {
    	smileyIconLayout.removeAllViews();
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getPixels(40), getPixels(40), Gravity.CENTER);
    										//�P�W���� createImageBtn ���t���h��~
    	int iconRes;
    	
    	for (String[] icons: getSmileyName())		//�ھ� getSmileyIcons() ��size��run~
    	{
    		iconRes = Integer.parseInt(icons[1]);
    		ImageButton iconBtn = new ImageButton(this);
    		iconBtn.setImageResource(iconRes);
    		iconBtn.setScaleType(ScaleType.CENTER_CROP);
    		iconBtn.setLayoutParams(params);
    		iconBtn.setTag(icons[0]);
    		iconBtn.setOnClickListener(btnClick);
    		smileyIconLayout.addView(iconBtn);
    	}
    }
	private String withSymbol(String text)		//�@�ӱN String ���e�᳣�[�W���w�Ÿ����p�\��~
	{
		sb = new StringBuilder();
		sb.append("#").append(text).append("#");
		return sb.toString();
	}
	
    public void setSmileyText(String smileyText)
    {
    	SmileysParser.init(this);							//�C�� setSmileyText ���ɭԡA���� SmileysParser ���s init �@���A
    	SmileysParser parser = SmileysParser.getInstance();	//�H�Ψӧ�s�s��images�ɦW�� HashMap
    	
    	String oriText = textInput.getText().toString();
    	int index = Math.max(textInput.getSelectionStart(), 0);
    	Log.i("Text Index", "" + index);
    	
    	sb = new StringBuilder(oriText);
    	sb.insert(index, smileyText);
    	
    	textInput.setText(parser.addSmileySpans(sb.toString()));
    	textInput.setSelection(index + smileyText.length());
    }
    
    public HashMap<String, Integer> getSmileyMap()		//�n�ᵹ SmileysParser �Y�A�ҥH�n���� HashMap
    {
    	HashMap<String, Integer> iconNameItem = new HashMap<String, Integer>(getSmileyName().size());
    	
    	for (String[] icons: getSmileyName())
    	{
    		iconNameItem.put(icons[0], Integer.parseInt(icons[1]));
    	}
    	return iconNameItem;
    }
    
    public List<String[]> getSmileyName()		//�N Resources ���� Drawable ���X�ӡA�ëإߦb List<String[]> ��
    {
    	List<String[]> smileyIconList = new ArrayList<String[]>();
    	String resStr;
    	
    	R.drawable drawable = new R.drawable();
    	Field[] drawRes = R.drawable.class.getFields();
    	
    	for (Field f: drawRes)
    	{
    		try
    		{
    			if (f.getName().contains("smiley"))		//�ǥѧP�_�W�١A�ӿz��X�ڭ̭n�� Drawable
    			{
    				resStr = String.valueOf(f.getInt(drawable));
    				smileyIconList.add(createStringArr(withSymbol(f.getName()), resStr));
    			}						//�N Drawable ����T��� smileyIconList ���A�榡���G/(DrawableName)/[0]�A(DrawableID)[1]
    		}
    		catch (IllegalArgumentException e) {
    			e.printStackTrace();
    			Log.e("getSmileyFailed!", e.getMessage().toString());
    		}
    		catch (IllegalAccessException e) {
    			e.printStackTrace();
    			Log.e("getSmileyFailed!", e.getMessage().toString());
    		}
    	}
    	return smileyIconList;
    }
    
    public HashMap<String, String> getImageMap()		//�ѩ� SmileyParser �O�Y HashMap �Ӥ��R��ơA�ҥH�o�̤]�� imageNames ���� HashMap�I
    {
    	HashMap<String, String> imgNameItem = new HashMap<String, String>(getImageName().size());
    	String imgName;
    	String imgFullName;
    	for (String[] img: getImageName())			//�� for each �I�s getImageName()�Arun����N���@��HashMap��~~
    	{
    		imgName = img[0];
    		imgFullName = img[1];
    		imgNameItem.put(imgName, imgFullName);
    	}
    	return imgNameItem;
    }
    
    public List<String[]> getImageName()			//�N�w�x�s��Image�ɮצW�٩�i�@�� List<String[]> ��
    {
    	File cacheDir;
    	try
    	{			/*---------�ϥ� File ���e�A���n�����H�U���P�_��!!! �Y�SSD�A�N�Τ�����cache��Ƨ��F--------*/
    		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
    			cacheDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.cache_dirname));
    		else
    			cacheDir = getCacheDir();
    		if (!cacheDir.exists())
    			cacheDir.mkdirs();
    				/*-------�Y�ۭq�� cache ��Ƨ����s�b�A�N�s�W�X�ӡI----*/
    		
    		File[] imgCount = cacheDir.listFiles();		// List �X cacheDir �̪��Ҧ��ɮ�

    		List<String[]> imageNameList = new ArrayList<String[]>();

    		String imgFullName;
    		StringBuilder imgNameSb;
    		String imgName;

    		for (File img: imgCount)
    		{
    			imgFullName = img.getName();	//�����ɮצW�١A�]�t���ɦW
    			imgNameSb = new StringBuilder(imgFullName).insert(imgFullName.lastIndexOf("%2F")+3, "/")
    					.insert(imgFullName.lastIndexOf(".")+1, "/");	//�ק�Image���ɦW~
    			imgName = imgNameSb.substring(imgNameSb.lastIndexOf("%2F")+3, imgNameSb.lastIndexOf("."))
    					.replace("%2B", "+");	//�s�� image name�A�t /../ ���t���ɦW

    			Log.i("imgName", imgName);
    			Log.i("imgFullName", imgFullName);

    			imageNameList.add(createStringArr(imgName, imgFullName)); //�N �w�ק�L���ɦW[0] & �����ɦW[1] add �i imageNameList ���I
    		}
    		return imageNameList;
    	}
    	catch (Exception e) {
    			e.printStackTrace();
    			Log.e("FileNotFound!", e.toString());
    	}
    	return null;
    }
    private String[] createStringArr(String imgName, String imgFullName)		//���� String[] ���@�Ӥp�F�F~
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
    	//createImageBtn();
    	createIconsBtn();
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
			//createIconsBtn();
			break;
		}
		return true;
	}
}
