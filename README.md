# nativescript-background-task

Run background task

## Features

* unzip file
* download big files
* move many files
* save large file
* post large file base64 gzip

### Using

```

var BackgroundTask = require("nativescript-background-task")

// unzip file
BackgroundTask.unzip({
	fromFile: zipFile,
	toFile: extractPath,
	doneCallback: function(){
		// done			
	},
	errorCallback: function(){
		// error			
	},
})

// get large file and save in destination path
BackgroundTask.getFile({
	url: 'http://www.mobilemind.com.br/makeyourself/coollife/images-2.1.zip',
	toFile: destinationFile,
	doneCallback: function(){
		// done
	},
	errorCallback: function(message){
		// error
	},
})

// copy files from origin path to destination path
BackgroundTask.copyFiles({
	fromFile: extractPath,
	toFile: movePath,
	doneCallback: function(){
		// done						
	},
	errorCallback: function(){
		// error			
	},
})

// save or copy many files or bitmap image.. 
var files = []
files.push({
	bitmap: bitmap, // to save bitmap
	fileDst: fileDst, // destination file path
	fileSrc: doc.fileSrc, // to pdf or another doc.. copy to destination
	quality: 30 // quality if is bitmap
})
BackgroundTask.saveLargeFiles({
	files: files
	doneCallback: function(){
		// done
	},
	errorCallback: function(error){
		// error 
	}
})

// post file base64 gzip

  BackgroundTask.postFiles({
    url: apiUrl, // api url
    items: [{    	
      fileSrc: fileSrc, // file origin path
      jsonKey: jsonKey, // internally post data[jsonKey] = file content
      identifier: identifier, // identifier to result
      data: { // you json object to post + data[jsonKey] = file content
	Id: 1
      }              
    }],
    headers: [
      { 'X-Auth-Token': token }                       
    ],
    doneCallback: function(dataPostList) {
    	for(var i in dataPostList){
		var postData = dataPostList[i]
		var data = JSON.parse(postData.result)
		var identifier = postData.identifier
		// process result
	}
    },
    errorCallback: function(error){
    	// error
    }
})

```
