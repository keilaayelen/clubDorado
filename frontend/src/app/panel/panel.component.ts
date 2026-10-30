import { Component, ElementRef, HostListener, OnInit, ViewChild } from '@angular/core';
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
  dragging = false;
  draggedId: number | null = null;
  editingId: number | null = null;
  draftTitle = '';

  private dragIndex = -1;
  private pointerId = -1;
  private orderBefore = '';

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

  startDrag(event: PointerEvent, index: number) {
    if ((event.target as HTMLElement).closest('button, input')) {
      return;
    }
    this.dragging = true;
    this.dragIndex = index;
    this.draggedId = this.promos[index].id;
    this.pointerId = event.pointerId;
    this.orderBefore = this.promoIds();
    (event.currentTarget as HTMLElement).setPointerCapture(event.pointerId);
  }

  @HostListener('document:pointermove', ['$event'])
  moveDrag(event: PointerEvent) {
    if (!this.dragging || event.pointerId !== this.pointerId) {
      return;
    }
    const cards = Array.from(document.querySelectorAll<HTMLElement>('article.promo'));
    const target = cards.findIndex(card => {
      const rect = card.getBoundingClientRect();
      return event.clientX >= rect.left && event.clientX <= rect.right
        && event.clientY >= rect.top && event.clientY <= rect.bottom;
    });
    if (target < 0 || target === this.dragIndex) {
      return;
    }
    const next = [...this.promos];
    const [moved] = next.splice(this.dragIndex, 1);
    next.splice(target, 0, moved);
    this.promos = next;
    this.dragIndex = target;
  }

  @HostListener('document:pointerup', ['$event'])
  @HostListener('document:pointercancel', ['$event'])
  endDrag(event: PointerEvent) {
    if (!this.dragging || event.pointerId !== this.pointerId) {
      return;
    }
    this.dragging = false;
    this.draggedId = null;
    if (this.promoIds() === this.orderBefore) {
      return;
    }
    this.promosApi.reorder(this.promos.map(promo => promo.id)).subscribe({
      next: promos => {
        this.promos = promos;
        this.message = 'Orden guardado';
        this.error = '';
      },
      error: () => {
        this.error = 'No se pudo guardar el orden';
        this.reload();
      }
    });
  }

  edit(promo: Promo) {
    this.editingId = promo.id;
    this.draftTitle = promo.title || '';
    this.error = '';
  }

  cancelEdit() {
    this.editingId = null;
    this.draftTitle = '';
  }

  saveTitle(promo: Promo) {
    this.promosApi.rename(promo.id, this.draftTitle.trim()).subscribe({
      next: updated => {
        this.promos = this.promos.map(item => item.id === updated.id ? updated : item);
        this.editingId = null;
        this.draftTitle = '';
        this.message = 'Título actualizado';
        this.error = '';
      },
      error: (err) => this.error = err.error?.error || 'No se pudo guardar el título'
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

  private promoIds() {
    return this.promos.map(promo => promo.id).join(',');
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
