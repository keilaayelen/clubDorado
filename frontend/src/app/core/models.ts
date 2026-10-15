export interface Promo {
  id: number;
  title: string;
  imageUrl: string;
  sortOrder: number;
}

export function isPromoVideo(promo: Pick<Promo, 'imageUrl'>): boolean {
  const path = promo.imageUrl.split('?')[0].toLowerCase();
  return path.endsWith('.mp4') || path.endsWith('.webm');
}

export interface PublicConfig {
  whatsappPhone: string;
  whatsappMessage: string;
}
