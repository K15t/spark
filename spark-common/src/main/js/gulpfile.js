var gulp = require('gulp');

var karma = require('karma');
var gutil = require('gulp-util');

var soynode = require('gulp-soynode');

var concat = require('gulp-concat');

gulp.task('compile-soy', function() {

    return gulp.src('src/**/*.soy')
        .pipe(soynode())
        .pipe(gulp.dest('target/build'));

});

gulp.task('test', ['compile-soy'], function(done) {

    new karma.Server({
        configFile: __dirname + '/karma_conf.js'
    }, function(err) {
        if (err === 0) {
            done();
        } else {
            done(new gutil.PluginError('karma', {
                message: 'Karma tests failed'
            }))
        }
    }).start();

});

gulp.task('tdd', ['compile-soy'], function(done) {

    new karma.Server({
        configFile: __dirname + '/karma_conf.js',
        singleRun: false,
        autoWatch: true
    }, done).start();

});

gulp.task('build', ['compile-soy'], function(){

      return gulp.src(['target/build/*.js', 'src/spark-bootstrap.js'])
           .pipe(concat('concated.js')) 
          .pipe(gulp.dest('target/dist'));
      
});