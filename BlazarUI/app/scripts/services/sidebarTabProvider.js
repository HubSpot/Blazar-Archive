import store from 'store';

const sidebarTabProvider = {

  haveSynced: false,
  tab: 'favorites',

  checkStorage: function() {
    if (!this.haveSynced) {
      this.getSidebarTab();
      this.haveSynced = true;
    }
  },

  changeTab: function(tab) {
    this.checkStorage();
    store.set('sidebarTab', tab);    
  },


  getSidebarTab: function() {
    if (this.haveSynced) {
      return this.tab;
    }

    this.haveSynced = true;
    return this.tab = store.get('sidebarTab') || 'starred';
  }

};

export default sidebarTabProvider;
