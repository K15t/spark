const path = require('path');

let distDir = path.resolve(process.env.DIST_DIR || 'target/gen');
let version = process.env.PROJ_VERSION || 'dev-version';

let baseConfig = {
    module: {
        rules: [
            {
                include: /\.js$/,
                use: [{loader: 'babel-loader', options: {presets: ['es2015', 'stage-2']}}]
                // simple-xdm needs stage-2 (Object spread operator)
            },
            {
                include: /\.soy$/,
                use: [
                    {loader: 'string-replace-loader', options: {
                        // add the autoescape mode to the soy namespace that will produce an output matching the one
                        // from the soy compiler Atlassian uses
                        // the source file has to stay without the autoescape attribute to avoid problems with add-ons
                        // using the soy template directly as a webresource
                        search: '\\{namespace ([^\\}]*)\\}',
                        flags: '',
                        replace: '{namespace $1 autoescape="deprecated-contextual"}'
                    }},
                    {loader: 'soy-template-loader'}
                ]
            },
            {
                include: /\.css$/,
                loader: 'style-loader!css-loader'
            },
            {
                include: /\.(js|soy|css)$/,
                use: [{loader: 'string-replace-loader', options: {
                    search: '{{spark_gulp_build_version}}',
                    replace: version
                }}]
            }
        ]
    }
};

module.exports = [
    Object.assign({}, baseConfig, {
        entry: './src/global.js',
        output: {
            filename: 'spark-dist.js',
            path: distDir
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