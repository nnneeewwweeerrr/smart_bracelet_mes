package com.mes.service;

import java.awt.image.BufferedImage;

public interface QrCodeService {
    
    BufferedImage generateQrCode(String content, int width, int height);
    
    byte[] generateQrCodeBytes(String content, int width, int height);
    
    String generateSnCode(Long productId);
    
    String generateWorkOrderCode(Long workOrderId);
    
    String generateMaterialCode(Long materialId);
    
    String generateStationCode(String stationName);
    
    String generateBatchCode();
}