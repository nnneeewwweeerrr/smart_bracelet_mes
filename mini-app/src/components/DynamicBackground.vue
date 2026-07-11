<template>
  <view class="dynamic-bg" :class="theme">
    <view class="bg-gradient-layer"></view>
    
    <view class="circle-container">
      <view class="circle circle-1">
        <view class="circle-ring"></view>
        <view class="circle-arc"></view>
        <view class="circle-center"></view>
      </view>
      <view class="circle circle-2">
        <view class="circle-ring"></view>
        <view class="circle-arc"></view>
        <view class="circle-center"></view>
      </view>
    </view>

    <view class="wave-container">
      <view class="wave wave-1"></view>
      <view class="wave wave-2"></view>
    </view>

    <view class="bg-particles">
      <view 
        v-for="particle in particles" 
        :key="particle.id" 
        class="particle"
        :style="{
          left: particle.x + '%',
          top: particle.y + '%',
          animationDelay: particle.delay + 's',
          animationDuration: particle.duration + 's',
          width: particle.size + 'rpx',
          height: particle.size + 'rpx'
        }"
      ></view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'DynamicBackground',
  props: {
    theme: {
      type: String,
      default: 'theme-primary'
    }
  },
  data() {
    return {
      particles: []
    }
  },
  mounted() {
    this.initParticles()
  },
  methods: {
    initParticles() {
      const particles = []
      for (let i = 0; i < 15; i++) {
        particles.push({
          id: i,
          x: Math.random() * 100,
          y: Math.random() * 100,
          size: Math.random() * 15 + 10,
          delay: Math.random() * 6,
          duration: Math.random() * 8 + 6
        })
      }
      this.particles = particles
    }
  }
}
</script>

<style lang="scss" scoped>
.dynamic-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  overflow: hidden;
}

.bg-gradient-layer {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

.theme-primary .bg-gradient-layer {
  background: linear-gradient(
    135deg,
    #E8F6FF 0%,
    #D4ECFF 25%,
    #F0F7FF 50%,
    #D4ECFF 75%,
    #E8F6FF 100%
  );
}

.theme-warm .bg-gradient-layer {
  background: linear-gradient(
    135deg,
    #FFF9F0 0%,
    #FFEDD5 25%,
    #FDF8F0 50%,
    #FFEDD5 75%,
    #FFF9F0 100%
  );
}

.theme-error .bg-gradient-layer {
  background: linear-gradient(
    135deg,
    #FFF0F0 0%,
    #FFE4E4 25%,
    #F8FBFF 50%,
    #FFE4E4 75%,
    #FFF0F0 100%
  );
}

.theme-success .bg-gradient-layer {
  background: linear-gradient(
    135deg,
    #F6FFED 0%,
    #D9F7BE 25%,
    #F0FFF0 50%,
    #D9F7BE 75%,
    #F6FFED 100%
  );
}

.circle-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.circle {
  position: absolute;
  animation: circleFloat ease-in-out infinite;
}

.circle-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  border-radius: 50%;
  border: 8rpx solid;
  transform: translate(-50%, -50%);
}

.circle-arc {
  position: absolute;
  top: 50%;
  left: 50%;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: arcRotate linear infinite;
}

.circle-center {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  transform: translate(-50%, -50%);
}

.circle-1 {
  width: 600rpx;
  height: 600rpx;
  top: -200rpx;
  right: -150rpx;
  animation-duration: 20s;
}

.circle-1 .circle-ring {
  width: 600rpx;
  height: 600rpx;
  border-color: rgba(59, 124, 255, 0.3);
}

.circle-1 .circle-arc {
  width: 600rpx;
  height: 600rpx;
  border: 16rpx solid transparent;
  border-top-color: rgba(59, 124, 255, 0.5);
  border-right-color: rgba(59, 124, 255, 0.2);
  animation-duration: 10s;
}

.circle-1 .circle-center {
  background: rgba(59, 124, 255, 0.4);
}

.circle-2 {
  width: 450rpx;
  height: 450rpx;
  bottom: -100rpx;
  left: -80rpx;
  animation-duration: 25s;
  animation-direction: reverse;
}

.circle-2 .circle-ring {
  width: 450rpx;
  height: 450rpx;
  border-color: rgba(100, 181, 246, 0.25);
}

.circle-2 .circle-arc {
  width: 450rpx;
  height: 450rpx;
  border: 12rpx solid transparent;
  border-bottom-color: rgba(100, 181, 246, 0.4);
  border-left-color: rgba(100, 181, 246, 0.15);
  animation-duration: 15s;
}

.circle-2 .circle-center {
  background: rgba(100, 181, 246, 0.35);
}

.theme-warm {
  .circle-1 .circle-ring {
    border-color: rgba(250, 173, 20, 0.3);
  }
  .circle-1 .circle-arc {
    border-top-color: rgba(250, 173, 20, 0.5);
    border-right-color: rgba(250, 173, 20, 0.2);
  }
  .circle-1 .circle-center {
    background: rgba(250, 173, 20, 0.4);
  }
  .circle-2 .circle-ring {
    border-color: rgba(255, 193, 7, 0.25);
  }
  .circle-2 .circle-arc {
    border-bottom-color: rgba(255, 193, 7, 0.4);
    border-left-color: rgba(255, 193, 7, 0.15);
  }
  .circle-2 .circle-center {
    background: rgba(255, 193, 7, 0.35);
  }
}

