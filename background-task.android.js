




exports.getFile = function(args){

  var callback = new mobilemind.com.br.nativescript.backgroundtask.CompleteCallback({
    onComplete: function(){
      if(args.doneCallback)
        args.doneCallback()
    },
    onError: function(e){
      if(args.errorCallback)
        args.errorCallback(e)
    }
  })

  var toFile = args.toFile
  var url = args.url

  mobilemind.com.br.nativescript.backgroundtask.HttpRequestToFileTask.doIt(callback, url, toFile);
}

exports.unzip = function(args){

  var callback = new mobilemind.com.br.nativescript.backgroundtask.CompleteCallback({
    onComplete: function(){
      if(args.doneCallback)
        args.doneCallback()
    },
    onError: function(e){
      if(args.errorCallback)
        args.errorCallback(e)
    }
  })

  var toFile = args.toFile
  var fromFile = args.fromFile

  mobilemind.com.br.nativescript.backgroundtask.UnzipTask.doIt(callback, fromFile, toFile);
}

exports.copyFiles = function(args){

  var callback = new mobilemind.com.br.nativescript.backgroundtask.CompleteCallback({
    onComplete: function(){
      if(args.doneCallback)
        args.doneCallback()
    },
    onError: function(e){
      if(args.errorCallback)
        args.errorCallback(e)
    }
  })

  var toFile = args.toFile
  var fromFile = args.fromFile

  mobilemind.com.br.nativescript.backgroundtask.CopyFilesTask.doIt(callback, fromFile, toFile);
}