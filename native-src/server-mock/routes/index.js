var express = require('express');
var router = express.Router();


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



module.exports = router;
