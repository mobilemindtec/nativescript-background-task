package mobilemind.com.br.nativescript.backgroundtask;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by ricardo on 1/23/17.
 */

public class LargeFileManagerTask extends AsyncTask {

    private CompleteCallback callback;
    private LargeFileBitmap files[];
    private boolean error;

    public static class LargeFileBitmap{
        public Bitmap bitmap;
        public String filePath;
        public int quality = 80;
    }

    public LargeFileManagerTask(CompleteCallback callback, LargeFileBitmap files[])
    {
        this.callback = callback;
        this.files = files;
    }

    @Override
    protected Object doInBackground(Object[] params) {

        try {

            for (LargeFileBitmap file: files) {

                Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;

                if(file.filePath.toLowerCase().endsWith(".png")){
                    format = Bitmap.CompressFormat.PNG;
                }

                Log.i("LargeFileManagerTask", file.filePath);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                file.bitmap.compress(format, file.quality, stream);
                File imageFile = new File(file.filePath);

                if (imageFile.exists()) {
                    Log.i("LargeFileManagerTask", "delete file " + file.filePath);
                    imageFile.delete();
                }

                FileOutputStream fo = new FileOutputStream(imageFile);
                fo.write(stream.toByteArray());
                fo.flush();
                fo.close();
                stream.close();
                Runtime.getRuntime().gc();
                System.gc();
                Thread.sleep(1000);

                Log.i("LargeFileManagerTask", "file saved " + file.filePath);
            }

        }catch (Exception e){
            if(callback != null){
                callback.onError(e.getMessage());
            }
            error = true;
            Log.e("LargeFileManagerTask", e.getMessage(), e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        if(callback != null && !error){
            Log.i("LargeFileManagerTask", "done callback");
            callback.onComplete(o);
        }else{
            Log.i("LargeFileManagerTask", "done null callback");
        }
    }

    public static void doIt(CompleteCallback callback, LargeFileBitmap files[]){
        new LargeFileManagerTask(callback, files).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
    }
}
