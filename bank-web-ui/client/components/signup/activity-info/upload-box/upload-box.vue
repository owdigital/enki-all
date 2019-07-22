<template>
    <div class="upload-box">
        <form enctype="multipart/form-data" novalidate v-if="isInitial || isSaving || isAdded">
            <div class="dropbox">
                <input type="file" multiple :name="uploadFieldName" @change="filesChange($event.target.name, $event.target.files);
                              fileCount = $event.target.files.length" accept="image/*" class="input-file">

                <p v-if="isInitial">
                    Drag your file(s) here or click to browse
                </p>
                <!--ADDED-->
                <div v-if="isAdded">
                  <p>Added {{ addedFiles.length }} file(s) for upload.</p>
                  <ul class="list-unstyled">
                    <li v-for="item in addedFiles">
                      <img :src="item.url" class="img-responsive img-thumbnail" :alt="item.name">
                    </li>
                  </ul>
                </div>

                <p v-if="isSaving">
                    Uploading {{ fileCount }} file(s).
                </p>
            </div>
        </form>

        <!--SUCCESS-->
        <div v-if="isSuccess">
            <p>Uploaded {{ uploadedFiles.length }} file(s) successfully.</p>
        </div>
        <!--FAILED-->
        <div v-if="isFailed">
            <p>Uploaded failed.</p>
            <p>
                <a href="javascript:void(0)" @click="reset()">Try again</a>
            </p>
            <pre>{{ uploadError }}</pre>
        </div>
    </div>
</template>

<style scoped lang="less">
.upload-box {
    width: 80%;
    align-items: center;
}

.dropbox {
    outline: 2px dashed lightgray;
    padding: 10px;
    position: relative;
    margin-top: 45px;
    margin-bottom: 55px;
  
}

.input-file {
    opacity: 0;
    width: 100%;
    height: 100%;
    position: absolute;
    top: 0;
    left: 0;
}

.dropbox:hover {
    background: rgb(241, 241, 241);
}

.dropbox p {
    text-align: center;
}

.img-thumbnail {
    max-width: 90px;
    max-height: 65px;
}

ul {
    list-style-type: none;
    display: flex;
    flex-wrap: wrap;
}

li {
    flex: 0 1 25%;
    margin: 3px;
}

</style>

<script src="./upload-box.js"></script>
