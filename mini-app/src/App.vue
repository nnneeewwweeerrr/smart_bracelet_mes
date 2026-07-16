<script>
import { isLoggedIn } from '@/utils/auth'

export default {
  onLaunch() {
    console.log('App Launch')
  },
  onShow() {
    console.log('App Show')
  },
  onHide() {
    console.log('App Hide')
  }
}
</script>

<style lang="scss">
@import 'uview-plus/index.scss';

$bg-gradient-primary: linear-gradient(180deg, #E8F6FF 0%, #F0F7FF 50%, #F5F8FF 100%);
$bg-gradient-warm: linear-gradient(180deg, #FFF9F0 0%, #FDF8F0 50%, #F8FBFF 100%);
$bg-gradient-error: linear-gradient(180deg, #FFF0F0 0%, #FFF5F5 50%, #F8FBFF 100%);
$bg-gradient-success: linear-gradient(180deg, #F6FFED 0%, #F0FFF0 50%, #F8FBFF 100%);

$shadow-card-lg: 0 12rpx 40rpx rgba(0, 0, 0, 0.06);
$shadow-glow-blue: 0 8rpx 32rpx rgba(59, 124, 255, 0.15);
$shadow-glow-green: 0 8rpx 32rpx rgba(103, 194, 58, 0.15);

page {
  background: transparent;
  font-size: 28rpx;
  color: #1F1F1F;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  min-height: 100vh;
}

::-webkit-scrollbar {
  display: none;
}

.fade-in {
  animation: fadeIn 0.4s ease-out;
}

.slide-up {
  animation: slideUp 0.4s ease-out;
}

.scale-in {
  animation: scaleIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes pulse-glow {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(59, 124, 255, 0.4);
  }
  50% {
    box-shadow: 0 0 20rpx 10rpx rgba(59, 124, 255, 0.1);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10rpx);
  }
}

.pulse-glow {
  animation: pulse-glow 2s ease-in-out infinite;
}

.float {
  animation: float 3s ease-in-out infinite;
}
</style>