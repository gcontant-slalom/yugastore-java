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

run_ycqlsh() {
  local ycqlsh_bin="$1"
  shift

  local ycqlsh_dir ycqlsh_py
  ycqlsh_dir="$(dirname "$ycqlsh_bin")"
  ycqlsh_py="${ycqlsh_dir}/ycqlsh.py"

  if [[ "$(uname -s)" == "Darwin" && -f "$ycqlsh_py" ]] && command -v python3 >/dev/null 2>&1; then
    exec python3 -c 'import multiprocessing as mp, runpy, sys
try:
    mp.set_start_method("fork")
except RuntimeError:
    pass
script_path = sys.argv[1]
sys.argv = sys.argv[1:]
runpy.run_path(script_path, run_name="__main__")' "$ycqlsh_py" "$@"
  fi

  exec "$ycqlsh_bin" "$@"
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

run_ycqlsh "$ycqlsh_bin" "$default_host" "$default_port" -k "$default_keyspace" "$@"