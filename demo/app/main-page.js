
var observableModule = require("data/observable");
var fs = require("file-system");
var BackgroundTask = require("nativescript-background-task")
var dialogs = require("ui/dialogs");
var imageSource = require("image-source")
var orm = require("nativescript-db-orm");
var Model = orm.Model
var application = require("application")

var viewModel = new observableModule.Observable({
  'message': '',
  'loading': false
})

//var server = "http://192.168.0.4:3000/"
var server = "http://10.0.0.102:3000/"

var Person = (function(_super){

  __extends(Person, _super);

  function Person(params){
    _super.apply(this, params);

    params = params || {}

    this.clazz = Person
    this.tableName = "person"
    this.columns = [
      { name: 'id', key: true },
      { name: 'name', type: 'string' }
    ]

    this.attrs = {}

    for(i in this.columns){
      this.attrs[this.columns[i].name] = params[this.columns[i].name]
    }

    Model.prototype._init.call(this, this, this.attrs)
  }

  return Person

})(Model)

exports.loaded = function(args) {
    var page = args.object;
    page.bindingContext = viewModel;

    var DbChecker = orm.DbChecker
    var dbChecker = new DbChecker()

    //dbChecker.onDebug(true)

    dbChecker.createOrUpdate(true, "demo.db", [
      new Person()
    ], function(){
      console.log("orm init successful")
    }, function (error) {
      console.log("error orm init " + error)
    })

    //console.log("---------------------------------")
    //console.log(android.database.sqlite.SQLiteDatabase.openOrCreateDatabase("test.db", {}))
    //console.log("---------------------------------")

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
		url: 'http://mobilemind.com.br/makeyourself/coollife/images-2.1.zip',
		toFile: destinationFile,
    headers: [
      { 'CustonHeader': 'Custon Value' }
    ],
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

exports.onGetPartialWebFile = function() {
  var temp = fs.knownFolders.temp().path;
  var destinationFile = fs.path.join(temp, "large_file.pdf")

  viewModel.set('loading', true)

  BackgroundTask.getFile({
    url: server + 'partial-download',
    toFile: destinationFile,
    partBytesSize: 0, // use default
    checkPartialDownload: true,
    headers: [
      
    ],
    doneCallback: function(){
      // done
      viewModel.set('loading', false)

      if(fs.File.exists(destinationFile)){
        showAlert("download success in path " + destinationFile);
      }else{
        showAlert("ops.. file not downloaded");
      }

    },
    errorCallback: function(obj){
      // error
      viewModel.set('loading', false)
      showAlert("Ops.. download error: " + obj[1]);
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
			}).catch(function(error){
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
  var current = fs.knownFolders.currentApp()
  var videoPath = fs.path.join(current.path, 'res/big_buck_bunny.mp4')
  var imagePath = fs.path.join(current.path, 'res/mobilemind.png')
  var temp = fs.knownFolders.temp().path;

  viewModel.set('loading', true)

  var img = imageSource.fromFile(imagePath);

  BackgroundTask.saveLargeFiles({
    files: [{
      fileSrc: videoPath,
      fileDst: fs.path.join(temp, "big_buck_bunny.mp4")
    },{
      image: img,
      fileDst: fs.path.join(temp, "image.png")
    }],
    doneCallback: function(result){
      viewModel.set('loading', false)
      showAlert('large file saved')
    },
    errorCallback: function(error){
      viewModel.set('loading', false)
      showAlert('Error on save large file: ' + error)
    }
  })
}

exports.onPostFile = function(){

  var current = fs.knownFolders.currentApp()
  var videoPath = fs.path.join(current.path, 'res/big_buck_bunny.mp4')

  viewModel.set('loading', true)

  BackgroundTask.postFiles({
    url: server,
    formData: false,
    gzip: true,
    items: [{
      fileSrc: videoPath,
      jsonKey: 'video',
      identifier: '10',
      data: {
        name: 'jonh',
        age: '33'
      }
    }],
    headers: [
      { 'X-Auth-Toke': 'token' },
      {'Content-Type': 'application/json'}
    ],
    doneCallback: function(results) {
      viewModel.set('loading', false)
      showAlert('post result: ' + results[0].result)
    },
    errorCallback: function(error) {
      viewModel.set('loading', false)
      showAlert('Error post file file: ' + error)
    }
  })
}

exports.onPostData = function(){

  var current = fs.knownFolders.currentApp()
  var videoPath = fs.path.join(current.path, 'res/big_buck_bunny.mp4')

  viewModel.set('loading', true)

  BackgroundTask.postData({
    url: server + 'raw',
    formData: false,
    gzip: true,
    items: [{
      fileSrc: videoPath,
      identifier: '10',
    }],
    headers: [
      {"Content-Type": "application/octet-stream"}
    ],
    doneCallback: function(results) {
      viewModel.set('loading', false)
      showAlert('post result: ' + results[0].result)
    },
    errorCallback: function(error) {
      viewModel.set('loading', false)
      showAlert('Error post file file: ' + error)
    }
  })
}

exports.onDbBatch = function () {

  var items = []

  for(var i = 0; i < 1000; i++){
      items.push({
        query: "insert into person (name) values (?)",
        args: ["Person " + i]
      })
  }

  viewModel.set('loading', true)

  BackgroundTask.dbBatch({
    dbName: "demo.db",
    items: items,
    doneCallback: function(){

      viewModel.set('loading', false)

      var model = new Person()

      model.count(function(err, result){
        showAlert('data count after batch insert: ' + result)
      })

    },
    errorCallback: function (error) {
      viewModel.set('loading', false)
      showAlert('Error db batch: ' + error)
    }
  })

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



