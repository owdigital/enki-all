<template>
    <nav class="navbar navbar-expand-lg navbar-light" :class="{ 'navbar-default': hasSecondaryNav, 'has-subnav': hasSecondaryNav }">
        <div class="container-fluid">
            <h1 class="navbar-brand"><a href="#"><span class="enki-logo">Enki</span></a></h1>
            <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
            </button>

            <!-- Collect the nav links, forms, and other content for toggling -->

            <div class="collapse navbar-collapse" id="navbarSupportedContent">
                <ul class="navbar-nav mr-auto">
                    <router-link tag="li" v-for="route in routes" :id="route.name + '-navbar'" :key="route.name" :to="{ name: route.name }" v-if="showRoute(route)" class="nav-item"><a>{{route.linkText}}</a></router-link>
                </ul>

                <div id="whatsthis" class="ml-auto">
                    <user-status-button ref="userStatusButton" :user="currentUser" :isLoggedIn="isLoggedIn"></user-status-button>
                </div>
            </div>
        </div>

        <transition name="nav-slide">
            <div class="navbar-secondary" v-if="hasSecondaryNav">
                <div class="container">
                    <div class="collapse navbar-collapse">
                        <ul class="nav navbar-nav" data-bind="foreach: { data: navActivePrimaryPage().children, as: 'subpage' }">
                            <router-link class="nav-link float-left" tag="li" v-for="route in childRoutes" :key="route.name" v-if="showRoute(route)"
                                         :to="{ name: route.name }"><a>{{route.linkText}}</a></router-link>
                        </ul>
                    </div>
                </div>
            </div>
        </transition>
    </nav>
</template>

<style scoped>
.navbar {
    margin: 0;
    padding: 0;
}
.navbar-nav{
    margin: 0 0 0 0;
}
.nav-item.active a {
    color: #1e8b9a;
    background-color: #89d8e3;
}
.nav-item a {
    margin-top: 5px;
    color: #1e8b9a;
    background-color: #fff;
    border-top-left-radius: 12px;
    border-top-right-radius: 12px;
    display: block;
    padding: 14.5px;
}

.nav-slide-enter {
    height: 0;
}

.nav-slide-enter-active {
    animation: slideIn 500ms;
}

.nav-slide-leave-active {
    animation: slideOut 500ms;
}
</style>

<script src="./ow-navbar.js"></script>
