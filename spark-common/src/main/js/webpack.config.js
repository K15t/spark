const path = require('path');

module.exports = (env) => {
    const version = env.buildVersion || 'DEV-SNAPSHOT';
    const distDir = path.resolve(env.distDir);

    return {
        mode: "production",
        stats: 'errors-only',
        entry: {
            'spark-dist': './src/global.js',
            'spark-dist.contentWindow': './src_contentwin/spark-contentwindow.js',
        },
        output: {
            filename: '[name].js',
            path: path.join(distDir, '/'),
        },
        externals: {
            jquery: "(window.AJS && window.AJS.$) || window.require('jquery')",
            ajs: "window.AJS || window.require('ajs')"
        },
        module: {
            rules: [
                {
                    test: /\.js$/,
                    use: [{ loader: 'babel-loader', options: { presets: ['@babel/preset-env'] } }]
                },
                {
                    test: /\.scss$/,
                    use: [
                        {
                            loader: 'style-loader'
                        },
                        {
                            loader: 'css-loader',
                            options: {
                                modules: 'global'
                            }
                        },
                        {
                            loader: 'sass-loader'
                        }
                    ]
                },
                {
                    test: /\.(js|css)$/,
                    use: [{
                        loader: 'string-replace-loader', options: {
                            search: '{{spark_build_version}}',
                            replace: version
                        }
                    }]
                },
                {
                    // Do not override global jQuery iFrameResizer fn, as some other plugins rely on it
                    test: /\/node_modules\/iframe-resizer\/js\/iframeResizer\.js/,
                    use: [{
                        loader: 'string-replace-loader', options: {
                            search: 'window.jQuery',
                            replace: 'false'
                        }
                    }]
                }
            ]
        }
    }
};
