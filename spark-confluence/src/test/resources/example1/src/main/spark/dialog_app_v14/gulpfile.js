var gulp = require('gulp');

// to be executed by the frontend-maven-plugin
gulp.task('default', function() {
    gulp.src('src/**')
        .pipe(gulp.dest('../../../../target/classes/spark/dialog_app_v14'));
});


// to be executed during development
gulp.task('dev', function() {
    gulp.src('src/**')
        .pipe(gulp.dest('../../../../target/spark-dev-builds/dialog_app_v14'));
});

// detects changes of any source file and kicks of the 'dev' task.
gulp.task('watch', function() {
    gulp.watch('**', ['dev'])
});
