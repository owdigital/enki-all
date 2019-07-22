/*
   This file is part of Enki.
  
   Copyright © 2016 - 2019 Oliver Wyman Ltd.
  
   Enki is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
  
   Enki is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
  
   You should have received a copy of the GNU General Public License
   along with Enki.  If not, see <https://www.gnu.org/licenses/>
*/

import _ from 'lodash';

export default {
  name: 'page-header',

  data() {
    return {
      currentUser: {},
      isLoggedIn: false,
      isAdmin: false
    };
  },

  created () {
    setTimeout(() => {
      this.isLoggedIn = window.app.context.loggedIn;
      this.isAdmin = window.app.context.isAdmin;
      this.currentUser = window.app.context.currentUser;
    }, 100);
  },
  methods: {
    showRoute: function(route) {
      return !route.omitFromNav &&
        (!route.needsLogin || this.isLoggedIn) &&
        (!route.needsAdmin || this.isAdmin) &&
        (!route.notAdmin || !this.isAdmin);
    }
  },
  computed: {
    routes () {
      return this.$router.options.routes
        .filter(this.showRoute)
        .sort((a, b) => {
          if (isFinite(a.index) && isFinite(b.index)) return a.index - b.index;
          else if (isFinite(a.index)) return -1;
          else if (isFinite(b.index)) return 1;
          else return a.name > b.name ? -1 : 0;
        });
    },

    topLevelRoute () {
      // This can probably be done better
      const topLevelNavPath = _.get(this, '$route.matched.0.path');
      return _.find(this.routes, { path: topLevelNavPath }) || {};
    },

    childRoutes () {
      return _.get(this.topLevelRoute, 'children', []);
    },

    hasSecondaryNav () {
      return _.find(this.childRoutes, this.showRoute) !== undefined;
    }
  }
};
