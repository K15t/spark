var gulp = require('gulp');

// to be executed by the maven frontend plugin
gulp.task('default', function() {
    gulp.src('src/**')
        .pipe(gulp.dest('../../../../target/classes/dialog_app_v14'));
});


// to be executed during development
gulp.task('dev', function() {
    gulp.src('src/**')
        .pipe(gulp.dest('../../../../target/spark/dialog_app_v14'));
});

// detects changes of any source file and kicks of the 'dev' task.
gulp.task('watch', function() {
    gulp.watch('**', ['dev'])
});
