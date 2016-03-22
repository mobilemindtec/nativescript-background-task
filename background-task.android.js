



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