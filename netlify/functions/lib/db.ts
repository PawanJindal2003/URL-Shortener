import { readFileSync } from 'node:fs';
import { join } from 'node:path';

import mysql, { type Pool, type PoolOptions } from 'mysql2/promise';

let pool: Pool | null = null;
let rdsCaBundle: string | null = null;

interface MysqlConfig {
  host: string;
  port: number;
  database: string;
  user: string;
  password: string;
  ssl?: PoolOptions['ssl'];
}

const RDS_CA_CANDIDATE_PATHS = [
  join(process.cwd(), 'netlify/certs/rds-global-bundle.pem'),
  join(__dirname, '..', '..', 'certs', 'rds-global-bundle.pem'),
];

function loadRdsCaBundle(): string {
  if (rdsCaBundle) {
    return rdsCaBundle;
  }

  for (const candidatePath of RDS_CA_CANDIDATE_PATHS) {
    try {
      rdsCaBundle = readFileSync(candidatePath, 'utf8');
      return rdsCaBundle;
    } catch {
      // Try the next known location.
    }
  }

  throw new Error('RDS CA bundle not found. Expected netlify/certs/rds-global-bundle.pem');
}

function resolveSslConfig(): PoolOptions['ssl'] | undefined {
  if (process.env.MYSQL_SSL !== 'true') {
    return undefined;
  }

  return {
    ca: loadRdsCaBundle(),
    rejectUnauthorized: true,
  };
}

function parseJdbcUrl(jdbcUrl: string): Omit<MysqlConfig, 'user' | 'password' | 'ssl'> {
  const match = jdbcUrl.match(/^jdbc:mysql:\/\/([^:/]+)(?::(\d+))?\/([^?]+)/i);
  if (!match) {
    throw new Error('Invalid JDBC_URL format. Expected jdbc:mysql://host:port/database');
  }

  return {
    host: match[1],
    port: Number.parseInt(match[2] ?? '3306', 10),
    database: match[3],
  };
}

function resolveMysqlConfig(): MysqlConfig {
  const jdbcUrl = process.env.JDBC_URL;
  const user = process.env.JDBC_USERNAME ?? process.env.MYSQL_USER;
  const password = process.env.JDBC_PASSWORD ?? process.env.MYSQL_PASSWORD;

  if (!user || !password) {
    throw new Error('Database credentials are not configured');
  }

  if (jdbcUrl) {
    return {
      ...parseJdbcUrl(jdbcUrl),
      user,
      password,
      ssl: resolveSslConfig(),
    };
  }

  const host = process.env.MYSQL_HOST;
  const database = process.env.MYSQL_DATABASE;
  if (!host || !database) {
    throw new Error('Set JDBC_URL or MYSQL_HOST and MYSQL_DATABASE');
  }

  return {
    host,
    port: Number.parseInt(process.env.MYSQL_PORT ?? '3306', 10),
    database,
    user,
    password,
    ssl: resolveSslConfig(),
  };
}

export function getPool(): Pool {
  if (!pool) {
    const config = resolveMysqlConfig();
    pool = mysql.createPool({
      ...config,
      waitForConnections: true,
      connectionLimit: 2,
      maxIdle: 1,
      idleTimeout: 60_000,
      enableKeepAlive: true,
    });
  }

  return pool;
}
