<template>
  <view class="create-page">
    <DynamicBackground theme="theme-error" />
    <view class="page-content">
    <view class="page-header">
      <view class="page-title-wrap">
        <text class="page-title">异常上报</text>
        <text class="page-desc">及时上报生产异常，保障生产顺畅</text>
      </view>
    </view>

    <view class="form-section">
      <u-form :model="form" labelPosition="left" labelWidth="160rpx">
        <u-form-item label="异常类型" prop="exceptionType">
          <view class="type-picker" @click="showTypePicker = true">
            <text class="type-value" :class="{ 'placeholder': !exceptionTypeName }">{{ exceptionTypeName || '请选择异常类型' }}</text>
            <text class="type-arrow">›</text>
          </view>
        </u-form-item>

        <u-form-item label="关联工单" prop="workOrderId">
          <u-input v-model="form.workOrderNo" placeholder="扫码或输入工单号">
            <template #suffix>
              <text class="suffix-scan" @click="handleScanOrder">扫</text>
            </template>
          </u-input>
        </u-form-item>

        <u-form-item label="异常描述" prop="description">
          <view class="desc-container">
            <u-textarea v-model="form.description" placeholder="请详细描述异常情况" count maxlength="500" class="desc-textarea"></u-textarea>
            
            <view class="voice-area" v-if="!isRecording">
              <view class="voice-btn-wrap">
                <view class="voice-btn" @click="handleVoice">
                  <text class="voice-icon">🎤</text>
                  <text class="voice-text">语音录入</text>
                </view>
              </view>
              <text class="voice-hint">点击麦克风，语音自动转文字</text>
            </view>
            
            <view class="recording-area" v-if="isRecording">
              <view class="recording-header">
                <view class="recording-dot"></view>
                <text class="recording-text">正在录音中</text>
                <text class="recording-time">{{ formatTime(recordingTime) }}</text>
              </view>
              <view class="recording-wave">
                <view class="wave-bar" v-for="i in 5" :key="i" :style="{ animationDelay: `${i * 0.1}s` }"></view>
              </view>
              <view class="recording-stop" @click="stopRecording">
                <text class="stop-icon">⏹️</text>
                <text class="stop-text">停止录音</text>
              </view>
            </view>
          </view>
        </u-form-item>

        <u-form-item label="发现工序">
          <u-input v-model="form.findProcess" placeholder="如：SMT贴片、功能测试">
            <template #suffix>
              <text class="suffix-scan" @click="handleScanProcess">扫</text>
            </template>
          </u-input>
        </u-form-item>

        <u-form-item label="发现人">
          <u-input v-model="form.findOperator" placeholder="输入发现人姓名"></u-input>
        </u-form-item>
      </u-form>

      <view class="photo-section">
        <view class="photo-header">
          <text class="photo-label">📷 上传异常照片</text>
          <text class="photo-count">{{ form.photos.length }}/3</text>
        </view>
        <view class="photo-upload">
          <view class="upload-item" v-for="(photo, idx) in form.photos" :key="idx" @click="previewPhoto(idx)">
            <image :src="photo" mode="aspectFill" class="upload-image"></image>
            <view class="upload-delete" @click.stop="removePhoto(idx)">✕</view>
            <view class="upload-number">{{ idx + 1 }}</view>
          </view>
          <view class="upload-btn" @click="choosePhoto" v-if="form.photos.length < 3">
            <text class="upload-icon">+</text>
            <text class="upload-text">拍照</text>
          </view>
        </view>
        <text class="photo-tip">最多上传3张照片，支持拍照或选择相册</text>
      </view>

      <view class="submit-section">
        <view class="submit-preview" v-if="form.description || form.photos.length > 0">
          <text class="preview-title">📋 上报预览</text>
          <view class="preview-content">
            <view class="preview-item" v-if="form.description">
              <text class="preview-label">描述</text>
              <text class="preview-text">{{ form.description }}</text>
            </view>
            <view class="preview-item" v-if="form.photos.length > 0">
              <text class="preview-label">照片</text>
              <view class="preview-photos">
                <image v-for="(photo, idx) in form.photos" :key="idx" :src="photo" mode="aspectFill" class="preview-photo"></image>
              </view>
            </view>
          </view>
        </view>

        <u-button type="error" :loading="loading" @click="handleSubmit" customStyle="margin-top: 40rpx; height: 88rpx; border-radius: 44rpx; font-size: 32rpx;">
          <text class="btn-icon">📤</text>
          提交上报
        </u-button>
      </view>
    </view>

    <u-picker :show="showTypePicker" :columns="typeColumns" keyName="label" @confirm="onTypeConfirm" @cancel="showTypePicker = false"></u-picker>
    
    <u-popup v-if="showPhotoPreview" :show="showPhotoPreview" mode="center" @close="showPhotoPreview = false">
      <view class="photo-preview-modal">
        <image :src="previewImage" mode="aspectFit" class="preview-image"></image>
        <view class="preview-close" @click="showPhotoPreview = false">✕</view>
      </view>
    </u-popup>
    </view>
  </view>
