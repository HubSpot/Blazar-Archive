import Reflux from 'reflux';
import SidebarFilterActions from '../actions/sidebarFilterActions';

const SidebarFilterStore = Reflux.createStore({

  listenables: SidebarFilterActions,

  init() {
    this.searchValue = '';
  },
  
  clearSearchValue() {
    this.searchValue = '';

    this.trigger({
      searchValue: this.searchValue
    });
  }

});

export default SidebarFilterStore;
