exports.getFile = function(args){
	var CompleteCallback = (function(_super){
		__extends(CompleteCallback, _super);
		function CompleteCallback(){
			_super.apply(this, arguments);
		}

		CompleteCallback.prototype.onComplete = function(){
	  		if(args.doneCallback)
	    		args.doneCallback()
		}

		CompleteCallback.prototype.onError = function(message){
	  		if(args.errorCallback)
	    		args.errorCallback(message)
		}

		CompleteCallback.ObjCProtocols = [NSBackgroundTaskCompleteCallback]

		return CompleteCallback

	}(NSObject))		
	var task = NSBackgroundTaskHttpRequestToFile.alloc().initWithUrlToFile(args.url, args.toFile)
	task.delegate = new CompleteCallback()
	task.runTask(); 
}

exports.unzip = function(args){
	var CompleteCallback = (function(_super){
		__extends(CompleteCallback, _super);
		function CompleteCallback(){
			_super.apply(this, arguments);
		}

		CompleteCallback.prototype.onComplete = function(){
	  		if(args.doneCallback)
	    		args.doneCallback()
		}

		CompleteCallback.prototype.onError = function(message){
	  		if(args.errorCallback)
	    		args.errorCallback(message)
		}

		CompleteCallback.ObjCProtocols = [NSBackgroundTaskCompleteCallback]

		return CompleteCallback

	}(NSObject))		
	var task = NSBackgroundTaskUnzipTask.alloc().initWithFromFileToFile(args.fromFile, args.toFile)
	task.delegate = new CompleteCallback()
	task.runTask(); 
}

exports.copyFiles = function(args){
	var CompleteCallback = (function(_super){
		__extends(CompleteCallback, _super);
		function CompleteCallback(){
			_super.apply(this, arguments);
		}

		CompleteCallback.prototype.onComplete = function(){
	  		if(args.doneCallback)
	    		args.doneCallback()
		}

		CompleteCallback.prototype.onError = function(message){
	  		if(args.errorCallback)
	    		args.errorCallback(message)
		}

		CompleteCallback.ObjCProtocols = [NSBackgroundTaskCompleteCallback]

		return CompleteCallback

	}(NSObject))		
	var task = NSBackgroundTaskCopyFiles.alloc().initWithFromFileToFile(args.fromFile, args.toFile)
	task.delegate = new CompleteCallback()
	task.runTask(); 
}