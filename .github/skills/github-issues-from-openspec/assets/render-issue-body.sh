#!/usr/bin/env bash

set -euo pipefail

usage() {
  cat <<'EOF'
Usage: render-issue-body.sh <sections.tsv> <output.md>

The manifest must be tab-separated with three columns per non-empty row:
  Section Title<TAB>Mode<TAB>Content File

Supported modes:
  raw       Copy the content file as-is under the section heading.
  bullets   Convert each non-empty line to a bullet unless already formatted.
  checklist Convert each non-empty line to an unchecked checklist item unless already formatted.

Example row:
  Summary<TAB>raw<TAB>/tmp/summary.txt
EOF
}

require_file() {
  local path="$1"
  if [[ ! -f "$path" ]]; then
    printf 'Missing content file: %s\n' "$path" >&2
    exit 1
  fi
}

write_bullets() {
  local content_file="$1"
  local line

  while IFS= read -r line || [[ -n "$line" ]]; do
    [[ -z "$line" ]] && continue

    case "$line" in
      '- '*|'* '*|'[ ] '*|'[x] '*|[0-9]*'. '*)
        printf '%s\n' "$line"
        ;;
      *)
        printf -- '- %s\n' "$line"
        ;;
    esac
  done < "$content_file"
}

write_checklist() {
  local content_file="$1"
  local line

  while IFS= read -r line || [[ -n "$line" ]]; do
    [[ -z "$line" ]] && continue

    case "$line" in
      '- [ ] '*|'- [x] '*)
        printf '%s\n' "$line"
        ;;
      *)
        printf -- '- [ ] %s\n' "$line"
        ;;
    esac
  done < "$content_file"
}

if [[ $# -ne 2 ]]; then
  usage >&2
  exit 1
fi

manifest_file="$1"
output_file="$2"

require_file "$manifest_file"

: > "$output_file"

while IFS=$'\t' read -r section_title mode content_file; do
  [[ -z "${section_title:-}" ]] && continue
  [[ "${section_title:0:1}" == "#" ]] && continue

  if [[ -z "${mode:-}" || -z "${content_file:-}" ]]; then
    printf 'Invalid manifest row. Expected 3 tab-separated columns: %s\n' "$section_title" >&2
    exit 1
  fi

  require_file "$content_file"

  {
    printf '## %s\n\n' "$section_title"

    case "$mode" in
      raw)
        cat "$content_file"
        ;;
      bullets)
        write_bullets "$content_file"
        ;;
      checklist)
        write_checklist "$content_file"
        ;;
      *)
        printf 'Unsupported mode: %s\n' "$mode" >&2
        exit 1
        ;;
    esac

    printf '\n\n'
  } >> "$output_file"
done < "$manifest_file"