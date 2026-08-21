package com.doc.pdfgen.controller;

import com.doc.pdfgen.persistence.RequestEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/stats")
public class StatsController {
 private final RequestEventRepository repository; private final String serviceKey;
 public StatsController(RequestEventRepository r,@Value("${stats.service-key:}")String k){repository=r;serviceKey=k;}
 @GetMapping public ResponseEntity<?> stats(@RequestHeader(value="X-Stats-Service-Key",required=false)String key){
  if(serviceKey.isBlank()||!java.security.MessageDigest.isEqual(serviceKey.getBytes(),Objects.toString(key,"").getBytes()))return ResponseEntity.status(403).body(Map.of("error","Statistics access denied"));
  return ResponseEntity.ok(Map.of("totalRequests",repository.count(),"byType",rows(repository.byType()),"byCountry",rows(repository.byCountry()),"recent",repository.recent()));
 }
 private List<Map<String,Object>> rows(List<Object[]> rows){return rows.stream().map(r->Map.<String,Object>of("name",Objects.toString(r[0],"XX"),"count",r[1])).toList();}
}
