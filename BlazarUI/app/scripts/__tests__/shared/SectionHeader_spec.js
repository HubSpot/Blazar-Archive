jest.dontMock('../../components/shared/SectionHeader.jsx');

import React from 'react/addons';
import SectionHeader from '../../components/shared/SectionHeader.jsx';

const TestUtils = React.addons.TestUtils;

function renderedOutput(elt) {
  const shallowRenderer = TestUtils.createRenderer();
  shallowRenderer.render(elt);
  return shallowRenderer.getRenderOutput();
}

describe('<SectionHeader />', () => {
  let sectionHeader;

  beforeEach(() => {
    sectionHeader = renderedOutput(<SectionHeader>Hey</SectionHeader>);
  });

  it('should have the correct tag', () => {
    expect(sectionHeader.type).toBe('h3');
  });

  it('should have the right class names', () => {
    expect(sectionHeader.props.className).toBe('section-header');
  });

  it('should have all the children', () => {
    expect(sectionHeader.props.children).toBe('Hey');
  });
});
