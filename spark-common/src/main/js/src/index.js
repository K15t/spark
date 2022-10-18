import bootstrap from './spark-bootstrap';

export default {
    __version: '{{spark_build_version}}',
    appLoader: bootstrap.appLoader,
    iframeAppLoader: bootstrap.iframeAppLoader
};