.theme-error {
  .circle-1 .circle-ring {
    border-color: rgba(245, 108, 108, 0.3);
  }
  .circle-1 .circle-arc {
    border-top-color: rgba(245, 108, 108, 0.5);
    border-right-color: rgba(245, 108, 108, 0.2);
  }
  .circle-1 .circle-center {
    background: rgba(245, 108, 108, 0.4);
  }
  .circle-2 .circle-ring {
    border-color: rgba(255, 117, 117, 0.25);
  }
  .circle-2 .circle-arc {
    border-bottom-color: rgba(255, 117, 117, 0.4);
    border-left-color: rgba(255, 117, 117, 0.15);
  }
  .circle-2 .circle-center {
    background: rgba(255, 117, 117, 0.35);
  }
}

.theme-success {
  .circle-1 .circle-ring {
    border-color: rgba(82, 196, 26, 0.3);
  }
  .circle-1 .circle-arc {
    border-top-color: rgba(82, 196, 26, 0.5);
    border-right-color: rgba(82, 196, 26, 0.2);
  }
  .circle-1 .circle-center {
    background: rgba(82, 196, 26, 0.4);
  }
  .circle-2 .circle-ring {
    border-color: rgba(103, 232, 166, 0.25);
  }
  .circle-2 .circle-arc {
    border-bottom-color: rgba(103, 232, 166, 0.4);
    border-left-color: rgba(103, 232, 166, 0.15);
  }
  .circle-2 .circle-center {
    background: rgba(103, 232, 166, 0.35);
  }
}

.wave-container {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 300rpx;
  overflow: hidden;
}

.wave {
  position: absolute;
  left: 0;
  right: 0;
  height: 100%;
  opacity: 0.15;
  animation: waveMove linear infinite;
}

.wave-1 {
  bottom: 0;
  background: linear-gradient(180deg, transparent 0%, rgba(59, 124, 255, 0.2) 50%, transparent 100%);
  animation-duration: 8s;
}

.wave-2 {
  bottom: 50rpx;
  background: linear-gradient(180deg, transparent 0%, rgba(100, 181, 246, 0.15) 50%, transparent 100%);
  animation-duration: 12s;
  animation-direction: reverse;
}

.theme-warm .wave-1 {
  background: linear-gradient(180deg, transparent 0%, rgba(250, 173, 20, 0.2) 50%, transparent 100%);
}

.theme-warm .wave-2 {
  background: linear-gradient(180deg, transparent 0%, rgba(255, 193, 7, 0.15) 50%, transparent 100%);
}

.theme-error .wave-1 {
  background: linear-gradient(180deg, transparent 0%, rgba(245, 108, 108, 0.2) 50%, transparent 100%);
}

.theme-error .wave-2 {
  background: linear-gradient(180deg, transparent 0%, rgba(255, 117, 117, 0.15) 50%, transparent 100%);
}

.theme-success .wave-1 {
  background: linear-gradient(180deg, transparent 0%, rgba(82, 196, 26, 0.2) 50%, transparent 100%);
}

.theme-success .wave-2 {
  background: linear-gradient(180deg, transparent 0%, rgba(103, 232, 166, 0.15) 50%, transparent 100%);
}

.bg-particles {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.particle {
  position: absolute;
  background: rgba(59, 124, 255, 0.5);
  border-radius: 50%;
  animation: particleFloat linear infinite;
  box-shadow: 0 0 20rpx rgba(59, 124, 255, 0.4);
}

.theme-warm .particle {
  background: rgba(250, 173, 20, 0.5);
  box-shadow: 0 0 20rpx rgba(250, 173, 20, 0.4);
}

.theme-error .particle {
  background: rgba(245, 108, 108, 0.5);
  box-shadow: 0 0 20rpx rgba(245, 108, 108, 0.4);
}

.theme-success .particle {
  background: rgba(82, 196, 26, 0.5);
  box-shadow: 0 0 20rpx rgba(82, 196, 26, 0.4);
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

@keyframes circleFloat {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  25% { transform: translate(-40rpx, 60rpx) rotate(3deg); }
  50% { transform: translate(20rpx, 80rpx) rotate(-2deg); }
  75% { transform: translate(-20rpx, 30rpx) rotate(2deg); }
}

@keyframes arcRotate {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

@keyframes waveMove {
  0% { transform: translateX(0); }
  100% { transform: translateX(60rpx); }
}

@keyframes particleFloat {
  0% {
    transform: translateY(100vh) translateX(0) scale(0);
    opacity: 0;
  }
  10% {
    opacity: 0.8;
    transform: translateY(90vh) translateX(20rpx) scale(1);
  }
  90% {
    opacity: 0.8;
    transform: translateY(10vh) translateX(-20rpx) scale(1);
  }
  100% {
    transform: translateY(-10vh) translateX(0) scale(0);
    opacity: 0;
  }
}
</style>
