package br.com.mobilemind.ns.task;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import br.com.mobilemind.ns.task.sql.DBHelper;

/**
 * Created by ricardo on 1/25/17.
 */

public class DbInsertBatchTask extends AsyncTask {

    private CompleteCallback callback;
    private DBHelper helper;
    private Context context;
    private String dbName;
    private List<Query> sqls;

    class Query {
        String query;
        String insertQuery;
        String updateQuery;
        Object[] params;
        String tableName;
        String updateKey;
        String updateKeyValue;

        public Query(String query, Object[] params) {
            this.query = query;
            this.params = params;
        }

        public Query(String insertQuery, String updateQuery, String tableName, String updateKey, String updateKeyValue, Object[] params) {
            this.insertQuery = insertQuery;
            this.updateQuery = updateQuery;
            this.tableName = tableName;
            this.updateKey = updateKey;
            this.updateKeyValue = updateKeyValue;
            this.params = params;
        }
    }

    public DbInsertBatchTask(Context context, String dbName, CompleteCallback callback) {
        this.callback = callback;
        this.context = context;
        this.dbName = dbName;
        sqls = new LinkedList<Query>();
    }

    public void addQuery(String sql, Object[] params) {
        sqls.add(new Query(sql, params));
    }

    public void addInsertOrUpdateQuery(String insertQuery, String updateQuery, String tableName, String updateKey, String updateKeyValue, Object[] params) {
        sqls.add(new Query(insertQuery, updateQuery, tableName, updateKey, updateKeyValue, params));
    }

    @Override
    protected Object doInBackground(Object[] params) {
        SQLiteDatabase db = null;
        try {
            Log.i("DbInsertBatchTask", "## open database " + this.dbName);
            helper = new DBHelper(context, this.dbName, 1);
            db = helper.getReadableDatabase();

            Log.i("DbInsertBatchTask", "## database " + this.dbName + " is isOpen=" + db.isOpen());

            db.beginTransaction();


            for (Query q : sqls) {
                if (q.query != null) {
                    helper.executeQuery(q.query, q.params, db);
                } else {
                    Long id = helper.getDataId("select id from " + q.tableName + " where " + q.updateKey + " = ?", new String[]{q.updateKeyValue});
                    if(id == null){
                        helper.executeQuery(q.insertQuery, q.params, db);
                    } else {
                        List args = new LinkedList(Arrays.asList(q.params));
                        args.add(q.updateKeyValue); 
                        q.params = args.toArray();
                        helper.executeQuery(q.updateQuery, q.params, db);
                    }
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DbInsertBatchTask", e.getMessage(), e);
            return  e;
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }

            if(helper != null)
                helper.close();
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
                Log.i("DbInsertBatchTask", "done callback");
                callback.onComplete(o);
            } else {
                Log.i("DbInsertBatchTask", "done null callback");
            }
        }
    }
}
