package com.doc.pdfgen.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {
    @PostMapping("/page-view")
    public ResponseEntity<Void> pageView() {
        return ResponseEntity.noContent().build();
    }
}
