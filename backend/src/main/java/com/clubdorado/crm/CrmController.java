package com.clubdorado.crm;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/crm")
public class CrmController {

  private final CrmService crm;

  public CrmController(CrmService crm) {
    this.crm = crm;
  }

  @GetMapping("/settings")
  public Map<String, String> settings() {
    return view(crm.current());
  }

  @PutMapping("/settings")
  public Map<String, String> update(@RequestBody Map<String, String> body) {
    CrmSettings saved = crm.update(
        body.get("welcomeMessage"),
        body.get("lineOneName"),
        body.get("lineOnePhone"),
        body.get("lineTwoName"),
        body.get("lineTwoPhone")
    );
    return view(saved);
  }

  private Map<String, String> view(CrmSettings settings) {
    return Map.of(
        "welcomeMessage", settings.getWelcomeMessage(),
        "lineOneName", blank(settings.getLineOneName()),
        "lineOnePhone", blank(settings.getLineOnePhone()),
        "lineTwoName", blank(settings.getLineTwoName()),
        "lineTwoPhone", blank(settings.getLineTwoPhone())
    );
  }

  private String blank(String value) {
    return value == null ? "" : value;
  }
}
