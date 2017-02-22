package br.com.mobilemind.ns.task;

/**
 * Created by ricardo on 3/22/16.
 */
public interface CompleteCallback {

    void onComplete(Object result);

    void onError(Object error);
}
