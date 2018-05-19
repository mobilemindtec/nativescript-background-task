var express = require('express');
var router = express.Router();
var path = require('path');
var mime = require('mime');
var fs = require("fs");
var path = require('path');

/* GET home page. */
router.get('/', function(req, res, next) {

  console.log('on get')

  console.log("*********************** headers")
  console.log(JSON.stringify(req.headers))
  console.log("*********************** headers")


  res.render('index', { title: 'Express' });
});


router.post('/', function(req, res, next){

  console.log('on post')

  console.log("*********************** body size")
  console.log(req.body.length)
  console.log("*********************** body size")

  //console.log(req.body.name)

  console.log("*********************** headers")
  console.log(JSON.stringify(req.headers))
  console.log("*********************** headers")

  res.json({
    message: 'OK'
  })

})

router.post('/raw', function(req, res, next){
  console.log('on post raw')

  console.log("*********************** body size")
  console.log(req.body.length)
  console.log("*********************** body size")

  //console.log(req.body.name)

  console.log("*********************** headers")
  console.log(JSON.stringify(req.headers))
  console.log("*********************** headers")



  res.json({
    message: 'OK'
  })
})

router.get('/partial-download', function(req, res, next) {

  console.log('on get partial-download')

  console.log("*********************** headers")
  console.log(JSON.stringify(req.headers))
  console.log("*********************** headers")

  var file = '/home/ricardo/Downloads/larg-file.pdf';

  if(req.method == 'GET'){
    if(req.headers['content-range']){
      readcontent(file, serveWithRanges, req, res)
    }else{
      console.log('range header not set')
      res.status(500).send('range header not set')
    }
  } else {
    console.log("HEAD")
    res.set("Accept-Ranges", "bytes")
    res.end()
  }
  

});


function serveWithRanges(request, response, content, file) {

  var filename = path.basename(file);
  var mimetype = mime.lookup(file);


  var range = request.headers['content-range'];
  var total = fs.statSync(file).size;
  var parts = range.replace("bytes", "").split("/")[0].trim().split("-");

  console.log("total=" + total)
  console.log(parts)

  var partialstart = parts[0];
  var partialend = parts[1];

  var start = parseInt(partialstart, 10);
  var end = partialend ? parseInt(partialend, 10) : total;
  var chunksize = (end-start);
  

  response.set('Content-disposition', 'attachment; filename=' + filename)
  response.set("Content-Range", "bytes " + start + "-" + end + "/" + total)
  response.set("Accept-Ranges", "bytes")
  response.set("Content-Length", chunksize)
  response.set("Content-Type", mimetype)

    
  

  if(start > total){
    console.log("start > total")
    response.set("Content-Length", 0)
    response.end()
  }
  else{

    if(end > total){
      end = start + (total-start)
      console.log("end > total. new end ="+end)
    }

    response.set("Content-Length", end-start)

    try{      
      response.end(content.slice(start, end)); 
    }catch(e){
      console.log('error ' + e)
    }
  }

}

function readcontent(file, callback, request, response) {
  var toreturn;
  fs.exists(file, function(exists) {
    if (exists) {
      fs.readFile(file, function(error, content) {
        if (error) {
          response.writeHead(500);
          response.end("<h1>500, internal error.</h1>");
          toreturn = undefined;
        }
        else {
          callback(request, response, content, file);
        }
      });
    } else {
      response.writeHead(404);
      response.end("<h1>404, not found.</h1>");
      toreturn = undefined;
    }
  });
  return toreturn;
}


module.exports = router;
