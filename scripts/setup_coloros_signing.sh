#!/usr/bin/env bash
# Configure the user's dedicated release signer. Run this once on a trusted local machine.
set -euo pipefail
umask 077

repo=astarion1965974/nga-just-works
sign_dir="$HOME/.config/nga-coloros-signing"
key="$sign_dir/nga-coloros-release.p12"
password_file="$sign_dir/password"
alias_name=nga-coloros

for executable in gh keytool openssl base64; do
    command -v "$executable" >/dev/null || { echo "Missing required command: $executable" >&2; exit 1; }
done
gh auth status --hostname github.com
configured="$(gh variable list --repo "$repo" --json name,value --jq '.[] | select(.name == "NGA_COUI_CERT_SHA256") | .value')"

mkdir -p "$sign_dir"
if [[ -e "$key" || -e "$password_file" ]]; then
    [[ -s "$key" && -s "$password_file" ]] || { echo "Restore both signing files before retrying." >&2; exit 1; }
else
    [[ -z "$configured" ]] || { echo "Restore the existing signing key from its backup." >&2; exit 1; }
    openssl rand -hex 32 > "$password_file"
    export NGA_LOCAL_SIGNING_PASSWORD="$(cat "$password_file")"
    keytool -genkeypair -alias "$alias_name" -keyalg RSA -keysize 3072 -validity 10000 \
        -storetype PKCS12 -keystore "$key" -storepass:env NGA_LOCAL_SIGNING_PASSWORD \
        -dname 'CN=NGA Just Works ColorOS Release'
fi

export NGA_LOCAL_SIGNING_PASSWORD="$(cat "$password_file")"
fingerprint="$(keytool -J-Duser.language=en -J-Duser.country=US -list -v -storetype PKCS12 \
    -keystore "$key" -storepass:env NGA_LOCAL_SIGNING_PASSWORD -alias "$alias_name" \
    | awk '/SHA256:/{print $2; exit}' | tr -d ':' | tr '[:upper:]' '[:lower:]')"
[[ "$fingerprint" =~ ^[0-9a-f]{64}$ ]]
if [[ -n "$configured" && "$configured" != "$fingerprint" ]]; then
    echo "The existing release fingerprint belongs to a different key. Restore the correct key." >&2
    exit 1
fi

base64 < "$key" | tr -d '\r\n' | gh secret set NGA_COUI_KEYSTORE_B64 --repo "$repo"
printf '%s' "$NGA_LOCAL_SIGNING_PASSWORD" | gh secret set NGA_COUI_KEYSTORE_PASSWORD --repo "$repo"
gh variable set NGA_COUI_CERT_SHA256 --repo "$repo" --body "$fingerprint"
unset NGA_LOCAL_SIGNING_PASSWORD
printf 'Fixed signer configured. Back up both files privately in %s.\n' "$sign_dir"
printf 'Public certificate SHA-256: %s\n' "$fingerprint"