</template>

<script>
import { scanCode } from '@/utils/scan'
import { createException } from '@/api/exception'
import { getUser } from '@/utils/auth'
import DynamicBackground from '@/components/DynamicBackground'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      form: {
        exceptionType: '',
        workOrderId: '',
        workOrderNo: '',
        description: '',
        findProcess: '',
        findOperator: '',
        photos: []
      },
      exceptionTypeName: '',
      typeList: [
        { label: '设备故障', value: 'equipment' },
        { label: '物料短缺', value: 'material' },
        { label: '质量问题', value: 'quality' },
        { label: '工艺异常', value: 'process' },
        { label: '安全隐患', value: 'safety' },
        { label: '其他异常', value: 'other' }
      ],
      typeColumns: [],
      showTypePicker: false,
      loading: false,
      isRecording: false,
      recordingTime: 0,
      recordingTimer: null,
      showPhotoPreview: false,
      previewImage: '',
      recorderManager: null
    }
  },
  onLoad(options) {
    this.typeColumns = [this.typeList]
    if (options.code) {
      this.form.workOrderNo = options.code
    }
    const user = getUser() || {}
    if (user.name) {
      this.form.findOperator = user.name
    }
  },
  onUnload() {
    this.stopRecording()
  },
  methods: {
    async handleScanOrder() {
      try {
        const code = await scanCode()
        this.form.workOrderNo = code
      } catch {
        // cancelled
      }
    },
    async handleScanProcess() {
      try {
        const code = await scanCode()
        this.form.findProcess = code
      } catch {
        // cancelled
      }
    },
    choosePhoto() {
      uni.chooseImage({
        count: 3 - this.form.photos.length,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.form.photos = [...this.form.photos, ...res.tempFilePaths]
          uni.showToast({ title: '图片上传成功', icon: 'success', duration: 1000 })
        },
        fail: (err) => {
          console.error('图片选择失败:', err)
          uni.showToast({ title: '图片选择失败', icon: 'none' })
        }
      })
    },
    removePhoto(index) {
      uni.showModal({
        title: '确认删除',
        content: '确定要删除这张照片吗？',
        success: (res) => {
          if (res.confirm) {
            this.form.photos.splice(index, 1)
          }
        }
      })
    },
    previewPhoto(index) {
      this.previewImage = this.form.photos[index]
      this.showPhotoPreview = true
    },
    onTypeConfirm({ value }) {
      const item = value[0]
      if (item) {
        this.form.exceptionType = item.value
        this.exceptionTypeName = item.label
      }
      this.showTypePicker = false
    },
    handleVoice() {
      if (this.isRecording) {
        this.stopRecording()
      } else {
        this.startRecording()
      }
    },
    startRecording() {
      this.isRecording = true
      this.recordingTime = 0
      
      const that = this
      
      uni.getSetting({
        success: (res) => {
          const authSetting = res.authSetting || {}
          const recordAuth = authSetting['scope.record']
          
          if (recordAuth === true) {
            that.doStartRecording()
          } else if (recordAuth === undefined) {
            uni.authorize({
              scope: 'scope.record',
              success: () => {
                that.doStartRecording()
              },
              fail: () => {
                that.isRecording = false
                uni.showModal({
                  title: '录音权限',
                  content: '需要录音权限才能使用语音功能，请点击确定前往设置开启',
                  showCancel: false,
                  success: () => {
                    uni.openSetting({
                      success: (settingRes) => {
                        if (settingRes.authSetting && settingRes.authSetting['scope.record'] === true) {
                          that.isRecording = true
                          that.doStartRecording()
                        }
                      }
                    })
                  }
                })
              }
            })
          } else {
            that.isRecording = false
            uni.showModal({
              title: '录音权限',
              content: '需要录音权限才能使用语音功能，请点击确定前往设置开启',
              showCancel: false,
              success: () => {
                uni.openSetting({
                  success: (settingRes) => {
                    if (settingRes.authSetting && settingRes.authSetting['scope.record'] === true) {
                      that.isRecording = true
                      that.doStartRecording()
                    }
                  }
                })
              }
            })
          }
        },
        fail: () => {
          that.isRecording = false
          uni.showToast({ title: '获取权限设置失败', icon: 'none' })
        }
      })
    },
    doStartRecording() {
      uni.showToast({
        title: '开始录音',
        icon: 'none',
        duration: 1500
      })

      this.recorderManager = uni.getRecorderManager()
      
      const that = this
      
      this.recorderManager.onStart = () => {
        that.isRecording = true
        that.recordingTimer = setInterval(() => {
          that.recordingTime++
        }, 1000)
      }
      
      this.recorderManager.onStop = (res) => {
        that.isRecording = false
        if (that.recordingTimer) {
          clearInterval(that.recordingTimer)
          that.recordingTimer = null
        }
        that.handleRecognize(res.tempFilePath)
      }
      
      this.recorderManager.onError = (err) => {
        that.isRecording = false
        if (that.recordingTimer) {
          clearInterval(that.recordingTimer)
          that.recordingTimer = null
        }
        uni.showToast({
          title: '录音失败',
          icon: 'none'
        })
      }
      
      this.recorderManager.start({
        duration: 60000,
        format: 'mp3',
        sampleRate: 16000,
        numberOfChannels: 1
      })
    },
    stopRecording() {
      if (!this.isRecording) return
      
      if (this.recorderManager) {
        try {
          this.recorderManager.stop()
        } catch {
          // ignore
        }
      } else {
        uni.stopRecord({
          success: () => {},
          fail: () => {}
        })
      }
      this.isRecording = false
      if (this.recordingTimer) {
        clearInterval(this.recordingTimer)
        this.recordingTimer = null
      }
    },
    async handleRecognize(filePath) {
      console.log('异常上报页面 - 开始语音识别, filePath:', filePath)
      if (!filePath) {
        uni.showToast({ title: '录音文件无效', icon: 'none' })
        return
      }
      uni.showLoading({ title: '识别中...' })
      try {
        const { post } = await import('@/utils/request')
        const data = await post('/ai/voice/recognize', { filePath })
        console.log('异常上报页面 - 语音识别结果:', data)
        uni.hideLoading()
        const text = data.text || data.content || ''
        console.log('异常上报页面 - 识别到的文本:', text)
        if (text) {
          this.form.description += (this.form.description ? '\n' : '') + text
          uni.showToast({ title: '语音识别成功', icon: 'success' })
        } else {
          uni.showToast({ title: '未识别到内容', icon: 'none' })
        }
      } catch (e) {
        console.error('异常上报页面 - 语音识别失败:', e)
        uni.hideLoading()
        uni.showToast({ title: '语音识别失败', icon: 'none' })
      }
    },
    formatTime(seconds) {
      const mins = Math.floor(seconds / 60)
      const secs = seconds % 60
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    },
    async handleSubmit() {
      if (!this.form.exceptionType) {
        uni.showToast({ title: '请选择异常类型', icon: 'none' })
        return
      }
      if (!this.form.description && this.form.photos.length === 0) {
        uni.showToast({ title: '请填写异常描述或上传照片', icon: 'none' })
        return
      }

      const user = getUser() || {}
      this.loading = true
      try {
        await createException({
          exceptionType: this.form.exceptionType,
          workOrderId: this.form.workOrderId || undefined,
          description: this.form.description,
          reporterId: user.id,
          findProcess: this.form.findProcess,
          findOperator: this.form.findOperator,
          photos: this.form.photos
        })
        uni.showToast({ title: '上报成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 1500)
      } catch {
        uni.showToast({ title: '上报成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 1500)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@keyframes wave {
  0%, 100% { height: 20%; }
  50% { height: 100%; }
}

.create-page {
  min-height: 100vh;
  background: #F5F7FA;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
  padding-bottom: 40rpx;
}

.page-header {
  padding: 40rpx 32rpx 32rpx;
  background: linear-gradient(135deg, #FF4D4F 0%, #FF7875 40%, #FF9E9E 80%, #FFCCC7 100%);
  border-radius: 0 0 48rpx 48rpx;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -120rpx;
    right: -80rpx;
    width: 300rpx;
    height: 300rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    animation: float 6s ease-in-out infinite;
  }
}

.page-title-wrap {
  position: relative;
  z-index: 1;
}

.page-title {
  font-size: 44rpx;
  font-weight: 700;
  color: #ffffff;
  display: block;
  margin-bottom: 8rpx;
}

.page-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  display: block;
}

.form-section {
  margin: 24rpx;
  background: linear-gradient(145deg, #FFFFFF 0%, #FFF9F9 100%);
  border-radius: 28rpx;
  padding: 34rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 0 0 1rpx rgba(255, 255, 255, 0.9);
}

.type-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  background: #F8FAFF;
  border-radius: 16rpx;
  border: 2rpx solid #E8E8E8;
  transition: all 0.3s ease;

  &:active {
    background: #F0F0F0;
    border-color: #FF4D4F;
  }
}

.type-value {
  font-size: 28rpx;
  color: #1F1F1F;
  font-weight: 500;

  &.placeholder {
    color: #BFBFBF;
  }
}

.type-arrow {
  font-size: 32rpx;
  color: #BFBFBF;
  font-weight: 300;
}

.suffix-scan {
  font-size: 24rpx;
  color: #FF4D4F;
  font-weight: 600;
}

.desc-container {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.desc-textarea {
  background: #FFFFFF;
  border-radius: 16rpx;
  border: 2rpx solid #F0F0F0;
}

.voice-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx;
  background: linear-gradient(145deg, #FFF7F7 0%, #FFFFFF 100%);
  border-radius: 16rpx;
  border: 2rpx dashed #FFCCCC;
}

.voice-btn-wrap {
  width: 120rpx;
  height: 120rpx;
}

.voice-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #FF4D4F 0%, #FF7875 100%);
  border-radius: 50%;
  box-shadow: 0 12rpx 32rpx rgba(255, 77, 79, 0.35);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.92);
    box-shadow: 0 6rpx 16rpx rgba(255, 77, 79, 0.35);
  }
}

.voice-icon {
  font-size: 40rpx;
  margin-bottom: 4rpx;
}

.voice-text {
  font-size: 22rpx;
  color: #ffffff;
  font-weight: 500;
}

.voice-hint {
  font-size: 24rpx;
  color: #999999;
}

.recording-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx;
  background: linear-gradient(145deg, rgba(255, 77, 79, 0.08) 0%, rgba(255, 120, 117, 0.12) 100%);
  border-radius: 20rpx;
  border: 2rpx solid rgba(255, 77, 79, 0.2);
}

.recording-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.recording-dot {
  width: 20rpx;
  height: 20rpx;
  background: #FF4D4F;
  border-radius: 50%;
  animation: pulse 1s ease-in-out infinite;
  box-shadow: 0 0 20rpx rgba(255, 77, 79, 0.6);
}

.recording-text {
  font-size: 30rpx;
  color: #FF4D4F;
  font-weight: 600;
}

.recording-time {
  font-size: 32rpx;
  font-weight: 700;
  color: #FF4D4F;
  font-family: monospace;
}

.recording-wave {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  height: 80rpx;
}

.wave-bar {
  width: 12rpx;
  background: linear-gradient(180deg, #FF4D4F 0%, #FF7875 100%);
  border-radius: 6rpx;
  animation: wave 0.5s ease-in-out infinite;
}

.recording-stop {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100rpx;
  height: 100rpx;
  background: #FFFFFF;
  border-radius: 50%;
  border: 4rpx solid #FF4D4F;
  box-shadow: 0 8rpx 24rpx rgba(255, 77, 79, 0.3);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.95);
    background: #FFF5F5;
  }
}

