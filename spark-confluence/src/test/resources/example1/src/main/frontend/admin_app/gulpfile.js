var gulp = require('gulp');

// to be executed by the maven frontend plugin
gulp.task('default', function() {
    gulp.src('src/**')
        .pipe(gulp.dest('../../../../target/classes/admin_app'));
});


// to be executed during development
gulp.task('dev', function() {
    gulp.src('src/**')
        .pipe(gulp.dest('../../../../target/spark/admin_app'));
});

// detects changes of any source file and kicks of the 'dev' task.
gulp.task('watch', function() {
    gulp.watch('**', ['dev'])
});
