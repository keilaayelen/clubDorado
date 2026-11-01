package com.clubdorado.crm;

import com.clubdorado.config.AppProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class CrmService {

  static final long SETTINGS_ID = 1L;
  static final String DEFAULT_WELCOME =
      "Hola, gracias por escribir a Golden Club. Ya recibimos tu mensaje y en un momento te atiende alguien del equipo.";

  private final CrmSettingsRepository settings;
  private final AppProperties properties;

  public CrmService(CrmSettingsRepository settings, AppProperties properties) {
    this.settings = settings;
    this.properties = properties;
  }

  @Transactional
  public synchronized String nextPublicPhone() {
    CrmSettings saved = current();
    List<String> phones = new ArrayList<>();
    addPhone(phones, saved.getLineOnePhone());
    addPhone(phones, saved.getLineTwoPhone());
    addPhone(phones, saved.getLineThreePhone());
    addPhone(phones, saved.getLineFourPhone());
    if (phones.isEmpty()) {
      return properties.whatsappPhone();
    }
    int cursor = saved.getRotationIndex() == null ? 0 : saved.getRotationIndex();
    int index = Math.floorMod(cursor, phones.size());
    saved.setRotationIndex(index + 1);
    settings.save(saved);
    return phones.get(index);
  }

  @Transactional
  public CrmSettings current() {
    return settings.findById(SETTINGS_ID).orElseGet(this::createDefault);
  }

  @Transactional
  public CrmSettings update(
      String welcomeMessage,
      String lineOneName,
      String lineOnePhone,
      String lineTwoName,
      String lineTwoPhone,
      String lineThreeName,
      String lineThreePhone,
      String lineFourName,
      String lineFourPhone
  ) {
    CrmSettings saved = current();
    saved.setWelcomeMessage(cleanMessage(welcomeMessage));
    saved.setLineOneName(cleanName(lineOneName));
    saved.setLineOnePhone(cleanPhone(lineOnePhone, "El primer número"));
    saved.setLineTwoName(cleanName(lineTwoName));
    saved.setLineTwoPhone(cleanPhone(lineTwoPhone, "El segundo número"));
    saved.setLineThreeName(cleanName(lineThreeName));
    saved.setLineThreePhone(cleanPhone(lineThreePhone, "El tercer número"));
    saved.setLineFourName(cleanName(lineFourName));
    saved.setLineFourPhone(cleanPhone(lineFourPhone, "El cuarto número"));
    return settings.save(saved);
  }

  private CrmSettings createDefault() {
    CrmSettings created = new CrmSettings();
    created.setId(SETTINGS_ID);
    created.setWelcomeMessage(DEFAULT_WELCOME);
    created.setLineOneName("");
    created.setLineOnePhone("");
    created.setLineTwoName("");
    created.setLineTwoPhone("");
    created.setLineThreeName("");
    created.setLineThreePhone("");
    created.setLineFourName("");
    created.setLineFourPhone("");
    created.setRotationIndex(0);
    return settings.save(created);
  }

  private void addPhone(List<String> phones, String phone) {
    if (phone != null && !phone.isBlank()) {
      phones.add(phone);
    }
  }

  private String cleanMessage(String value) {
    String text = value == null ? "" : value.trim();
    if (text.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escribí el mensaje inicial del bot");
    }
    if (text.length() > 500) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mensaje puede tener hasta 500 caracteres");
    }
    return text;
  }

  private String cleanName(String value) {
    String text = value == null ? "" : value.trim();
    if (text.length() > 40) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la línea puede tener hasta 40 caracteres");
    }
    return text;
  }

  private String cleanPhone(String value, String label) {
    String text = value == null ? "" : value.replaceAll("\\s+", "");
    if (text.isEmpty()) {
      return "";
    }
    if (!text.matches("\\d{8,15}")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " tiene que tener entre 8 y 15 dígitos, sin + ni espacios");
    }
    return text;
  }
}
