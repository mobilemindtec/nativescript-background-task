package br.com.mobilemind.nativescript.backgroundtask;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;


public class Task extends AsyncTask{


  public static interface TaskWorker {
      void run();
      void done();
  }

  protected Activity context;
  private ProgressDialog _pd;
  private String title;    
  private String message;  
  private TaskWorker worker;      

  public Task(Activity context, String title, String message) {
      this.context = context;
      this.title = title;
      this.message = message;
  }

  public void updateMessage(final String message) {
      if (_pd != null) {
          context.runOnUiThread(new Runnable() {

              @Override
              public void run() {
                  _pd.setMessage(message);
              }
          });
      }
  }

  protected void onPreExecute() {
      _pd = ProgressDialog.show(context, title, message, true, false);
      super.onPreExecute();
  }

  @SuppressWarnings("unchecked")
  protected void onPostExecute(Object result) {
      try {
          _pd.dismiss();
      } catch (Exception e) {
      }
      super.onPostExecute(result);

      this.worker.done();
  }


  public void start(TaskWorker worker){
    this.worker = worker;
    this.execute();
  }

  @Override
  protected Object doInBackground(Object... params) {
    this.worker.run();
    return null;
  }

}