.stop-icon {
  font-size: 36rpx;
}

.stop-text {
  font-size: 20rpx;
  color: #FF4D4F;
  font-weight: 600;
}

.photo-section {
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid rgba(0, 0, 0, 0.06);
}

.photo-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.photo-label {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
}

.photo-count {
  font-size: 24rpx;
  color: #FF4D4F;
  font-weight: 500;
}

.photo-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.upload-item {
  width: 180rpx;
  height: 180rpx;
  position: relative;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);

  &:active {
    transform: scale(0.97);
  }
}

.upload-image {
  width: 100%;
  height: 100%;
}

.upload-delete {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 40rpx;
  height: 40rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  color: #ffffff;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-number {
  position: absolute;
  bottom: 8rpx;
  right: 8rpx;
  width: 32rpx;
  height: 32rpx;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  font-size: 20rpx;
  color: #1F1F1F;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-btn {
  width: 180rpx;
  height: 180rpx;
  border: 2rpx dashed #D9D9D9;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;

  &:active {
    background: rgba(245, 108, 108, 0.05);
    border-color: #FF4D4F;
  }
}

.upload-icon {
  font-size: 48rpx;
  color: #D9D9D9;
}

.upload-text {
  font-size: 24rpx;
  color: #D9D9D9;
  margin-top: 8rpx;
}

.photo-tip {
  font-size: 22rpx;
  color: #BFBFBF;
  display: block;
  margin-top: 12rpx;
}

.submit-section {
  margin-top: 24rpx;
}

.submit-preview {
  background: #F8FAFF;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}

.preview-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
  margin-bottom: 16rpx;
}

.preview-content {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.preview-item {
  display: flex;
  gap: 16rpx;
}

.preview-label {
  font-size: 24rpx;
  color: #8C8C8C;
  flex-shrink: 0;
}

.preview-text {
  font-size: 24rpx;
  color: #4A4A4A;
  flex: 1;
  line-height: 1.5;
}

.preview-photos {
  display: flex;
  gap: 12rpx;
}

.preview-photo {
  width: 80rpx;
  height: 80rpx;
  border-radius: 8rpx;
}

.btn-icon {
  margin-right: 8rpx;
}

.photo-preview-modal {
  width: 650rpx;
  height: 700rpx;
  padding: 0;
  border-radius: 24rpx;
  overflow: hidden;
  position: relative;
  background: #000000;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.preview-close {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  width: 56rpx;
  height: 56rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  color: #ffffff;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>