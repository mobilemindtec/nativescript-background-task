package br.com.mobilemind.ns.task;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Created by ricardo on 3/22/16.
 */
public class HttpRequestToFileTask extends AsyncTask {



    private CompleteCallback callback;
    private String toFile;
    private String url;
    private String identifier;
    private boolean checkPartialDownload;
    private int partBytesSize = 1024 * 1024 * 2; // 2MB
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

    public void setCheckPartialDownload(boolean checkPartialDownload){
        this.checkPartialDownload = checkPartialDownload;
    }

    public void setPartBytesSize(int partBytesSize){
        if(partBytesSize > 0)
            this.partBytesSize = partBytesSize;
    }

    @Override
    protected Object doInBackground(Object[] params) {

        try {

            boolean supportsPartialDownload = false;            
            long fileDownloadedSize = 0;


            if(this.checkPartialDownload){
                supportsPartialDownload = checkServerSupportPartialDownload();
            } 

            Log.i("HttpRequestToFileTask", "supportsPartialDownload=" + supportsPartialDownload);

            while(true){

                URL url = new URL(this.url);

                Log.i("HttpRequestToFileTask", "ULR=" + this.url);
                Log.i("HttpRequestToFileTask", "toFile=" + this.toFile);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                for (String name : this.httpHeaders.keySet()){
                    connection.setRequestProperty(name, this.httpHeaders.get(name));
                }

                if(supportsPartialDownload){
                    
                    File destinationFile = new File(this.toFile + ".part");
                    
                    if(destinationFile.exists()){
                        fileDownloadedSize = destinationFile.length();
                    }

                    long rangeStart = fileDownloadedSize;
                    long rangeEnd = fileDownloadedSize + partBytesSize;
                    
                    connection.setRequestProperty("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/*");

                }                  

                int statusCode = connection.getResponseCode();
                
                if (statusCode >= 400) {
                    String errorMessage = errorToString(connection.getErrorStream());
                    throw new Exception("Download error. StatusCode: " + statusCode + ", Message: " + errorMessage);
                }

                InputStream inStream = connection.getInputStream();
                

                Log.i("HttpRequestToFileTask", "statusCode=" + statusCode);

                String fileDestinationName = this.toFile;

                if(supportsPartialDownload){
                    fileDestinationName += ".part";
                }

                BufferedInputStream bis = new BufferedInputStream(inStream);
                FileOutputStream fos = new FileOutputStream(fileDestinationName, supportsPartialDownload);


                int BUFFER_SIZE = 20 * 1024;
                byte[] buffer = new byte[BUFFER_SIZE];
                int actual = 0;
                int totalRead = 0;

                while (actual != -1) {
                    fos.write(buffer, 0, actual);
                    actual = bis.read(buffer, 0, BUFFER_SIZE);

                    if(actual > 0)
                        totalRead += actual;
                }

                //Log.i("HttpRequestToFileTask", "totalRead=" + totalRead);

                fos.flush();
                fos.close();
                bis.close();
                inStream.close();
                connection.disconnect();

                if(!supportsPartialDownload){
                    break;
                } else {

                    if(totalRead < partBytesSize){
                        // end of download
                        File file = new File(fileDestinationName);
                        File newfile = new File(this.toFile);

                        //Log.i("HttpRequestToFileTask", "FILE DESTINATION LEN=" + file.length());

                        if (newfile.exists())
                            newfile.delete();

                        boolean success = file.renameTo(newfile);

                        if(!success)
                            throw new Exception("error rename partial download to " + this.toFile);                        

                        break;
                    }
                }
            }
        }catch (Exception e){
            Log.e("HttpRequestToFileTask", e.getMessage(), e);
            return  e;
        }
        return this.identifier;
    }

    private boolean checkServerSupportPartialDownload(){
        try{
            URL url = new URL(this.url);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            for (String name : this.httpHeaders.keySet())
                connection.setRequestProperty(name, this.httpHeaders.get(name));

            connection.setRequestMethod("HEAD");

            int statusCode = connection.getResponseCode();

            if(statusCode != 200){
                String errorMessage = errorToString(connection.getErrorStream());
                Log.i("HttpRequestToFileTask", "server error on get HEAD: status: " + statusCode + ", message: " + errorMessage);
                return false;
            }

            Map<String, List<String>> map = connection.getHeaderFields();

            if(map.containsKey("Accept-Ranges") && !map.get("Accept-Ranges").isEmpty()){
                
                Log.i("HttpRequestToFileTask", "server acceps partial download: " + map.get("Accept-Ranges").get(0));
                
                if(map.get("Accept-Ranges") != null)
                    return "bytes".equals(map.get("Accept-Ranges").get(0).trim());

            } else {
                Log.i("HttpRequestToFileTask", "server does not acceps partial download");
            }            
        }catch(Exception e){
            Log.e("HttpRequestToFileTask", e.getMessage(), e);
        }

        return false;

    }

    private String errorToString(InputStream inStream) throws Exception{
        BufferedInputStream bis = new BufferedInputStream(inStream);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while(result != -1) {
            buf.write((byte) result);
            result = bis.read();
        }        
        return buf.toString("UTF-8");        
    }

    public static boolean isCompletedDownload(String destinationFile){
        File completeFile = new File(destinationFile);
        File partialFile = new File(destinationFile + ".part");

        if(partialFile.exists())
            return false;

        return completeFile.exists();
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
