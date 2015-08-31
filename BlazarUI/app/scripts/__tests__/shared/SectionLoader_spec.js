jest.dontMock('../../components/shared/SectionLoader.jsx');

import React from 'react/addons';
import SectionLoader from '../../components/shared/SectionLoader.jsx';

const TestUtils = React.addons.TestUtils;

function renderedOutput(elt) {
  const shallowRenderer = TestUtils.createRenderer();
  shallowRenderer.render(elt);
  return shallowRenderer.getRenderOutput();
}

describe('<SectionLoader />', () => {

  let sectionLoader;

  beforeEach(() => {
    sectionLoader = renderedOutput(<SectionLoader />);
  });

  it('should have the right tag', () => {
    expect(sectionLoader.type).toBe('div');
  });

  it('should have the right class names', () => {
    expect(sectionLoader.props.className).toBe('section-loader');
  });

});
