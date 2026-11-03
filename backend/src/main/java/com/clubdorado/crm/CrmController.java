package com.clubdorado.crm;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crm")
public class CrmController {

  private final CrmService crm;
  private final ContactService contacts;

  public CrmController(CrmService crm, ContactService contacts) {
    this.crm = crm;
    this.contacts = contacts;
  }

  @GetMapping("/contacts")
  public List<Map<String, String>> contacts() {
    return contacts.list().stream().map(this::contactView).toList();
  }

  @PostMapping("/contacts")
  public Map<String, String> saveContact(@RequestBody Map<String, String> body) {
    Contact saved = contacts.save(body.get("name"), body.get("phone"));
    return contactView(saved);
  }

  @DeleteMapping("/contacts/{phone}")
  public void deleteContact(@PathVariable String phone) {
    contacts.delete(phone);
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
        body.get("lineTwoPhone"),
        body.get("lineThreeName"),
        body.get("lineThreePhone"),
        body.get("lineFourName"),
        body.get("lineFourPhone")
    );
    return view(saved);
  }

  private Map<String, String> view(CrmSettings settings) {
    return Map.of(
        "welcomeMessage", settings.getWelcomeMessage(),
        "lineOneName", blank(settings.getLineOneName()),
        "lineOnePhone", blank(settings.getLineOnePhone()),
        "lineTwoName", blank(settings.getLineTwoName()),
        "lineTwoPhone", blank(settings.getLineTwoPhone()),
        "lineThreeName", blank(settings.getLineThreeName()),
        "lineThreePhone", blank(settings.getLineThreePhone()),
        "lineFourName", blank(settings.getLineFourName()),
        "lineFourPhone", blank(settings.getLineFourPhone())
    );
  }

  private Map<String, String> contactView(Contact contact) {
    Map<String, String> view = new LinkedHashMap<>();
    view.put("id", String.valueOf(contact.getId()));
    view.put("name", blank(contact.getName()));
    view.put("phone", contact.getPhone());
    view.put("createdAt", contact.getCreatedAt().toString());
    return view;
  }

  private String blank(String value) {
    return value == null ? "" : value;
  }
}
