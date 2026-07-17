package com.mes.controller;

import com.mes.common.Result;
import com.mes.service.QrCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/qrcode")
public class QrCodeController {

    @Autowired
    private QrCodeService qrCodeService;

    @GetMapping("/generate")
    public void generateQrCode(
            @RequestParam String content,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height) throws IOException {
        
        BufferedImage image = qrCodeService.generateQrCode(content, width, height);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", outputStream);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", "qrcode.png");
        
        byte[] imageBytes = outputStream.toByteArray();
        outputStream.close();
        
        javax.servlet.http.HttpServletResponse response = 
                ((org.springframework.web.context.request.ServletRequestAttributes)
                        org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                        .getResponse();
        response.setContentType("image/png");
        response.setContentLength(imageBytes.length);
        response.getOutputStream().write(imageBytes);
        response.getOutputStream().flush();
    }

    @PostMapping("/generate/sn")
    public Result<Map<String, Object>> generateSnQrCode(@RequestBody Map<String, Object> params) {
        Long productId = params.containsKey("productId") ? Long.parseLong(params.get("productId").toString()) : null;
        String snCode = qrCodeService.generateSnCode(productId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", snCode);
        result.put("qrcodeUrl", "/api/qrcode/generate?content=" + snCode);
        
        return Result.success(result);
    }

    @PostMapping("/generate/workorder")
    public Result<Map<String, Object>> generateWorkOrderQrCode(@RequestBody Map<String, Object> params) {
        Long workOrderId = params.containsKey("workOrderId") ? Long.parseLong(params.get("workOrderId").toString()) : null;
        String woCode = qrCodeService.generateWorkOrderCode(workOrderId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", woCode);
        result.put("qrcodeUrl", "/api/qrcode/generate?content=" + woCode);
        
        return Result.success(result);
    }

    @PostMapping("/generate/material")
    public Result<Map<String, Object>> generateMaterialQrCode(@RequestBody Map<String, Object> params) {
        Long materialId = params.containsKey("materialId") ? Long.parseLong(params.get("materialId").toString()) : null;
        String materialCode = qrCodeService.generateMaterialCode(materialId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", materialCode);
        result.put("qrcodeUrl", "/api/qrcode/generate?content=" + materialCode);
        
        return Result.success(result);
    }

    @PostMapping("/generate/station")
    public Result<Map<String, Object>> generateStationQrCode(@RequestBody Map<String, Object> params) {
        String stationName = (String) params.get("stationName");
        if (stationName == null || stationName.trim().isEmpty()) {
            stationName = "Station";
        }
        String stationCode = qrCodeService.generateStationCode(stationName);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", stationCode);
        result.put("qrcodeUrl", "/api/qrcode/generate?content=" + stationCode);
        
        return Result.success(result);
    }

    @PostMapping("/generate/batch")
    public Result<Map<String, Object>> generateBatchQrCode() {
        String batchCode = qrCodeService.generateBatchCode();
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", batchCode);
        result.put("qrcodeUrl", "/api/qrcode/generate?content=" + batchCode);
        
        return Result.success(result);
    }

    @PostMapping("/generate/custom")
    public Result<Map<String, Object>> generateCustomQrCode(@RequestBody Map<String, Object> params) {
        String content = (String) params.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("内容不能为空");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", content);
        result.put("qrcodeUrl", "/api/qrcode/generate?content=" + java.net.URLEncoder.encode(content));
        
        return Result.success(result);
    }

    @GetMapping("/test")
    public Result<Map<String, Object>> testQrCode() {
        Map<String, Object> result = new HashMap<>();
        result.put("snCode", "SN20240115001");
        result.put("snQrCodeUrl", "/api/qrcode/generate?content=SN20240115001");
        result.put("woCode", "WO20240115001");
        result.put("woQrCodeUrl", "/api/qrcode/generate?content=WO20240115001");
        result.put("stationCode", "STATION0101");
        result.put("stationQrCodeUrl", "/api/qrcode/generate?content=STATION0101");
        
        return Result.success(result);
    }
}