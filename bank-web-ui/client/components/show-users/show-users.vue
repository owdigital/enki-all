<template>
  <!-- Component template content goes here -->
  <div class="container show-users">
  <div class="row"
    <h3>Bank users list</h3>


    <div class="col-sm-6">
        <div class="panel panel-default">

          <div class="panel-body">
          <div class="row">
            <form class="form-inline add-item-form" v-on:submit.prevent="createItem">
              <div class="form-group form-group-lg">
                <input type="text" class="form-control" placeholder="New user's name" v-focus v-model="newItem.username" required>
              </div>
              <button type="submit" class="btn btn-lg btn-primary"><span class="glyphicon glyphicon-plus"></span> Add user</button>
            </form>
            </div>
          </div>

        </div>
      </div>

      <div class="col-sm-6">
        <div class="panel panel-default panel-table">

          <div class="panel-heading">Existing users</div>
          <div class="panel-body">
            <table v-if="items.length" class="table table-hover table-striped items-table">
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Id</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in items">
                  <td class="text-left">{{item.username}}</td>
                  <td class="text-left">{{item.id}}</td>
                  <td class="text-right">
                    <button class="btn btn-lg btn-primary" v-on:click="showEditModal(item)"><span class="glyphicon glyphicon-edit"></span> Edit</button>
                    <button class="btn btn-lg btn-primary" v-on:click="deleteItem(item)"><span class="glyphicon glyphicon-trash"></span> Delete</button>
                  </td>
              </tr>
              </tbody>
            </table>
            <p class="lead" v-else>There are no users yet, try adding one</p>
          </div>

        </div>
      </div>


      <modal name="modal-edit" classes="modal-outer" :width="620" :adaptive="true">
        <form class="form-horizontal modal-dialog modal-edit" v-on:submit.prevent="editItem" v-if="editedItem">
          <div class="modal-content">
            <div class="modal-header"><h3>Edit user</h3></div>
            <div class="modal-body">
              <p>Use this modal to edit the user with id: <strong>{{editedItem.id}}</strong>.</p>
              <div class="form-group">

                <label for="editNameInput" class="col-sm-3 control-label">Username</label>
                <div class="col-sm-9">
                  <input type="text" class="form-control" id="editNameInput" placeholder="User Name" v-focus v-model="editedItem.username" required>
                </div>
              </div>

              <div class="form-group">
                <label for="editFirstNameInput" class="col-sm-3 control-label">First Name</label>
                <div class="col-sm-9">
                  <input type="text" class="form-control" id="editFirstNameInput" placeholder="First Name" v-model="editedItem.piiData.firstName">
                </div>
              </div>

              <div class="form-group">
                <label for="editLastNameInput" class="col-sm-3 control-label">Last Name</label>
                <div class="col-sm-9">
                  <input type="text" class="form-control" id="editLastNameInput" placeholder="Last Name" v-model="editedItem.piiData.lastName">
                </div>
              </div>

              <div class="form-group">
                <label for="editPasswordInput" class="col-sm-3 control-label">Password</label>
                <div class="col-sm-9">
                  <input type="password" class="form-control" id="editPasswordInput" autocomplete="new-password" v-model="editedItem.password">
                </div>
              </div>
            </div>

            <div class="modal-footer text-right">
              <button type="button" class="btn btn-lg btn-default" @click="hideEditModal()">Cancel</button>
              <button type="submit" class="btn btn-lg btn-primary">Save</button>
            </div>
          </div>
        </form>
      </modal>

    </div>
  </div>
</template>

<style scoped lang="less">
  // your less rules go here. They are scoped to this particular component
  .features-container{
    margin: 0 auto;
    ul{
      display:inline-block
    }
  }
  .panel-table .panel-body {
    padding: 0;
  }
</style>

<!-- The view model goes on the separated file -->
<script src="./show-users.js"></script>
