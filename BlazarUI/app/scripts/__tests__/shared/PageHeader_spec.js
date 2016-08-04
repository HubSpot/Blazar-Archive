jest.dontMock('../../components/shared/PageHeader.jsx');

import React from 'react/addons';
import PageHeader from '../../components/shared/PageHeader.jsx';

const TestUtils = React.addons.TestUtils;

function renderedOutput(elt) {
  const shallowRenderer = TestUtils.createRenderer();
  shallowRenderer.render(elt);
  return shallowRenderer.getRenderOutput();
}

describe('<PageHeader />', () => {
  let pageHeader;

  beforeEach(() => {
    pageHeader = renderedOutput(<PageHeader>How now brown cow.</PageHeader>);
  });

  it('should have the correct tag', () => {
    expect(pageHeader.type).toBe('div');
  });

  it('should have the correct class names', () => {
    expect(pageHeader.props.className).toBe('page-header');
  });

  it('should render all the children', () => {
    expect(pageHeader.props.children).toBe('How now brown cow.');
  });
});
