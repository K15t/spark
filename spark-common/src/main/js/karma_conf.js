const webpackConf = require('./webpack.config.js');

// take a webpack config matching the real build config
// and adapt it to be suitable for test runs
webpackConfig = Object.assign({}, webpackConf[0]);
delete webpackConfig['output'];
delete webpackConfig['entry'];
webpackConfig.devtool = 'inline-source-map';

module.exports = function(config) {
    config.set({
        basePath: '',
        frameworks: ['jasmine'],
        reporters: ['spec'],
        files: [
            'node_modules/jquery/dist/jquery.js',
            'node_modules/soyutils/soyutils_nogoog.js',
            'test/mocks/**/*.js',
            { pattern: 'test/specs/*.js', watched: false } // webpack does the watching here
        ],
        preprocessors: {
            'test/specs/*.js': ['webpack', 'sourcemap']
        },
        browsers: ['PhantomJS'],
        singleRun: true,
        webpack: webpackConfig,
        webpackMiddleware: {
            stats: 'errors-only'
        }
    });
};