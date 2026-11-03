package com.clubdorado.crm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

  Optional<Contact> findByPhone(String phone);

  List<Contact> findAllByOrderByCreatedAtDesc();
}
