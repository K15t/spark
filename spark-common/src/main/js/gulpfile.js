var gulp = require('gulp');

var karma = require('karma');
var gutil = require('gulp-util');

var soynode = require('gulp-soynode');
var css2js = require('gulp-css2js');

var concat = require('gulp-concat');

var del = require('del');

var distDir = 'target/dist';
if (process.env.DIST_DIR) {
    distDir = process.env.DIST_DIR;
}

gulp.task('compile-soy', function() {

    return gulp.src('src/**/*.soy')
        .pipe(soynode())
        .pipe(gulp.dest('target/build'));

});

gulp.task('compile-css-to-js', function() {

    return gulp.src('src/*.css')
        .pipe(concat('spark-styles.css'))
        .pipe(css2js())
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

gulp.task('build', ['test', 'compile-css-to-js'], function(){

      return gulp.src([
        'node_modules/iframe-resizer/js/iframeResizer.js', 'target/build/*.js', 'src/spark-bootstrap.js'])
           .pipe(concat('spark-dist.js')) 
          .pipe(gulp.dest('target/gen'));
      
});

gulp.task('clean-dist', function(done) {
    del('target/dist').then(done());
});

gulp.task('dist', ['clean-dist', 'build'], function() {

   return gulp.src(['target/gen/*.js', 'src/*',
            'node_modules/iframe-resizer/js/iframeResizer.js',
            'node_modules/iframe-resizer/js/iframeResizer.contentWindow.js',
            'node_modules/iframe-resizer/js/iframeResizer.min.js',
            'node_modules/iframe-resizer/js/iframeResizer.contentWindow.min.js'])
       .pipe(gulp.dest(distDir));

});