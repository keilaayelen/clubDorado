package com.clubdorado.promo;

import com.clubdorado.config.AppProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class PromoService {

  private static final Set<String> ALLOWED = Set.of(
      "image/jpeg",
      "image/png",
      "image/webp",
      "video/mp4",
      "video/webm"
  );

  private final PromoRepository promos;
  private final Path uploadDir;

  public PromoService(PromoRepository promos, AppProperties properties) throws IOException {
    this.promos = promos;
    this.uploadDir = Path.of(properties.uploadDir()).toAbsolutePath().normalize();
    Files.createDirectories(this.uploadDir);
  }

  public List<Promo> list() {
    return promos.findAllByOrderBySortOrderAscIdAsc();
  }

  public Promo create(String title, MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Elegí una foto o un video");
    }
    String stored = UUID.randomUUID() + extension(file);
    Files.copy(file.getInputStream(), uploadDir.resolve(stored));

    int nextOrder = list().stream().mapToInt(Promo::getSortOrder).max().orElse(-1) + 1;
    Promo promo = new Promo();
    promo.setFileName(stored);
    promo.setTitle(cleanTitle(title));
    promo.setSortOrder(nextOrder);
    return promos.save(promo);
  }

  public void delete(Long id) throws IOException {
    Promo promo = promos.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Esa promo no existe"));
    Files.deleteIfExists(uploadDir.resolve(promo.getFileName()));
    promos.delete(promo);
  }

  public Path file(String fileName) {
    if (fileName == null || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archivo inválido");
    }
    Path path = uploadDir.resolve(fileName).normalize();
    if (!path.startsWith(uploadDir) || !Files.exists(path)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Archivo no encontrado");
    }
    return path;
  }

  public void seed(Path sourceDir) throws IOException {
    if (promos.count() > 0 || sourceDir == null || !Files.isDirectory(sourceDir)) {
      return;
    }
    String[] names = {
        "promo-golden-club.jpg",
        "promo-bonus-oro.jpg",
        "promo-noche-dorada.jpg"
    };
    int order = 0;
    for (String name : names) {
      Path from = sourceDir.resolve(name);
      if (!Files.exists(from)) {
        continue;
      }
      String stored = UUID.randomUUID() + ".jpg";
      Files.copy(from, uploadDir.resolve(stored));
      Promo promo = new Promo();
      promo.setFileName(stored);
      promo.setTitle("");
      promo.setSortOrder(order++);
      promos.save(promo);
    }
  }

  private String cleanTitle(String title) {
    if (title == null) {
      return "";
    }
    String trimmed = title.trim();
    if (trimmed.length() > 80) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El título puede tener hasta 80 letras");
    }
    return trimmed;
  }

  private String extension(MultipartFile file) {
    String fromName = extensionFromName(file.getOriginalFilename());
    if (!fromName.isEmpty()) {
      return fromName;
    }
    String type = normalizeType(file.getContentType());
    if (ALLOWED.contains(type)) {
      return switch (type) {
        case "image/png" -> ".png";
        case "image/webp" -> ".webp";
        case "video/mp4" -> ".mp4";
        case "video/webm" -> ".webm";
        default -> ".jpg";
      };
    }
    throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "El archivo tiene que ser JPG, PNG, WEBP, MP4 o WEBM"
    );
  }

  private String normalizeType(String type) {
    if (type == null) {
      return "";
    }
    String lower = type.toLowerCase(Locale.ROOT).trim();
    int separator = lower.indexOf(';');
    if (separator >= 0) {
      lower = lower.substring(0, separator).trim();
    }
    return lower;
  }

  private String extensionFromName(String original) {
    if (original == null) {
      return "";
    }
    String name = original.toLowerCase(Locale.ROOT);
    int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
    if (slash >= 0) {
      name = name.substring(slash + 1);
    }
    if (name.endsWith(".jpeg") || name.endsWith(".jpg")) {
      return ".jpg";
    }
    if (name.endsWith(".png")) {
      return ".png";
    }
    if (name.endsWith(".webp")) {
      return ".webp";
    }
    if (name.endsWith(".mp4")) {
      return ".mp4";
    }
    if (name.endsWith(".webm")) {
      return ".webm";
    }
    return "";
  }
}
