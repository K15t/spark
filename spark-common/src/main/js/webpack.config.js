const path = require('path');
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');

let distDir = path.resolve(process.env.DIST_DIR || 'target/gen');
let version = process.env.PROJ_VERSION || 'dev-version';

let baseConfig = {
    module: {
        rules: [
            {
                include: /\.js$/,
                use: [{ loader: 'babel-loader', options: { presets: ['es2015', 'stage-2'] } }]
                // simple-xdm needs stage-2 (Object spread operator)
            },
            {
                include: /\.css$/,
                loader: 'style-loader!css-loader'
            },
            {
                include: /\.scss$/,
                loader: 'style-loader!css-loader!sass-loader'
            },
            {
                include: /\.(js|css)$/,
                use: [{
                    loader: 'string-replace-loader', options: {
                        search: '{{spark_gulp_build_version}}',
                        replace: version
                    }
                }]
            },
            {
                // Do not override global jQuery iFrameResizer fn, as some other plugins rely on it
                include: /\/node_modules\/iframe-resizer\/js\/iframeResizer\.js/,
                loader: [{
                    loader: 'string-replace-loader', options: {
                        search: 'window.jQuery',
                        replace: 'false'
                    }
                }]
            }
        ]
    },
    plugins: [
        new UglifyJSPlugin()
    ]
};

module.exports = [
    Object.assign({}, baseConfig, {
        entry: './src/global.js',
        output: {
            filename: 'spark-dist.js',
            path: distDir
        },
        externals: {
            jquery: "(window.AJS && window.AJS.$) || window.require('jquery')",
            ajs: "window.AJS || window.require('ajs')"
        }
    }),
    Object.assign({}, baseConfig, {
        entry: './src_contentwin/spark-contentwindow.js',
        output: {
            filename: 'spark-dist.contentWindow.js',
            path: distDir
        }
    })
];
