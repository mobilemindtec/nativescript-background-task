


exports.getFile = function(args){

  var callback = new mobilemind.com.br.nativescript.backgroundtask.CompleteCallback({
    onComplete: function(result){
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
    onComplete: function(result){
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
    onComplete: function(result){
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

/*
  args = {
    files: [
      { 
        bitmap: 
        filePath:
        quality: 
      }
    ]
  }

*/
exports.saveLargeImages = function(args){

  var callback = new mobilemind.com.br.nativescript.backgroundtask.CompleteCallback({
    onComplete: function(result){
      if(args.doneCallback)
        args.doneCallback()
    },
    onError: function(e){
      if(args.errorCallback)
        args.errorCallback(e)
    }
  })
  
  var largeFiles = []

  for(var i in args.files) {

    var item = args.files[i]
    var large = new mobilemind.com.br.nativescript.backgroundtask.LargeFileBitmap()
    large.bitmap = item.bitmal
    large.filePath = item.filePath
    large.quality = item.quality

    largeFiles.push(large)

  }  

  mobilemind.com.br.nativescript.backgroundtask.LargeFileManagerTask.doIt(callback, largeFiles);

}

/*
  
  args = {
    filePath: 
    fileJsonKey:
    data: - json data 
    url: 
  }

*/

exports.postFile = function(args){

  var callback = new mobilemind.com.br.nativescript.backgroundtask.CompleteCallback({
    onComplete: function(result){
      if(args.doneCallback)
        args.doneCallback(result)
    },
    onError: function(e){
      if(args.errorCallback)
        args.errorCallback(e)
    }
  })

  var filePath = args.filePath
  var fileJsonKey = args.fileJsonKey
  var httpPostData = new mobilemind.com.br.nativescript.backgroundtask.HttpPostFileTask.HttpPostData(filePath, fileJsonKey)
  
  console.log("## filePath=" + filePath)
  console.log("## fileJsonKey=" + fileJsonKey)

  for(var key in args.data){
    httpPostData.addValue(key, args.data[key])
    console.log("## add json data=" + key + "=" + args.data[key])
  }

  var httpPostFileTask = new mobilemind.com.br.nativescript.backgroundtask.HttpPostFileTask(args.url, httpPostData, callback)

  if(args.headers){
    for(var i in args.headers){
      var header = rgs.headers[i]
      for(var key in header){
        httpPostFileTask.addHeader(key, header[key])
        console.log("## add header=" + key + "=" + header[key])
      }
    }
  }

  console.log("## httpPostFileTask.executeOnExecutor")
  httpPostFileTask.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR, null)
}