jest.dontMock('../../components/shared/Icon.jsx');

import React from 'react/addons';
import Icon from '../../components/shared/Icon.jsx';

const TestUtils = React.addons.TestUtils;

function renderedOutput(elt) {
  const shallowRenderer = TestUtils.createRenderer();
  shallowRenderer.render(elt);
  return shallowRenderer.getRenderOutput();
}

describe('<Icon />', () => {

  it('should generate all the class names', () => {
    const result = renderedOutput(<Icon type='octicon' name='file-directory' classNames="icon-roomy" />);
    expect(result.props.className).toEqual('octicon octicon-file-directory icon-roomy');
  });

  it('should generate the correct title', () => {
    const result = renderedOutput(<Icon type='octicon' title='The arsonist has oddly shaped feet' name='file-directory' classNames="icon-roomy" />);
    expect(result.props.title).toEqual('The arsonist has oddly shaped feet');
  });

});
