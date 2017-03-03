package br.com.mobilemind.ns.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import br.com.mobilemind.api.droidutil.rest.WsExecutor;
import br.com.mobilemind.api.security.key.Base64;
import br.com.mobilemind.api.droidutil.rest.RestException;
/**
 * Created by ricardo on 1/23/17.
 */

public class HttpPostFileTask extends AsyncTask {

    private CompleteCallback callback;
    private ArrayList<HttpPostData> postDataFiles;
    private Map<String, String> httpHeaders;
    private String url;
    private boolean useGzip = true;

    public HttpPostFileTask(String url, CompleteCallback callback) {
        this.httpHeaders = new HashMap<String, String>();
        this.callback = callback;
        this.postDataFiles = new ArrayList<HttpPostData>();
        this.url = url;
    }

    public HttpPostFileTask addData(HttpPostData data){
        this.postDataFiles.add(data);
        return  this;
    }

    public void setUseGzip(boolean useGzip) {
        this.useGzip = useGzip;
    }

    public HttpPostFileTask addHeader(String key, String value) {
        this.httpHeaders.put(key, value);
        return this;
    }

    @Override
    protected Object doInBackground(Object[] params) {

        String imgs[] = new String[]{".png", ".jpeg", ".jpg"};

        try {
            for (HttpPostData postData : this.postDataFiles) {

                boolean isImg = false;

                for (String s : imgs) {
                    if (postData.fileSrc.toLowerCase().endsWith(s)) {
                        isImg = true;
                        break;
                    }
                }
                ByteArrayOutputStream bStream = new ByteArrayOutputStream();

                if (isImg) {

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    BitmapFactory.decodeFile(postData.fileSrc, options);
                    options.inSampleSize = calculateInSampleSize(options, 500, 500);
                    options.inJustDecodeBounds = false;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;

                    Bitmap bitmap = BitmapFactory.decodeFile(postData.fileSrc, options);

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bStream);
                    bitmap.recycle();
                } else {
                    FileInputStream ios = new FileInputStream(postData.fileSrc);
                    byte[] buffer = new byte[4096];
                    int read = 0;
                    while ((read = ios.read(buffer)) != -1) {
                        bStream.write(buffer, 0, read);
                    }
                    buffer = null;
                    ios.close();
                }

                bStream.flush();

                if(this.useGzip)
                    postData.json.put(postData.jsonKey, Base64.encodeBytes(bStream.toByteArray(), Base64.GZIP));
                else
                    postData.json.put(postData.jsonKey, Base64.encodeBytes(bStream.toByteArray()));

                bStream.close();
                bStream = null;

                Log.i("HttpPostFileTask", "ULR=" + this.url);
                Log.i("HttpPostFileTask", "FILE LEN=" + postData.json.get(postData.jsonKey).length());

                WsExecutor httpService = new WsExecutor(null, 30000);

                httpService.setBaseUrl(this.url)
                        .setEntity(new JSONObject(postData.json).toString());

                
                for (String key : this.httpHeaders.keySet())
                    httpService.addHeaderParam(key, this.httpHeaders.get(key));

                try {
                    postData.json.put(postData.jsonKey, null);
                    postData.result = httpService.executePostAsString();
                } finally {
                    Runtime.getRuntime().gc();
                    System.gc();
                    Thread.sleep(1000);
                }

                Log.i("HttpPostFileTask", "FILE RESULT=" + postData.result);

                Map<String, String> resHeaders = httpService.getResponseHeaders();

                for(String name : resHeaders.keySet())
                  postData.responseHeaders.put(name, resHeaders.get(name));

                httpService = null;

            }
        } catch (Exception e) {
            Log.e("HttpPostFileTask", e.getMessage(), e);
            return e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        try{
            if(o instanceof RestException){
                JSONObject json = new JSONObject();
                json.put("statusCode", ((RestException)o).getHttpSatatus());
                json.put("content", ((RestException)o).getContent());
                json.put("message", ((RestException)o).getMessage());
                Log.i("HttpPostFileTask", "done with error " + json.toString());
                if (callback != null) {
                    callback.onError(json.toString());
                }else {
                    Log.i("HttpPostFileTask", "done null callback");
                }
            }else if(o instanceof  Exception){
                if (callback != null) {
                    callback.onError(((Exception) o).getMessage());
                } else {
                    Log.i("HttpPostFileTask", "done null callback");
                }
            }else {
                if (callback != null) {
                    Log.i("HttpPostFileTask", "done callback");
                    callback.onComplete(this.postDataFiles.toArray());
                } else {
                    Log.i("HttpPostFileTask", "done null callback");
                }
            }
        }catch(Exception e){
            Log.e("HttpPostFileFormDataTask", "error on callback call: " + e.getMessage(), e);
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
