<template>
  <view class="profile-page">
    <DynamicBackground theme="theme-primary"></DynamicBackground>
    <view class="page-content">
    <view class="profile-card">
      <u-form :model="form" labelPosition="left" labelWidth="160rpx">
        <u-form-item label="用户名">
          <u-input v-model="form.userName" disabled></u-input>
        </u-form-item>
        <u-form-item label="姓名">
          <u-input v-model="form.realName" disabled></u-input>
        </u-form-item>
        <u-form-item label="角色">
          <u-input v-model="roleText" disabled></u-input>
        </u-form-item>
        <u-form-item label="手机号">
          <u-input v-model="form.phone" placeholder="请输入手机号" :disabled="!editing"></u-input>
        </u-form-item>
        <u-form-item label="邮箱">
          <u-input v-model="form.email" placeholder="请输入邮箱" :disabled="!editing"></u-input>
        </u-form-item>
      </u-form>
    </view>

    <view class="action-btn">
      <u-button v-if="!editing" type="primary" @click="editing = true" customStyle="height: 88rpx; border-radius: 44rpx;">
        编辑信息
      </u-button>
      <u-button v-else type="primary" :loading="loading" @click="handleSave" customStyle="height: 88rpx; border-radius: 44rpx;">
        保存
      </u-button>
    </view>

    <view class="pwd-section">
      <text class="section-title">安全设置</text>
      <u-cell-group>
        <u-cell title="修改密码" isLink @click="showPwdPopup = true"></u-cell>
      </u-cell-group>
    </view>
    </view>

    <u-popup :show="showPwdPopup" mode="bottom" round="24" @close="showPwdPopup = false" closeable>
      <view class="pwd-popup">
        <text class="popup-title">修改密码</text>
        <u-form :model="pwdForm" labelPosition="left" labelWidth="160rpx">
          <u-form-item label="旧密码">
            <u-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入旧密码"></u-input>
          </u-form-item>
          <u-form-item label="新密码">
            <u-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码"></u-input>
          </u-form-item>
          <u-form-item label="确认密码">
            <u-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码"></u-input>
          </u-form-item>
        </u-form>
        <u-button type="primary" :loading="pwdLoading" @click="handleChangePwd" customStyle="margin-top: 30rpx; height: 88rpx; border-radius: 44rpx;">
          确认修改
        </u-button>
      </view>
    </u-popup>
  </view>
</template>

<script>
import DynamicBackground from '@/components/DynamicBackground'
import { getUser, setUser } from '@/utils/auth'
import { updateUserInfo, changePassword } from '@/api/user'

export default {
  components: { DynamicBackground },
  data() {
    return {
      form: {},
      editing: false,
      loading: false,
      showPwdPopup: false,
      pwdForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      pwdLoading: false
    }
  },
  computed: {
    roleText() {
      const roleMap = { 1: '管理员', 2: '订单专员', 3: '采购专员', 4: '仓库管理员', 5: '生产主管', 6: '质检人员', 7: '一线工人' }
      return roleMap[this.form.roleId] || '未知角色'
    }
  },
  onLoad() {
    this.form = { ...getUser() } || {}
  },
  methods: {
    async handleSave() {
      this.loading = true
      try {
        await updateUserInfo({
          id: this.form.id,
          phone: this.form.phone,
          email: this.form.email
        })
        setUser(this.form)
        uni.showToast({ title: '保存成功', icon: 'success' })
        this.editing = false
      } catch {
        // handled
      } finally {
        this.loading = false
      }
    },
    async handleChangePwd() {
      if (!this.pwdForm.oldPassword || !this.pwdForm.newPassword) {
        uni.showToast({ title: '请填写完整', icon: 'none' })
        return
      }
      if (this.pwdForm.newPassword !== this.pwdForm.confirmPassword) {
        uni.showToast({ title: '两次密码不一致', icon: 'none' })
        return
      }
      this.pwdLoading = true
      try {
        await changePassword(this.pwdForm)
        uni.showToast({ title: '密码修改成功', icon: 'success' })
        this.showPwdPopup = false
        this.pwdForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
      } catch {
        // handled
      } finally {
        this.pwdLoading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: #F5F7FA;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
  padding: 24rpx;
}

.profile-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 34rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.action-btn {
  margin-top: 34rpx;
}

.pwd-section {
  margin-top: 34rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
  margin-bottom: 16rpx;
  display: block;
}

.pwd-popup {
  padding: 40rpx 30rpx;
}

.popup-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
  margin-bottom: 30rpx;
  text-align: center;
}
</style>