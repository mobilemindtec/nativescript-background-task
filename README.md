# nativescript-background-task

Run background task

## Features

* unzip file
* download big files
* move many files
* save large file
* post large file base64 gzip
* run sql batch (insert, update, delete)

## Configs
### Android
Add at app.gradle

```
allprojects {
    repositories {
        jcenter()
        mavenCentral()
        maven {
            url "http://nexus.mobilemind.com.br/nexus/content/repositories/mobile-mind"
        }
        maven {
            url "http://nexus.mobilemind.com.br/nexus/content/repositories/mobile-mind-droid"
        }
    }
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

BackgroundTask.dbBatchInsert({
	dbName: dbName,
	items: items,
	doneCallback: function(){
		// done
	},
	errorCallback: function(error){
		// error
	}
})



```
