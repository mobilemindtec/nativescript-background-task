
var observableModule = require("data/observable");
var fs = require("file-system");
var BackgroundTask = require("nativescript-background-task")
var dialogs = require("ui/dialogs");

var viewModel = new observableModule.Observable({
  'message': '',
  'loading': false
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
		showAlert('Zip file does not exists.. do zip download.')
		return
	}

  viewModel.set('loading', true)

	BackgroundTask.unzip({
		fromFile: zipFile,
		toFile: extractPath,
		doneCallback: function(){
			// done

			fs.Folder.fromPath(extractPath).getEntities().then(function(entities){
        viewModel.set('loading', false)
				showAlert("Unziped " + entities.length + " files to " + extractPath)

			}).then(function(error){
        viewModel.set('loading', false)
				showAlert('Error on list directory ' + extractPath + ': ' + error)
			})
		},
		errorCallback: function(error){
			// error
      viewModel.set('loading', false)
			showAlert('Error on extract zip: ' + error)
		},
	})
}

exports.onGetWebFile = function(){

	var temp = fs.knownFolders.temp().path;
	var destinationFile = fs.path.join(temp, "images-2.1.zip")

  viewModel.set('loading', true)

	BackgroundTask.getFile({
		url: 'http://www.mobilemind.com.br/makeyourself/coollife/images-2.1.zip',
		toFile: destinationFile,
		doneCallback: function(){
			// done
			viewModel.set('loading', false)

			if(fs.File.exists(destinationFile)){
				showAlert("download success in path " + destinationFile);
			}else{
				showAlert("ops.. file not downloaded");
			}

		},
		errorCallback: function(message){
			// error
			viewModel.set('loading', false)
			showAlert("Ops.. download error: " + message);
		},
	})
}

exports.onCopyFiles = function(){
	var temp = fs.knownFolders.temp().path;
	var extractPath = fs.path.join(temp, 'unziped_files')
	var movePath = fs.path.join(temp, "moved_files")

	if(!fs.Folder.exists(extractPath)){
		showAlert('Unziped folder does not exists.. do unzip file.')
		return
	}

  viewModel.set('loading', true)

	BackgroundTask.copyFiles({
		fromFile: extractPath,
		toFile: movePath,
		doneCallback: function(){
      viewModel.set('loading', false)
			// done
			fs.Folder.fromPath(movePath).getEntities().then(function(entities){
				showAlert("Moved " + entities.length + " files to " + movePath)
			}).then(function(error){
				showAlert('Error on list directory ' + movePath + ': ' + error)
			})
		},
		errorCallback: function(error){
			// error
      viewModel.set('loading', false)
			showAlert('Error on move files: ' + error)
		},
	})
}


exports.onSaveLargeFile = function(){

}

exports.onPostFile = function(){

}

exports.onDbBatch = function () {

}

exports.onSplitFile = function () {

  var current = fs.knownFolders.currentApp()
  var videoPath = fs.path.join(current.path, 'res/big_buck_bunny.mp4')
  var temp = fs.knownFolders.temp().path;

  viewModel.set('loading', true)

  BackgroundTask.splitFiles({
    files: [{
      fileSrc: videoPath,
      filePartPath: temp,
      filePartName: 'video_',
      filePartSufix: 'part',
      filePartMaxSize: 1 // 1MB
    }],
    doneCallback: function(result){
      viewModel.set('loading', false)
      console.log(result[0].fileParts)
      showAlert('Split OK. Parts: ' + result[0].fileParts)
    },
    errorCallback: function(error){
      viewModel.set('loading', false)
      showAlert('Error on split file: ' + error)
    }
  })
}

function showAlert(message) {
  var options = {
    title: "Background Tasks",
    message: message,
    okButtonText: "OK"
  }

  dialogs.alert(options)
}
