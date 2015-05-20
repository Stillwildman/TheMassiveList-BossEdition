package com.vincent.massivelist_BE;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
//import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

public class GetWebImg
{
	//private final String TAG_NAME = "m";
	public final int DOWNLOAD_ERROR = 0, DOWNLOAD_FINISH = 1;
	
	private HashMap<String, Bitmap> picMap = new HashMap<String, Bitmap>();		//宣告一個HashMap用來存網址及圖片用的
	private Context context;
	
	public GetWebImg(Context context)
	{
	    this.context = context;
	}
	public Bitmap getImg(String url)
	{
		return picMap.get(url);
	}

	public boolean IsCache(String url)		//判斷是否有暫存
	{
		return picMap.containsKey(url);
	}

	public boolean IsDownLoadFine(String url)	//判斷圖片是否下載成功
	{
		return (picMap.get(url)!= null)?true:false;
	}

	public boolean IsLoading(String url)		//判斷圖片是否下載中
	{
		return (IsCache(url) == true && IsDownLoadFine(url) == false) ?true:false;
	}

	public void LoadUrlPic(final String url, final Handler handler)
	{
		picMap.put(url, null);	//放到暫存的空間

		new Thread(new Runnable() {
			@Override
			public void run() {
				Bitmap temp = LoadUrlPic(context, url);
			    //Bitmap temp = LoadUrlPic(u);//下載圖片的自訂函數
				
				if (temp == null)	//如果下載失敗
				{
					picMap.remove(url);		//移出暫存空間
					handler.sendMessage(handler.obtainMessage(DOWNLOAD_ERROR, null));
				} else
				{
					picMap.put(url, temp);		//存起來
					handler.sendMessage(handler.obtainMessage(DOWNLOAD_FINISH, temp));
				}
			}
		}).start();
	}

//	private Bitmap LoadUrlPic(final String u){
//		try {
//			Log.d(TAG_NAME, "DownLoadUrlPic "+u);
//			InputStream is = new URL(u).openStream();
//			int i;
//			byte[] data = new byte[1024];
//			ByteArrayBuffer buf = new ByteArrayBuffer(1024);
//			while ((i = is.read(data)) != -1)
//				buf.append(data, 0, i);
//			is.close();
//			return BitmapFactory.decodeByteArray(buf.toByteArray(), 0, buf.length());
//		} catch (Exception e) {
//			Log.d(TAG_NAME, e.toString());
//		}
//		return null;
//	}
	//抓網路的圖
    public synchronized Bitmap LoadUrlPic(Context c, String url)
    {
        URL imgUrl;
        Bitmap defaultImg = BitmapFactory.decodeResource(context.getResources(), com.vincent.massivelist_BE.R.drawable.coffee_icon);
        Bitmap webImg = null;
        
        try {
            imgUrl = new URL(url);
        }
        catch (MalformedURLException e) {
            Log.d("MalformedURLException", e.toString());
            return defaultImg;		//抓不到網路圖時, 讀預設圖片
        }
        
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) imgUrl.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(false);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.connect();
            
            InputStream inputStream = httpURLConnection.getInputStream();
            
            int length = (int) httpURLConnection.getContentLength();
            int tmpLength = 512;
            int readLen = 0, desPos = 0;
            
            byte[] img = new byte[length];
            byte[] tmp = new byte[tmpLength];
            
            if (length != -1)
            {
                while ((readLen = inputStream.read(tmp)) > 0)
                {
                    System.arraycopy(tmp, 0, img, desPos, readLen);
                    desPos += readLen;
                }
                webImg = BitmapFactory.decodeByteArray(img, 0, img.length);
            }
            httpURLConnection.disconnect();
        }
        catch (IOException e) {
            Log.d("IOException", e.toString());
            return defaultImg;		//抓不到網路圖時, 讀預設圖片
        }
        return webImg;
    }
}