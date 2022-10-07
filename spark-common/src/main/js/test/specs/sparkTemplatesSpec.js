import sparkTemplates from '../../src/spark-common-templates';

describe('SPARK templates', function() {

    describe('appFullscreenContaineriFrame', function() {

        beforeEach(function() {
            this.iframeTemplate = sparkTemplates.appFullscreenContainerIframe;
        });

        it('valid arguments return a result', function() {

            var result = this.iframeTemplate({ 'id': 'test-id', 'src': 'test-src', 'createOptions': {} });

            expect(result).toBeDefined();

        });

        it('it requires the expected attributes', function() {
            // 'id', 'src', and then some 'createOptions' are expected by the using code in spark

            // the wrapping of the calls in anonymous functions are needed for jasmine to be able
            // to call the functions later with correct wrappers
            // if no arguments were needed, also this.iframeTemplate (but not this.iframeTemplate()) would work

            expect(function() {
                this.iframeTemplate();
            }).toThrowError(TypeError);

            expect(function() {
                this.iframeTemplate('test-id', 'test-src', {});
            }).toThrow();

            expect(function() {
                this.iframeTemplate({
                    'id': 'test-id',
                    'srcc': 'typoed-src',
                    'createOptions': {}
                })
            }).toThrow();

            expect(function() {
                this.iframeTemplate({
                    'id': 'test-id',
                    'src': 'test-src',
                    // missing createOptions
                })
            }).toThrow();

            // some calls that should work
            expect(this.iframeTemplate({
                'id': 'test-id',
                'src': 'test-src',
                'createOptions': { 'addChrome': false }
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

            // no dialog chrome asked, should have no buttons
            expect(resEl.find('button').length).toEqual(0);

            expect(resEl.hasClass('spark-fullscreen-wrapper')).toBeTruthy();

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

            expect(resEl.hasClass('spark-fullscreen-wrapper')).toBeTruthy();

            var iframeEl = resEl.find('iframe');

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

    describe('appBootstrapContainerDialog2WithiFrame', function() {
        it('valid arguments return a result', function() {

            var result = sparkTemplates.appBootstrapContainerDialog2WithiFrame({
                'id': 'test-bs-dial-id',
                'title': 'test-bs-dial-title',
                'src': 'test-bs-dial-src',
                'createOptions': {
                    'label': {
                        'close': 'test-close-label'
                    }
                },
                'className': 'test-bs-dial-class-name'
            });

            expect(result).toBeDefined();

        });

        it('creates expected basic html structure', function() {

            const templateParams = {
                'id': 'test-bs-dial-id',
                'title': 'test-bs-dial-title',
                'src': 'test-bs-dial-src',
                'createOptions': {
                    'label': {
                        'close': 'test-close-label'
                    }
                },
                'className': 'test-bs-dial-class-name'
            };

            const resEl = $(sparkTemplates.appBootstrapContainerDialog2WithiFrame(templateParams));

            expect(resEl.attr('id')).toEqual('test-bs-dial-id');

            expect(resEl.find('header').hasClass('aui-dialog2-header')).toBeTruthy();
            expect(resEl.find('h2').text()).toEqual('test-bs-dial-title');

            const expIframeElem = $(sparkTemplates.appBootstrapContaineriFrame({
                'id': 'test-bs-dial-id',
                'src': 'test-bs-dial-src',
                'className': 'test-bs-dial-class-name',
                'createOptions': templateParams.createOptions
            }));

            expect(resEl.find('iframe').html()).toEqual(expIframeElem.html());

            expect(resEl.find('button').length).toEqual(1);

            // test with adding the submit button

            const templateParams2 = {
                'id': 'test-bs-dial-id',
                'title': 'test-bs-dial-title',
                'src': 'test-bs-dial-src',
                'createOptions': {
                    'showSubmitButton': true,
                    'label': {
                        'close': 'test-close-label',
                        'submit': 'test-submit-label'
                    }
                },
                'className': 'test-bs-dial-class-name'
            };

            const resEl2 = $(sparkTemplates.appBootstrapContainerDialog2WithiFrame(templateParams2));

            expect(resEl2.find('button').length).toEqual(2);

            expect(resEl2.find('button:first-child').text().trim()).toEqual('test-submit-label');

            expect(resEl2.find('button:nth-child(2)').text().trim()).toEqual('test-close-label');


        });

    });

    describe('appBootstrapContaineriFrame', function() {
        it('valid arguments return a result', function() {

            var result = sparkTemplates.appBootstrapContaineriFrame({
                'id': 'test-bs-id',
                'src': 'test-bs-src',
                'createOptions': {},
                'className': 'test-bs-class-name'
            });
            expect(result).toBeDefined();

        });

        it('creates expected basic html structure', function() {

            var resEl = $(sparkTemplates.appBootstrapContaineriFrame({
                'id': 'test-bs-id',
                'src': 'test-bs-src',
                'createOptions': {
                   'width': '42px'
                },
                'className': 'test-bs-class-name'
            }));

            expect(resEl.is('iframe')).toBeTruthy();

            expect(resEl.attr('id')).toEqual('test-bs-id-iframe');

            expect(resEl.hasClass('test-bs-class-name')).toBeTruthy();

            expect(resEl.attr('src')).toEqual('test-bs-src');

            expect(resEl.attr('width')).toEqual('42px');

        });

    });

});
