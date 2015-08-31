jest.dontMock('../../components/shared/Breadcrumb.jsx');

import React from 'react/addons';
// import { Link } from 'react-router';
import Breadcrumb from '../../components/shared/Breadcrumb.jsx';

const TestUtils = React.addons.TestUtils;

function renderedOutput(elt) {
  const shallowRenderer = TestUtils.createRenderer();
  shallowRenderer.render(elt);
  return shallowRenderer.getRenderOutput();
}

describe('<Breadcrumb />', () => {

  let breadcrumb;
  const appRoot = 'http://localhost:5000';

  beforeEach(() => {
    const path = appRoot + '/builds/github.com/HubSpot/Blazar';
    breadcrumb = renderedOutput(
      <Breadcrumb
        path={path}
        appRoot='http://localhost:5000/'
      />
    );
  });

  it('should have the correct tag', () => {
    expect(breadcrumb.type).toBe('div');
  });

  it('should have the correct class names', () => {
    expect(breadcrumb.props.className).toBe('breadcrumbs');
  });

  // To do:
  // it('should render all the children', () => {
  // });


});
