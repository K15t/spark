var gulp = require('gulp');

// to be executed by the frontend-maven-plugin
gulp.task('default', function() {
    gulp.src('src/**')
        .pipe(gulp.dest('../../../../target/classes/spark/dialog-app_v13'));
});


// to be executed during development
gulp.task('dev', function() {
    gulp.src('src/**')
        .pipe(gulp.dest('../../../../target/spark-dev-builds/dialog-app_v13'));
});

// detects changes of any source file and kicks of the 'dev' task.
gulp.task('watch', function() {
    gulp.watch('**', ['dev'])
});
