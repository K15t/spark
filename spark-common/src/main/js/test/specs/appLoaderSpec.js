import spark from '../../src/spark-bootstrap';
import sparkTemplates from '../../src/spark-common-templates';

describe('AppLoader', function() {

    describe('loadApp', function() {

        beforeEach(function() {

            this.testControl = AJS.testControl;

            this.appBootstrapContaineriFrame = spyOn(sparkTemplates, 'appBootstrapContaineriFrame')
                .and.returnValue('');

        });

        it('adds trailing slash if needed taking possible query string into account', function() {

            this.testControl.contextPath = '/test/context';

            // location is bit cumbersome to mock, but its attributes should be same here as in tested path
            var expSrcContext = location.protocol + '//' + location.host +
                '/test/context';
            var element = "<div></div>";

            this.appBootstrapContaineriFrame.calls.reset();

            spark.appLoader.loadApp(element, 'test-app', '/test/app/path');

            expect(this.appBootstrapContaineriFrame).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcContext + '/test/app/path/',
                'createOptions': jasmine.any(Object),
                'className': jasmine.any(String)
            });

            this.appBootstrapContaineriFrame.calls.reset();

            spark.appLoader.loadApp(element, 'test-app', '/test/app/path/');

            expect(this.appBootstrapContaineriFrame).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcContext + '/test/app/path/',
                'createOptions': jasmine.any(Object),
                'className': jasmine.any(String)
            });

            this.appBootstrapContaineriFrame.calls.reset();

            spark.appLoader.loadApp(element, 'test-app', '/test/app/path?query=param');

            expect(this.appBootstrapContaineriFrame).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcContext + '/test/app/path/?query=param',
                'createOptions': jasmine.any(Object),
                'className': jasmine.any(String)
            });


            this.appBootstrapContaineriFrame.calls.reset();

            spark.appLoader.loadApp(element, 'test-app', '/test/app/path/?query=param');

            expect(this.appBootstrapContaineriFrame).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcContext + '/test/app/path/?query=param',
                'createOptions': jasmine.any(Object),
                'className': jasmine.any(String)
            });

            this.appBootstrapContaineriFrame.calls.reset();

            spark.appLoader.loadApp(element, 'test-app', '/test/app/path/index.html?query=param');

            expect(this.appBootstrapContaineriFrame).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcContext + '/test/app/path/index.html?query=param',
                'createOptions': jasmine.any(Object),
                'className': jasmine.any(String)
            });


            this.appBootstrapContaineriFrame.calls.reset();

            spark.appLoader.loadApp(element, 'test-app', '/test/app/path/index.html');

            expect(this.appBootstrapContaineriFrame).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcContext + '/test/app/path/index.html',
                'createOptions': jasmine.any(Object),
                'className': jasmine.any(String)
            });

        });

    });


});