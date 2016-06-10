    package mobilemind.com.br.nativescript.backgroundtask;

    import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


    /**
     * Created by ricardo on 3/22/16.
     */
    public class CopyFilesTask extends AsyncTask {

        private CompleteCallback callback;
        private File toFile;
        private File fromFile;
        private boolean error;


        public CopyFilesTask(CompleteCallback callback,String fromFile, String toFile)
        {
            Log.i("CopyFilesTask", "toFile=" + toFile);
            Log.i("CopyFilesTask", "fromFile=" + fromFile);

            this.callback = callback;
            this.toFile = new File(toFile);
            this.fromFile = new File(fromFile);
        }

        @Override
        protected Object doInBackground(Object[] params) {

            try
            {
                copyDirectoryOneLocationToAnotherLocation(this.fromFile, this.toFile);
            }
            catch(Exception e)
            {
                if(callback != null){
                    callback.onError(e.getMessage());
                }
                error = true;
                Log.e("CopyFileTask", e.getMessage(), e);
            }

            return  null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if(callback != null && !error){
                Log.i("CopyFileTask", "done callback");
                callback.onComplete();
            }else{
                Log.i("CopyFileTask", "done null callback");
            }
        }

        public static void doIt(CompleteCallback callback, String fromFile, String toFile){
            new CopyFilesTask(callback, fromFile, toFile).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        }


        public void copyDirectoryOneLocationToAnotherLocation(File sourceLocation, File targetLocation) throws IOException {


                Log.i("CopyFileTask", "copy " + sourceLocation + " to " + targetLocation);

                if (sourceLocation.isDirectory()) {

                    if(targetLocation.exists() && targetLocation.isFile()){

                        targetLocation.delete();
                    }

                    if (!targetLocation.exists()) {

                        targetLocation.mkdir();
                    }

                    String[] children = sourceLocation.list();


                    for (int i = 0; i < sourceLocation.listFiles().length; i++) {
                        File newsource = new File(sourceLocation, children[i]);
                        File newtarget = new File(targetLocation, children[i]);
                        copyDirectoryOneLocationToAnotherLocation(newsource, newtarget);
                    }
                } else {

                    if(targetLocation.exists())
                        targetLocation.delete();

                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceLocation));
                    FileOutputStream fout = new FileOutputStream(targetLocation);
                    int count = 0;
                    byte buffer[] = new byte[1024];
                    while ((count = bis.read(buffer)) != -1) {
                        fout.write(buffer, 0, count);
                    }

                    fout.close();
                    bis.close();
                }
        }
    }
