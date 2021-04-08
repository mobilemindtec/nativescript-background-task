import { Application } from "@nativescript/core"

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
  var identifier = args.identifier + ""
  var partBytesSize = args.partBytesSize 
  var checkPartialDownload = args.checkPartialDownload

  var task = new br.com.mobilemind.ns.task.HttpRequestToFileTask(callback, url, toFile, identifier)
  task.setCheckPartialDownload(checkPartialDownload == undefined ? false : checkPartialDownload)
  task.setPartBytesSize( partBytesSize || 0)

  if(args.headers){
    for(var i in args.headers){
      var header = args.headers[i]
      for(var key in header){
        task.addHeader(key, header[key])
      }
    }
  }

  task.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR, null)
}

exports.isPartialDownloadCompleted = function(args) {
  return br.com.mobilemind.ns.task.HttpRequestToFileTask.isCompletedDownload(args.toFile)
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
        image: // bitmap or uiimage
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
      if(args.doneCallback){
        args.doneCallback()
        return
      }
    }

    var largeFiles = []

    for(var i in args.files) {

      var item = args.files[i]
      var large = new br.com.mobilemind.ns.task.LargeFilePersisterTask.LargeFile()
      large.bitmap = item.image.android || item.image
      large.fileDst = item.fileDst
      large.fileSrc = item.fileSrc
      large.quality = item.quality

      largeFiles.push(large)

    }

    br.com.mobilemind.ns.task.LargeFilePersisterTask.doIt(callback, largeFiles);
  }catch(error){
    console.log("BackgroundTask.saveLargeFiles error=" + error)
    if(args.errorCallback)
      args.errorCallback(error)
  }

}

/*
  args = {
    files: [
      {
        fileSrc:
        filePartPath:
        filePartName:
        filePartSufix: default is "part"
        filePartMaxSize: default is 5 (5MB)
      }
    ]
  }

*/

exports.splitFiles = function(args){

  var callback = createCallback(args)

  try{

    if(!args.files || args.files.length == 0){
      if(args.doneCallback){
        args.doneCallback()
        return
      }
    }

    var splitFiles = []

    for(var i in args.files) {

      var item = args.files[i]
      var splitFile = new br.com.mobilemind.ns.task.SplitFilesTask.SplitFile()

      splitFile.fileSrc = item.fileSrc
      splitFile.filePartPath = item.filePartPath
      splitFile.filePartMaxSize = item.filePartMaxSize || splitFile.filePartMaxSize
      splitFile.filePartName = item.filePartName
      splitFile.filePartSufix = item.filePartSufix || splitFile.filePartSufix

      splitFiles.push(splitFile)

    }

    br.com.mobilemind.ns.task.SplitFilesTask.doIt(callback, splitFiles);

  }catch(error){
    console.log("BackgroundTask.splitFiles error=" + error)
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

    var httpPostFileTask

    if(args.formData){
      httpPostFileTask = new br.com.mobilemind.ns.task.HttpPostFileFormDataTask(args.url, callback)
    } else {
      httpPostFileTask = new br.com.mobilemind.ns.task.HttpPostFileTask(args.url, callback)
    }

    for(var i in args.items){
      var jsonItem = args.items[i]
      var fileSrc = jsonItem.fileSrc
      var jsonKey = jsonItem.jsonKey
      var jsonData = jsonItem.data

      var httpPostData = new br.com.mobilemind.ns.task.HttpPostData(fileSrc, jsonKey)
      httpPostData.identifier = jsonItem.identifier

      for(var key in jsonData){
        httpPostData.addJsonValue(key, jsonData[key])
      }

      httpPostFileTask.addData(httpPostData)

    }

    if(args.gzip)
      httpPostFileTask.setUseGzip(args.gzip)

    if(args.headers){
      for(var i in args.headers){
        var header = args.headers[i]
        for(var key in header){
          httpPostFileTask.addHeader(key, header[key])
        }
      }
    }

    httpPostFileTask.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR, null)
  }catch(error){
    console.log("BackgroundTask.postFiles error=" + error)

    if(args.errorCallback)
      args.errorCallback(error)
  }
}

exports.postData = function(args){

  var callback = createCallback(args)

  try{

    var task = new br.com.mobilemind.ns.task.HttpPostDataTask(args.url, callback)


    for(var i in args.items){
      var jsonItem = args.items[i]
      var fileSrc = jsonItem.fileSrc
      var jsonKey = jsonItem.jsonKey
      var jsonData = jsonItem.data

      var httpPostData = new br.com.mobilemind.ns.task.HttpPostData(fileSrc, jsonKey)
      httpPostData.identifier = jsonItem.identifier

      for(var key in jsonData){
        httpPostData.addJsonValue(key, jsonData[key])
      }

      task.addData(httpPostData)

    }

    if(args.gzip)
      task.setUseGzip(args.gzip)

    if(args.splitMaxSize > 0)
      task.setSplitMaxSize(args.splitMaxSize)


    if(args.headers){
      for(var i in args.headers){
        var header = args.headers[i]
        for(var key in header){
          task.addHeader(key, header[key])
        }
      }
    }

    task.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR, null)
    
  }catch(error){
    console.log("BackgroundTask.postFiles error=" + error)

    if(args.errorCallback)
      args.errorCallback(error)
  }
}

/*

  insert/update/delete

  args = {
    dbName
    items: [
      {
        query:
        args
      }
    ]
  }

  insert or update

  args = {
    dbName
    items: [
      {
        insertQuery:
        updateQuery:
        tableName:
        updateKey:  // where column name
        updateKeyValue: // where column value
        params:
      }
    ]
  }

*/
exports.dbBatch = function(args){
  var callback = createCallback(args)

  try{
    var context = Application.android.foregroundActivity || Application.android.startActivity
    var dbName = args.dbName

    var dbInsertBatchTask = new br.com.mobilemind.ns.task.DbInsertBatchTask(context, dbName, callback)

    for(var i in args.items){
      var item = args.items[i]
      if(item.query)
        dbInsertBatchTask.addQuery(item.query, item.args)
      else
        dbInsertBatchTask.addInsertOrUpdateQuery(item.insertQuery, item.updateQuery, item.tableName, item.updateKey + "", item.updateKeyValue, item.args)
    }

    dbInsertBatchTask.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR, null)

  }catch(error){
    console.log("BackgroundTask.dbBatchInsert error=" + error)

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
