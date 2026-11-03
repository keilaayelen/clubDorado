import { Component, ElementRef, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { isPromoVideo, Promo } from '../core/models';
import { PromoService } from '../core/promo.service';

@Component({
  selector: 'app-home',
  imports: [FormsModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit, OnDestroy {
  promos: Promo[] = [];
  index = 0;
  expanded = false;
  whatsappHref = '/api/whatsapp';
  leadName = '';
  leadPhone = '';
  leadNote = '';
  instagramUrl = 'https://www.instagram.com/abyyy_cajera/';
  facebookUrl = 'https://www.facebook.com/profile.php?id=61595250094917';
  phrases = [
    'Cargamos las 24 hs',
    'Atención todos los días',
    'Cargá cuando quieras',
    'Promos nuevas cada semana'
  ];
  readonly coins = [
    { id: 1, left: '7%', top: '18%', size: '28px', delay: '0s', duration: '6.4s' },
    { id: 2, left: '82%', top: '12%', size: '18px', delay: '0.6s', duration: '8s' },
    { id: 3, left: '70%', top: '30%', size: '36px', delay: '1.2s', duration: '7.2s' },
    { id: 4, left: '14%', top: '46%', size: '22px', delay: '0.3s', duration: '9s' },
    { id: 5, left: '88%', top: '52%', size: '26px', delay: '1.8s', duration: '6.8s' },
    { id: 6, left: '24%', top: '64%', size: '16px', delay: '0.9s', duration: '7.6s' },
    { id: 7, left: '58%', top: '8%', size: '14px', delay: '1.4s', duration: '8.4s' },
    { id: 8, left: '40%', top: '72%', size: '20px', delay: '0.2s', duration: '6.2s' }
  ];

  private timer?: ReturnType<typeof setInterval>;
  private playbackTimer?: ReturnType<typeof setTimeout>;
  private startX = 0;
  private tracking = false;

  constructor(
    private promosApi: PromoService,
    private host: ElementRef<HTMLElement>
  ) {}

  isVideo(promo: Promo) {
    return isPromoVideo(promo);
  }

  onVideoReady(slide: number) {
    if (slide === this.index) {
      this.syncPlayback();
    }
  }

  ngOnInit() {
    this.promosApi.list().subscribe({
      next: promos => {
        this.promos = promos;
        this.arm();
      }
    });
  }

  freshWhatsapp(event: Event) {
    const phone = this.leadPhone.replace(/\s+/g, '');
    const name = this.leadName.trim();
    const link = event.currentTarget as HTMLAnchorElement;
    link.href = '/api/whatsapp?t=' + Date.now();
    if (!name && !phone) {
      this.leadNote = '';
      return;
    }
    if (!/^\d{8,15}$/.test(phone)) {
      this.leadNote = 'Ese número no se guardó. WhatsApp se abre igual.';
      return;
    }
    this.leadNote = '';
    this.promosApi.saveContact(name, phone).subscribe({
      next: () => this.leadNote = name ? 'Listo, quedaste registrado.' : 'Guardamos tu número.',
      error: () => this.leadNote = 'No se pudo guardar el registro. WhatsApp se abre igual.'
    });
  }

  ngOnDestroy() {
    this.clear();
    if (this.playbackTimer) {
      clearTimeout(this.playbackTimer);
    }
  }

  onPointerDown(event: PointerEvent) {
    if (this.promos.length < 2) {
      return;
    }
    this.tracking = true;
    this.startX = event.clientX;
  }

  stopTracking() {
    this.tracking = false;
  }

  onPointerUp(event: PointerEvent) {
    if (!this.tracking) {
      return;
    }
    this.tracking = false;
    const delta = event.clientX - this.startX;
    if (delta <= -40) {
      this.step(1);
      return;
    }
    if (delta >= 40) {
      this.step(-1);
      return;
    }
    const target = event.target as HTMLElement | null;
    if (target?.closest('button')) {
      return;
    }
    this.openExpanded();
  }

  onViewerUp(event: PointerEvent) {
    if (!this.tracking) {
      return;
    }
    this.tracking = false;
    const delta = event.clientX - this.startX;
    if (delta <= -40) {
      this.step(1);
    } else if (delta >= 40) {
      this.step(-1);
    }
  }

  openExpanded() {
    this.expanded = true;
    this.clear();
    this.playCurrent(true);
  }

  closeExpanded() {
    this.expanded = false;
    this.arm();
  }

  step(direction: number) {
    if (this.promos.length < 2) {
      return;
    }
    const total = this.promos.length;
    this.index = (this.index + direction + total) % total;
    if (this.expanded) {
      this.clear();
      this.playCurrent(true);
      return;
    }
    this.arm();
  }

  goTo(next: number) {
    this.index = next;
    if (this.expanded) {
      this.clear();
      this.playCurrent(true);
      return;
    }
    this.arm();
  }

  private playCurrent(reset: boolean) {
    this.schedulePlayback();
    if (this.playbackTimer) {
      clearTimeout(this.playbackTimer);
    }
    this.playbackTimer = setTimeout(() => this.syncPlayback(reset), 0);
  }

  private arm() {
    this.clear();
    this.schedulePlayback();
    if (this.expanded || this.promos.length < 2) {
      return;
    }
    this.timer = setInterval(() => {
      this.index = (this.index + 1) % this.promos.length;
      this.schedulePlayback();
    }, 3000);
  }

  private schedulePlayback() {
    if (this.playbackTimer) {
      clearTimeout(this.playbackTimer);
    }
    this.playbackTimer = setTimeout(() => this.syncPlayback(), 0);
  }

  private syncPlayback(reset = false) {
    const videos = this.host.nativeElement.querySelectorAll<HTMLVideoElement>('video.clip');
    videos.forEach((video) => {
      const slide = Number(video.dataset['slide']);
      video.muted = true;
      video.playsInline = true;
      video.loop = true;
      if (!this.expanded && slide === this.index) {
        const pending = video.play();
        if (pending) {
          pending.catch(() => undefined);
        }
      } else {
        video.pause();
      }
    });

    const expandedVideo = this.host.nativeElement.querySelector<HTMLVideoElement>('video.viewer-clip');
    if (!expandedVideo) {
      return;
    }
    expandedVideo.playsInline = true;
    expandedVideo.loop = true;
    expandedVideo.muted = false;
    if (reset) {
      expandedVideo.currentTime = 0;
    }
    const pending = expandedVideo.play();
    if (pending) {
      pending.catch(() => {
        expandedVideo.muted = true;
        expandedVideo.play().catch(() => undefined);
      });
    }
  }

  private clear() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = undefined;
    }
  }
}
