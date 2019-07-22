<template>
  <div class="panel panel-default enki-bg">
    <h3 class="enki-header">My privacy dashboard</h3>
    <div class="enki-bottom" v-if="shareData.length > 0 || metaData.length > 0">
    <strong>Who has access to my personal information, for what purpose? Has it been shared?</strong></div>

    <div class="alert alert-danger enki-top-bottom" v-else><strong>No data available, make sure you are logged in and your internet connection is working.</strong></div>
    <div v-if="shareData.length > 0 || metaData.length > 0">
      <h4>What does
      <select v-model="selectedBank" @change="selectedBankChanged" id="bank-dropdown">
        <option disabled value="">this organisation</option>
        <option v-for="bank in banks">{{ bank }}</option>
      </select>
      know about me?</h4>
      <vue-good-table :columns="byBankColumns" :rows="byBankData" :paginate="true" :lineNumbers="true" styleClass="table table-bordered" id="bank-table">
        <template slot="table-column" slot-scope="props">
            <span v-if="props.column.field =='piitype'">
                <i class="fa fa-address-card"></i> {{props.column.label}}
            </span>
            <span v-else-if="props.column.field == 'start_date'">
                <i class="fa fa-calendar"></i> {{props.column.label}}
            </span>
            <span v-else-if="props.column.field == 'end_date'">
                <i class="fa fa-calendar"></i> {{props.column.label}}
            </span>
            <span v-else>
                {{props.column.label}}
            </span>
          </template>
          <template slot="table-row-after" slot-scope="props">
            <td>
                <a v-if="props.row.name == 'bank-a'" :href="bankUrl + '/#/profile-view'" class="bank-a">Change {{ props.row.piitype }} at {{props.row.name}} <i class="fa fa-external-link"></i></a>
                <a v-else-if="props.row.name == 'bank-b'" :href="bankUrl + '/#/profile-view'"  class="bank-b">Change {{ props.row.piitype }} {{props.row.name}} <i class="fa fa-external-link"></i></a>
                <a v-else href="#"> What {{props.row.name}}</a>

              <button class="btn pull-right" @click="deleteRow(props)"><i class="fa fa-trash"></i></button>
            </td>
          </template>
      </vue-good-table>

      <h4>Who knows my
      <select v-model="selectedPIIType" @change="selectedPIITypeChanged" id="pii-type-dropdown">
        <option disabled value="">details</option>
        <option v-for="piiType in piiTypes">{{ piiType }}</option>
      </select>
      ?</h4>
      <vue-good-table :columns="byPIITypeColumns" :rows="byPIITypeData" :paginate="true" :lineNumbers="true" styleClass="table table-bordered" id="pii-type-table">
        <template slot="table-column" slot-scope="props">
            <span v-if="props.column.field =='name'">
                <i class="fa fa-user"></i> {{props.column.label}}
            </span>
            <span v-else-if="props.column.field == 'start_date'">
                <i class="fa fa-calendar"></i> {{props.column.label}}
            </span>
            <span v-else-if="props.column.field == 'end_date'">
                <i class="fa fa-calendar"></i> {{props.column.label}}
            </span>
            <span v-else>
                {{props.column.label}}
            </span>
          </template>
          <template slot="table-row-after" slot-scope="props">
            <td>
                <a v-if="props.row.name == 'bank-a'" :href="bankUrl + '/#/profile-view'" class="bank-a">Change {{ props.row.piitype }} at {{props.row.name}} <i class="fa fa-external-link"></i></a>
                <a v-else-if="props.row.name == 'bank-b'" :href="bankUrl + '/#/profile-view'"  class="bank-b">Change {{ props.row.piitype }} {{props.row.name}} <i class="fa fa-external-link"></i></a>
                <a v-else href="#"> What {{props.row.name}}</a>

              <button class="btn pull-right" @click="deleteRow(props)"><i class="fa fa-trash"></i></button>
            </td>
          </template>
          <template slot="table-row" slot-scope="props">
            <span v-if="props.column.field == 'name'">
               <span v-if="props.row.name == 'bank-a'" class="bank-a">{{props.row.name}}</span>
               <span v-else-if="props.row.name == 'bank-b'" class="bank-b">{{props.row.name}}</span>
               <span v-else>{{props.row.name}}</span>
            </span>
            <span v-else-if="props.column.field == 'piitype' || props.column.field == 'purpose'">
              {{props.row[props.column.field]}}
            </span>
            <span v-else>
              {{props.formattedRow[props.column.field]}}
            </span>
          </template>
      </vue-good-table>
      <div style="clear:both"></div>
    </div>
    <div class="enki-top-bottom" v-else>No content available.</div>

      <modal name="modal-delete" classes="modal-outer" :width="620" :pivot-y="0.3" height="auto">
        <form class="form-horizontal modal-dialog modal-delete" v-on:submit.prevent="deleteItem" v-if="editedItem">
          <div class="modal-content">
            <div class="modal-header"><h3>Withdraw permissions</h3></div>
            <div class="modal-body">
              <p>I confirm I would like to stop
                <span v-if="editedItem.name == 'bank-a'" class="bank-a"> {{editedItem.name}}</span>
                <span v-else-if="editedItem.name == 'bank-b'" class="bank-b"> {{editedItem.name}}</span>
                <span v-else> {{editedItem.name}}</span>
                from using my {{editedItem.piitype}}</p>
              <div class="form-group">

                <label for="editNameInput" class="col-sm-6 control-label">Who</label>
                <div class="col-sm-6">
                  <input type="text" id="editNameInput" placeholder="Who" v-focus v-model="editedItem.name" disabled>
                </div>

                <label for="editTypeInput" class="col-sm-6 control-label">Detail</label>
                <div class="col-sm-6">
                  <input type="text" id="editTypeInput" placeholder="Type" v-focus v-model="editedItem.piitype" disabled>
                </div>

                <label for="editPurposeInput" class="col-sm-6 control-label">Purpose</label>
                <div class="col-sm-6">
                  <input type="text" id="editPurposeInput" placeholder="Purpose" v-focus v-model="editedItem.purpose" disabled>
                </div>

                <label for="editStartDateInput" class="col-sm-6 control-label">Since</label>
                <div class="col-sm-6">
                  <datepicker id="editStartDateInput" placeholder="Start Date" v-focus v-model="editedItem.start_date" class="disabled-picker" disabled-picker></datepicker>
                </div>

                <label for="editExpDateInput" class="col-sm-6 control-label">My consent expires on</label>
                <div class="col-sm-6">
                  <datepicker id="editExpDateInput" placeholder="Expiration Date" v-focus v-model="editedItem.end_date" class="disabled-picker" disabled-picker></datepicker>
                </div>
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-default pull-right" @click="hideDeleteModal()">Cancel</button>
              <a v-if="editedItem.name == 'bank-a'" :href="bankUrl + '/#/profile-view'" class="btn btn-danger pull-left">Request withdrawal of permissions</a>
              <a v-else-if="editedItem.name == 'bank-b'" :href="bankUrl + '/#/profile-view'"  class="btn btn-danger pull-left">Request withdrawal of permissions</a>
              <a v-else href="#" class="btn btn-danger pull-left"> Request withdrawal of permissions</a>

            </div>
          </div>
        </form>
      </modal>
  </div>
</template>

<style scoped>
input[type="text"]:disabled {
  opacity: 0.5;
}

.disabled-picker {
  opacity: 0.5 !important;
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
</style>

<!-- The view model goes on the separated file -->
<script src="./profile-view.js">
</script>
