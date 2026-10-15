import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Promo, PublicConfig } from './models';

@Injectable({ providedIn: 'root' })
export class PromoService {
  constructor(private http: HttpClient) {}

  list() {
    return this.http.get<Promo[]>('/api/promos');
  }

  config() {
    return this.http.get<PublicConfig>('/api/config');
  }

  upload(file: File, title: string) {
    const data = new FormData();
    data.append('file', file);
    data.append('title', title);
    return this.http.post<Promo>('/api/promos', data);
  }

  remove(id: number) {
    return this.http.delete(`/api/promos/${id}`);
  }
}
