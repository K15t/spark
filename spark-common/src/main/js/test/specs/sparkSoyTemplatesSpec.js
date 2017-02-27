describe('SPARK soy templates', function() {

    describe('appFullscreenContaineriFrame', function() {

        beforeEach(function() {
            var currSpark = SPARK.__versions.get();
            this.iframeTemplate = currSpark.Common.Templates.appFullscreenContaineriFrame;
        });

        it('valid arguments return a result', function() {

            var result = this.iframeTemplate({'id': 'test-id', 'src': 'test-src', 'createOptions': {}});

            expect(result).toBeDefined();

        });

        it('it requires the expected attributes', function() {
            // 'id', 'src', and then some 'createOptions' are expected by the using code in spark
            // to be used / needed (failing when needed arguments are not specified is a soy feature)

            // the wrapping of the calls in anonymous functions are needed for jasmine to be able
            // to call the functions later with correct wrappers
            // if no arguments were needed, also this.iframeTemplate (but not this.iframeTemplate()) would work

            expect(function() {this.iframeTemplate();}).toThrowError(TypeError);

            expect(function() {this.iframeTemplate('test-id', 'test-src', {});}).toThrow();

            expect(function() {this.iframeTemplate({
                'id': 'test-id',
                'srcc': 'typoed-src',
                'createOptions': {}
            })}).toThrow();

            expect(function() {this.iframeTemplate({
                'id': 'test-id',
                'src': 'test-src',
                // missing createOptions
            })}).toThrow();

            // some calls that should work
            expect(this.iframeTemplate({
                'id': 'test-id',
                'src': 'test-src',
                'createOptions': {'addChrome': false}
            })).toBeDefined();

            // createOptions can also contain extra data
            expect(this.iframeTemplate({
                'id': 'test-id',
                'src': 'test-src',
                'createOptions': {
                    'addChrome': true,
                    'some': {
                        'more': 'data',
                        'ok': true
                    }
                }
            })).toBeDefined();

        });

        it('adds expected elements with expected attributes', function() {

            var resEl = $(this.iframeTemplate({
                'id': 'testing-id',
                'src': '/src/of/iframe',
                'createOptions': {}
            }));

            expect(resEl.attr('id')).toEqual('testing-id');

            expect(resEl.find('iframe').length).toEqual(1);

            // no dialog chrome asked, should have no buttons
            expect(resEl.find('button').length).toEqual(0);

            expect(resEl.hasClass('spark-fullscreen-wrapper')).toBeTruthy();

            var iframeEl = resEl.find('iframe');

            expect(iframeEl.attr('src')).toEqual('/src/of/iframe');

            expect(iframeEl.attr('id')).toEqual('testing-id-iframe');

            expect(iframeEl.hasClass('spark-fullscreen-iframe')).toBeTruthy();

        });

        it('creates expected elements when dialog chrome is asked', function() {

            var resEl = $(this.iframeTemplate({
                    'id': 'testing-id',
                    'src': '/src/of/iframe',
                    'createOptions': {
                        'addChrome': true
                    }
                }));

            // test that the important parts about the main structure work also when chrome is added

            expect(resEl.attr('id')).toEqual('testing-id');

            expect(resEl.find('iframe').length).toEqual(1);

            expect(resEl.hasClass('spark-fullscreen-wrapper')).toBeTruthy();

            var iframeEl = resEl.find('iframe');

            expect(iframeEl.attr('src')).toEqual('/src/of/iframe');

            expect(iframeEl.attr('id')).toEqual('testing-id-iframe');

            // test dialog chrome was added and has expected ids etc

            expect(resEl.find('button').length).toEqual(2);

            expect(resEl.find('.spark-fullscreen-chrome').length).toEqual(1);

            var cancelBtn = resEl.find('#testing-id-chrome-cancel');
            var submitBtn = resEl.find('#testing-id-chrome-submit');

            expect(cancelBtn.length).toEqual(1);
            expect(cancelBtn.hasClass('aui-iconfont-close-dialog')).toBeTruthy();

            expect(submitBtn.length).toEqual(1);
            expect(submitBtn.hasClass('aui-iconfont-success')).toBeTruthy();

        });

    });

});