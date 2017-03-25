package br.com.mobilemind.ns.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.mobilemind.api.droidutil.rest.RestException;
import br.com.mobilemind.api.rest.RestStatus;
import br.com.mobilemind.api.rest.RestTools;
import br.com.mobilemind.api.security.key.Base64;

/**
 * Created by ricardo on 1/23/17.
 */

public class HttpPostDataTask extends AsyncTask {

    private CompleteCallback callback;
    private ArrayList<HttpPostData> postDataFiles;
    private Map<String, String> httpHeaders;
    private String url;
    private boolean useGzip;
    public int splitMaxSize;
    private boolean resultToUtf8 = true;
    private String charset = RestTools.CHARSET_UTF8;

    public HttpPostDataTask(String url, CompleteCallback callback) {
        this.httpHeaders = new HashMap<String, String>();
        this.callback = callback;
        this.postDataFiles = new ArrayList<HttpPostData>();
        this.url = url;
    }

    public HttpPostDataTask addData(HttpPostData data){
        this.postDataFiles.add(data);
        return  this;
    }

    public void setUseGzip(boolean useGzip) {
        this.useGzip = useGzip;
    }


    public void setSplitMaxSize(int splitMaxSize) {
        this.splitMaxSize = splitMaxSize;
    }

    public HttpPostDataTask addHeader(String key, String value) {
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

                boolean isJson = false, isFormData = false, isBinary = false, isMultiPart = false;

                if(!this.httpHeaders.containsKey("Content-Type") && !this.httpHeaders.containsKey("content-type")){
                    this.httpHeaders.put("Content-Type", "application/json");
                }

                String contentType = this.httpHeaders.get("Content-Type");

                if(contentType.toLowerCase().endsWith("binary/octet-stream") || contentType .toLowerCase().endsWith("application/octet-stream")){
                    isBinary = true;
                }else if(contentType .toLowerCase().endsWith("application/json")){
                    isJson = true;
                } else if(contentType.toLowerCase().endsWith("application/x-www-form-urlencoded") || contentType.toLowerCase().endsWith("application/form-data")){
                    isFormData = true;
                }


                final int contentSize = bStream.size();
                final int splitSize = this.splitMaxSize;
                final int mb = 1048576;
                final int kb = 1024 * 4;
                int partSize = mb * splitSize;
                int len = 0;

                int partCount = contentSize / partSize;

                if(contentSize % partSize > 0)
                    partCount++;

                if(this.splitMaxSize == 0){
                    partSize = contentSize;
                    partCount = 1;
                }

                for(int i = 0; i < partCount; i++) {
                    byte[] buf = null;
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(this.url);

                    if(contentSize % partSize > 0 && i == partCount - 1){
                        len = contentSize % partSize;
                    }else{
                        len = partSize;
                    }

                    if(partCount == 1){
                        buf = bStream.toByteArray();
                    }else{
                        buf = new byte[partSize];
                        bStream.write(buf, i * partSize, len);
                    }

                    String contentData = null;
                    if(this.useGzip){
                        contentData = Base64.encodeBytes(buf, Base64.GZIP);
                    }else{
                        contentData = Base64.encodeBytes(buf);
                    }

                    if(isJson || isFormData){
                        postData.json.put(postData.jsonKey, contentData);
                    }

                    int postLength = contentData.length();

                    if(isFormData){

                        Set<String> keys = postData.json.keySet();
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(keys.size());

                        for (String key : keys) {
                            nameValuePairs.add(new BasicNameValuePair(key, postData.json.get(key)));
                        }

                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                        postLength = new JSONObject(postData.json).toString().length();

                        Log.i("HttpPostDataTask", "post form-data content");

                    }else if(isJson){

                        String postDataStr = new JSONObject(postData.json).toString();
                        httppost.setEntity(new ByteArrayEntity(postDataStr.getBytes(
                                charset)));

                        postLength = postDataStr.length();
                        Log.i("HttpPostDataTask", "post json content");
                    }
                    else if(isBinary){
                        httppost.setEntity(new ByteArrayEntity(buf));
                    }

                    for (String key : this.httpHeaders.keySet())
                        httppost.setHeader(key, this.httpHeaders.get(key));


                    //httppost.setHeader("Content-Length", postLength + "");

                    Log.i("HttpPostDataTask", "post binary content");
                    execute(httpclient, httppost, postData);



                    bStream.close();
                    bStream = null;


                    Runtime.getRuntime().gc();
                    System.gc();
                    Thread.sleep(100);

                }
            }
        } catch (Exception e) {
            Log.e("HttpPostDataTask", e.getMessage(), e);
            return e;
        }

        return null;
    }

    private void execute(HttpClient httpclient, HttpPost httppost, HttpPostData postData){

        HttpResponse response = null;

        try {
            response = httpclient.execute(httppost);
        } catch (HttpHostConnectException e) {
            if (e.getMessage().contains("refused")) {
                throw new RestException(RestStatus.HTTP_CONNECTION_REFUSED, "conneciton refused");
            } else {
                throw new RestException(e.getMessage(), e);
            }
        } catch (ConnectTimeoutException e) {
            throw new RestException(RestStatus.HTTP_CONNECTION_TIME_OUT, "connection timeout");
        } catch (Exception e) {
            throw new RestException(e.getMessage(), e);
        }


        Header headers[] = response.getAllHeaders();

        for(Header header : headers)
            postData.responseHeaders.put(header.getName(), header.getValue());

        HttpEntity httpEntity = response.getEntity();
        InputStream instream = null;
        String result = null;

        if (httpEntity != null) {
            try {
                instream = httpEntity.getContent();
                result = convertStreamToString(instream);
                postData.result = result;
                Log.i("HttpPostDataTask", "result = [" + result + "]");
            } catch (Exception e) {
                throw new RestException(e.getMessage(), e);
            } finally {
                if (instream != null) {
                    try {
                        instream.close();
                    } catch (IOException io) {
                    }
                }
            }
        }else {
            Log.i("HttpPostDataTask", "httpEntity is null");
        }

        if(response.getStatusLine().getStatusCode() != RestStatus.OK){
            Log.i("HttpPostDataTask", "response code = [" + response.getStatusLine().getStatusCode() + "]");
            Log.i("HttpPostDataTask", "response reason = [" + response.getStatusLine().getReasonPhrase() + "]");
            throw new RestException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), result);
        }
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
                Log.i("HttpPostDataTask", "done with error " + json.toString());
                if (callback != null) {
                    callback.onError(json.toString());
                }else {
                    Log.i("HttpPostDataTask", "done null callback");
                }
            }else if(o instanceof  Exception){
                if (callback != null) {
                    callback.onError(((Exception) o).getMessage());
                } else {
                    Log.i("HttpPostDataTask", "done null callback");
                }
            }else {
                if (callback != null) {
                    Log.i("HttpPostDataTask", "done callback");
                    callback.onComplete(this.postDataFiles.toArray());
                } else {
                    Log.i("HttpPostDataTask", "done null callback");
                }
            }
        }catch(Exception e){
            Log.e("HttpPostDataTask", "error on callback call: " + e.getMessage(), e);
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

    public String convertStreamToString(InputStream is)
            throws IOException {
        /*
         * To convert the InputStream to String we use the
         * BufferedReader.readLine() method. We iterate until the BufferedReader
         * return null which means there's no more data to read. Each line will
         * appended to a StringBuilder and returned as String.
         */
        BufferedReader reader = null;

        if (resultToUtf8) {
            reader = new BufferedReader(new InputStreamReader(is,
                    "UTF-8"));
        } else {
            reader = new BufferedReader(new InputStreamReader(is));
        }

        StringBuilder sb = new StringBuilder();

        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        return sb.toString();
    }
}
