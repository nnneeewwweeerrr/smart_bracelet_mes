export function scanCode() {
  return new Promise((resolve, reject) => {
    uni.scanCode({
      onlyFromCamera: false,
      scanType: ['qrCode', 'barCode'],
      success(res) {
        resolve(res.result)
      },
      fail(err) {
        if (err.errMsg && err.errMsg.indexOf('cancel') === -1) {
          uni.showToast({ title: '扫码失败，请重试', icon: 'none' })
        }
        reject(err)
      }
    })
  })
}

export function parseScanResult(result) {
  if (!result) return null

  try {
    const url = new URL(result)
    const type = url.pathname.replace(/^\//, '')
    const params = Object.fromEntries(url.searchParams)
    return { type, params, raw: result }
  } catch {
    return { type: 'text', params: { content: result }, raw: result }
  }
}

export function navigateByScanResult(scanResult) {
  const parsed = parseScanResult(scanResult)
  if (!parsed) return

  const routeMap = {
    'stock/in': '/pages/stock/index?action=in&code=' + encodeURIComponent(parsed.raw),
    'stock/out': '/pages/stock/index?action=out&code=' + encodeURIComponent(parsed.raw),
    'production/report': '/pages/production/index?action=report&code=' + encodeURIComponent(parsed.raw),
    'quality/check': '/pages/quality/index?action=check&code=' + encodeURIComponent(parsed.raw),
    'exception': '/pages/exception/index?action=create&code=' + encodeURIComponent(parsed.raw)
  }

  const targetUrl = routeMap[parsed.type]
  if (targetUrl) {
    uni.navigateTo({ url: targetUrl })
  } else {
    uni.showModal({
      title: '扫码结果',
      content: parsed.raw,
      showCancel: false
    })
  }
}