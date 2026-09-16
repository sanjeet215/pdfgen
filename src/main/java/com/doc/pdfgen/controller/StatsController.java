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
  Set<String> returningVisitorIds=new HashSet<>(repository.returningVisitorIds());long visitors=repository.visitorCount();
  return ResponseEntity.ok(Map.ofEntries(Map.entry("totalRequests",repository.count()),Map.entry("visitors",visitors),Map.entry("newVisitors",Math.max(0,visitors-returningVisitorIds.size())),Map.entry("returningVisitors",returningVisitorIds.size()),Map.entry("sessions",repository.sessionCount()),Map.entry("toolUses",repository.toolUseCount()),Map.entry("downloads",repository.downloadCount()),Map.entry("usefulVisits",repository.usefulVisitCount()),Map.entry("byType",rows(repository.byType())),Map.entry("byCountry",rows(repository.byCountry())),Map.entry("byMode",rows(repository.byMode())),Map.entry("recent",recent(returningVisitorIds))));
 }
 private List<Map<String,Object>> rows(List<Object[]> rows){return rows.stream().map(r->Map.<String,Object>of("name",Objects.toString(r[0],"XX"),"count",r[1])).toList();}
 private List<Object[]> recent(Set<String> returningVisitorIds){return repository.recent().stream().map(row->{Object[] enriched=new Object[row.length+1];System.arraycopy(row,0,enriched,0,12);String visitorId=Objects.toString(row[11],null);enriched[12]=visitorId==null?"UNKNOWN":returningVisitorIds.contains(visitorId)?"RETURNING":"NEW";System.arraycopy(row,12,enriched,13,row.length-12);return enriched;}).toList();}
}
