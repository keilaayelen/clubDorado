package com.clubdorado.crm;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class ContactService {

  private final ContactRepository contacts;

  public ContactService(ContactRepository contacts) {
    this.contacts = contacts;
  }

  @Transactional
  public Contact save(String name, String phone) {
    String cleanPhone = phone == null ? "" : phone.replaceAll("\\s+", "");
    if (!cleanPhone.matches("\\d{8,15}")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El número tiene que tener entre 8 y 15 dígitos, sin + ni espacios");
    }
    String cleanName = name == null ? "" : name.trim();
    if (cleanName.length() > 40) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre puede tener hasta 40 letras");
    }
    Contact contact = contacts.findByPhone(cleanPhone).orElseGet(Contact::new);
    contact.setName(cleanName);
    contact.setPhone(cleanPhone);
    contact.setCreatedAt(Instant.now());
    return contacts.save(contact);
  }

  public List<Contact> list() {
    return contacts.findAllByOrderByCreatedAtDesc();
  }

  @Transactional
  public void delete(String phone) {
    contacts.findByPhone(phone).ifPresent(contacts::delete);
  }
}
