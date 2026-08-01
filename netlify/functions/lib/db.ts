import mysql, { type Pool, type PoolOptions } from 'mysql2/promise';

let pool: Pool | null = null;

interface MysqlConfig {
  host: string;
  port: number;
  database: string;
  user: string;
  password: string;
  ssl?: PoolOptions['ssl'];
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
      ssl: process.env.MYSQL_SSL === 'true' ? { rejectUnauthorized: true } : undefined,
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
    ssl: process.env.MYSQL_SSL === 'true' ? { rejectUnauthorized: true } : undefined,
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
