package br.com.mobilemind.ns.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.StatusLine;
import br.com.mobilemind.api.rest.RestStatus;
import br.com.mobilemind.api.droidutil.rest.RestException;
import org.apache.http.HttpEntity;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;

import br.com.mobilemind.api.security.key.Base64;

/**
 * Created by ricardo on 1/23/17.
 */

public class HttpPostFileFormDataTask extends AsyncTask {

    private CompleteCallback callback;
    private ArrayList<HttpPostData> postDataFiles;
    private Map<String, String> httpHeaders;
    private String url;
    private boolean useGzip = true;
    private boolean resultToUtf8 = true;

    public HttpPostFileFormDataTask(String url, CompleteCallback callback) {
        this.httpHeaders = new HashMap<String, String>();
        this.callback = callback;
        this.postDataFiles = new ArrayList<HttpPostData>();
        this.url = url;
    }

    public HttpPostFileFormDataTask addData(HttpPostData data){
        this.postDataFiles.add(data);
        return  this;
    }

    public void setUseGzip(boolean useGzip) {
        this.useGzip = useGzip;
    }

    public HttpPostFileFormDataTask addHeader(String key, String value) {
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

                Log.i("HttpPostFileFormDataTask", "ULR=" + this.url);
                Log.i("HttpPostFileFormDataTask", "FILE LEN=" + postData.json.get(postData.jsonKey).length());

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(this.url);

                Set<String> keys = postData.json.keySet();
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(keys.size());

                for(String key : keys){
                    nameValuePairs.add(new BasicNameValuePair(key, postData.json.get(key)));
                }
                
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                for (String key : this.httpHeaders.keySet())
                    httppost.addHeader(key, this.httpHeaders.get(key));


                HttpResponse response = null;


                try {
                    postData.json.put(postData.jsonKey, null);
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
                

                if(response.getStatusLine().getStatusCode() != RestStatus.OK){
                    throw new RestException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
                }

                Header headers[] = response.getAllHeaders();

                for(Header header : headers)
                    postData.responseHeaders.put(header.getName(), header.getValue());

                HttpEntity httpEntity = response.getEntity();
                InputStream instream = null;

                if (httpEntity != null) {
                    try {
                        instream = httpEntity.getContent();
                        postData.result = convertStreamToString(instream);
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
                }                              

                Runtime.getRuntime().gc();
                System.gc();
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Log.e("HttpPostFileFormDataTask", e.getMessage(), e);
            return e;
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
                Log.i("HttpPostFileFormDataTask", "done callback");
                callback.onComplete(this.postDataFiles.toArray());
            } else {
                Log.i("HttpPostFileFormDataTask", "done null callback");
            }
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
