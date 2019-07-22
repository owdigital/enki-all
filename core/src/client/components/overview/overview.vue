<template>
    <div class="card bg-light mt-3 ml-3 mr-3">
        <div class="card-body">

            <h2 class="partner-logo mb-3"><span class="bank-a">Bank A</span></h2>

                <div class="row mt-3 mb-3">
                      <highcharts ref="streamchart" :options="chartOpts" style="width: 100%; height: 50%; max-height: 600px;"></highcharts>
                </div>

                <div role="tablist" class="row mt-3">
                  <div class="col-sm-3">
                      <div class="card bg-danger" role="tab" id="forgetTab">
                          <div class="card-body">
                              <p class="card-text ">Right to be forgotten requests</p>
                              <p class="card-text lead">490
                                  <b-btn href="#" v-b-toggle.accordion1  :pressed="true"
                                         class="btn-danger pull-right">Review
                                  </b-btn>
                              </p>
                          </div>
                      </div>
                  </div>
                  <div class="col-sm-3">
                      <div class="card bg-warning" role="tab" id="expiredTab">
                          <div class="card-body">
                              <p class="card-text">Expired consent cases</p>
                              <p class="card-text lead">123
                                  <b-btn href="#" v-b-toggle.accordion2  :pressed="true"
                                         class="btn-warning pull-right">Review
                                  </b-btn>
                              </p>
                          </div>
                      </div>
                  </div>
                  <div class="col-sm-3">
                      <div class="card bg-secondary" role="tab" id="soonTab">
                          <div class="card-body">
                              <p class="card-text ">Consent renewals</p>
                              <p class="card-text lead">453,334
                                  <b-btn href="#" v-b-toggle.accordion3  :pressed="true"
                                         class="btn-secondary pull-right">Review
                                  </b-btn>
                              </p>
                          </div>
                      </div>
                  </div>
                  <div class="col-sm-3">
                      <div class="card bg-info" role="tab" id="sharingTab">
                          <div class="card-body">
                              <p class="card-text">Customers sharing data</p>
                              <p class="card-text lead">7,654,321
                                  <b-btn href="#" v-b-toggle.accordion4  :pressed="true"
                                         class="btn-info pull-right">Explore
                                  </b-btn>
                              </p>
                          </div>
                      </div>
                  </div>
                </div>

                <div class="row mt-3 ">
                  <b-collapse id="accordion1" accordion="my-accordion" role="tabpanel" class="col-sm-12">

                      <div class="card border-danger">
                          <div class="card-header bg-danger text-white">
                          Right to be forgotten requests

                            <div class="form-row col-sm-3 pull-right">
                              <div class="col-sm-6">
                                <b-form-input :id="`type-${type}`" :type="type" value="Search" :pressed="true"></b-form-input>
                              </div>
                              <div class="col-sm-4">
                                <b-btn href="#" class="btn btn-danger" >Search</b-btn>
                              </div>
                            </div>
                          </div>

                          <table class="table table-sm table-hover table-fixed" style="border-collapse:collapse;">
                              <thead>
                              <tr>
                                  <th style="width: 5%">#</th>
                                  <th scope="col" style="width: 10%">Ref</th>
                                  <th scope="col" style="width: 20%">Product</th>
                                  <th scope="col" style="width: 12%">Expiry date</th>
                                  <th scope="col" style="width: 15%">Fields</th>
                                  <th scope="col" style="width: 15%">Area</th>
                                  <th scope="col" style="width: 15%">Details</th>
                              </tr>
                              </thead>
                              <tbody>
                              <tr v-for="(item, index) in ForgetConsent" :key="item.AccountNumber">
                                  <td class="co">{{ index }}</td>
                                  <td>{{item.AccountNumber}}</td>
                                  <td>{{item.Product}}</td>
                                  <td>{{item.ExpiryDate}}</td>
                                  <td>
                                      <b-btn :id="`pop-${index}-${item.AccountNumber}`" variant="danger">
                                          {{item.Fields.length}}
                                      </b-btn>
                                      <b-popover :target="`pop-${index}-${item.AccountNumber}`"
                                                 triggers="hover focus">
                                          <template slot="title">{{item.Fields.length}} fields</template>
                                          <ul class="list-group">
                                              <li class="list-group-item" v-for='fieldname in item.Fields'>
                                                  {{ fieldname }}
                                              </li>
                                          </ul>
                                      </b-popover>
                                  </td>
                                  <td>{{item.Area}}</td>
                                  <td>
                                      <div>
                                          <b-btn v-b-modal="`mod-${index}-${item.AccountNumber}`"
                                                 variant="danger">Details
                                          </b-btn>

                                          <b-modal :id="`mod-${index}-${item.AccountNumber}`" size="lg"
                                            header-class="bg-danger"
                                            footer-class="bg-danger">

                                                <div slot="modal-header" >
                                                  <h5>Consent for {{item.Product}} account {{item.AccountNumber}} expired on {{item.ExpiryDate}}</h5>
                                                </div>

                                                <div class="alert alert-danger" role="alert">
                                                  <strong>PII</strong>
                                                  <ul class="list-inline">
                                                      <li class="list-inline-item" v-for='fieldname in item.Fields'>
                                                          {{ fieldname }}
                                                      </li>
                                                  </ul>
                                                </div>

                                                <table class="table table-bordered">
                                                    <tr v-for="(entry, n) in item.Log">
                                                      <td><strong>{{entry.date}}</strong></td>
                                                      <td>CRM</td>
                                                      <td>{{entry.activity}}</td>
                                                    </tr>
                                                </table>

                                                <form>
                                                  <div class="form-group">
                                                    <label for="exampleInputEmail1">Add log entry</label>
                                                    <input type="email" class="form-control" id="exampleInputEmail1" placeholder="Enter notes">
                                                  </div>
                                                  <div class="form-check">
                                                    <input class="form-check-input" type="checkbox" value="" id="defaultCheck1">
                                                    <label class="form-check-label" for="defaultCheck1">
                                                    Send email to case team
                                                    </label>
                                                    <small class="form-text text-muted">Send prompt </small>
                                                  </div>
                                                  <b-btn class="pull-right btn-danger">Post</b-btn>
                                                </form>

                                               <div slot="modal-footer" class="bg-danger">
                                                 <b-btn class="float-right bg-white" variant="outline-danger" @click="show=false">
                                                   Close
                                                 </b-btn>
                                               </div>
                                            </b-modal>

                                      </div>

                                  </td>
                              </tr>
                              </tbody>
                          </table>
                          <div class="card-footer bg-danger text-white">
                              <b-btn href="#" variant="danger"  :pressed="true"
                                     class="btn-danger pull-right">Export this list to CSV
                              </b-btn>
                          </div>
                      </div>
                  </b-collapse>

                  <b-collapse id="accordion2" accordion="my-accordion" role="tabpanel" class="col-sm-12">

                      <div class="card border-warning">
                          <div class="card-header bg-warning text-white">Expired consent cases

                            <div class="form-row col-sm-3 pull-right">
                              <div class="col-sm-6">
                                <b-form-input :id="`type-${type}`" :type="type" value="Search"></b-form-input>
                              </div>
                              <div class="col-sm-4">
                                <b-btn href="#" class="btn btn-warning"  :pressed="true">Search</b-btn>
                              </div>
                            </div>
                          </div>

                          <table class="table table-sm table-hover table-fixed" style="border-collapse:collapse;">
                              <thead>
                              <tr>
                                  <th style="width: 5%">#</th>
                                  <th scope="col" style="width: 10%">Ref</th>
                                  <th scope="col" style="width: 20%">Product</th>
                                  <th scope="col" style="width: 12%">Expiry date</th>
                                  <th scope="col" style="width: 15%">Fields</th>
                                  <th scope="col" style="width: 15%">Area</th>
                                  <th scope="col" style="width: 15%">Details</th>
                              </tr>
                              </thead>
                              <tbody>
                              <tr v-for="(item, index) in ExpiredConsent" :key="item.AccountNumber">
                                  <td class="co">{{ index }}</td>
                                  <td>{{item.AccountNumber}}</td>
                                  <td>{{item.Product}}</td>
                                  <td>{{item.ExpiryDate}}</td>
                                  <td>
                                      <b-btn :id="`pop-${index}-${item.AccountNumber}`" variant="warning">
                                          {{item.Fields.length}}
                                      </b-btn>
                                      <b-popover :target="`pop-${index}-${item.AccountNumber}`"
                                                 triggers="hover focus">
                                          <template slot="title">{{item.Fields.length}} fields</template>
                                          <ul class="list-group">
                                              <li class="list-group-item" v-for='fieldname in item.Fields'>
                                                  {{ fieldname }}
                                              </li>
                                          </ul>
                                      </b-popover>
                                  </td>
                                  <td>{{item.Area}}</td>
                                  <td>
                                      <div>
                                          <b-btn v-b-modal="`mod-${index}-${item.AccountNumber}`"
                                                 variant="warning">Details
                                          </b-btn>

                                          <b-modal :id="`mod-${index}-${item.AccountNumber}`" size="lg"
                                            header-class="bg-warning"
                                            footer-class="bg-warning">

                                                <div slot="modal-header" >
                                                  <h5>Consent for {{item.Product}} account {{item.AccountNumber}} expired on {{item.ExpiryDate}}</h5>
                                                </div>

                                                <div class="alert alert-warning" role="alert">
                                                  <strong>PII</strong>
                                                  <ul class="list-inline">
                                                      <li class="list-inline-item" v-for='fieldname in item.Fields'>
                                                          {{ fieldname }}
                                                      </li>
                                                  </ul>
                                                </div>

                                                <table class="table table-bordered">
                                                    <tr v-for="(entry, n) in item.Log">
                                                      <td><strong>{{entry.date}}</strong></td>
                                                      <td>CRM</td>
                                                      <td>{{entry.activity}}</td>
                                                    </tr>
                                                </table>

                                                <form>
                                                  <div class="form-group">
                                                    <label for="exampleInputEmail1">Add log entry</label>
                                                    <input type="email" class="form-control" id="exampleInputEmail1" placeholder="Enter notes">
                                                  </div>
                                                  <div class="form-check">
                                                    <input class="form-check-input" type="checkbox" value="" id="defaultCheck1">
                                                    <label class="form-check-label" for="defaultCheck1">
                                                    Send email to case team
                                                    </label>
                                                    <small class="form-text text-muted">Send prompt </small>
                                                  </div>
                                                  <b-btn class="pull-right btn-warning">Post</b-btn>
                                                </form>

                                               <div slot="modal-footer" class="bg-warning">
                                                 <b-btn class="float-right bg-white" variant="outline-warning" @click="show=false">
                                                   Close
                                                 </b-btn>
                                               </div>
                                            </b-modal>

                                      </div>
                                  </td>
                              </tr>
                              </tbody>
                          </table>

                          <div class="card-footer bg-warning text-white">
                              <b-btn href="#"  variant="warning" :pressed="true"
                                     class="btn-warning pull-right">Export this list to CSV
                              </b-btn>
                          </div>
                      </div>
                  </b-collapse>

                  <b-collapse id="accordion3" accordion="my-accordion" role="tabpanel" class="col-sm-12">

                      <div class="card border-secondary">
                          <div class="card-header bg-secondary text-white">Consent renewals

                            <div class="form-row col-sm-3 pull-right">
                              <div class="col-sm-6">
                                <b-form-input :id="`type-${type}`" :type="type" value="Search"></b-form-input>
                              </div>
                              <div class="col-sm-4">
                                <b-btn href="#" class="btn btn-secondary"  :pressed="true">Search</b-btn>
                              </div>
                            </div>

                          </div>
                          <table class="table table-sm table-hover table-fixed" style="border-collapse:collapse;">
                              <thead>
                              <tr>
                                  <th style="width: 5%">#</th>
                                  <th scope="col" style="width: 10%">Ref</th>
                                  <th scope="col" style="width: 20%">Product</th>
                                  <th scope="col" style="width: 12%">Expiry date</th>
                                  <th scope="col" style="width: 15%">Fields</th>
                                  <th scope="col" style="width: 15%">Area</th>
                                  <th scope="col" style="width: 15%">Details</th>
                              </tr>
                              </thead>
                              <tbody>
                              <tr v-for="(item, index) in SoonConsent" :key="item.AccountNumber">
                                  <td class="co">{{ index }}</td>
                                  <td>{{item.AccountNumber}}</td>
                                  <td>{{item.Product}}</td>
                                  <td>{{item.ExpiryDate}}</td>
                                  <td>

                                      <b-btn :id="`pop-${index}-${item.AccountNumber}`" variant="secondary">
                                          {{item.Fields.length}}
                                      </b-btn>
                                      <b-popover :target="`pop-${index}-${item.AccountNumber}`"
                                                 triggers="hover focus">
                                          <template slot="title">{{item.Fields.length}} fields</template>
                                          <ul class="list-group">
                                              <li class="list-group-item" v-for='fieldname in item.Fields'>
                                                  {{ fieldname }}
                                              </li>
                                          </ul>
                                      </b-popover>
                                  </td>
                                  <td>{{item.Area}}</td>
                                  <td>
                                      <div>
                                          <b-btn v-b-modal="`mod-${index}-${item.AccountNumber}`"
                                                 variant="secondary">Details
                                          </b-btn>

                                          <b-modal :id="`mod-${index}-${item.AccountNumber}`" size="lg"
                                            header-class="bg-secondary"
                                            footer-class="bg-secondary">

                                                <div slot="modal-header" >
                                                  <h5>Consent for {{item.Product}} account {{item.AccountNumber}} expired on {{item.ExpiryDate}}</h5>
                                                </div>

                                                <div class="alert alert-secondary" role="alert">
                                                  <strong>PII</strong>
                                                  <ul class="list-inline">
                                                      <li class="list-inline-item" v-for='fieldname in item.Fields'>
                                                          {{ fieldname }}
                                                      </li>
                                                  </ul>
                                                </div>

                                                <table class="table table-bordered">
                                                    <tr v-for="(entry, n) in item.Log">
                                                      <td><strong>{{entry.date}}</strong></td>
                                                      <td>CRM</td>
                                                      <td>{{entry.activity}}</td>
                                                    </tr>
                                                </table>

                                                <form>
                                                  <div class="form-group">
                                                    <label for="exampleInputEmail1">Add log entry</label>
                                                    <input type="email" class="form-control" id="exampleInputEmail1" placeholder="Enter notes">
                                                  </div>
                                                  <div class="form-check">
                                                    <input class="form-check-input" type="checkbox" value="" id="defaultCheck1">
                                                    <label class="form-check-label" for="defaultCheck1">
                                                    Send email to case team
                                                    </label>
                                                    <small class="form-text text-muted">Send prompt </small>
                                                  </div>
                                                  <b-btn class="pull-right btn-secondary">Post</b-btn>
                                                </form>

                                               <div slot="modal-footer" class="bg-danger">
                                                 <b-btn class="float-right bg-white" variant="outline-secondary" @click="show=false">
                                                   Close
                                                 </b-btn>
                                               </div>
                                            </b-modal>

                                      </div>

                                  </td>
                              </tr>
                              </tbody>
                          </table>

                          <div class="card-footer bg-secondary text-white">
                              <b-btn href="#" variant="secondary"
                                     class="btn-secondary pull-right" :pressed="true">Export this list to CSV
                              </b-btn>
                          </div>
                      </div>
                  </b-collapse>

                  <b-collapse id="accordion4" accordion="my-accordion" role="tabpanel" class="col-sm-12">

                      <div class="card border-info">
                          <div class="card-header bg-info text-white">Explore customers' consents</div>
                          <div class="card-body">
                              <b-form-input v-model="searchText"
                                            type="text"
                                            placeholder="Search by name, email address, account number.."></b-form-input>

                              <b-button type="submit" variant="outline-info">Search</b-button>
                          </div>

                          <div class="card-footer bg-info text-white">
                              <b-btn href="#" v-b-toggle.accordion4 variant="info" :pressed="true"
                                     class="btn-info pull-right">Export this list to CSV
                              </b-btn>
                          </div>
                      </div>
                  </b-collapse>
                </div>

            </div>
        </div>
    </div>
</template>

<style scoped>

    .hidden {
        display: none;
    }

    input[type="text"]:disabled {
        opacity: 0.5;
    }

    .list-group-item.active {
        background: #f7f7f7;
        border-color: #f7f7f7;
        color: black;
    }

    .enki-bottom {
        margin-bottom: 20px;
    }

    .enki-top-bottom {
        margin-top: 20px;
        margin-bottom: 20px;
    }

    .panel-heading {
        border-color: transparent;
        border-radius: 0px;
    }

    .enki-line {
        background: #d4d4d4;
        height: 2px;
    }

    div.content {
        all: revert;
    }

    .popover-body {
        padding: 0 !important;
    }

    .popover-body li {
        border-left: 0;
        border-right: 0;
    }


</style>

<!-- The view model goes on the separated file -->
<script src="./overview.js">
</script>
