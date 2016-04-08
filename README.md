# nativescript-background-task

Run background task

## Features

* unzip file
* download big files
* move many files

### Using

```

var BackgroundTask = require("nativescript-background-task")

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

```
