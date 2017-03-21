# nativescript-background-task

Run background task

## Features

* unzip file
* download big files
* move many files
* save large file
* post large file base64 gzip json or form data (base64 gzip)
* split large file
* run sql batch (insert, update, delete)

** IOS Pod code at http://github.com/mobilemindtec/nativescript-background-task-ios-source

## Configs
### Android
Add at app.gradle

```
allprojects {
    repositories {
        jcenter()
        mavenCentral()
        maven {
            url "http://nexus.mobilemind.com.br/repository/mobile-mind-m2"
        }
        maven {
            url "http://nexus.mobilemind.com.br/repository/mobile-mind-droid-m2"
        }
    }
}

android {
	useLibrary 'org.apache.http.legacy'
}
dependencies{
  compile('br.com.mobilemind.api:mobilemind-droid-util:1.4', {
      exclude group: 'com.google.android', module: 'android'
      exclude group: 'com.google.android', module: 'support-v4'
      exclude group: 'br.com.mobilemind.api', module: 'mobilemind-utils'
  })
  compile 'br.com.mobilemind.api:mobilemind-utils:1.3'
}

```
## Using

```

var BackgroundTask = require("nativescript-background-task")

// unzip file
BackgroundTask.unzip({
	fromFile: zipFile,
	toFile: extractPath,	
	doneCallback: function(){
		// done			
	},
	errorCallback: function(error){
		// error			
	},
})

// get large file and save in destination path
BackgroundTask.getFile({
	url: 'http://www.mobilemind.com.br/makeyourself/coollife/images-2.1.zip',
	toFile: destinationFile,
	identifier: 1,
	doneCallback: function(identifier){
		// done
	},
	errorCallback: function(error){
		var identifier = error[0]
		var message = error[1]
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

// save or copy many files or bitmap or uiimage image.. 
var files = []
files.push({
	image: image, // to save bitmap or uiimage
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
    formData: false, // use form data to post. default is json
    gzip: true, // use base64 gzip
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
		// get response headers names postData.getHeaderNames()
		// get response header value postData.getHeaderValue("name")
		// get response headers postData.getHeaders()
		// get response object postData.response
		// process result
	}
    },
    errorCallback: function(error){
    	// error
    }
})

// sql batch
var items = []

// insert / update / delete
items.push({
	query: 'insert into foo name values(?)',
	args: ['john']
})

// to insert or update
items.push({
	insertQuery: 'insert into foo name values(?)',
	updateQuery: 'update foo set name = ? where id = ?',
	tableName: 'foo',
	updateKey: 'id',
	updateKeyValue: '1',
	args: ['john']
})

BackgroundTask.dbBatch({
	dbName: dbName,
	items: items,
	doneCallback: function(){
		// done
	},
	errorCallback: function(error){
		// error
	}
})

// split large files

BackgroundTask.splitFiles({
	files: [{
		fileSrc: "/file/to/split.mp4"
		filePartPath: "/path/to/save/part/"
		fileParthName: "FileName" 
		fileParteSufix: "part" // create FileName.part
		filePartMaxSize: 3 // 3MB
	}],
	doneCallback: function(data){

		var files = []
		for(i in data){
			for(filePath in data[i].fileParts)
				files.push(it) // get name of parts
		}			
	},
	errorCallback: function(error){
		// error
	}
})

```
