#!/bin/bash
# ══════════════════════════════════════════════════════════════
#  generate-keystore.sh
#  Run this ONCE to create your release signing keystore.
#  Keep the output file (tasbih-release.keystore) SECRET —
#  never commit it to Git!
# ══════════════════════════════════════════════════════════════

set -e

KEYSTORE_FILE="tasbih-release.keystore"
KEY_ALIAS="tasbih"
VALIDITY_DAYS=10000   # ~27 years (Play Store recommends after 2033)

echo ""
echo "══════════════════════════════════════"
echo "  Tasbih — Keystore Generator"
echo "══════════════════════════════════════"
echo ""
echo "You'll be asked for:"
echo "  • Keystore password  (remember this!)"
echo "  • Key password       (can be the same)"
echo "  • Your name & organization info"
echo ""

keytool -genkey \
  -v \
  -keystore "$KEYSTORE_FILE" \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity "$VALIDITY_DAYS"

echo ""
echo "✅ Keystore created: $KEYSTORE_FILE"
echo ""
echo "══════════════════════════════════════"
echo "  NEXT STEPS:"
echo "══════════════════════════════════════"
echo ""
echo "1. Convert to base64 for GitHub Actions secret:"
echo "   base64 -i $KEYSTORE_FILE | pbcopy   # macOS"
echo "   base64 $KEYSTORE_FILE | xclip       # Linux"
echo ""
echo "2. Add these 4 GitHub repository secrets:"
echo "   Go to: github.com/khadrx/tasbih → Settings → Secrets → Actions"
echo ""
echo "   KEYSTORE_BASE64  →  paste the base64 output from step 1"
echo "   KEYSTORE_PASS    →  your keystore password"
echo "   KEY_ALIAS        →  tasbih"
echo "   KEY_PASS         →  your key password"
echo ""
echo "3. NEVER commit $KEYSTORE_FILE to Git!"
echo "   Make sure it's in your .gitignore"
echo ""
echo "4. To trigger a release build:"
echo "   git tag v1.0.0"
echo "   git push origin v1.0.0"
echo ""