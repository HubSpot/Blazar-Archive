import React, {Component, PropTypes} from 'react';
import SectionLoader from '../shared/SectionLoader.jsx';
import StarredProvider from '../StarredProvider';
import DashboardStarredRepo from './DashboardStarredRepo.jsx';
import PageHeader from '../shared/PageHeader.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Headline from '../shared/headline/Headline.jsx';
import Icon from '../shared/Icon.jsx';

class Dashboard extends Component {

  render() {
    if (this.props.loading) {
      return (
        <SectionLoader />
      );
    }

    let starredRepos = [];
    if (this.props.builds) {
      this.props.builds.grouped.forEach(function(repo) {
        if (StarredProvider.hasStar({ repo: repo.repository, branch: repo.branch }) !== -1) {
          starredRepos.push(
            <DashboardStarredRepo key={repo.repository} repo={repo}></DashboardStarredRepo>
          );
        }
      });
    }

    if (starredRepos.length === 0) {
      starredRepos = (<span className="dashboard__placeholder">You have no starred repos.</span>);
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
  builds: PropTypes.object
};

export default Dashboard;
