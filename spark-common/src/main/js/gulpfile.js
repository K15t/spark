var gulp = require('gulp');
var karma = require('karma');
var gutil = require('gulp-util');
var soynode = require('gulp-soynode');
var css2js = require('gulp-css2js');
var concat = require('gulp-concat');
var del = require('del');
var replace = require('gulp-replace');

var distDir = 'target/dist';
if (process.env.DIST_DIR) {
    distDir = process.env.DIST_DIR;
}

var version = 'dev-version';
if (process.env.PROJ_VERSION) {
    version = process.env.PROJ_VERSION;
}

gulp.task('prepare-soy', function() {
    // add the autoescape mode to the soy namespace that will produce an output matching the one
    // from the soy compiler Atlassian uses
    // the source file has to stay without the autoescape attribute to avoid problems with add-ons
    // using the soy template directly as a webresource
    return gulp.src('src/**/*.soy')
        .pipe(replace(/\{namespace ([^\}]*)\}/, '{namespace $1 autoescape="deprecated-contextual"}'))
        .pipe(gulp.dest('target/gen'));

});

gulp.task('compile-soy', ['prepare-soy'], function() {

    return gulp.src('target/gen/**/*.soy')
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

gulp.task('build', ['test', 'compile-css-to-js'], function() {

    return gulp.src([
        'node_modules/iframe-resizer/js/iframeResizer.js',
        'src/spark-noconflict-header.js', 'target/build/*.js', 'src/spark-bootstrap.js'])
        .pipe(replace('{{spark_gulp_build_version}}', version))
        .pipe(concat('spark-dist.js'))
        .pipe(gulp.dest('target/gen'));

});

gulp.task('build-cont-win', function() {

    return gulp.src([
        'node_modules/iframe-resizer/js/iframeResizer.contentWindow.js',
        'src_contentwin/spark-contentwindow.js'
    ])
        .pipe(concat('spark-dist.contentWindow.js'))
        .pipe(gulp.dest('target/gen'));

});

gulp.task('clean-dist', function(done) {
    del('target/dist').then(done());
});

gulp.task('dist', ['clean-dist', 'build', 'build-cont-win'], function() {

    return gulp.src(['target/gen/*.js', 'src/*',
        'node_modules/iframe-resizer/js/iframeResizer.js',
        'node_modules/iframe-resizer/js/iframeResizer.contentWindow.js',
        'node_modules/iframe-resizer/js/iframeResizer.min.js',
        'node_modules/iframe-resizer/js/iframeResizer.contentWindow.min.js'])
        .pipe(gulp.dest(distDir));

});