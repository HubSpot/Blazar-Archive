jest.dontMock('../../components/shared/Sha.jsx');
jest.dontMock('../../components/shared/Icon.jsx');

import React from 'react/addons';
import Sha from '../../components/shared/Sha.jsx';

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

});
