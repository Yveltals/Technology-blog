<template>
<div style="min-height: 870px;">
  <div class="pagebg land"></div>
    <div style="width: 80%" class="container">
    <h1 class="t_nav">
      <a href="/" class="n1">网站首页</a>
      <a href="javascript:void(0);" @click="back()" class="n2">文件资源</a>
    </h1>
    <div class="infosbox">
      <el-table :data="files" >
        <el-table-column label="文件名" width="200">
          <template slot-scope="scope">
              <span style="margin-left: 10px">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="120">
          <template slot-scope="scope">
                  <el-tag type="info" size="medium">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="大小" width="120">
          <template slot-scope="scope">
              <span style="margin-left: 10px">{{ scope.row.size }}</span>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" width="200">
          <template slot-scope="scope">
              <i class="el-icon-time"></i>
              <span style="margin-left: 10px">{{ scope.row.uploadTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template slot-scope="scope">
              <el-button size="mini" type="primary" 
                  @click="DownloadFile(scope.row.id,scope.row.name,scope.row.size)">下载</el-button>
              <el-button size="mini" type="danger" 
                  @click="DeleteFile(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div style="width: 32%;overflow: hidden; float: right">
      <el-upload
          drag    action="123"
          :show-file-list="false"
          :on-change="fileChange"
          :http-request="SubbmitFile"
          >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <div class="el-upload__tip" slot="tip">上传文件不超过50Mb</div>
      </el-upload>
      <br>
      <!-- 上传下载进度-->
      <div style="width:80%;margin-left:10%">
        <el-progress  v-if="upShow==true" :text-inside="true" :stroke-width="26" :percentage="(uploadRate*100).toFixed(2)"></el-progress>
        <el-progress v-if="downShow==true" status="success" :text-inside="true" :stroke-width="26" :percentage="(downloadRate*100).toFixed(2)"></el-progress>
      </div>
    </div>
  </div>
</div>
</template>

<script>
    import axios from 'axios'
    import user from '@/api/user'
    import store from '@/store/store'
    import qs from 'qs';
    var token =  localStorage.getItem('token')
    
    export default {
    name: 'file',
    data() {
        return {
            files:[{id:0, name:'',size:0, type:'',uploadTime:''}],
            fileList: [],
            file: {},
            uploadRate: 0,
            downloadRate: 0,
            uploadStyle: {width: '0%'},
            downloadStyle: {width: '0%'},
            upShow: false,
            downShow: false
      }
    },
    created() {
      this.load();
    },
    methods: {
        fileChange(e,list){
            this.file=e;
        },
        SubbmitFile(){
            this.upShow = true
            let param = new FormData(); 
            var vm = this;
            param.append("file", this.file.raw);
            let config = {
              headers: { 'Content-Type': 'multipart/form-data' },
              onUploadProgress(e) {
                if(e.lengthComputable) { //gzip导致文件大小差异，防止进度条超界
                  var rate = vm.uploadRate = e.loaded / e.total;
                  if (rate < 1) {
                    // console.log(rate)
                    vm.uploadRate = rate;
                    vm.uploadStyle.width = (rate *100).toFixed(2)+ '%';
                  }
                }
              }
            }
            config.headers['Authorization'] = store.state.token
            console.log(param)
            
            axios.post("/api/file/upload/", param, config,{timeout:900000})
            .then(response => {
              if (response) { //返回时置进度条100%
                vm.uploadRate = 1;
                vm.uploadStyle.width = '100.00%';
                this.load()
                this.file={}
                this.upShow = false
                console.log(response.data);
              } else {
                alert(response.data.msg);
              }
            })
            .catch(function (err) {
              console.log(err);
            });
        },
        load() {
            user.getFile().then(res => {
                console.log(res.data);
                console.info("-------reload-------");
                this.files = res.data;
                this.uploadRate = 0,
                this.uploadStyle.width = '0%'
                this.downloadRate = 0,
                this.downloadStyle.width = '0%'
            })
        },
        DeleteFile(id) {
            this.$confirm('确认删除文件？','提示',{
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(()=>{
                user.fileDelete(id)
                .then(res =>{
                    this.load()
                    console.log("Deleted.")
                })
                .catch(() => {
                    this.$notify({title: '提示',type: 'error',message: '删除失败，请联系管理员',duration: 3000 });
                });
            }).catch(()=>{})
        },
        DownloadFile(id,name,size) {
            // console.log("开始下载文件id："+ id)
            this.downShow = true
            var vm=this
            user.fileDownload(id,vm,size)
            .then(res => {
                console.info("接收文件流完毕 开始下载...")
                console.log(res)
                if (res) {
                    vm.downloadRate = 1;
                    vm.downloadStyle.width = '100.00%';
                    this.load()
                    this.downShow = false
                    const content = res;
                    const blob = new Blob([content]);
                    const fileName = name;
                    if ("download" in document.createElement("a")) {
                        // 非IE下载
                        const elink = document.createElement("a");
                        elink.href = URL.createObjectURL(blob);
                        elink.download = fileName;
                        elink.style.display = "none";
                        document.body.appendChild(elink);
                        elink.click();
                        URL.revokeObjectURL(elink.href); // 释放URL 对象
                        document.body.removeChild(elink);
                    } else {// IE10+下载
                        navigator.msSaveBlob(blob, fileName);
                    }
                }
            })
            .catch(() => {
                this.$notify({title: '提示',type: 'error',message: '下载失败，请联系管理员',duration: 3000 });
            });
        },
    },
  }
</script>

<style scoped>
.upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    margin: 0, 0, 0, 10px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
  }
.progress-wrapper { position: relative; height: 50px; border-radius: 5px; background-color: lightgrey; }
.progress-wrapper .progress-progress { position: absolute; left: 0; top: 0; height: 100%; width: 0%; border-radius: 5px; background-color: darkturquoise; z-index: 1; }
.progress-wrapper .progress-rate { position: relative; text-align: center; font-size: 14px; line-height: 50px; }
</style>
