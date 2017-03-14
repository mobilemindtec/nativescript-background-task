var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express' });
});


router.post('/', function(req, res, next){

  console.log("*********************** body")
  console.log(req.body)
  console.log("*********************** body")

  console.log(req.body.name)

  console.log("*********************** headers")
  console.log(JSON.stringify(req.headers))
  console.log("*********************** headers")

  res.json({
    message: 'OK'
  })

})



module.exports = router;
