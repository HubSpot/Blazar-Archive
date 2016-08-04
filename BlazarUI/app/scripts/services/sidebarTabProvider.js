import store from 'store';

const sidebarTabProvider = {

  haveSynced: false,
  tab: 'favorites',

  checkStorage() {
    if (!this.haveSynced) {
      this.getSidebarTab();
      this.haveSynced = true;
    }
  },

  changeTab(tab) {
    this.checkStorage();
    store.set('sidebarTab', tab);
  },


  getSidebarTab() {
    if (this.haveSynced) {
      return this.tab;
    }

    this.haveSynced = true;
    this.tab = store.get('sidebarTab') || 'starred';
    return this.tab;
  }

};

export default sidebarTabProvider;
