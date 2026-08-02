import { randomUUID } from 'node:crypto';

import type { ResultSetHeader, RowDataPacket } from 'mysql2/promise';

import { getPool } from './db';
import { ApiError, type ShortUrl } from './types';

interface ShortUrlRow extends RowDataPacket {
  id: number;
  original_url: string;
  short_code: string;
  expires_at: Date | null;
  click_count: number;
}

interface ExistsRow extends RowDataPacket {
  exists_flag: number;
}

const DEFAULT_EXPIRATION_DAYS = 2;
const MAX_SHORT_CODE_GENERATION_ATTEMPTS = 5;
const SYSTEM_USER = 'system';

function toIsoString(value: Date | null): string | null {
  if (!value) {
    return null;
  }

  return value.toISOString().replace(/\.\d{3}Z$/, '');
}

function toDto(row: ShortUrlRow): ShortUrl {
  return {
    id: row.id,
    originalUrl: row.original_url,
    shortCode: row.short_code,
    expiresAt: toIsoString(row.expires_at),
    clickCount: Number(row.click_count),
  };
}

function generateShortCode(): string {
  return randomUUID().replace(/-/g, '').slice(0, 8);
}

export class ShortUrlService {
  async createShortUrl(originalUrl: string): Promise<ShortUrl> {
    const normalizedUrl = originalUrl?.trim() ?? '';
    if (!normalizedUrl) {
      throw new ApiError(400, 'URL must not be blank');
    }

    const pool = getPool();
    const [existingRows] = await pool.query<ShortUrlRow[]>(
      'SELECT id, original_url, short_code, expires_at, click_count FROM short_url WHERE original_url = ? LIMIT 1',
      [normalizedUrl],
    );

    if (existingRows.length > 0) {
      return toDto(existingRows[0]);
    }

    const shortCode = await this.generateUniqueShortCode();
    const expiresAt = new Date();
    expiresAt.setDate(expiresAt.getDate() + DEFAULT_EXPIRATION_DAYS);
    const now = new Date();

    const [result] = await pool.query<ResultSetHeader>(
      `INSERT INTO short_url (
        original_url, short_code, created_by, created_at, updated_by, updated_at, expires_at, click_count
      ) VALUES (?, ?, ?, ?, ?, ?, ?, 0)`,
      [normalizedUrl, shortCode, SYSTEM_USER, now, SYSTEM_USER, now, expiresAt],
    );

    return {
      id: result.insertId,
      originalUrl: normalizedUrl,
      shortCode,
      expiresAt: toIsoString(expiresAt),
      clickCount: 0,
    };
  }

  async redirect(shortCode: string): Promise<string> {
    const shortUrl = await this.findByShortCodeOrThrow(shortCode);

    if (shortUrl.expires_at && shortUrl.expires_at.getTime() < Date.now()) {
      throw new ApiError(410, 'Short URL has expired');
    }

    const pool = getPool();
    await pool.query(
      'UPDATE short_url SET click_count = click_count + 1, updated_at = ?, updated_by = ? WHERE id = ?',
      [new Date(), SYSTEM_USER, shortUrl.id],
    );

    return shortUrl.original_url;
  }

  async getMetadata(shortCode: string): Promise<ShortUrl> {
    const shortUrl = await this.findByShortCodeOrThrow(shortCode);
    return toDto(shortUrl);
  }

  async deleteUrl(shortCode: string): Promise<void> {
    const shortUrl = await this.findByShortCodeOrThrow(shortCode);
    const pool = getPool();
    await pool.query('DELETE FROM short_url WHERE id = ?', [shortUrl.id]);
  }

  private async findByShortCodeOrThrow(shortCode: string): Promise<ShortUrlRow> {
    const pool = getPool();
    const [rows] = await pool.query<ShortUrlRow[]>(
      'SELECT id, original_url, short_code, expires_at, click_count FROM short_url WHERE short_code = ? LIMIT 1',
      [shortCode],
    );

    if (rows.length === 0) {
      throw new ApiError(404, 'Short URL not found');
    }

    return rows[0];
  }

  private async generateUniqueShortCode(): Promise<string> {
    const pool = getPool();

    for (let attempt = 0; attempt < MAX_SHORT_CODE_GENERATION_ATTEMPTS; attempt += 1) {
      const shortCode = generateShortCode();
      const [rows] = await pool.query<ExistsRow[]>(
        'SELECT EXISTS(SELECT 1 FROM short_url WHERE short_code = ?) AS exists_flag',
        [shortCode],
      );

      if (!rows[0]?.exists_flag) {
        return shortCode;
      }
    }

    throw new ApiError(500, 'Unable to generate unique short code');
  }
}