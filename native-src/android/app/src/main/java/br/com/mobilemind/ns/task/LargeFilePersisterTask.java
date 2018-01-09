package br.com.mobilemind.ns.task;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by ricardo on 1/23/17.
 */

public class LargeFilePersisterTask extends AsyncTask {

    private CompleteCallback callback;
    private LargeFile largeFiles[];

    public static class LargeFile{
        public Bitmap bitmap;
        public String fileDst;
        public String fileSrc;
        public int quality = 80;
    }

    public LargeFilePersisterTask(CompleteCallback callback, LargeFile largeFiles[])
    {
        this.callback = callback;
        this.largeFiles = largeFiles;
    }

    @Override
    protected Object doInBackground(Object[] params) {

        String imgs[] = new String[]{".png", ".jpeg", ".jpg"};

        try {

            for (LargeFile file: largeFiles) {

                boolean isImg = false;

                for(String s : imgs){
                    if(file.fileDst.toLowerCase().endsWith(s)){
                        isImg = true;
                        break;
                    }
                }

                ByteArrayOutputStream bStream = new ByteArrayOutputStream();

                if(isImg && file.bitmap != null) {
                    Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;

                    if (file.fileDst.toLowerCase().endsWith(".png")) {
                        format = Bitmap.CompressFormat.PNG;
                    }

                    file.bitmap.compress(format, file.quality, bStream );

                } else {

                    File src = new File(file.fileSrc);

                    if(!src.exists()){
                        throw new RuntimeException("file " + file.fileSrc + " not found to copy");
                    }

                    InputStream in = new FileInputStream(src);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        bStream .write(buf, 0, len);
                    }
                    in.close();
                }

                Log.i("LargeFilePersisterTask", "save file at " + file.fileDst);

                File imageFile = new File(file.fileDst);

                if (imageFile.exists()) {
                    Log.i("LargeFilePersisterTask", "delete file " + file.fileDst);
                    imageFile.delete();
                }

                FileOutputStream fo = new FileOutputStream(imageFile);
                fo.write(bStream.toByteArray());
                fo.flush();
                fo.close();
                bStream.close();
                Runtime.getRuntime().gc();
                System.gc();
                Thread.sleep(1000);

                Log.i("LargeFilePersisterTask", "file saved at " + file.fileDst);
            }

        }catch (Exception e){
            Log.e("LargeFilePersisterTask", e.getMessage(), e);
            return  e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        if(o instanceof  Exception){
            if (callback != null) {
                callback.onError(((Exception) o).getMessage());
            }
        }else {
            if (callback != null) {
                Log.i("LargeFilePersisterTask", "done callback");
                callback.onComplete(o);
            } else {
                Log.i("LargeFilePersisterTask", "done null callback");
            }
        }
    }

    public static void doIt(CompleteCallback callback, LargeFile largeFiles[]){
        new LargeFilePersisterTask(callback, largeFiles).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
    }
}
