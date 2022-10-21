const webpackConf = require('./webpack.config.js');

// take a webpack config matching the real build config
// and adapt it to be suitable for test runs
webpackConfig = webpackConf({distDir: '../../../target/classes/com/k15t/spark', buildVersion:'DEV-SNAPSHOT'});
delete webpackConfig['output'];
delete webpackConfig['entry'];
webpackConfig.mode = 'production'; // set to 'development' for test debugging

process.env.CHROME_BIN = require('puppeteer').executablePath()

module.exports = (config) => {
    config.set({
        plugins: [
            'karma-jasmine',
            'karma-webpack',
            'karma-spec-reporter',
            'karma-chrome-launcher',
            'karma-sourcemap-loader'
        ],
        frameworks: ['jasmine', 'webpack'],
        reporters: ['spec'],
        files: [
            'node_modules/jquery/dist/jquery.js',
            'test/mocks/**/*.js',
            { pattern: 'test/specs/*.js', watched: false } // webpack does the watching here
        ],
        preprocessors: {
            'test/specs/*.js': ['webpack', 'sourcemap']
        },
        browsers: ['ChromeHeadless'],
        singleRun: true,
        webpack: webpackConfig,
    });
};
