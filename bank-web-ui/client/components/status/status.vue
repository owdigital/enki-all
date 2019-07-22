<template>
    <div class="container-fluid">
        <div class="row">
            <h3>Build details</h3>
            <table class="table table-responsive table-striped table-bordered">
                <tbody>
                <tr>
                    <th class="col-xs-2">Git revision</th>
                    <td class="col-xs-10">{{revision}}</td>
                </tr>
                <tr>
                    <th>Build time</th>
                    <td>{{buildTime}}</td>
                </tr>
                <tr>
                    <th>Build number</th>
                    <td>{{buildNumber}}</td>
                </tr>
                <tr>
                    <th>Startup time</th>
                    <td>{{startupTime}}</td>
                </tr>
                <tr>
                    <th>CWD (Current Working Dir)</th>
                    <td>{{cwd}}</td>
                </tr>
                </tbody>
            </table>
        </div>

        <div class="row">
            <h3>Integrations</h3>
            <table class="table table-responsive table-striped table-bordered">
                <tbody>
                <tr v-for="check in statusChecks">
                    <th class="col-xs-2">{{check.basename}}</th>
                    <td class="col-xs-2">{{check.status}}</td>
                    <td class="col-xs-8">{{check.description}}</td>
                </tr>
                </tbody>
            </table>
        </div>

        <div class="row">
            <h3>Active Configuration</h3>
            <h6>*Any relative path/dir is relative to CWD</h6>
            <table class="table table-responsive table-striped table-bordered">
                <tbody>
                <tr v-for="conf in config">
                    <th class="col-xs-2">{{conf.key}}</th>
                    <td class="col-xs-10">{{conf.value}}</td>
                </tr>
                </tbody>
            </table>
        </div>

        <div class="row">
            <h3>Dependencies</h3>
            <div class="row">
                <div class="col-sm-12">
                    <h4>Server-side</h4>
                    <table class="table table-responsive table-striped table-bordered">
                        <thead>
                        <tr>
                            <th class="col-xs-4">Name</th>
                            <th class="col-xs-4">Configured version</th>
                            <th class="col-xs-4">Installed version</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr v-for="dep in serverDependencies">
                            <th>{{dep.depname}}</th>
                            <td>{{dep.configuredVersion}}</td>
                            <td>{{dep.installedVersion}}</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</template>

<script src="./status.js"></script>

