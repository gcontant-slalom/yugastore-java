#!/usr/bin/env bash
set -euo pipefail

default_host="${YCQL_HOST:-127.0.0.1}"
default_port="${YCQL_PORT:-9042}"
default_keyspace="${YCQL_KEYSPACE:-cronos}"

find_ycqlsh() {
  if command -v ycqlsh >/dev/null 2>&1; then
    command -v ycqlsh
    return 0
  fi

  if [[ -n "${YB_HOME:-}" && -x "${YB_HOME}/bin/ycqlsh" ]]; then
    printf '%s\n' "${YB_HOME}/bin/ycqlsh"
    return 0
  fi

  if [[ -n "${YUGABYTE_HOME:-}" && -x "${YUGABYTE_HOME}/bin/ycqlsh" ]]; then
    printf '%s\n' "${YUGABYTE_HOME}/bin/ycqlsh"
    return 0
  fi

  local candidate
  for candidate in \
    "$HOME/Downloads/dev-runtime/yugabyte-2025.2.2.2/bin/ycqlsh" \
    "$HOME/yugabyte/bin/ycqlsh"; do
    if [[ -x "$candidate" ]]; then
      printf '%s\n' "$candidate"
      return 0
    fi
  done

  return 1
}

if [[ "${1:-}" == "--help" ]]; then
  cat <<'EOF'
Usage: ./ycqlsh.sh [ycqlsh args]

Defaults:
  host: 127.0.0.1
  port: 9042
  keyspace: cronos

Override defaults with environment variables:
  YCQL_HOST, YCQL_PORT, YCQL_KEYSPACE

Examples:
  ./ycqlsh.sh
  ./ycqlsh.sh -e "DESCRIBE TABLES;"
  YCQL_KEYSPACE=system_schema ./ycqlsh.sh
EOF
  exit 0
fi

if ! ycqlsh_bin="$(find_ycqlsh)"; then
  cat <<'EOF' >&2
Unable to locate ycqlsh.

Set YB_HOME or YUGABYTE_HOME to your YugabyteDB install, or add ycqlsh to PATH.
EOF
  exit 1
fi

exec "$ycqlsh_bin" "$default_host" "$default_port" -k "$default_keyspace" "$@"