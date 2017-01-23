package mobilemind.com.br.nativescript.backgroundtask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import br.com.mobilemind.api.droidutil.rest.WsExecutor;
import br.com.mobilemind.api.security.key.Base64;

/**
 * Created by ricardo on 1/23/17.
 */

public class HttpPostFileTask extends AsyncTask {

    private CompleteCallback callback;
    private boolean error;
    private HttpPostData postData;
    private Map<String, String> httpHeaders;
    private String url;

    public HttpPostFileTask(String url, HttpPostData postData, CompleteCallback callback){
        this.httpHeaders = new HashMap<String, String>();
        this.callback = callback;
        this.postData = postData;
        this.url = url;
    }

    public HttpPostFileTask addHeader(String key, String value){
        this.httpHeaders.put(key, value);
        return  this;
    }

    public static class HttpPostData{
        public String filePath;
        public String fileJsonKey;
        private Map<String, String> json;


        public HttpPostData(String filePath, String fileJsonKey){
            this.filePath = filePath;
            this.fileJsonKey = fileJsonKey;
            json = new HashMap<String, String>();
        }

        public HttpPostData addValue(String key, String value){
            json.put(key, value);
            return this;
        }
    }

    @Override
    protected Object doInBackground(Object[] params) {

        try {

            Bitmap bitmap = BitmapFactory.decodeFile(postData.filePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            postData.json.put(postData.fileJsonKey, Base64.encodeBytes(stream.toByteArray(), Base64.GZIP));

            Log.i("HttpPostFileTask", "ULR=" + this.url);

            WsExecutor httpService = new WsExecutor(null, 30000);
            httpService.setBaseUrl(this.url)
                    .setEntity(new JSONObject(postData.json).toString());

            for(String key : this.httpHeaders.keySet())
                httpService.addHeaderParam(key, this.httpHeaders.get(key));

            return httpService.executePostAsString();

        }catch (Exception e){
            if(callback != null){
                callback.onError(e.getMessage());
            }
            error = true;
            Log.e("HttpPostFileTask", e.getMessage(), e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        if(callback != null && !error){
            Log.i("HttpPostFileTask", "done callback");
            callback.onComplete(o);
        }else{
            Log.i("HttpPostFileTask", "done null callback");
        }
    }
}
