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

public class SplitFilesTask extends AsyncTask {

    private CompleteCallback callback;
    private SplitFile splitFiles[];

    public static class SplitFile{
        public String fileSrc;
        public String filePartPath;
        //MB
        public int filePartMaxSize = 5;
        public String fileParthName;
        public String fileParteSufix = "part";
        public int filePartCount;
        public String[] fileParts;
    }

    public SplitFilesTask(CompleteCallback callback, SplitFile splitFiles[])
    {
        this.callback = callback;
        this.splitFiles = splitFiles;
    }

    @Override
    protected Object doInBackground(Object[] params) {

        try {

            for (SplitFile file: splitFiles) {


                File src = new File(file.fileSrc);
                InputStream in = new FileInputStream(src);

                if(!src.exists()){
                    throw new RuntimeException("file " + file.fileSrc + " not found to split");
                }                

                final int mb = 1048576;
                final int kb = 1024 * 4;
                final int partSize = mb * file.filePartMaxSize;
                byte[] buf = new byte[kb];
                int len = 0;

                int partCount = in.available() / partSize;

                if(in.available() % partSize > 0)
                    partCount++;                        
                
                file.fileParts = new String[partCount];
                file.filePartCount = partCount;

                for(int i = 0; i < partCount; i++){

                    ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                    int read = 0;

                    while ((len = in.read(buf)) > 0) {
                    
                        read += len;
                    
                        bStream .write(buf, 0, len);

                        if(read >= partSize){
                            read = 0;
                            break;
                        }
                    }                        

                    File fileSrc = new File(file.filePartPath, file.fileParthName + "_" + (i + 1) + "." + file.fileParteSufix);
                    Log.i("SplitFilesTask", "save part file at " + fileSrc.getAbsolutePath());

                    file.fileParts[i] = fileSrc.getAbsolutePath();


                    if (fileSrc.exists()) {
                        Log.i("SplitFilesTask", "delete file before create " + fileSrc.getAbsolutePath());
                        fileSrc.delete();
                    }                    

                    FileOutputStream fo = new FileOutputStream(fileSrc);
                    fo.write(bStream.toByteArray());
                    fo.flush();
                    fo.close();
                    bStream.close();                                             
                }
            
                in.close(); 

                Runtime.getRuntime().gc();
                System.gc();
                Thread.sleep(1000);

            }

        }catch (Exception e){
            Log.e("SplitFilesTask", e.getMessage(), e);
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
                Log.i("SplitFilesTask", "done callback");
                callback.onComplete(this.splitFiles);
            } else {
                Log.i("SplitFilesTask", "done null callback");
            }
        }
    }

    public static void doIt(CompleteCallback callback, SplitFile splitFiles[]){
        new SplitFilesTask(callback, splitFiles).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
    }
}
