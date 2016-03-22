package mobilemind.com.br.nativescript.backgroundtask;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ricardo on 3/22/16.
 */
public class HttpRequestToFileTask extends AsyncTask {

    public interface CompleteCallback
    {
        void onComplete(Object result, Object context);
    }

    private CompleteCallback callback;


    public HttpRequestToFileTask(CompleteCallback callback)
    {
        this.callback = callback;
    }

    @Override
    protected Object doInBackground(Object[] params) {

        try {
            String paramUrl= params[0].toString();
            String toFile = params[1].toString();
            URL url = new URL(paramUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();


            int statusCode = connection.getResponseCode();

            InputStream inStream;
            if (statusCode >= 400) {
                inStream = connection.getErrorStream();
                throw new Exception("download error status: " + statusCode);
            }else {
                inStream = connection.getInputStream();
            }


            BufferedInputStream bis = new BufferedInputStream(inStream);
            FileOutputStream fos = new FileOutputStream(toFile);

            int BUFFER_SIZE = 23 * 1024;
            byte[] buffer = new byte[BUFFER_SIZE];
            int actual = 0;
            while (actual != -1) {
                fos.write(buffer, 0, actual);
                actual = bis.read(buffer, 0, BUFFER_SIZE);
            }

            fos.close();
            bis.close();
            inStream.close();

        }catch (Exception e){
            throw new RuntimeException(e);
        }

        return null;
    }

    public static void doIt(CompleteCallback callback, String url, String toFile){
        Object params = new Object[]{url, toFile};
        new HttpRequestToFileTask(callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }
}
