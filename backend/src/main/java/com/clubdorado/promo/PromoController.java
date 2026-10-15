package com.clubdorado.promo;

import com.clubdorado.config.AppProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class PromoController {

  private final PromoService promos;
  private final AppProperties properties;

  public PromoController(PromoService promos, AppProperties properties) {
    this.promos = promos;
    this.properties = properties;
  }

  @GetMapping("/config")
  public Map<String, String> config() {
    return Map.of(
        "whatsappPhone", properties.whatsappPhone(),
        "whatsappMessage", properties.whatsappMessage()
    );
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
