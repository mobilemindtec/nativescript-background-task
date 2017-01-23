package mobilemind.com.br.nativescript.backgroundtask;

/**
 * Created by ricardo on 3/22/16.
 */
public interface CompleteCallback {

    void onComplete(Object result);

    void onError(String message);
}
