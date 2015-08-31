jest.dontMock('../../components/shared/PageContainer.jsx');

import React from 'react/addons';
import PageContainer from '../../components/shared/PageContainer.jsx';

const TestUtils = React.addons.TestUtils;

function renderedOutput(elt) {
  const shallowRenderer = TestUtils.createRenderer();
  shallowRenderer.render(elt);
  return shallowRenderer.getRenderOutput();
}

describe('<PageContainer />', () => {

  // let pageContainer, pageContainerRendered;
  let pageContainer;

  beforeEach(() => {

    const component = (
      <PageContainer
        headline='I am headline'
        classNames='banana'
      >
        <p>The human torch was denied a bank loan.</p>
      </PageContainer>
    );

    pageContainer = renderedOutput(component);
    // pageContainerRendered = TestUtils.renderIntoDocument(component);
  });

  it('should have the correct tag', () => {
    expect(pageContainer.type).toBe('div');
  });

  it('should render all the children', () => {
    expect(pageContainer.props.children).toEqual([
      <h2 className='PageContainer__headline'>I am headline</h2>,
      <p>The human torch was denied a bank loan.</p>
    ]);
  });

  // To do
  // it('should have the correct class names', () => {
  //   expect().toEqual('page-content banana');
  // });


});
