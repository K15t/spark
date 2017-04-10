var gulp = require('gulp');
var karma = require('karma');
var gutil = require('gulp-util');
var del = require('del');
var webpack = require('webpack');

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

gulp.task('build', function(callback) {

    webpack(require('./webpack.config'), function(err, stats) {
       if (err) {
           throw new gutil.PluginError("webpack", err);
       }

        gutil.log("[webpack]", stats.toString());
        callback();
    });

});

gulp.task('clean-dist', function(done) {
    del('target/dist').then(done());
});

gulp.task('dist', ['clean-dist', 'build']);