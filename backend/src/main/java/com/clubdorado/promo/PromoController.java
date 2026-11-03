package com.clubdorado.promo;

import com.clubdorado.config.AppProperties;
import com.clubdorado.crm.CrmService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class PromoController {

  private final PromoService promos;
  private final AppProperties properties;
  private final CrmService crm;

  public PromoController(PromoService promos, AppProperties properties, CrmService crm) {
    this.promos = promos;
    this.properties = properties;
    this.crm = crm;
  }

  @GetMapping("/config")
  public Map<String, String> config() {
    return Map.of(
        "whatsappPhone", properties.whatsappPhone(),
        "whatsappMessage", properties.whatsappMessage()
    );
  }

  @GetMapping("/whatsapp")
  public ResponseEntity<Void> whatsapp() {
    String phone = crm.nextPublicPhone().replaceAll("\\D", "");
    String text = URLEncoder.encode(properties.whatsappMessage(), StandardCharsets.UTF_8).replace("+", "%20");
    return ResponseEntity.status(HttpStatus.FOUND)
        .cacheControl(CacheControl.noStore())
        .header(HttpHeaders.LOCATION, "https://wa.me/" + phone + "?text=" + text)
        .build();
  }

  @GetMapping("/promos")
  public List<PromoResponse> list() {
    return promos.list().stream().map(this::toResponse).toList();
  }

  @PostMapping(value = "/promos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public PromoResponse create(
      @RequestParam(value = "title", required = false) String title,
      @RequestParam("file") MultipartFile file
  ) throws IOException {
    return toResponse(promos.create(title, file));
  }

  @PutMapping("/promos/order")
  public List<PromoResponse> reorder(@RequestBody List<Long> ids) {
    return promos.reorder(ids).stream().map(this::toResponse).toList();
  }

  @PutMapping("/promos/{id}")
  public PromoResponse rename(@PathVariable Long id, @RequestBody Map<String, String> body) {
    return toResponse(promos.rename(id, body.get("title")));
  }

  @DeleteMapping("/promos/{id}")
  public void delete(@PathVariable Long id) throws IOException {
    promos.delete(id);
  }

  @GetMapping("/files/{fileName}")
  public ResponseEntity<Resource> file(@PathVariable String fileName) {
    var path = promos.file(fileName);
    return ResponseEntity.ok()
        .contentType(mediaType(fileName))
        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
        .body(new FileSystemResource(path));
  }

  private MediaType mediaType(String fileName) {
    String lower = fileName.toLowerCase(Locale.ROOT);
    if (lower.endsWith(".png")) {
      return MediaType.IMAGE_PNG;
    }
    if (lower.endsWith(".webp")) {
      return MediaType.parseMediaType("image/webp");
    }
    if (lower.endsWith(".mp4")) {
      return MediaType.parseMediaType("video/mp4");
    }
    if (lower.endsWith(".webm")) {
      return MediaType.parseMediaType("video/webm");
    }
    return MediaType.IMAGE_JPEG;
  }

  private PromoResponse toResponse(Promo promo) {
    return new PromoResponse(
        promo.getId(),
        promo.getTitle() == null ? "" : promo.getTitle(),
        "/api/files/" + promo.getFileName(),
        promo.getSortOrder()
    );
  }

  public record PromoResponse(Long id, String title, String imageUrl, int sortOrder) {}
}
