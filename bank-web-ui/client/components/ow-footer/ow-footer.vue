<template>
  <div>
    <footer class="site-footer">
      <div class="container">
        <div class="row">
          <div class="col-md-3 col-sm-6 col-xs-12 fbox">
            <h4><span class="glyphicon glyphicon-piggy-bank"></span> {{$store.getters.config.bankName}}</h4>
            <p class="text enki-logo">In partnership with <a href="/#/about-enki">Enki</a></p>

          </div>
        </div>
      </div>

      <div id="copyright">
        <div class="container">
          <div class="row">
            <div class="col-md-4">
              <p class="pull-left">&copy; 2017 <span class="glyphicon glyphicon-piggy-bank"></span> {{$store.getters.config.bankName}}</p>
            </div>
            <div class="col-md-8">

            </div>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script src="./ow-footer.js"></script>
