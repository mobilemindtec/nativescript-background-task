package br.com.mobilemind.ns.task;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ricardo on 3/22/16.
 */
public class HttpRequestToFileTask extends AsyncTask {



    private CompleteCallback callback;
    private String toFile;
    private String url;
    private String identifier;
    private Map<String, String> httpHeaders;


    public HttpRequestToFileTask(CompleteCallback callback, String url, String toFile, String identifier)
    {
        this.callback = callback;
        this.toFile = toFile;
        this.url = url;
        this.identifier = identifier;
        this.httpHeaders = new HashMap<String, String>();
    }

    public void addHeader(String name, String value){
        this.httpHeaders.put(name, value);
    }

    @Override
    protected Object doInBackground(Object[] params) {

        try {
            URL url = new URL(this.url);

            Log.i("HttpRequestToFileTask", "ULR=" + this.url);
            Log.i("HttpRequestToFileTask", "toFile=" + this.toFile);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            for (String name : this.httpHeaders.keySet())
                connection.setRequestProperty(name, this.httpHeaders.get(name));

            int statusCode = connection.getResponseCode();

            InputStream inStream;
            if (statusCode >= 400) {
                inStream = connection.getErrorStream();
                throw new Exception("download error status: " + statusCode);
            }else {
                inStream = connection.getInputStream();
            }

            Log.i("HttpRequestToFileTask", "statusCode=" + statusCode);

            BufferedInputStream bis = new BufferedInputStream(inStream);
            FileOutputStream fos = new FileOutputStream(this.toFile);


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
            Log.e("HttpRequestToFileTask", e.getMessage(), e);
            return  e;
        }

        return this.identifier;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        if(o instanceof  Exception){
            if (callback != null) {
                callback.onError(new Object[]{identifier, ((Exception) o).getMessage()});
            }
        }else {
            if (callback != null) {
                Log.i("HttpRequestToFileTask", "done callback");
                callback.onComplete(o);
            } else {
                Log.i("HttpRequestToFileTask", "done null callback");
            }
        }
    }
}
