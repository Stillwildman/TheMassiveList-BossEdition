package com.vincent.massivelist_BE;

import java.io.File;
import java.net.URLEncoder;
import android.content.Context;
import android.os.Environment;

public class FileCache
{   
    private File cacheDir;
    
    public FileCache(Context context)
    {
        //Find the dir to save cached images
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            cacheDir = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.cache_dirname));
        else
            cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }
    
    @SuppressWarnings("deprecation")
	public File getFile(String url)
    {
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        //String filename = String.valueOf(url.hashCode());
        //Another possible solution (thanks to grantland)
        String filename = URLEncoder.encode(url);
        File f = new File(cacheDir, filename);
        return f;
    }
    
    public void clear()
    {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f:files)
            f.delete();
    }
}