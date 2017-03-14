var fs = require("file-system")

exports.getFile = function(args){

	var CompleteCallback = createCallback(args)

	var toFile = args.toFile
  var url = args.url
  var identifier = args.identifier + ""
	var task = NSBackgroundTaskHttpRequestToFile.alloc().initWithUrlToFileIdentifier(url, toFile, identifier)
	task.delegate = CompleteCallback.new()
	task.runTask();
}

exports.unzip = function(args){

	var CompleteCallback = createCallback(args)

	var task = NSBackgroundTaskUnzipTask.alloc().initWithFromFileToFile(args.fromFile, args.toFile)
	task.delegate = CompleteCallback.new()
	task.runTask();
}

exports.copyFiles = function(args){

	var CompleteCallback = createCallback(args)

	var task = NSBackgroundTaskCopyFiles.alloc().initWithFromFileToFile(args.fromFile, args.toFile)
	task.delegate = CompleteCallback.new()
	task.runTask();
}

/*
  args = {
    files: [
      {
        image:
        fileDst:
        fileSrc:
        quality:
      }
    ]
  }

*/
exports.saveLargeFiles = function(args){

  var CompleteCallback = createCallback(args)

  try{

    if(!args.files || args.files.length == 0){
      if(args.doneCallback){
				args.doneCallback()
				return
			}
    }

		var task = NSLargeFilePersisterTask.new()
		task.delegate = CompleteCallback.new()

    for(var i in args.files) {

      var item = args.files[i]
      var largeFile = NSLargeFile.new()
      largeFile.image = item.image.ios || item.image
      largeFile.fileDst = item.fileDst
      largeFile.fileSrc = item.fileSrc
      largeFile.quality = item.quality || 0

      task.addLargeFile(largeFile)
    }

		task.runTask()

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
        fileParthName:
        fileParteSufix: default is "part"
        filePartMaxSize: default is 5 (5MB)
      }
    ]
  }

*/

exports.splitFiles = function(args){

  var CompleteCallback = createCallback(args)

  try{

    if(!args.files || args.files.length == 0){
      if(args.doneCallback)
        args.doneCallback()
    }

		var task = NSSplitFileTask.new()
		task.delegate = CompleteCallback.new()

    for(var i in args.files) {

      var item = args.files[i]

			var splitFile = NSSplitFile.new()
      splitFile.fileSrc = item.fileSrc
      splitFile.filePartPath = item.filePartPath
      splitFile.filePartMaxSize = item.filePartMaxSize || splitFile.filePartMaxSize
      splitFile.filePartName = item.filePartName
      splitFile.filePartSufix = item.filePartSufix || splitFile.filePartSufix

      task.addSplitFile(splitFile)
    }

    task.runTask()

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

  var CompleteCallback = createCallback(args)

  try{

    var task = NSHttpPostFileTask.alloc().initWithUrl(args.url)
		task.delegate = CompleteCallback.new()

    if(args.formData){
      task.setUseFormData(true)
    }

		if(args.gzip){
			task.setUseGzip(true)
		}

    for(var i in args.items){
      var jsonItem = args.items[i]
      var fileSrc = jsonItem.fileSrc
      var jsonKey = jsonItem.jsonKey
      var jsonData = jsonItem.data

      var httpPostFile = NSHttpPostFile.alloc().initWithFileSrcJsonKey(fileSrc, jsonKey)
      httpPostFile.identifier = jsonItem.identifier

      for(var key in jsonData){
        httpPostFile.addJsonKeyValue(key, jsonData[key])
      }

      task.addPostFile(httpPostFile)

    }

    if(args.gzip == false)
      task.setUseGzip(false)

    if(args.headers){
      for(var i in args.headers){
        var header = args.headers[i]
        for(var key in header){
          task.addHeaderWithNameAndValue(key, header[key])
        }
      }
    }

    task.runTask()


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
  var CompleteCallback = createCallback(args)

	var dbPath = fs.path.join(fs.knownFolders.documents().path, args.dbName)

  try{

    var task = NSDbBatchTask.alloc().initWithDbPath(dbPath)
		task.delegate = CompleteCallback.new()

    for(var i in args.items){
      var item = args.items[i]

			var query = NSQuery.new()

      if(item.query){
				query.query = item.query
				query.params = item.args
			} else {
				query.insertQuery = item.insertQuery
				query.updateQuery = item.updateQuery
				query.tableName = item.tableName
				query.updateKey = item.updateKey + ""
				query.updateKeyValue = item.updateKeyValue + ""
				query.params = item.args
			}

			task.addQuery(query)

    }

    task.runTask()

  }catch(error){
    console.log("BackgroundTask.dbBatchInsert error=" + error)

    if(args.errorCallback)
      args.errorCallback(error)
  }

}

function createCallback(args){

	var CompleteCallback = (function(_super){
		__extends(CompleteCallback, _super);
		function CompleteCallback(){
			_super.apply(this, arguments);
		}

		CompleteCallback.prototype.onComplete = function(result){
	  		if(args.doneCallback)
	    		args.doneCallback(result)
		}

		CompleteCallback.prototype.onError = function(message){
	  		if(args.errorCallback)
	    		args.errorCallback(message)
		}

		CompleteCallback.ObjCProtocols = [NSBackgroundTaskCompleteCallback]

		return CompleteCallback

	}(NSObject))

	return CompleteCallback
}
