var gulp = require('gulp');
var karma = require('karma');
var gutil = require('gulp-util');
var del = require('del');
var webpack = require('webpack');

var distDir = process.env.DIST_DIR || 'target/gen';

gulp.task('test', function(done) {

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

gulp.task('tdd', function(done) {

    new karma.Server({
        configFile: __dirname + '/karma_conf.js',
        singleRun: false,
        autoWatch: true
    }, done).start();

});

gulp.task('build', ['test'], function(callback) {

    webpack(require('./webpack.config'), function(err, stats) {
        if (err) {
            throw new gutil.PluginError("webpack", err);
        }

        let compErrors = [].concat(...stats.stats.map((stat) => (stat.compilation.errors || [])));
        if(compErrors.length > 0) {
            throw new gutil.PluginError('webpack', 'There were compilation errors:\n' + compErrors.map((err) => (err.message)).join('\n'));
        }

        gutil.log('[webpack]', stats.toString());
        callback();
    });

});

gulp.task('clean-dist', function(done) {
    del('target/dist').then(done());
});

gulp.task('dist', ['clean-dist', 'build']);