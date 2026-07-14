package com.mes.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.mes.service.QrCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
public class QrCodeServiceImpl implements QrCodeService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");
    private final Random random = new Random();

    @Override
    public BufferedImage generateQrCode(String content, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M);
            
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException e) {
            log.error("生成二维码失败", e);
            throw new RuntimeException("生成二维码失败", e);
        }
    }

    @Override
    public byte[] generateQrCodeBytes(String content, int width, int height) {
        BufferedImage image = generateQrCode(content, width, height);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("转换二维码为字节数组失败", e);
            throw new RuntimeException("转换二维码为字节数组失败", e);
        }
    }

    @Override
    public String generateSnCode(Long productId) {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        int randomNum = 100000 + random.nextInt(900000);
        return "SN" + dateStr + randomNum;
    }

    @Override
    public String generateWorkOrderCode(Long workOrderId) {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        int randomNum = 100000 + random.nextInt(900000);
        return "WO" + dateStr + randomNum;
    }

    @Override
    public String generateMaterialCode(Long materialId) {
        int typeNum = 1000 + random.nextInt(9000);
        int seqNum = 100000 + random.nextInt(900000);
        return "MTRL-" + typeNum + "-" + seqNum;
    }

    @Override
    public String generateStationCode(String stationName) {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        int hash = Math.abs(stationName.hashCode()) % 10000;
        return "STATION" + dateStr.substring(2) + String.format("%04d", hash);
    }

    @Override
    public String generateBatchCode() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        int randomNum = 1000 + random.nextInt(9000);
        return "BATCH" + dateStr + randomNum;
    }
}