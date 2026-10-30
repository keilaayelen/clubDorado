package com.clubdorado.crm;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CrmService {

  static final long SETTINGS_ID = 1L;
  static final String DEFAULT_WELCOME =
      "Hola, gracias por escribir a Golden Club. Ya recibimos tu mensaje y en un momento te atiende alguien del equipo.";

  private final CrmSettingsRepository settings;

  public CrmService(CrmSettingsRepository settings) {
    this.settings = settings;
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
      String lineTwoPhone
  ) {
    CrmSettings saved = current();
    saved.setWelcomeMessage(cleanMessage(welcomeMessage));
    saved.setLineOneName(cleanName(lineOneName));
    saved.setLineOnePhone(cleanPhone(lineOnePhone, "El primer número"));
    saved.setLineTwoName(cleanName(lineTwoName));
    saved.setLineTwoPhone(cleanPhone(lineTwoPhone, "El segundo número"));
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
    return settings.save(created);
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
