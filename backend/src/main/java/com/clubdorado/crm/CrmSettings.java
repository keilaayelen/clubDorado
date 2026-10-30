package com.clubdorado.crm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "crm_settings")
public class CrmSettings {

  @Id
  private Long id;

  @Column(nullable = false, length = 500)
  private String welcomeMessage;

  @Column(length = 40)
  private String lineOneName;

  @Column(length = 20)
  private String lineOnePhone;

  @Column(length = 40)
  private String lineTwoName;

  @Column(length = 20)
  private String lineTwoPhone;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getWelcomeMessage() {
    return welcomeMessage;
  }

  public void setWelcomeMessage(String welcomeMessage) {
    this.welcomeMessage = welcomeMessage;
  }

  public String getLineOneName() {
    return lineOneName;
  }

  public void setLineOneName(String lineOneName) {
    this.lineOneName = lineOneName;
  }

  public String getLineOnePhone() {
    return lineOnePhone;
  }

  public void setLineOnePhone(String lineOnePhone) {
    this.lineOnePhone = lineOnePhone;
  }

  public String getLineTwoName() {
    return lineTwoName;
  }

  public void setLineTwoName(String lineTwoName) {
    this.lineTwoName = lineTwoName;
  }

  public String getLineTwoPhone() {
    return lineTwoPhone;
  }

  public void setLineTwoPhone(String lineTwoPhone) {
    this.lineTwoPhone = lineTwoPhone;
  }
}
