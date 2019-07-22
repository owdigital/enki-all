<template>
    <transition name="sidebar-slide">
        <div class="sidebar sb-left sb-collapsible" :class="{'expanded': expanded, 'collapsed': !expanded}">
            <div class="sidebar-inner">

                <div class="sidebar-heading" @click="toggleCollapsed">
                    <span class="sb-title">Filters</span>
                    <span class="sb-toggle fa fa-2x pull-right" :class="{'fa-angle-left': expanded, 'fa-angle-right': !expanded}"></span>
                </div>

                <div class="sidebar-content">
                    <div class="panelSectionHeader">
                        <h5>Sidebar Content Goes Here</h5>
                    </div>
                </div>

            </div>
        </div>
    </transition>
</template>

<style scoped>
    .sidebar {
        transition: left 500ms;
    }

    .expanded {
        left: 0;
    }

    .collapsed {
        left: -195px;
    }
</style>

<script src="./ow-sidebar.js"></script>