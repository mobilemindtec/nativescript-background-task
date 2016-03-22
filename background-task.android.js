



exports.start = function(args) {
  
  var task = new br.com.mobilemind.nativescript.backgroundtask.Task(
    args.context, 
    args.title,
    args.messsage
  )

  var worker = new br.com.mobilemind.nativescript.backgroundtask.Task.TaskWorker({
    run: function(){
      args.run()
    },
    done: function(){
      args.done()
    }
  })

  task.start(worker)

  return task

}


exports.getFile = function(args){

  var callback = new mobilemind.com.br.nativescript.backgroundtask.CompleteCallback({
    onComplete: function(result){
      if(args.doneCallback)
        args.doneCallback()
    }
  })

  var toFile = args.toFile
  var url = args.url

  mobilemind.com.br.nativescript.backgroundtask.HttpRequestToFileTask.doIt(callback, url, toFile);
}