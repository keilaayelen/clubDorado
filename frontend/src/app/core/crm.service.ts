import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CrmContact, CrmSettings } from './models';

@Injectable({ providedIn: 'root' })
export class CrmService {
  constructor(private http: HttpClient) {}

  settings() {
    return this.http.get<CrmSettings>('/api/crm/settings');
  }

  save(settings: CrmSettings) {
    return this.http.put<CrmSettings>('/api/crm/settings', settings);
  }

  contacts() {
    return this.http.get<CrmContact[]>('/api/crm/contacts');
  }
}
