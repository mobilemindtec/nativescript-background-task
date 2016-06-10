package mobilemind.com.br.nativescript.backgroundtask;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by ricardo on 3/22/16.
 */
public class UnzipTask extends AsyncTask {

    private CompleteCallback callback;
    private String toFile;
    private String fromFile;
    private boolean error;    


    public UnzipTask(CompleteCallback callback,String fromFile, String toFile)
    {
        this.callback = callback;
        this.toFile = toFile;
        this.fromFile = fromFile;

        if(!this.toFile.endsWith("/"))
            this.toFile += "/";
    }

    @Override
    protected Object doInBackground(Object[] params) {

        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new FileInputStream(this.fromFile);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                // zapis do souboru
                filename = ze.getName();

                Log.i("UnzipTask", "zip file name " + filename);

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(this.toFile + filename);
                    fmd.mkdirs();
                    continue;
                }

                File file = new File(this.toFile + filename);
                FileOutputStream fout = new FileOutputStream(file);

                // cteni zipu a zapis
                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        }
        catch(Exception e)
        {
            if(callback != null){
                callback.onError(e.getMessage());
            }
            error = true;
            Log.e("UnzipTask", e.getMessage(), e);
        }

        return  null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        if(callback != null && !error){
            Log.i("UnzipTask", "done callback");
            callback.onComplete();
        }else{
            Log.i("UnzipTask", "done null callback");
        }
    }

    public static void doIt(CompleteCallback callback, String fromFile, String toFile){
        new UnzipTask(callback, fromFile, toFile).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
    }
}
