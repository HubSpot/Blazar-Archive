jest.dontMock('../../components/shared/Sha.jsx');
jest.dontMock('../../components/shared/Copyable.jsx');
jest.dontMock('../../components/shared/Icon.jsx');

import React from 'react/addons';
import Sha from '../../components/shared/Sha.jsx';
// import Copyable from '../../components/shared/Copyable.jsx';
// import Icon from '../../components/shared/Icon.jsx';

const TestUtils = React.addons.TestUtils;

function renderedOutput(elt) {
  const shallowRenderer = TestUtils.createRenderer();
  shallowRenderer.render(elt);
  return shallowRenderer.getRenderOutput();
}

describe('<Sha />', () => {

  let sha, gitInfo, build;

  beforeEach(() => {
    gitInfo = {
      active: true,
      branch: 'master',
      host: 'github.com',
      id: 1234,
      organization: 'HubSpot',
      repository: 'Blazar',
      repositoryId: 456
    };

    build = {
      buildLink: '/builds/github.com/HubSpot/Blazar/Master/BlazarData/1',
      buildNumber: 1,
      id: 9494,
      moduleId: 3434,
      sha: 'fljdksfsdkfj3r3fwef',
      startTimestamp: 1439839686884,
      state: 'LAUNCHING'
    };

    sha = renderedOutput(<Sha gitInfo={gitInfo} build={build}></Sha>);

  });

  it('should have the right tag', () => {
    expect(sha.type).toBe('span');
  });


  // TO DO: Get <a /> link to render properly
  // ========================================
  // it('should render all the children', () => {
  //
  //   expect(sha.props.children).toEqual([
  //     <Copyable text='fljdksfsdkfj3r3fwef'>
  //       <Icon type='octicon' classNames='icon-roomy fa-link' name='clippy' />
  //     </Copyable>,
  //     <a href='https://github.com/HubSpot/Blazar/commit/fljdksfsdkfj3r3fwef/' target="_blank">fljdksfs</a>
  //   ]);
  //
  // });

});
