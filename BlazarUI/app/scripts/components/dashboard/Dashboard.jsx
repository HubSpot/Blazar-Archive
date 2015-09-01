import React, {Component, PropTypes} from 'react';
import SectionLoader from '../shared/SectionLoader.jsx';
import DashboardStarredRepo from './DashboardStarredRepo.jsx';
import PageHeader from '../shared/PageHeader.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Headline from '../shared/headline/Headline.jsx';
import MutedMessage from '../shared/MutedMessage.jsx';
import Icon from '../shared/Icon.jsx';
import Helpers from '../ComponentHelpers';

class Dashboard extends Component {

  render() {
    if (this.props.loading) {
      return (
        <SectionLoader />
      );
    }

    let starredRepos = [];
    if (this.props.builds) {
      this.props.builds.grouped.forEach((repo) => {
        if (Helpers.isStarred(this.props.stars, repo.repository, repo.branch)) {
          starredRepos.push(
            <DashboardStarredRepo key={repo.repoModuleKey} repo={repo}></DashboardStarredRepo>
          );
        }
      }.bind(this));
    }

    if (starredRepos.length === 0) {
      starredRepos = (
        <MutedMessage> You have no starred repos. </MutedMessage>
      );
    }

    return (
      <div>
        <PageHeader>
          <Headline>
            <Icon name="tachometer" classNames="headline-icon"></Icon>
            Dashboard
          </Headline>
        </PageHeader>

        <UIGrid>
          <UIGridItem size={12}>
            <h4 className="dashboard__section-title">Starred Repos</h4>
            {starredRepos}
          </UIGridItem>
        </UIGrid>

      </div>
    );
  }
}

Dashboard.propTypes = {
  loading: PropTypes.bool,
  builds: PropTypes.object,
  stars: PropTypes.array.isRequired
};

export default Dashboard;
