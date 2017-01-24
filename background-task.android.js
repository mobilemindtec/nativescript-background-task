

/*
  args = {
    
    url:
    toFile: - file path to save

  }
*/
exports.getFile = function(args){
  var callback = createCallback(args)
  var toFile = args.toFile
  var url = args.url
  br.com.mobilemind.ns.task.HttpRequestToFileTask.doIt(callback, url, toFile);
}

/*
  
  args = {
    fromFile:
    toFile: 
  }

*/
exports.unzip = function(args){
  var callback = createCallback(args)
  var toFile = args.toFile
  var fromFile = args.fromFile
  br.com.mobilemind.ns.task.UnzipTask.doIt(callback, fromFile, toFile);
}

/*
  
  args = {
    fromFile: 
    toFile: 
  }

*/
exports.copyFiles = function(args){
  var callback = createCallback(args)
  var toFile = args.toFile
  var fromFile = args.fromFile
  br.com.mobilemind.ns.task.CopyFilesTask.doIt(callback, fromFile, toFile);
}

/*
  args = {
    files: [
      { 
        bitmap: 
        fileDst:
        fileSrc: 
        quality: 
      }
    ]
  }

*/
exports.saveLargeFiles = function(args){

  var callback = createCallback(args)
  
  try{

    if(!args.files || args.files.length == 0){
      if(args.doneCallback)
        args.doneCallback()      
    }

    var largeFiles = []

    for(var i in args.files) {

      var item = args.files[i]
      var large = new br.com.mobilemind.ns.task.LargeFilePersisterTask.LargeFile()
      large.bitmap = item.bitmap
      large.fileDst = item.fileDst
      large.fileSrc = item.fileSrc
      large.quality = item.quality

      largeFiles.push(large)

    }  
    console.log("largeFiles largeFiles.length=" + largeFiles.length)
    br.com.mobilemind.ns.task.LargeFilePersisterTask.doIt(callback, largeFiles);    
  }catch(error){
    console.log("largeFiles error=" + error)
    if(args.errorCallback)
      args.errorCallback(error)
  }

}

/*
  
  args = {        
    url: 
    items: [
      {
        jsonKey:
        fileSrc: 
        data: - json data
      }
    ]
    headers: [
      {}
    ]
  }

*/

exports.postFiles = function(args){

  var callback = createCallback(args)

  try{    
    
    var httpPostDataList = []

    for(var i in args.items){
      var jsonItem = args.items[i]
      var fileSrc = jsonItem.fileSrc
      var jsonKey = jsonItem.jsonKey
      var jsonData = jsonItem.data
      
      var httpPostData = new br.com.mobilemind.ns.task.HttpPostFileTask.HttpPostData(fileSrc, jsonKey)

      for(var key in jsonData){
        httpPostData.addJsonValue(key, jsonData[key])
        console.log("## add json data=" + key + "=" + jsonData[key])
      }

      httpPostDataList.push(httpPostData)
      
    }

    var httpPostFileTask = new br.com.mobilemind.ns.task.HttpPostFileTask(args.url, httpPostDataList, callback)

    if(args.headers){
      for(var i in args.headers){
        var header = args.headers[i]
        for(var key in header){
          httpPostFileTask.addHeader(key, header[key])
          console.log("## add header=" + key + "=" + header[key])
        }
      }
    }

    console.log("## httpPostFileTask.executeOnExecutor")
    httpPostFileTask.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR, null)
  }catch(error){
    console.log("## postFile error=" + error)

    if(args.errorCallback)
      args.errorCallback(error)

  }
}

function createCallback(args){

  var callback = new br.com.mobilemind.ns.task.CompleteCallback({
    onComplete: function(result){
      if(args.doneCallback)
        args.doneCallback(result)
    },
    onError: function(e){
      if(args.errorCallback)
        args.errorCallback(e)
    }
  })  

  return callback

}