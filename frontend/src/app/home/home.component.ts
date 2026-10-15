import { Component, ElementRef, OnDestroy, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { isPromoVideo, Promo } from '../core/models';
import { PromoService } from '../core/promo.service';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit, OnDestroy {
  promos: Promo[] = [];
  index = 0;
  whatsappHref = 'https://wa.me/5492234376766';
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
    forkJoin({
      promos: this.promosApi.list(),
      config: this.promosApi.config()
    }).subscribe({
      next: ({ promos, config }) => {
        this.promos = promos;
        const phone = (config.whatsappPhone || '').replace(/\D/g, '');
        const text = encodeURIComponent(config.whatsappMessage || 'Hola');
        this.whatsappHref = phone
          ? `https://wa.me/${phone}?text=${text}`
          : `https://wa.me/?text=${text}`;
        this.arm();
      }
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
    } else if (delta >= 40) {
      this.step(-1);
    }
  }

  step(direction: number) {
    if (this.promos.length < 2) {
      return;
    }
    const total = this.promos.length;
    this.index = (this.index + direction + total) % total;
    this.arm();
  }

  goTo(next: number) {
    this.index = next;
    this.arm();
  }

  private arm() {
    this.clear();
    this.schedulePlayback();
    if (this.promos.length < 2) {
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

  private syncPlayback() {
    const videos = this.host.nativeElement.querySelectorAll<HTMLVideoElement>('video.clip');
    videos.forEach((video) => {
      const slide = Number(video.dataset['slide']);
      video.muted = true;
      video.playsInline = true;
      video.loop = true;
      if (slide === this.index) {
        const pending = video.play();
        if (pending) {
          pending.catch(() => undefined);
        }
      } else {
        video.pause();
      }
    });
  }

  private clear() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = undefined;
    }
  }
}
