var gulp = require('gulp');

var karma = require('karma');
var gutil = require('gulp-util');

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
    }).start();

});
