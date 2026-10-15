import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { isPromoVideo, Promo } from '../core/models';
import { PromoService } from '../core/promo.service';

@Component({
  selector: 'app-panel',
  imports: [FormsModule, RouterLink],
  templateUrl: './panel.component.html',
  styleUrl: './panel.component.css'
})
export class PanelComponent implements OnInit {
  promos: Promo[] = [];
  title = '';
  error = '';
  message = '';
  uploading = false;

  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;

  constructor(
    private promosApi: PromoService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.reload();
  }

  reload() {
    this.promosApi.list().subscribe({
      next: promos => this.promos = promos,
      error: () => this.error = 'No se pudieron leer las promos'
    });
  }

  isVideo(promo: Promo) {
    return isPromoVideo(promo);
  }

  upload() {
    const file = this.fileInput?.nativeElement.files?.[0];
    if (!file) {
      this.error = 'Elegí una foto o un video';
      this.message = '';
      return;
    }
    if (!this.accepted(file)) {
      this.error = 'El archivo tiene que ser JPG, PNG, WEBP, MP4 o WEBM';
      this.message = '';
      return;
    }
    this.uploading = true;
    this.error = '';
    this.promosApi.upload(file, this.title.trim()).subscribe({
      next: () => {
        this.uploading = false;
        this.title = '';
        this.message = 'Promo cargada';
        if (this.fileInput) {
          this.fileInput.nativeElement.value = '';
        }
        this.reload();
      },
      error: (err) => {
        this.uploading = false;
        this.error = err.error?.error || 'No se pudo subir el archivo';
      }
    });
  }

  remove(promo: Promo) {
    if (!confirm('¿Borrar esta promo?')) {
      return;
    }
    this.promosApi.remove(promo.id).subscribe({
      next: () => {
        this.message = 'Promo borrada';
        this.error = '';
        this.reload();
      },
      error: () => this.error = 'No se pudo borrar'
    });
  }

  private accepted(file: File) {
    const type = (file.type || '').toLowerCase().split(';')[0].trim();
    if (['image/jpeg', 'image/png', 'image/webp', 'video/mp4', 'video/webm'].includes(type)) {
      return true;
    }
    if (type && type !== 'application/octet-stream') {
      return false;
    }
    return /\.(jpe?g|png|webp|mp4|webm)$/i.test(file.name);
  }

  logout() {
    this.auth.logout().subscribe({
      next: () => this.router.navigateByUrl('/'),
      error: () => {
        localStorage.removeItem('gc_token');
        this.router.navigateByUrl('/');
      }
    });
  }
}
