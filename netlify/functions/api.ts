import type { Handler, HandlerEvent } from '@netlify/functions';

import { ShortUrlService } from './lib/short-url-service';
import { ApiError } from './lib/types';

const JSON_HEADERS = {
  'Content-Type': 'application/json',
};

function jsonResponse(statusCode: number, body: unknown) {
  return {
    statusCode,
    headers: JSON_HEADERS,
    body: JSON.stringify(body),
  };
}

function errorResponse(error: unknown) {
  if (error instanceof ApiError) {
    return jsonResponse(error.statusCode, { message: error.message });
  }

  console.error('Unhandled API error', error);
  return jsonResponse(500, { message: 'Internal server error' });
}

function parseBody(event: HandlerEvent): Record<string, unknown> {
  if (!event.body) {
    return {};
  }

  try {
    return JSON.parse(event.body) as Record<string, unknown>;
  } catch {
    throw new ApiError(400, 'Invalid JSON body');
  }
}

export const handler: Handler = async (event) => {
  if (event.httpMethod === 'OPTIONS') {
    return { statusCode: 204, body: '' };
  }

  const path = event.path;
  const service = new ShortUrlService();

  try {
    if (event.httpMethod === 'POST' && /^\/api\/v1\/urls\/?$/.test(path)) {
      const body = parseBody(event);
      const url = typeof body.url === 'string' ? body.url : '';
      const created = await service.createShortUrl(url);
      return jsonResponse(201, created);
    }

    const metadataMatch = path.match(/^\/api\/v1\/urls\/([^/]+)\/metadata\/?$/);
    if (event.httpMethod === 'GET' && metadataMatch) {
      const metadata = await service.getMetadata(metadataMatch[1]);
      return jsonResponse(200, metadata);
    }

    const shortCodeMatch = path.match(/^\/api\/v1\/urls\/([^/]+)\/?$/);
    if (event.httpMethod === 'GET' && shortCodeMatch) {
      const originalUrl = await service.redirect(shortCodeMatch[1]);
      return {
        statusCode: 302,
        headers: {
          Location: originalUrl,
        },
        body: '',
      };
    }

    if (event.httpMethod === 'DELETE' && shortCodeMatch) {
      await service.deleteUrl(shortCodeMatch[1]);
      return {
        statusCode: 204,
        body: '',
      };
    }

    return jsonResponse(404, { message: 'Not found' });
  } catch (error) {
    return errorResponse(error);
  }
};
