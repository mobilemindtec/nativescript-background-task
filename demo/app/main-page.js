
var observableModule = require("data/observable");
var fs = require("file-system");
var ViewUtil = require("nativescript-view-util")
var BackgroundTask = require("nativescript-background-task")
var dialogs = require("ui/dialogs");

var viewModel = new observableModule.Observable({   
  'message': '' 
})

exports.loaded = function(args) {
    var page = args.object;
    page.bindingContext = viewModel;
}

exports.onUnzip = function(){

	var temp = fs.knownFolders.temp();
	var extractPath = fs.path.join(temp.path, 'unziped_files')
	var zipFile = fs.path.join(temp.path, "images-2.1.zip")

	if(!fs.File.exists(zipFile)){
		dialogs.alert('Zip file does not exists.. do zip download.')
		return
	}

	BackgroundTask.unzip({
		fromFile: zipFile,
		toFile: extractPath,
		doneCallback: function(){
			// done			

			fs.Folder.fromPath(extractPath).getEntities().then(function(entities){

				dialogs.alert("Unziped " + entities.length + " files to " + extractPath)

			}).then(function(error){
				dialogs.alert('Error on list directory ' + extractPath + ': ' + error)
			})
		},
		errorCallback: function(){
			// error			
			dialogs.alert('Error on extract zip!')
		},
	})
}

exports.onGetWebFile = function(){

	var temp = fs.knownFolders.temp().path;		
	var destinationFile = fs.path.join(temp, "images-2.1.zip")

	ViewUtil.progressOpen({
		title: 'NativeScript',
		message: 'Getting web file.. wait!!'
	})

	BackgroundTask.getFile({
		url: 'http://www.mobilemind.com.br/makeyourself/coollife/images-2.1.zip',
		toFile: destinationFile,
		doneCallback: function(){
			// done
			ViewUtil.progressClose()

			if(fs.File.exists(destinationFile)){
				dialogs.alert("download success in path " + destinationFile);	
			}else{
				dialogs.alert("ops.. file not downloaded");
			}
			
		},
		errorCallback: function(message){
			// error
			ViewUtil.progressClose()
			dialogs.alert("Ops.. download error: " + message);
		},
	})
}

exports.onCopyFiles = function(){
	var temp = fs.knownFolders.temp().path;
	var extractPath = fs.path.join(temp, 'unziped_files')
	var movePath = fs.path.join(temp, "moved_files")

	if(!fs.Folder.exists(extractPath)){
		dialogs.alert('Unziped folder does not exists.. do unzip file.')
		return
	}

	BackgroundTask.copyFiles({
		fromFile: extractPath,
		toFile: movePath,
		doneCallback: function(){
			// done						
			fs.Folder.fromPath(movePath).getEntities().then(function(entities){				
				dialogs.alert("Moved " + entities.length + " files to " + movePath)
			}).then(function(error){
				dialogs.alert('Error on list directory ' + movePath + ': ' + error)
			})
		},
		errorCallback: function(){
			// error			
			dialogs.alert('Error on move files!')
		},
	})
}


