<template>
  <div class="main-content">
    <div style="height: 1px"></div>
    <div style="width: 70%; background-color: white; margin: 30px auto; border-radius: 20px">
      <div style="padding-bottom: 10px">
        <div style="display: flex; font-size: 18px; color: #000000FF; line-height: 80px; border-bottom: #cccccc 1px solid;">
          <div style="margin-left: 20px; flex: 1">全部商品（{{ goodsData.length }}件）</div>
          <div style="flex: 2; text-align: right">
            <el-select v-model="addressId" placeholder="请选择收货地址" style="width: 70%">
              <el-option v-for="item in addressData" :label="item.username + ' - ' + item.useraddress + ' - ' + item.phone" :value="item.id"></el-option>
            </el-select>
          </div>
          <div style="flex: 1; font-size: 16px; text-align: right; padding-right: 20px">
            已选商品 ￥ {{totalPrice}} <el-button type="danger" round @click="showConfirmCodeDialog()">下单</el-button>

            <el-dialog title="剩余支付时间（超时自动关闭）" :visible.sync="dialogVisible" center width="25%" >
              <div style="display: flex;flex-direction: column;align-items: center;justify-content: center;">
                <div class="time"><i class="el-icon-time"></i> {{min}}分钟 {{sec}}秒 </div>
                <img src="@/assets/imgs/duolaAmeng.gif" alt=" ">
              </div>
              
              <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消支付</el-button>
        <el-button type="primary" @click="confirmDelete()">确定支付</el-button>
      </span>
            </el-dialog>


          </div>
        </div>
        <div style="margin: 20px 0; padding: 0 50px">
          <div class="table">
            <el-table :data="goodsData" strip @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="55" align="center"></el-table-column>
              <el-table-column label="商品图片" width="120px">
                <template v-slot="scope">
                  <el-image style="width: 80px; height: 60px; border-radius: 3px" v-if="scope.row.goodsImg"
                            :src="scope.row.goodsImg" :preview-src-list="[scope.row.goodsImg]"></el-image>
                </template>
              </el-table-column>
              <el-table-column prop="goodsName" label="商品名称" width="240px">
                <template v-slot="scope">
                  <a :href="'/front/detail?id=' + scope.row.goodsId">{{scope.row.goodsName}}</a>
                </template>
              </el-table-column>
              <el-table-column prop="businessName" label="店铺名称">
                <template v-slot="scope">
                  <a :href="'/front/business?id=' + scope.row.businessId">{{scope.row.businessName}}</a>
                </template>
              </el-table-column>
              <el-table-column prop="goodsPrice" label="商品价格"></el-table-column>
              <el-table-column prop="num" label="选择数量">
                <template v-slot="scope">
                  <el-input-number v-model="scope.row.num" style="width: 100px" @change="handleChange(scope.row)" :min="1"></el-input-number>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="180">
                <template v-slot="scope">
                  <el-button size="mini" type="danger" plain @click="del(scope.row.id)">移除购物车</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination" style="margin-top: 20px">
              <el-pagination
                  background
                  @current-change="handleCurrentChange"
                  :current-page="pageNum"
                  :page-sizes="[5, 10, 20]"
                  :page-size="pageSize"
                  layout="total, prev, pager, next"
                  :total="total">
              </el-pagination>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

export default {

  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      goodsData: [],
      pageNum: 1,   // 当前的页码
      pageSize: 10,  // 每页显示的个数
      totalPrice: 0,
      total: 0,
      addressId: null,
      addressData: [],
      selectedData: [],
      dialogVisible: false,
      countdownTimer: null, // 添加一个计时器变量

      min:'',
      sec:''
    }
  },
  mounted() {
    this.loadGoods(1)
    this.loadAddress()
  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    //弹窗输入对话框
    showConfirmCodeDialog() {
      this.min = '';
      this.sec = '';
      this.clearCountdownTimer(); // 清除旧的计时器

      this.dialogVisible = true; // 显示弹窗
      this.countdown()

    },
    //自动取消
    auto_cancellation(){
      this.clearCountdownTimer(); // 清除计时器
      this.dialogVisible=false
    },
    //确定
    confirmDelete() {
      this.clearCountdownTimer(); // 清除计时器
      this.dialogVisible=false
      this.pay()

    },
    //清除计数器
    clearCountdownTimer() {
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer); // 清除计时器
        this.countdownTimer = null; // 将计时器变量置为 null
      }
    },
    //倒计时
    countdown() {
      const end = Date.now() + (15 * 60 * 1000); // 当前时间加上十五分钟的毫秒数
      const that = this;

      function updateTimer() {
        const now = Date.now();
        const msec = end - now;

        if (msec < 0) return;

        const min = Math.floor(msec / 1000 / 60);
        const sec = Math.floor(msec / 1000 % 60);

        that.min = min > 9 ? min : '0' + min;
        that.sec = sec > 9 ? sec : '0' + sec;

        if (min === 0 && sec === 0) {
          // 倒计时结束执行操作
          that.auto_cancellation();
          return;
        }

      }
      updateTimer(); // 更新一次时间，避免显示初始值为 15:00
      this.countdownTimer = setInterval(updateTimer, 1000); // 设置计时器

    },
    loadAddress() {
      this.$request.get('/address/selectAll').then(res => {
        if (res.code === '200') {
          this.addressData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    loadGoods(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/cart/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
        }
      }).then(res => {
        if (res.code === '200') {
          this.goodsData = res.data?.list
          this.total = res.data?.total
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    navTo(url) {
      location.href = url
    },
    del(id) {
      this.$request.delete('/cart/delete/' + id).then(res => {
        if (res.code === '200') {
          this.$message.success('移除成功')
          this.loadGoods(1)
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    handleCurrentChange(pageNum) {
      this.loadGoods(pageNum)
    },
    handleSelectionChange(rows) {
      this.totalPrice = 0
      this.selectedData = rows
      // 计计算总价格
      this.selectedData.forEach(item => {
        this.totalPrice += (item.goodsPrice * item.num)
      })
    },
    handleChange(row) {
      this.totalPrice = 0
      this.selectedData.forEach(item => {
        this.totalPrice += item.goodsPrice * item.num
      })
    },
    pay() {
      if (!this.addressId) {
        this.$message.warning('请选择收货地址')
        return
      }
      if (!this.selectedData || this.selectedData.length === 0) {
        this.$message.warning('请选择商品')
        return
      }
      let data = {
        userId: this.user.id,
        addressId: this.addressId,
        status: '待发货',
        cartData: this.selectedData
      }
      this.$request.post('/orders/add', data).then(res => {
        if (res.code === '200') {
          this.$message.success('操作成功')
          this.loadGoods(1)
        } else {
          this.$message.error(res.msg)
        }
      })
    }
  }
}
</script>
